package com.mim.watch.data.local
// Room DAO
import androidx.room.*

@Dao
interface BaselineDao {
    // 활성 Baseline 하나 조회(없으면 null)
    @Query("SELECT * FROM user_baselines WHERE isActive = 1 LIMIT 1")
    fun getActive(): BaselineEntity?

    // Baseline 저장 또는 교체(같은 PK면 교체)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(entity: BaselineEntity)

    // 모든 Baseline 비활성화(새 활성 세팅 전 단계)
    @Query("UPDATE user_baselines SET isActive = 0")
    fun deactivateAll()
}
