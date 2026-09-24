package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Employee
import com.example.ui.components.DocumentAttachmentBox
import com.example.ui.dialogs.DocPreviewDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.EmployeeViewModel
import com.example.util.StorageHelper
import java.io.File
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterEmployeeScreen(
    viewModel: EmployeeViewModel,
    existingEmployee: Employee? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Employee Primary Info
    var fullName by remember { mutableStateOf(existingEmployee?.fullName ?: "") }
    var phone by remember { mutableStateOf(existingEmployee?.phone ?: "") }
    var designation by remember { mutableStateOf(existingEmployee?.designation ?: "") }
    var department by remember { mutableStateOf(existingEmployee?.department ?: "") }
    var basicSalary by remember {
        mutableStateOf(if (existingEmployee != null && existingEmployee.basicSalary > 0) existingEmployee.basicSalary.toInt().toString() else "")
    }
    var employeeCode by remember {
        mutableStateOf(existingEmployee?.employeeCode ?: "EMP-${Random.nextInt(10000, 99999)}")
    }
    var photoUri by remember { mutableStateOf(existingEmployee?.photoUri) }

    // Official Documents
    var aadharNumber by remember { mutableStateOf(existingEmployee?.aadharNumber ?: "") }
    var aadharDocUri by remember { mutableStateOf(existingEmployee?.aadharDocUri) }
    var panNumber by remember { mutableStateOf(existingEmployee?.panNumber ?: "") }
    var panDocUri by remember { mutableStateOf(existingEmployee?.panDocUri) }
    var uinNumber by remember { mutableStateOf(existingEmployee?.uinNumber ?: "") }

    // Bank Details
    var bankName by remember { mutableStateOf(existingEmployee?.bankName ?: "") }
    var bankAccountNumber by remember { mutableStateOf(existingEmployee?.bankAccountNumber ?: "") }
    var bankIfsc by remember { mutableStateOf(existingEmployee?.bankIfsc ?: "") }
    var bankHolderName by remember { mutableStateOf(existingEmployee?.bankHolderName ?: "") }

    // Nominee Details
    var nomineeName by remember { mutableStateOf(existingEmployee?.nomineeName ?: "") }
    var nomineeRelation by remember { mutableStateOf(existingEmployee?.nomineeRelation ?: "") }
    var nomineePhone by remember { mutableStateOf(existingEmployee?.nomineePhone ?: "") }
    var nomineeAadharNumber by remember { mutableStateOf(existingEmployee?.nomineeAadharNumber ?: "") }
    var nomineeAadharDocUri by remember { mutableStateOf(existingEmployee?.nomineeAadharDocUri) }

    var previewImageTitle by remember { mutableStateOf<String?>(null) }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    // Photo pickers
    val employeePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = StorageHelper.persistImageUri(context, it, "emp_photo")
            if (savedPath != null) photoUri = savedPath
        }
    }

    val aadharDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = StorageHelper.persistImageUri(context, it, "emp_aadhar")
            if (savedPath != null) aadharDocUri = savedPath
        }
    }

    val panDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = StorageHelper.persistImageUri(context, it, "emp_pan")
            if (savedPath != null) panDocUri = savedPath
        }
    }

    val nomineeAadharPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = StorageHelper.persistImageUri(context, it, "nominee_aadhar")
            if (savedPath != null) nomineeAadharDocUri = savedPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingEmployee == null) "New Employee Register" else "Edit Employee",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section 1: Employee Payment Code Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = NavyDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EMPLOYEE PAYMENT CODE",
                            fontSize = 11.sp,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        OutlinedTextField(
                            value = employeeCode,
                            onValueChange = { employeeCode = it.uppercase() },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f)
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("employee_code_input")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Personal Profile & Photo
            Text(
                text = "1. Employee Personal Details",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Photo Uploader Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldAccent, CircleShape)
                        .clickable {
                            if (photoUri != null) {
                                previewImageTitle = "Employee Photo"
                                previewImagePath = photoUri
                            } else {
                                employeePhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoUri.isNullOrBlank() && File(photoUri!!).exists()) {
                        AsyncImage(
                            model = File(photoUri!!),
                            contentDescription = "Employee Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Upload Photo",
                            tint = NavyPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Employee Photo *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (!photoUri.isNullOrBlank()) "Photo selected ✓" else "Tap to upload passport photo",
                        fontSize = 12.sp,
                        color = if (!photoUri.isNullOrBlank()) StatusActiveGreen else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            employeePhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (!photoUri.isNullOrBlank()) "Change Photo" else "Upload Photo", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Employee Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("full_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { char -> char.isDigit() } },
                label = { Text("Employee Mobile Number *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation / Role") },
                    placeholder = { Text("e.g. Supervisor") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    placeholder = { Text("e.g. Production") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = basicSalary,
                onValueChange = { basicSalary = it.filter { char -> char.isDigit() } },
                label = { Text("Basic Salary / Monthly Wage (₹)") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = NavyPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Official Documents
            Text(
                text = "2. Official Documents (Aadhar, PAN, UIN, Bank)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Aadhar Card
            OutlinedTextField(
                value = aadharNumber,
                onValueChange = { if (it.length <= 12) aadharNumber = it.filter { c -> c.isDigit() } },
                label = { Text("Aadhar Card Number (12 Digits) *") },
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = NavyPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("aadhar_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(6.dp))
            DocumentAttachmentBox(
                title = "Aadhar Card Document Photo",
                uriString = aadharDocUri,
                onPickImage = {
                    aadharDocPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onViewImage = {
                    previewImageTitle = "Aadhar Card Photo"
                    previewImagePath = aadharDocUri
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // PAN Card
            OutlinedTextField(
                value = panNumber,
                onValueChange = { if (it.length <= 10) panNumber = it.uppercase() },
                label = { Text("PAN Card Number") },
                placeholder = { Text("ABCDE1234F") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = NavyPrimary) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(6.dp))
            DocumentAttachmentBox(
                title = "PAN Card Document Photo",
                uriString = panDocUri,
                onPickImage = {
                    panDocPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onViewImage = {
                    previewImageTitle = "PAN Card Photo"
                    previewImagePath = panDocUri
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // UIN Number (Universal Identification Number / UAN)
            OutlinedTextField(
                value = uinNumber,
                onValueChange = { uinNumber = it },
                label = { Text("UIN Number (Universal Identification / PF UAN)") },
                placeholder = { Text("e.g. 100928172635") },
                leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = NavyPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bank Account Details
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bank Account Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        placeholder = { Text("e.g. State Bank of India") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bankAccountNumber,
                        onValueChange = { bankAccountNumber = it.filter { c -> c.isDigit() } },
                        label = { Text("Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bankIfsc,
                            onValueChange = { bankIfsc = it.uppercase() },
                            label = { Text("IFSC Code") },
                            placeholder = { Text("SBIN0001234") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bankHolderName,
                            onValueChange = { bankHolderName = it },
                            label = { Text("A/C Holder Name") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Nominee Details
            Text(
                text = "3. Employee Nominee Details",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nomineeName,
                onValueChange = { nomineeName = it },
                label = { Text("Nominee Full Name") },
                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = NavyPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nomineeRelation,
                    onValueChange = { nomineeRelation = it },
                    label = { Text("Relationship") },
                    placeholder = { Text("e.g. Spouse / Father") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nomineePhone,
                    onValueChange = { nomineePhone = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominee Mobile") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nomineeAadharNumber,
                onValueChange = { if (it.length <= 12) nomineeAadharNumber = it.filter { c -> c.isDigit() } },
                label = { Text("Nominee Aadhar Card Number") },
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = NavyPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            DocumentAttachmentBox(
                title = "Nominee Aadhar Card Photo",
                uriString = nomineeAadharDocUri,
                onPickImage = {
                    nomineeAadharPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onViewImage = {
                    previewImageTitle = "Nominee Aadhar Photo"
                    previewImagePath = nomineeAadharDocUri
                }
            )

            if (formError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formError!!,
                        color = StatusExpiredRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Register Button
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        formError = "Please enter Employee Full Name"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        formError = "Please enter Employee Mobile Number"
                        return@Button
                    }

                    formError = null
                    val employee = (existingEmployee ?: Employee(employeeCode = employeeCode, fullName = fullName, phone = phone)).copy(
                        employeeCode = employeeCode.ifBlank { "EMP-${Random.nextInt(10000, 99999)}" },
                        fullName = fullName.trim(),
                        phone = phone.trim(),
                        designation = designation.trim(),
                        department = department.trim(),
                        basicSalary = basicSalary.toDoubleOrNull() ?: 0.0,
                        photoUri = photoUri,
                        aadharNumber = aadharNumber.trim(),
                        aadharDocUri = aadharDocUri,
                        panNumber = panNumber.trim(),
                        panDocUri = panDocUri,
                        uinNumber = uinNumber.trim(),
                        bankName = bankName.trim(),
                        bankAccountNumber = bankAccountNumber.trim(),
                        bankIfsc = bankIfsc.trim(),
                        bankHolderName = bankHolderName.trim(),
                        nomineeName = nomineeName.trim(),
                        nomineeRelation = nomineeRelation.trim(),
                        nomineePhone = nomineePhone.trim(),
                        nomineeAadharNumber = nomineeAadharNumber.trim(),
                        nomineeAadharDocUri = nomineeAadharDocUri
                    )

                    if (existingEmployee == null) {
                        viewModel.registerEmployee(employee) {
                            onNavigateBack()
                        }
                    } else {
                        viewModel.updateEmployee(employee) {
                            onNavigateBack()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_employee_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = GoldLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingEmployee == null) "Register Employee & Activate 60-Day Code" else "Update Employee Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Full screen Document Preview dialog
    if (previewImagePath != null && previewImageTitle != null) {
        DocPreviewDialog(
            title = previewImageTitle!!,
            imagePath = previewImagePath!!,
            onDismiss = {
                previewImagePath = null
                previewImageTitle = null
            }
        )
    }
}
