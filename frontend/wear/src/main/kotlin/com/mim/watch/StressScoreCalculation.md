# 워치 스트레스 점수 계산 로직 상세 분석

## 1. 개요

이 문서는 워치 앱의 스트레스 점수 계산, 데이터 처리 및 이상치(급성 스트레스) 감지 메커니즘을 상세히 설명합니다. 전체 프로세스는 실시간으로 수집되는 생체 신호(HR, IBI)를 기반으로 하며, 여러 모듈의 상호작용을 통해 최종 결과를 도출합니다.

---

## 2. 데이터 흐름 및 핵심 모듈

```
[SensorCollector] -> [HrvCalculator] -> [StressScore] -> [StressLevelMapper]
       |
       `-> [DataBufferManager] -> [OutlierDetector]
                           |
                           `-> [BioSample (대표 샘플)] -> DB 저장 / 서버 전송
```

### 2.1. 데이터 수집 (`sensors/SensorCollector.kt`)

-   **역할**: 워치의 생체 신호 센서로부터 주기적으로(약 1초) 데이터를 수집합니다.
-   **핵심 데이터**:
    -   `hr` (Heart Rate): 심박수
    -   `ibi` (Inter-Beat Interval): 심박과 심박 사이의 간격 (ms 단위)
-   **출력**: 수집된 데이터는 `SensorSample` 데이터 클래스 객체로 래핑되어 다른 모듈로 전달됩니다.

    ```kotlin
    // core/measurement/SensorSample.kt
    data class SensorSample(
        val timestamp: Long, // 데이터 수집 시각
        val hr: Double,      // 심박수
        val ibi: Int         // 심박 간격
    )
    ```

### 2.2. 개별 샘플 처리 (점수 및 레벨 계산)

모든 `SensorSample`은 생성될 때마다 개별적으로 스트레스 점수와 레벨이 계산됩니다.

#### A. HRV 계산 (`core/measurement/HrvCalculator.kt`)

-   **역할**: 스트레스 측정의 핵심 지표인 **RMSSD (Root Mean Square of Successive Differences)** 값을 계산합니다.
-   **입력**: IBI 값들의 리스트 (`List<Int>`)
-   **로직**: RMSSD는 심박 간격의 연속적인 차이를 통해 심박 변이도(HRV)를 측정하며, 자율신경계의 균형 상태를 반영합니다. 스트레스가 높을수록 부교감 신경이 억제되어 RMSSD 값이 낮아지는 경향을 보입니다. `calculateRmssd` 함수가 이 로직을 담당합니다.

#### B. 스트레스 점수 계산 (`core/measurement/StressScore.kt`)

-   **역할**: 현재 RMSSD를 개인별 기준선(Baseline) RMSSD와 비교하여 0~100 사이의 점수를 산출합니다.
-   **입력**:
    -   `currentRmssd`: 현재 측정된 RMSSD 값
    -   `baselineRmssd`: 사용자의 평상시 평균 RMSSD 값 (사전에 측정 및 저장)
-   **핵심 로직**: `calculateStressScore` 함수 내에 구현되어 있습니다. 점수는 **현재 RMSSD가 개인의 기준선 대비 얼마나 안정적인 상태인지를 정량화**한 값입니다.
    -   `currentRmssd`가 `baselineRmssd`와 비슷하거나 높으면 -> 스트레스 낮음 -> **높은 점수 (예: 100점 근접)**
    -   `currentRmssd`가 `baselineRmssd`보다 현저히 낮으면 -> 스트레스 높음 -> **낮은 점수 (예: 0점 근접)**

#### C. 스트레스 레벨 매핑 (`core/measurement/StressLevelMapper.kt`)

-   **역할**: 계산된 스트레스 점수(0~100)를 4단계의 직관적인 레벨로 변환합니다.
-   **매핑 규칙 (예시)**:
    -   **Level 1 (낮음)**: 76 ~ 100점
    -   **Level 2 (보통)**: 51 ~ 75점
    -   **Level 3 (높음)**: 26 ~ 50점
    -   **Level 4 (매우 높음)**: 0 ~ 25점

---

## 3. 데이터 버퍼링, 대표 샘플 생성 및 이상치 감지

### 3.1. 데이터 버퍼링 (`services/DataBufferManager.kt`)

-   **역할**: 위 과정을 거쳐 점수와 레벨이 부여된 `SensorSample` 객체를 **10개씩** 버퍼에 수집합니다.
-   **트리거**: 버퍼가 10개 샘플로 가득 차면, '대표 샘플 생성'과 '이상치 감지' 로직이 동시에 트리거됩니다.

### 3.2. 대표 샘플 생성 및 활용 (`data/model/BioSample.kt`)

-   **역할**: 10개의 샘플을 대표하는 단일 `BioSample` 객체를 생성하여, DB 저장 및 서버 전송 등 주요 비즈니스 로직에 사용합니다.
-   **생성 로직**:
    -   10개 샘플의 **평균 심박수(avgHr)**, **평균 스트레스 점수(avgStressScore)** 등을 계산합니다.
    -   타임스탬프는 일반적으로 버퍼의 마지막 샘플 시간을 사용합니다.
-   **활용**: 생성된 `BioSample`은 `BaselineRepository`를 통해 데이터베이스에 저장되거나, `WearMessageSender`를 통해 모바일 앱으로 전송됩니다. 이는 장기적인 스트레스 추이 분석의 기반 데이터가 됩니다.

### 3.3. 이상치 감지 (`core/measurement/OutlierDetector.kt`)

-   **역할**: 10개의 샘플 버퍼를 분석하여 '급성 스트레스' 상태를 실시간으로 감지합니다.
-   **판단 기준**:
    > 버퍼에 수집된 10개의 `SensorSample` 중, **8개 이상이 Level 4 (매우 높음) 상태일 경우**
-   **결과**: 이 조건이 충족되면 '이상치(Outlier)'로 판단하고, `ShakeTrigger` (진동 알림)나 `BioForegroundService` (UI 업데이트) 같은 다른 서비스에 이벤트를 전파하여 사용자에게 즉각적인 피드백을 제공합니다.

---

## 4. 최종 요약

1.  **실시간 수집/계산**: 1초마다 HR/IBI 수집 -> RMSSD(HRV) 계산 -> 스트레스 점수/레벨 산출.
2.  **버퍼링**: 계산 완료된 샘플 10개를 `DataBufferManager`에 저장.
3.  **분기 처리 (10개 샘플 도달 시)**:
    -   **이상치 감지**: 10개 중 8개 이상이 4레벨이면 '급성 스트레스'로 판단하고 알림.
    -   **대표 데이터 생성**: 10개 샘플의 평균값으로 `BioSample`을 만들어 DB 저장 및 전송 (장기 추이 분석용).

이러한 이원화된 구조를 통해 실시간 피드백과 장기적인 데이터 분석을 모두 처리합니다.
