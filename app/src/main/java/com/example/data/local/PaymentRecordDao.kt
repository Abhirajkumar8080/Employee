package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {
    @Query("SELECT * FROM payment_records WHERE employeeId = :employeeId ORDER BY paymentDate DESC")
    fun getPaymentsForEmployee(employeeId: Long): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Query("SELECT SUM(amount) FROM payment_records WHERE employeeId = :employeeId")
    fun getTotalPaidForEmployee(employeeId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Delete
    suspend fun deletePayment(payment: PaymentRecord)
}
