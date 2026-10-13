package com.baburamtechnologies.expensetracker.feature.transactions.data.local

import androidx.room.*
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type AND level = :level AND (is_active = 1 OR :includeInactive = 1) ORDER BY display_order ASC, name ASC")
    fun getCategoriesByTypeAndLevel(type: String, level: Int, includeInactive: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parent_id = :parentId AND (is_active = 1 OR :includeInactive = 1) ORDER BY display_order ASC, name ASC")
    fun getCategoriesByParent(parentId: String, includeInactive: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id AND is_system = 0")
    suspend fun deleteCustomCategory(id: String)
    
    @Query("UPDATE categories SET is_active = :isActive WHERE id = :id")
    suspend fun setCategoryActive(id: String, isActive: Boolean)
}
