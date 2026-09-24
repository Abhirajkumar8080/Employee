package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "renewal_records",
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
data class RenewalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeCode: String,
    val amount: Double = 149.0,
    val renewalDate: Long = System.currentTimeMillis(),
    val ownerUpi: String = "9798093650",
    val utrNumber: String = "",
    val extendedDays: Int = 60,
    val remarks: String = "60-Day Code Reactivation via UPI"
)
