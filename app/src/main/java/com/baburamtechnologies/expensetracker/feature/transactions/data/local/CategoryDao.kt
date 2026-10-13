package com.baburamtechnologies.expensetracker.feature.transactions.data.local

import androidx.room.*
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("""
        SELECT * FROM categories 
        WHERE type = :type 
          AND level = :level 
          AND (profile_id IS NULL OR profile_id = :profileId) 
          AND (is_active = 1 OR :includeInactive = 1) 
        ORDER BY display_order ASC, name ASC
    """)
    fun getCategoriesByTypeAndLevel(type: String, level: Int, includeInactive: Int, profileId: String?): Flow<List<CategoryEntity>>

    @Query("""
        SELECT * FROM categories 
        WHERE parent_id = :parentId 
          AND (profile_id IS NULL OR profile_id = :profileId) 
          AND (is_active = 1 OR :includeInactive = 1) 
        ORDER BY display_order ASC, name ASC
    """)
    fun getCategoriesByParent(parentId: String, includeInactive: Int, profileId: String?): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND (profile_id IS NULL OR profile_id = :profileId)")
    suspend fun getCategoryById(id: String, profileId: String?): CategoryEntity?

    @Query("SELECT * FROM categories WHERE profile_id IS NULL OR profile_id = :profileId")
    suspend fun getAllCategoriesForUser(profileId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id AND is_system = 0 AND profile_id = :profileId")
    suspend fun deleteCustomCategory(id: String, profileId: String)
    
    @Query("UPDATE categories SET is_active = :isActive WHERE id = :id AND (profile_id IS NULL OR profile_id = :profileId)")
    suspend fun setCategoryActive(id: String, isActive: Boolean, profileId: String)

    @Query("DELETE FROM categories WHERE profile_id = :profileId")
    suspend fun deleteCategoriesForUser(profileId: String)
}
