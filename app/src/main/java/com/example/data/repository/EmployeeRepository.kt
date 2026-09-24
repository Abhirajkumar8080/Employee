package com.example.data.repository

import com.example.data.local.EmployeeDao
import com.example.data.local.PaymentRecordDao
import com.example.data.local.RenewalRecordDao
import com.example.data.model.Employee
import com.example.data.model.PaymentRecord
import com.example.data.model.RenewalRecord
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class EmployeeRepository(
    private val employeeDao: EmployeeDao,
    private val paymentRecordDao: PaymentRecordDao,
    private val renewalRecordDao: RenewalRecordDao
) {
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()

    fun getEmployeeById(id: Long): Flow<Employee?> = employeeDao.getEmployeeById(id)

    suspend fun getEmployeeByIdDirect(id: Long): Employee? = employeeDao.getEmployeeByIdDirect(id)

    fun getPaymentsForEmployee(employeeId: Long): Flow<List<PaymentRecord>> =
        paymentRecordDao.getPaymentsForEmployee(employeeId)

    fun getTotalPaidForEmployee(employeeId: Long): Flow<Double?> =
        paymentRecordDao.getTotalPaidForEmployee(employeeId)

    fun getRenewalsForEmployee(employeeId: Long): Flow<List<RenewalRecord>> =
        renewalRecordDao.getRenewalsForEmployee(employeeId)

    /**
     * Registers a new employee with a unique Employee Payment Code
     * and sets an initial 60-day validity window.
     */
    suspend fun registerEmployee(employee: Employee): Long {
        val now = System.currentTimeMillis()
        val code = if (employee.employeeCode.isBlank()) {
            generateUniqueCode()
        } else {
            employee.employeeCode.trim().uppercase()
        }

        val sixtyDaysMillis = 60L * 24 * 60 * 60 * 1000L
        val finalizedEmployee = employee.copy(
            employeeCode = code,
            registrationDate = if (employee.registrationDate > 0) employee.registrationDate else now,
            validUntilDate = now + sixtyDaysMillis
        )
        return employeeDao.insertEmployee(finalizedEmployee)
    }

    suspend fun updateEmployee(employee: Employee) {
        employeeDao.updateEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee) {
        employeeDao.deleteEmployee(employee)
    }

    /**
     * Records a payment made on the Employee Payment Code (e.g. Salary, Advance, Bonus).
     * Rule: Recording a payment resets and extends the 60-Day active window!
     */
    suspend fun recordEmployeePayment(
        employeeId: Long,
        amount: Double,
        paymentType: String,
        paymentMode: String,
        referenceNote: String,
        paymentDate: Long = System.currentTimeMillis()
    ): Long {
        val employee = employeeDao.getEmployeeByIdDirect(employeeId) ?: return -1

        val receiptNumber = "RCP-${System.currentTimeMillis().toString().takeLast(6)}"
        val paymentRecord = PaymentRecord(
            employeeId = employeeId,
            employeeCode = employee.employeeCode,
            amount = amount,
            paymentDate = paymentDate,
            paymentType = paymentType,
            paymentMode = paymentMode,
            referenceNote = referenceNote,
            receiptNumber = receiptNumber
        )
        val paymentId = paymentRecordDao.insertPayment(paymentRecord)

        // Reset the 60-day timer starting from this payment date
        val sixtyDaysMillis = 60L * 24 * 60 * 60 * 1000L
        val newValidUntil = paymentDate + sixtyDaysMillis

        employeeDao.updateValidityAndPaymentDate(
            id = employeeId,
            validUntil = newValidUntil,
            lastPaymentDate = paymentDate
        )

        return paymentId
    }

    /**
     * Reactivates / Renews a suspended or active code via payment to App Owner's UPI (9798093650).
     * Automatically updates the employee's code validity for another 60 days.
     */
    suspend fun reactivateCodeViaOwnerPayment(
        employeeId: Long,
        amount: Double = 149.0,
        utrNumber: String = "",
        ownerUpi: String = "9798093650",
        remarks: String = "Code reactivated via owner UPI"
    ): Long {
        val employee = employeeDao.getEmployeeByIdDirect(employeeId) ?: return -1
        val now = System.currentTimeMillis()

        val renewalRecord = RenewalRecord(
            employeeId = employeeId,
            employeeCode = employee.employeeCode,
            amount = amount,
            renewalDate = now,
            ownerUpi = ownerUpi,
            utrNumber = utrNumber.trim(),
            extendedDays = 60,
            remarks = remarks
        )
        val renewalId = renewalRecordDao.insertRenewal(renewalRecord)

        // Extend validity by 60 days from now (or from existing validity if still in the future)
        val sixtyDaysMillis = 60L * 24 * 60 * 60 * 1000L
        val baseTime = if (employee.validUntilDate > now) employee.validUntilDate else now
        val newValidUntil = baseTime + sixtyDaysMillis

        employeeDao.updateValidityOnly(
            id = employeeId,
            validUntil = newValidUntil
        )

        return renewalId
    }

    private fun generateUniqueCode(): String {
        val randomNum = Random.nextInt(10000, 99999)
        return "EMP-$randomNum"
    }
}
