package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY id DESC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    fun getEmployeeById(id: Long): Flow<Employee?>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeByIdDirect(id: Long): Employee?

    @Query("SELECT * FROM employees WHERE employeeCode = :code LIMIT 1")
    suspend fun getEmployeeByCode(code: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("UPDATE employees SET validUntilDate = :validUntil, lastPaymentDate = :lastPaymentDate WHERE id = :id")
    suspend fun updateValidityAndPaymentDate(id: Long, validUntil: Long, lastPaymentDate: Long)

    @Query("UPDATE employees SET validUntilDate = :validUntil WHERE id = :id")
    suspend fun updateValidityOnly(id: Long, validUntil: Long)

    @Query("SELECT COUNT(*) FROM employees")
    fun getEmployeeCount(): Flow<Int>
}
