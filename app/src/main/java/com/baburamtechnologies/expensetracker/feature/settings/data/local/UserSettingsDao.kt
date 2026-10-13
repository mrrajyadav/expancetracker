package com.baburamtechnologies.expensetracker.feature.settings.data.local

import androidx.room.*
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE user_id = :userId")
    fun getUserSettings(userId: String): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateUserSettings(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings WHERE user_id = :userId")
    suspend fun getUserSettingsForUser(userId: String): UserSettingsEntity?

    @Query("DELETE FROM user_settings WHERE user_id = :userId")
    suspend fun deleteUserSettingsForUser(userId: String)
}
