package dev.chungjungsoo.gptmobile.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.chungjungsoo.gptmobile.data.database.entity.OfflineModel
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineModelDao {
    @Query("SELECT * FROM offline_models ORDER BY downloadDate DESC")
    fun getAllModels(): Flow<List<OfflineModel>>

    @Query("SELECT * FROM offline_models WHERE id = :id")
    suspend fun getModelById(id: Int): OfflineModel?

    @Query("SELECT * FROM offline_models WHERE modelId = :modelId")
    suspend fun getModelByModelId(modelId: String): OfflineModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: OfflineModel): Long

    @Update
    suspend fun updateModel(model: OfflineModel)

    @Delete
    suspend fun deleteModel(model: OfflineModel)

    @Query("DELETE FROM offline_models WHERE id = :id")
    suspend fun deleteModelById(id: Int)

    @Query("SELECT COUNT(*) FROM offline_models")
    suspend fun getModelCount(): Int
}
