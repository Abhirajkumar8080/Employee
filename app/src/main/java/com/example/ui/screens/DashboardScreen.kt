package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.CodeStatus
import com.example.data.model.Employee
import com.example.ui.components.CodeDisplayPill
import com.example.ui.components.OwnerUpiCard
import com.example.ui.components.StatusBadge
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.dialogs.RenewCodeDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.EmployeeViewModel
import com.example.ui.viewmodel.FilterStatus
import com.example.util.UpiHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: EmployeeViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val employees by viewModel.filteredEmployees.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var employeeForPayment by remember { mutableStateOf<Employee?>(null) }
    var employeeForRenewal by remember { mutableStateOf<Employee?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Employee Register",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Payment Code & 60-Day Management",
                                fontSize = 11.sp,
                                color = GoldLight
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRegister,
                containerColor = GoldAccent,
                contentColor = NavyDark,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("New Employee", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("new_employee_fab")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // App Owner UPI Banner
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OwnerUpiCard(
                        onCopyUpi = {
                            UpiHelper.copyToClipboard(
                                context,
                                "Owner UPI ID",
                                UpiHelper.OWNER_UPI_NUMBER
                            )
                        }
                    )
                }
            }

            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total",
                        count = stats.totalEmployees,
                        color = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active",
                        count = stats.activeCount,
                        color = StatusActiveGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Expiring",
                        count = stats.expiringSoonCount,
                        color = StatusWarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Code Band",
                        count = stats.bandExpiredCount,
                        color = StatusExpiredRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by name, code, phone, aadhar...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterStatusChip(
                        label = "All (${stats.totalEmployees})",
                        selected = selectedFilter == FilterStatus.ALL,
                        onClick = { viewModel.setFilter(FilterStatus.ALL) },
                        activeColor = NavyPrimary
                    )
                    FilterStatusChip(
                        label = "Active (${stats.activeCount})",
                        selected = selectedFilter == FilterStatus.ACTIVE,
                        onClick = { viewModel.setFilter(FilterStatus.ACTIVE) },
                        activeColor = StatusActiveGreen
                    )
                    FilterStatusChip(
                        label = "Expiring (${stats.expiringSoonCount})",
                        selected = selectedFilter == FilterStatus.EXPIRING_SOON,
                        onClick = { viewModel.setFilter(FilterStatus.EXPIRING_SOON) },
                        activeColor = StatusWarningAmber
                    )
                    FilterStatusChip(
                        label = "Band (${stats.bandExpiredCount})",
                        selected = selectedFilter == FilterStatus.BAND_EXPIRED,
                        onClick = { viewModel.setFilter(FilterStatus.BAND_EXPIRED) },
                        activeColor = StatusExpiredRed
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Empty State
            if (employees.isEmpty()) {
                item {
                    EmptyStateView(
                        isFiltered = searchQuery.isNotEmpty() || selectedFilter != FilterStatus.ALL,
                        onAddEmployee = onNavigateToRegister
                    )
                }
            } else {
                // Employees List
                items(employees, key = { it.id }) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onClick = { onNavigateToDetail(employee.id) },
                        onPayClick = { employeeForPayment = employee },
                        onRenewClick = { employeeForRenewal = employee },
                        onCopyCode = {
                            UpiHelper.copyToClipboard(
                                context,
                                "Employee Code",
                                employee.employeeCode
                            )
                        },
                        onCallClick = {
                            if (employee.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${employee.phone}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    // Record Payment Dialog
    employeeForPayment?.let { emp ->
        RecordPaymentDialog(
            employee = emp,
            onDismiss = { employeeForPayment = null },
            onSavePayment = { amount, type, mode, note ->
                viewModel.recordPayment(emp.id, amount, type, mode, note)
                employeeForPayment = null
            }
        )
    }

    // Renew Code Dialog (Owner UPI)
    employeeForRenewal?.let { emp ->
        RenewCodeDialog(
            employee = emp,
            onDismiss = { employeeForRenewal = null },
            onConfirmRenewal = { amount, utr ->
                viewModel.reactivateCodeViaOwnerUpi(emp.id, amount, utr)
                employeeForRenewal = null
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun FilterStatusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = activeColor,
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onClick: () -> Unit,
    onPayClick: () -> Unit,
    onRenewClick: () -> Unit,
    onCopyCode: () -> Unit,
    onCallClick: () -> Unit
) {
    val status = employee.getCodeStatus()
    val remainingDays = employee.getRemainingDays()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (status == CodeStatus.BAND_EXPIRED) Color(0xFFFFF7F7) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (status == CodeStatus.BAND_EXPIRED) StatusExpiredRed.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .testTag("employee_card_${employee.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Photo, Name, Code, Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Photo or Initials
                if (!employee.photoUri.isNullOrBlank() && File(employee.photoUri).exists()) {
                    AsyncImage(
                        model = File(employee.photoUri),
                        contentDescription = employee.fullName,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, GoldAccent, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = employee.fullName.take(2).uppercase(),
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = employee.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    if (employee.designation.isNotBlank()) {
                        Text(
                            text = "${employee.designation}${if (employee.department.isNotBlank()) " • " + employee.department else ""}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeDisplayPill(
                        code = employee.employeeCode,
                        onCopy = onCopyCode
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(status = status, remainingDays = remainingDays)
                    if (employee.phone.isNotBlank()) {
                        IconButton(
                            onClick = onCallClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = NavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // 60-Day Info & Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (status == CodeStatus.BAND_EXPIRED) {
                            "Code Band (60+ Din No Payment)"
                        } else {
                            "$remainingDays Days left before code locks"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (status == CodeStatus.BAND_EXPIRED) StatusExpiredRed else Color(0xFF475569)
                    )
                    Text(
                        text = if (employee.lastPaymentDate != null) {
                            "Last paid: ${UpiHelper.formatDateShort(employee.lastPaymentDate)}"
                        } else {
                            "Registered: ${UpiHelper.formatDateShort(employee.registrationDate)}"
                        },
                        fontSize = 10.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // If Code is band/expired, highlight Renew UPI button
                    if (status == CodeStatus.BAND_EXPIRED) {
                        Button(
                            onClick = onRenewClick,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusExpiredRed),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chalu Karein", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onPayClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusActiveGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusActiveGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pay Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    isFiltered: Boolean,
    onAddEmployee: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.People,
                contentDescription = null,
                tint = GoldDark,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (isFiltered) "No employees found" else "No employees registered yet",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isFiltered) "Try adjusting your search query or status filter." else "Register your first employee to assign payment codes, track Aadhar/PAN documents, and manage 60-day active periods.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
        if (!isFiltered) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddEmployee,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Register New Employee", fontWeight = FontWeight.Bold)
            }
        }
    }
}
