package com.baburamtechnologies.expensetracker.feature.transactions.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category

/**
 * Extension to resolve the display name of a category in a Composable context.
 */
@Composable
fun Category.displayName(): String {
    return if (isSystem) {
        name.resolveCategoryName()
    } else {
        name
    }
}

/**
 * Extension to resolve the display name of a category using a Context.
 * Useful for non-composable contexts like lambdas.
 */
fun Category.getDisplayName(context: Context): String {
    return if (isSystem) {
        name.resolveCategoryName(context)
    } else {
        name
    }
}

/**
 * Resolves a string as a category name (handles resource IDs).
 */
@Composable
fun String.resolveCategoryName(): String {
    val resId = this.toIntOrNull()
    return if (resId != null) {
        stringResource(id = resId)
    } else {
        this
    }
}

/**
 * Resolves a string as a category name using Context (handles resource IDs).
 */
fun String.resolveCategoryName(context: Context): String {
    val resId = this.toIntOrNull()
    return if (resId != null) {
        try {
            context.getString(resId)
        } catch (e: Exception) {
            this
        }
    } else {
        this
    }
}
