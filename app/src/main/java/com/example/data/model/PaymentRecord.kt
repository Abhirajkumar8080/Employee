package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId"])]
)
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeCode: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentType: String, // Salary, Advance, Incentive, Bonus, Overtime
    val paymentMode: String, // UPI, Cash, Bank Transfer, Cheque
    val referenceNote: String = "",
    val receiptNumber: String = ""
)
