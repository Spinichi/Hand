package com.mim.watch.data.local
// Room DB 생성/싱글턴
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BaselineEntity::class], // DB가 관리할 테이블 목록
    version = 1,                        // 스키마 버전
    exportSchema = false                // 스키마 내보내기 비활성
)
abstract class AppDb : RoomDatabase() {
    abstract fun baselineDao(): BaselineDao // DAO 제공

    companion object {
        @Volatile private var INSTANCE: AppDb? = null   // 싱글턴 보관
        // 전역에서 DB 인스턴스를 얻는 정적 함수
        fun get(context: Context): AppDb =
            INSTANCE ?: synchronized(this) {    // 멀티스레드 안전
                INSTANCE ?: Room.databaseBuilder(      // DB 빌더 호출
                    context.applicationContext,         // 앱 컨텍스트 사용
                    AppDb::class.java,          // DB 클래스 타임
                    "watch_local.db"            // DB 파일명
                ).build().also { INSTANCE = it }        // 생성 후 캐싱
            }
    }
}
