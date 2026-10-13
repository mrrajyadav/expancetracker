package com.baburamtechnologies.expensetracker.feature.transactions.data.local

import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CategoryLocalDataSource {
    fun getCategoriesByTypeAndLevel(type: String, level: Int, includeInactive: Boolean, profileId: String?): Flow<List<CategoryEntity>>
    fun getCategoriesByParent(parentId: String, includeInactive: Boolean, profileId: String?): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(id: String, profileId: String?): CategoryEntity?
    suspend fun getAllCategoriesForUser(profileId: String): List<CategoryEntity>
    suspend fun insertCategories(categories: List<CategoryEntity>)
    suspend fun insertCategory(category: CategoryEntity)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCustomCategory(id: String, profileId: String)
    suspend fun setCategoryActive(id: String, isActive: Boolean, profileId: String)
    suspend fun deleteCategoriesForUser(profileId: String)
}

class CategoryLocalDataSourceImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryLocalDataSource {
    override fun getCategoriesByTypeAndLevel(type: String, level: Int, includeInactive: Boolean, profileId: String?) = 
        categoryDao.getCategoriesByTypeAndLevel(type, level, if (includeInactive) 1 else 0, profileId)
    
    override fun getCategoriesByParent(parentId: String, includeInactive: Boolean, profileId: String?) = 
        categoryDao.getCategoriesByParent(parentId, if (includeInactive) 1 else 0, profileId)

    override suspend fun getCategoryById(id: String, profileId: String?) = categoryDao.getCategoryById(id, profileId)

    override suspend fun getAllCategoriesForUser(profileId: String) = categoryDao.getAllCategoriesForUser(profileId)

    override suspend fun insertCategories(categories: List<CategoryEntity>) = categoryDao.insertCategories(categories)

    override suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)

    override suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)

    override suspend fun deleteCustomCategory(id: String, profileId: String) = categoryDao.deleteCustomCategory(id, profileId)

    override suspend fun setCategoryActive(id: String, isActive: Boolean, profileId: String) = categoryDao.setCategoryActive(id, isActive, profileId)

    override suspend fun deleteCategoriesForUser(profileId: String) = categoryDao.deleteCategoriesForUser(profileId)
}
