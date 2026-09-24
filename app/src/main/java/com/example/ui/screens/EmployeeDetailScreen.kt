package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.PaymentRecord
import com.example.data.model.RenewalRecord
import com.example.ui.components.CodeDisplayPill
import com.example.ui.components.ExpiryTrackerCard
import com.example.ui.dialogs.DocPreviewDialog
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.dialogs.RenewCodeDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.EmployeeViewModel
import com.example.util.UpiHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    employeeId: Long,
    viewModel: EmployeeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Employee) -> Unit
) {
    val context = LocalContext.current
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val employee = allEmployees.find { it.id == employeeId }

    val payments by viewModel.getPaymentsForEmployee(employeeId).collectAsStateWithLifecycle()
    val totalPaid by viewModel.getTotalPaidForEmployee(employeeId).collectAsStateWithLifecycle()
    val renewals by viewModel.getRenewalsForEmployee(employeeId).collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showRenewDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var previewDocTitle by remember { mutableStateOf<String?>(null) }
    var previewDocPath by remember { mutableStateOf<String?>(null) }

    if (employee == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NavyPrimary)
        }
        return
    }

    val status = employee.getCodeStatus()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Employee Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(employee) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = {
                        val shareText = """
                            EMPLOYEE DETAILS
                            Name: ${employee.fullName}
                            Payment Code: ${employee.employeeCode}
                            Phone: ${employee.phone}
                            Designation: ${employee.designation} (${employee.department})
                            Aadhar: ${employee.aadharNumber.ifBlank { "N/A" }}
                            PAN: ${employee.panNumber.ifBlank { "N/A" }}
                            UIN: ${employee.uinNumber.ifBlank { "N/A" }}
                            Bank: ${employee.bankName} - A/C: ${employee.bankAccountNumber} (IFSC: ${employee.bankIfsc})
                            Nominee: ${employee.nomineeName} (${employee.nomineeRelation}) - ${employee.nomineePhone}
                            Code Status: ${status.name} (Valid till ${UpiHelper.formatDateShort(employee.validUntilDate)})
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Employee Details"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A80))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Profile Header Card
            item {
                Card(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Photo
                        if (!employee.photoUri.isNullOrBlank() && File(employee.photoUri).exists()) {
                            AsyncImage(
                                model = File(employee.photoUri),
                                contentDescription = employee.fullName,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, GoldAccent, CircleShape)
                                    .clickable {
                                        previewDocTitle = "Employee Photo"
                                        previewDocPath = employee.photoUri
                                    },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(NavyLight)
                                    .border(2.dp, GoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = employee.fullName.take(2).uppercase(),
                                    color = GoldLight,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = employee.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (employee.designation.isNotBlank()) {
                            Text(
                                text = "${employee.designation}${if (employee.department.isNotBlank()) " • " + employee.department else ""}",
                                fontSize = 13.sp,
                                color = GoldLight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        CodeDisplayPill(
                            code = employee.employeeCode,
                            onCopy = {
                                UpiHelper.copyToClipboard(context, "Employee Code", employee.employeeCode)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick action buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (employee.phone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${employee.phone}"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(employee.phone, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Expiry Tracker 60-Day Box
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExpiryTrackerCard(
                        employee = employee,
                        onRenewClick = { showRenewDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Action Buttons: Record Payment & Reactivate Code
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showPaymentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusActiveGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_pay_button")
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payment", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showRenewDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == CodeStatus.BAND_EXPIRED) StatusExpiredRed else GoldDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_renew_button")
                    ) {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (status == CodeStatus.BAND_EXPIRED) "Chalu Karein (UPI)" else "Renew Code (UPI)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tabs: Details, Payments, Renewals
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Details & Docs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Payments (${payments.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Renewals (${renewals.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: Documents & Details
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Official Documents Card
                            DetailSectionCard(title = "Official Identification Documents", icon = Icons.Default.Badge) {
                                DetailRow("Aadhar Number", employee.aadharNumber.ifBlank { "Not provided" })
                                if (!employee.aadharDocUri.isNullOrBlank()) {
                                    DocAttachmentRow("Aadhar Card Photo") {
                                        previewDocTitle = "Aadhar Card Document"
                                        previewDocPath = employee.aadharDocUri
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))
                                DetailRow("PAN Number", employee.panNumber.ifBlank { "Not provided" })
                                if (!employee.panDocUri.isNullOrBlank()) {
                                    DocAttachmentRow("PAN Card Photo") {
                                        previewDocTitle = "PAN Card Document"
                                        previewDocPath = employee.panDocUri
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))
                                DetailRow("UIN Number (UAN/PF)", employee.uinNumber.ifBlank { "Not provided" })
                            }

                            // Bank Details Card
                            DetailSectionCard(title = "Bank Account Information", icon = Icons.Default.AccountBalance) {
                                DetailRow("Bank Name", employee.bankName.ifBlank { "Not provided" })
                                DetailRow("Account Number", employee.bankAccountNumber.ifBlank { "Not provided" })
                                DetailRow("IFSC Code", employee.bankIfsc.ifBlank { "Not provided" })
                                DetailRow("Account Holder", employee.bankHolderName.ifBlank { "Not provided" })
                                if (employee.basicSalary > 0) {
                                    DetailRow("Base Salary / Wage", UpiHelper.formatCurrency(employee.basicSalary))
                                }
                            }

                            // Nominee Details Card
                            DetailSectionCard(title = "Nominee Information", icon = Icons.Default.FamilyRestroom) {
                                DetailRow("Nominee Name", employee.nomineeName.ifBlank { "Not provided" })
                                DetailRow("Relationship", employee.nomineeRelation.ifBlank { "Not provided" })
                                DetailRow("Nominee Mobile", employee.nomineePhone.ifBlank { "Not provided" })
                                DetailRow("Nominee Aadhar", employee.nomineeAadharNumber.ifBlank { "Not provided" })
                                if (!employee.nomineeAadharDocUri.isNullOrBlank()) {
                                    DocAttachmentRow("Nominee Aadhar Photo") {
                                        previewDocTitle = "Nominee Aadhar Document"
                                        previewDocPath = employee.nomineeAadharDocUri
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                1 -> {
                    // TAB 1: Payment History
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusActiveGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Amount Paid", fontSize = 12.sp, color = Color(0xFF166534))
                                    Text(
                                        text = UpiHelper.formatCurrency(totalPaid ?: 0.0),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                }
                                Button(
                                    onClick = { showPaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusActiveGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Payment", fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (payments.isEmpty()) {
                        item {
                            EmptyTabNotice("No payments logged yet. When you record a salary or advance payment on this code, the 60-day validity will automatically refresh!")
                        }
                    } else {
                        items(payments, key = { it.id }) { payment ->
                            PaymentRecordItem(payment = payment)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                2 -> {
                    // TAB 2: Renewals & Reactivation History
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Code Reactivation via App Owner UPI",
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Owner UPI: ${UpiHelper.OWNER_UPI_NUMBER} • Each renewal extends validity by 60 Days",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (renewals.isEmpty()) {
                        item {
                            EmptyTabNotice("No owner UPI renewals yet. When a code expires (after 60 days without payment), you can reactivate it by paying to app owner's UPI ${UpiHelper.OWNER_UPI_NUMBER}!")
                        }
                    } else {
                        items(renewals, key = { it.id }) { renewal ->
                            RenewalRecordItem(renewal = renewal)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showPaymentDialog) {
        RecordPaymentDialog(
            employee = employee,
            onDismiss = { showPaymentDialog = false },
            onSavePayment = { amount, type, mode, note ->
                viewModel.recordPayment(employee.id, amount, type, mode, note)
                showPaymentDialog = false
            }
        )
    }

    if (showRenewDialog) {
        RenewCodeDialog(
            employee = employee,
            onDismiss = { showRenewDialog = false },
            onConfirmRenewal = { amount, utr ->
                viewModel.reactivateCodeViaOwnerUpi(employee.id, amount, utr)
                showRenewDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Employee?") },
            text = { Text("Are you sure you want to delete ${employee.fullName} (${employee.employeeCode})? This will also remove all associated payment and renewal history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEmployee(employee) {
                            showDeleteConfirm = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = StatusExpiredRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (previewDocPath != null && previewDocTitle != null) {
        DocPreviewDialog(
            title = previewDocTitle!!,
            imagePath = previewDocPath!!,
            onDismiss = {
                previewDocPath = null
                previewDocTitle = null
            }
        )
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.5.sp, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun DocAttachmentRow(label: String, onView: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Image, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 12.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        }
        TextButton(
            onClick = onView,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("View Photo", fontSize = 12.sp, color = NavyPrimary)
        }
    }
}

@Composable
private fun PaymentRecordItem(payment: PaymentRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StatusActiveGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${payment.paymentType} • ${payment.paymentMode}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = UpiHelper.formatDate(payment.paymentDate),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    if (payment.referenceNote.isNotBlank()) {
                        Text(
                            text = payment.referenceNote,
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${UpiHelper.formatCurrency(payment.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = StatusActiveGreen
                )
                Text(
                    text = "Refreshed 60d ✓",
                    fontSize = 10.sp,
                    color = StatusActiveGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RenewalRecordItem(renewal: RenewalRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Autorenew, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Owner UPI Reactivation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = UpiHelper.formatDate(renewal.renewalDate),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    if (renewal.utrNumber.isNotBlank()) {
                        Text(
                            text = "UTR: ${renewal.utrNumber}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Paid ${UpiHelper.formatCurrency(renewal.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyPrimary
                )
                Text(
                    text = "+${renewal.extendedDays} Days Active",
                    fontSize = 10.5.sp,
                    color = StatusActiveGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyTabNotice(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
