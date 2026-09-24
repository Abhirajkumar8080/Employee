package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeCode: String, // e.g. "EMP-48201"
    val fullName: String,
    val phone: String,
    val designation: String = "",
    val department: String = "",
    val basicSalary: Double = 0.0,
    val photoUri: String? = null,
    
    // Employee Documents
    val aadharNumber: String = "",
    val aadharDocUri: String? = null,
    val panNumber: String = "",
    val panDocUri: String? = null,
    val uinNumber: String = "", // Universal Identification Number / UAN
    
    // Bank Details
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankIfsc: String = "",
    val bankHolderName: String = "",
    
    // Nominee Details
    val nomineeName: String = "",
    val nomineeRelation: String = "",
    val nomineePhone: String = "",
    val nomineeAadharNumber: String = "",
    val nomineeAadharDocUri: String? = null,
    
    // Timeline & 60-Day Payment Code Rules
    val registrationDate: Long = System.currentTimeMillis(),
    val lastPaymentDate: Long? = null,
    val validUntilDate: Long = System.currentTimeMillis() + (60L * 24 * 60 * 60 * 1000L),
    val notes: String = ""
) {
    /**
     * Calculates days remaining until the employee code is suspended ("Band").
     * A code expires if no payment is received or renewed for 60 days.
     */
    fun getRemainingDays(currentTime: Long = System.currentTimeMillis()): Long {
        val diffMillis = validUntilDate - currentTime
        return if (diffMillis <= 0) 0 else (diffMillis / (1000L * 60 * 60 * 24)) + 1
    }

    /**
     * Determines current operational status of the employee code.
     */
    fun getCodeStatus(currentTime: Long = System.currentTimeMillis()): CodeStatus {
        val remaining = getRemainingDays(currentTime)
        return when {
            validUntilDate <= currentTime || remaining <= 0 -> CodeStatus.BAND_EXPIRED
            remaining <= 7 -> CodeStatus.EXPIRING_SOON
            else -> CodeStatus.ACTIVE
        }
    }
}

enum class CodeStatus {
    ACTIVE,          // Code is active and payments are up to date
    EXPIRING_SOON,   // Code expires within 7 days
    BAND_EXPIRED     // Code is suspended ("Band") - 60+ days without payment
}
