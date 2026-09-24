package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RenewalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RenewalRecordDao {
    @Query("SELECT * FROM renewal_records WHERE employeeId = :employeeId ORDER BY renewalDate DESC")
    fun getRenewalsForEmployee(employeeId: Long): Flow<List<RenewalRecord>>

    @Query("SELECT * FROM renewal_records ORDER BY renewalDate DESC")
    fun getAllRenewals(): Flow<List<RenewalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRenewal(renewal: RenewalRecord): Long
}
