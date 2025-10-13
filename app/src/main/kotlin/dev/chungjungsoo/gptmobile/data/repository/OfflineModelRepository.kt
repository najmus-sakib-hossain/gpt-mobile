package dev.chungjungsoo.gptmobile.data.repository

import dev.chungjungsoo.gptmobile.data.database.dao.OfflineModelDao
import dev.chungjungsoo.gptmobile.data.database.entity.OfflineModel
import dev.chungjungsoo.gptmobile.data.network.HuggingFaceApi
import dev.chungjungsoo.gptmobile.data.dto.HFModelSearchResult
import dev.chungjungsoo.gptmobile.data.dto.HFModelInfo
import dev.chungjungsoo.gptmobile.data.dto.HFModelFile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineModelRepository @Inject constructor(
    private val offlineModelDao: OfflineModelDao,
    private val huggingFaceApi: HuggingFaceApi
) {
    // Local database operations
    fun getAllModels(): Flow<List<OfflineModel>> = offlineModelDao.getAllModels()

    suspend fun getModelById(id: Int): OfflineModel? = offlineModelDao.getModelById(id)

    suspend fun getModelByModelId(modelId: String): OfflineModel? = 
        offlineModelDao.getModelByModelId(modelId)

    suspend fun insertModel(model: OfflineModel): Long = offlineModelDao.insertModel(model)

    suspend fun updateModel(model: OfflineModel) = offlineModelDao.updateModel(model)

    suspend fun deleteModel(model: OfflineModel) = offlineModelDao.deleteModel(model)

    suspend fun deleteModelById(id: Int) = offlineModelDao.deleteModelById(id)

    suspend fun getModelCount(): Int = offlineModelDao.getModelCount()

    // HuggingFace API operations
    suspend fun searchModels(
        query: String,
        filter: String = "gguf",
        limit: Int = 20,
        sort: String = "downloads"
    ): List<HFModelSearchResult> = huggingFaceApi.searchModels(query, filter, limit, sort)

    suspend fun getModelInfo(modelId: String): HFModelInfo? = huggingFaceApi.getModelInfo(modelId)

    suspend fun getModelFiles(modelId: String): List<HFModelFile> = 
        huggingFaceApi.getModelFiles(modelId)
}
