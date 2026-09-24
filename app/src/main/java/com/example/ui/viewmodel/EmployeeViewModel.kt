package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CodeStatus
import com.example.data.model.Employee
import com.example.data.model.PaymentRecord
import com.example.data.model.RenewalRecord
import com.example.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterStatus {
    ALL,
    ACTIVE,
    EXPIRING_SOON,
    BAND_EXPIRED
}

data class DashboardStats(
    val totalEmployees: Int = 0,
    val activeCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val bandExpiredCount: Int = 0
)

class EmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmployeeRepository
    
    init {
        val db = AppDatabase.getInstance(application)
        repository = EmployeeRepository(
            employeeDao = db.employeeDao(),
            paymentRecordDao = db.paymentRecordDao(),
            renewalRecordDao = db.renewalRecordDao()
        )
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterStatus.ALL)
    val selectedFilter: StateFlow<FilterStatus> = _selectedFilter.asStateFlow()

    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredEmployees: StateFlow<List<Employee>> = combine(
        allEmployees,
        _searchQuery,
        _selectedFilter
    ) { employees, query, filter ->
        val now = System.currentTimeMillis()
        employees.filter { emp ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                emp.fullName.lowercase().contains(q) ||
                emp.employeeCode.lowercase().contains(q) ||
                emp.phone.contains(q) ||
                emp.designation.lowercase().contains(q) ||
                emp.aadharNumber.contains(q) ||
                emp.uinNumber.lowercase().contains(q)
            }

            val status = emp.getCodeStatus(now)
            val matchesFilter = when (filter) {
                FilterStatus.ALL -> true
                FilterStatus.ACTIVE -> status == CodeStatus.ACTIVE
                FilterStatus.EXPIRING_SOON -> status == CodeStatus.EXPIRING_SOON
                FilterStatus.BAND_EXPIRED -> status == CodeStatus.BAND_EXPIRED
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats: StateFlow<DashboardStats> = allEmployees.combine(_searchQuery) { employees, _ ->
        val now = System.currentTimeMillis()
        var active = 0
        var expiringSoon = 0
        var band = 0
        for (emp in employees) {
            when (emp.getCodeStatus(now)) {
                CodeStatus.ACTIVE -> active++
                CodeStatus.EXPIRING_SOON -> expiringSoon++
                CodeStatus.BAND_EXPIRED -> band++
            }
        }
        DashboardStats(
            totalEmployees = employees.size,
            activeCount = active,
            expiringSoonCount = expiringSoon,
            bandExpiredCount = band
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: FilterStatus) {
        _selectedFilter.value = filter
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun registerEmployee(employee: Employee, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val id = repository.registerEmployee(employee)
                _snackbarMessage.value = "Employee registered successfully! Code: ${employee.employeeCode.ifBlank { "Generated" }}"
                onComplete(id)
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to register employee: ${e.message}"
            }
        }
    }

    fun updateEmployee(employee: Employee, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateEmployee(employee)
                _snackbarMessage.value = "Employee updated successfully!"
                onComplete()
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to update employee: ${e.message}"
            }
        }
    }

    fun deleteEmployee(employee: Employee, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteEmployee(employee)
                _snackbarMessage.value = "Employee record deleted"
                onComplete()
            } catch (e: Exception) {
                _snackbarMessage.value = "Error deleting employee: ${e.message}"
            }
        }
    }

    fun recordPayment(
        employeeId: Long,
        amount: Double,
        paymentType: String,
        paymentMode: String,
        note: String,
        paymentDate: Long = System.currentTimeMillis(),
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.recordEmployeePayment(
                    employeeId = employeeId,
                    amount = amount,
                    paymentType = paymentType,
                    paymentMode = paymentMode,
                    referenceNote = note,
                    paymentDate = paymentDate
                )
                _snackbarMessage.value = "Payment recorded! 60-Day Code validity refreshed."
                onComplete()
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to record payment: ${e.message}"
            }
        }
    }

    fun reactivateCodeViaOwnerUpi(
        employeeId: Long,
        amount: Double,
        utrNumber: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.reactivateCodeViaOwnerPayment(
                    employeeId = employeeId,
                    amount = amount,
                    utrNumber = utrNumber,
                    ownerUpi = "9798093650"
                )
                _snackbarMessage.value = "Payment confirmed! Code has been reactivated for 60 Days."
                onComplete()
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to activate code: ${e.message}"
            }
        }
    }

    fun getPaymentsForEmployee(employeeId: Long): StateFlow<List<PaymentRecord>> {
        return repository.getPaymentsForEmployee(employeeId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun getTotalPaidForEmployee(employeeId: Long): StateFlow<Double?> {
        return repository.getTotalPaidForEmployee(employeeId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )
    }

    fun getRenewalsForEmployee(employeeId: Long): StateFlow<List<RenewalRecord>> {
        return repository.getRenewalsForEmployee(employeeId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}
