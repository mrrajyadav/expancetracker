package com.baburamtechnologies.expensetracker.feature.transactions.data.local

import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CategoryLocalDataSource {
    fun getCategoriesByTypeAndLevel(type: String, level: Int, includeInactive: Boolean): Flow<List<CategoryEntity>>
    fun getCategoriesByParent(parentId: String, includeInactive: Boolean): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(id: String): CategoryEntity?
    suspend fun insertCategories(categories: List<CategoryEntity>)
    suspend fun insertCategory(category: CategoryEntity)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCustomCategory(id: String)
    suspend fun setCategoryActive(id: String, isActive: Boolean)
}

class CategoryLocalDataSourceImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryLocalDataSource {
    override fun getCategoriesByTypeAndLevel(type: String, level: Int, includeInactive: Boolean) = 
        categoryDao.getCategoriesByTypeAndLevel(type, level, if (includeInactive) 1 else 0)
    
    override fun getCategoriesByParent(parentId: String, includeInactive: Boolean) = 
        categoryDao.getCategoriesByParent(parentId, if (includeInactive) 1 else 0)
    override suspend fun getCategoryById(id: String) = categoryDao.getCategoryById(id)
    override suspend fun insertCategories(categories: List<CategoryEntity>) = categoryDao.insertCategories(categories)
    override suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    override suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    override suspend fun deleteCustomCategory(id: String) = categoryDao.deleteCustomCategory(id)
    override suspend fun setCategoryActive(id: String, isActive: Boolean) = categoryDao.setCategoryActive(id, isActive)
}
