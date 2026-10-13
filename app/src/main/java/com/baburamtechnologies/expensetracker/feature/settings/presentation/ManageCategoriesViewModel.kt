package com.baburamtechnologies.expensetracker.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val getCategoriesByTypeAndLevelUseCase: GetCategoriesByTypeAndLevelUseCase,
    private val getCategoriesByParentUseCase: GetCategoriesByParentUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val _selectedLevel1 = MutableStateFlow<Category?>(null)
    private val _selectedLevel2 = MutableStateFlow<Category?>(null)

    private val level1Categories: StateFlow<List<Category>> = _selectedType
        .flatMapLatest { type -> getCategoriesByTypeAndLevelUseCase(type, 1, includeInactive = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val level2Categories: StateFlow<List<Category>> = _selectedLevel1
        .flatMapLatest { category ->
            if (category != null) getCategoriesByParentUseCase(category.id, includeInactive = true)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val level3Categories: StateFlow<List<Category>> = _selectedLevel2
        .flatMapLatest { category ->
            if (category != null) getCategoriesByParentUseCase(category.id, includeInactive = true)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ManageCategoriesUiState> = combine(
        _selectedType,
        _selectedLevel1,
        _selectedLevel2,
        level1Categories,
        level2Categories,
        level3Categories
    ) { args ->
        ManageCategoriesUiState(
            selectedType = args[0] as TransactionType,
            selectedLevel1 = args[1] as Category?,
            selectedLevel2 = args[2] as Category?,
            level1Categories = args[3] as List<Category>,
            level2Categories = args[4] as List<Category>,
            level3Categories = args[5] as List<Category>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ManageCategoriesUiState())

    fun onTypeChange(type: TransactionType) {
        _selectedType.value = type
        _selectedLevel1.value = null
        _selectedLevel2.value = null
    }

    fun onLevel1Select(category: Category?) {
        _selectedLevel1.value = category
        _selectedLevel2.value = null
    }

    fun onLevel2Select(category: Category?) {
        _selectedLevel2.value = category
    }

    fun addLevel3Category(name: String) {
        val parent = _selectedLevel2.value ?: return
        val category = Category(
            id = UUID.randomUUID().toString(),
            parentId = parent.id,
            type = _selectedType.value,
            level = 3,
            name = name,
            displayOrder = level3Categories.value.size,
            isSystem = false,
            isActive = true
        )
        viewModelScope.launch {
            addCategoryUseCase(category)
        }
    }

    fun updateCategory(category: Category, newName: String) {
        viewModelScope.launch {
            updateCategoryUseCase(category.copy(name = newName))
        }
    }

    fun toggleCategoryActive(category: Category) {
        viewModelScope.launch {
            updateCategoryUseCase(category.copy(isActive = !category.isActive))
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            deleteCategoryUseCase(id)
        }
    }
}

data class ManageCategoriesUiState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val level1Categories: List<Category> = emptyList(),
    val level2Categories: List<Category> = emptyList(),
    val level3Categories: List<Category> = emptyList(),
    val selectedLevel1: Category? = null,
    val selectedLevel2: Category? = null
)
