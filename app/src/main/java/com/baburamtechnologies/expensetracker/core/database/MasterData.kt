package com.baburamtechnologies.expensetracker.core.database

import com.baburamtechnologies.expensetracker.R
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import java.time.Instant

object MasterData {
    
    private val now = Instant.now().toString()

    fun getSystemCategories(): List<CategoryEntity> {
        val categories = mutableListOf<CategoryEntity>()

        // --- EXPENSE CATEGORIES ---
        
        // Food & Dining
        val food = "cat_food"
        categories.add(createCategory(food, null, "EXPENSE", 1, R.string.cat_food, 0))
        categories.add(createCategory("cat_rest", food, "EXPENSE", 2, R.string.cat_rest, 0))
        categories.add(createCategory("cat_groc", food, "EXPENSE", 2, R.string.cat_groc, 1))
        categories.add(createCategory("cat_coffee", food, "EXPENSE", 2, R.string.cat_coffee, 2))
        categories.add(createCategory("cat_fastfood", food, "EXPENSE", 2, R.string.cat_fastfood, 3))

        // Shopping
        val shopping = "cat_shopping"
        categories.add(createCategory(shopping, null, "EXPENSE", 1, R.string.cat_shopping, 1))
        categories.add(createCategory("cat_clothing", shopping, "EXPENSE", 2, R.string.cat_clothing, 0))
        categories.add(createCategory("cat_electronics", shopping, "EXPENSE", 2, R.string.cat_electronics, 1))
        categories.add(createCategory("cat_home", shopping, "EXPENSE", 2, R.string.cat_home, 2))
        categories.add(createCategory("cat_acc", shopping, "EXPENSE", 2, R.string.cat_acc, 3))

        // Transportation
        val transport = "cat_transport"
        categories.add(createCategory(transport, null, "EXPENSE", 1, R.string.cat_transport, 2))
        categories.add(createCategory("cat_fuel", transport, "EXPENSE", 2, R.string.cat_fuel, 0))
        categories.add(createCategory("cat_public", transport, "EXPENSE", 2, R.string.cat_public, 1))
        categories.add(createCategory("cat_cab", transport, "EXPENSE", 2, R.string.cat_cab, 2))
        categories.add(createCategory("cat_maint", transport, "EXPENSE", 2, R.string.cat_maint, 3))

        // Bills & Utilities
        val bills = "cat_bills"
        categories.add(createCategory(bills, null, "EXPENSE", 1, R.string.cat_bills, 3))
        categories.add(createCategory("cat_rent", bills, "EXPENSE", 2, R.string.cat_rent, 0))
        categories.add(createCategory("cat_electricity", bills, "EXPENSE", 2, R.string.cat_electricity, 1))
        categories.add(createCategory("cat_water", bills, "EXPENSE", 2, R.string.cat_water, 2))
        categories.add(createCategory("cat_internet", bills, "EXPENSE", 2, R.string.cat_internet, 3))
        categories.add(createCategory("cat_mobile", bills, "EXPENSE", 2, R.string.cat_mobile, 4))

        // Health & Wellness
        val health = "cat_health"
        categories.add(createCategory(health, null, "EXPENSE", 1, R.string.cat_health, 4))
        categories.add(createCategory("cat_doctor", health, "EXPENSE", 2, R.string.cat_doctor, 0))
        categories.add(createCategory("cat_pharmacy", health, "EXPENSE", 2, R.string.cat_pharmacy, 1))
        categories.add(createCategory("cat_gym", health, "EXPENSE", 2, R.string.cat_gym, 2))
        categories.add(createCategory("cat_insur", health, "EXPENSE", 2, R.string.cat_insur, 3))

        // Entertainment
        val entertainment = "cat_ent"
        categories.add(createCategory(entertainment, null, "EXPENSE", 1, R.string.cat_ent, 5))
        categories.add(createCategory("cat_movies", entertainment, "EXPENSE", 2, R.string.cat_movies, 0))
        categories.add(createCategory("cat_gaming", entertainment, "EXPENSE", 2, R.string.cat_gaming, 1))
        categories.add(createCategory("cat_ott", entertainment, "EXPENSE", 2, R.string.cat_ott, 2))
        categories.add(createCategory("cat_events", entertainment, "EXPENSE", 2, R.string.cat_events, 3))

        // Education
        val education = "cat_edu"
        categories.add(createCategory(education, null, "EXPENSE", 1, R.string.cat_edu, 6))
        categories.add(createCategory("cat_books", education, "EXPENSE", 2, R.string.cat_books, 0))
        categories.add(createCategory("cat_courses", education, "EXPENSE", 2, R.string.cat_courses, 1))
        categories.add(createCategory("cat_tuition", education, "EXPENSE", 2, R.string.cat_tuition, 2))

        // Investments
        val invest = "cat_invest"
        categories.add(createCategory(invest, null, "EXPENSE", 1, R.string.cat_invest, 7))
        categories.add(createCategory("cat_stocks", invest, "EXPENSE", 2, R.string.cat_stocks, 0))
        categories.add(createCategory("cat_mf", invest, "EXPENSE", 2, R.string.cat_mf, 1))
        categories.add(createCategory("cat_gold", invest, "EXPENSE", 2, R.string.cat_gold, 2))
        categories.add(createCategory("cat_realestate", invest, "EXPENSE", 2, R.string.cat_realestate, 3))

        // Personal Care
        val personal = "cat_personal"
        categories.add(createCategory(personal, null, "EXPENSE", 1, R.string.cat_personal, 8))
        categories.add(createCategory("cat_salon", personal, "EXPENSE", 2, R.string.cat_salon, 0))
        categories.add(createCategory("cat_cosmetics", personal, "EXPENSE", 2, R.string.cat_cosmetics, 1))
        categories.add(createCategory("cat_laundry", personal, "EXPENSE", 2, R.string.cat_laundry, 2))

        // Miscellaneous
        val misc = "cat_misc"
        categories.add(createCategory(misc, null, "EXPENSE", 1, R.string.cat_misc, 9))
        categories.add(createCategory("cat_gifts", misc, "EXPENSE", 2, R.string.cat_gifts, 0))
        categories.add(createCategory("cat_donation", misc, "EXPENSE", 2, R.string.cat_donation, 1))
        categories.add(createCategory("cat_taxes", misc, "EXPENSE", 2, R.string.cat_taxes, 2))
        categories.add(createCategory("cat_other", misc, "EXPENSE", 2, R.string.cat_other, 3))

        // --- INCOME CATEGORIES ---
        
        // Active Income
        val activeInc = "cat_active_inc"
        categories.add(createCategory(activeInc, null, "INCOME", 1, R.string.cat_active_inc, 0))
        categories.add(createCategory("cat_salary", activeInc, "INCOME", 2, R.string.cat_salary, 0))
        categories.add(createCategory("cat_business", activeInc, "INCOME", 2, R.string.cat_business, 1))
        categories.add(createCategory("cat_freelance", activeInc, "INCOME", 2, R.string.cat_freelance, 2))

        // Passive Income
        val passiveInc = "cat_passive_inc"
        categories.add(createCategory(passiveInc, null, "INCOME", 1, R.string.cat_passive_inc, 1))
        categories.add(createCategory("cat_dividends", passiveInc, "INCOME", 2, R.string.cat_dividends, 0))
        categories.add(createCategory("cat_interest", passiveInc, "INCOME", 2, R.string.cat_interest, 1))
        categories.add(createCategory("cat_rent_inc", passiveInc, "INCOME", 2, R.string.cat_rent_inc, 2))
        categories.add(createCategory("cat_royalties", passiveInc, "INCOME", 2, R.string.cat_royalties, 3))

        // Occasional Income
        val occInc = "cat_occ_inc"
        categories.add(createCategory(occInc, null, "INCOME", 1, R.string.cat_occ_inc, 2))
        categories.add(createCategory("cat_occ_gifts", occInc, "INCOME", 2, R.string.cat_occ_gifts, 0))
        categories.add(createCategory("cat_refunds", occInc, "INCOME", 2, R.string.cat_refunds, 1))
        categories.add(createCategory("cat_tax_rebate", occInc, "INCOME", 2, R.string.cat_tax_rebate, 2))
        categories.add(createCategory("cat_awards", occInc, "INCOME", 2, R.string.cat_awards, 3))

        return categories
    }

    private fun createCategory(
        id: String,
        parentId: String?,
        type: String,
        level: Int,
        nameResId: Int,
        order: Int
    ) = CategoryEntity(
        id = id,
        parentId = parentId,
        type = type,
        level = level,
        name = nameResId.toString(), // Store Res ID as string for now, to be resolved by mapper/UI
        displayOrder = order,
        isSystem = true,
        isActive = true,
        profileId = null,
        createdAt = now,
        updatedAt = now
    )
}
