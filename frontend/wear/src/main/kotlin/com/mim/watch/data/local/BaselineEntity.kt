package com.mim.watch.data.local
// ROOM 엔터티
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mim.watch.core.baseline.BaseLine

@Entity(tableName = "user_baselines") // 유저 아이디 없이 로컬 1인 기준 개인임
data class BaselineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // 알아서 pk 하나 +1
    val version: Int,       // 계산 규칙 버전
    val isActive: Boolean, // 현재 사용 여부
    val hrvSdnnMean: Double,
    val hrvSdnnStd: Double,
    val hrvRmssdMean: Double,
    val hrvRmssdStd: Double,
    val hrMean: Double,
    val hrStd: Double,
    val objTempMean: Double,
    val objTempStd: Double
) {
    // 엔터티 -> 도메인 모델(BaseLine) 변환
    fun toModel() = BaseLine(
        hrvSdnnMean, hrvSdnnStd,
        hrvRmssdMean, hrvRmssdStd,
        hrMean, hrStd,
        objTempMean, objTempStd,
        version, isActive
    )
}
