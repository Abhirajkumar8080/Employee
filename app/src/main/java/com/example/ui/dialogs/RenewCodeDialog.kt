package com.example.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Employee
import com.example.ui.theme.*
import com.example.util.UpiHelper

@Composable
fun RenewCodeDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onConfirmRenewal: (amount: Double, utr: String) -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("149") }
    var utrText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = GoldDark,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Reactivate Employee Code",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Code: ${employee.employeeCode} (${employee.fullName})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldDark,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule Info Banner
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "60 Day payment na hone par code band ho jata hai. Chalu karvane ke liye app owner ke UPI par pay karein. Code automatic 60 din ke liye update ho jayega!",
                            fontSize = 11.5.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Owner UPI Box
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "APP OWNER UPI ID",
                            fontSize = 11.sp,
                            color = GoldLight,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = UpiHelper.OWNER_UPI_NUMBER,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    UpiHelper.copyToClipboard(
                                        context,
                                        "Owner UPI ID",
                                        UpiHelper.OWNER_UPI_NUMBER
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = NavyDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy UPI", fontSize = 12.sp, color = NavyDark, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val amt = amountText.toDoubleOrNull() ?: 149.0
                                    UpiHelper.launchUpiPayment(
                                        context = context,
                                        upiId = UpiHelper.OWNER_UPI_NUMBER,
                                        name = "Employee Register App Owner",
                                        amount = amt,
                                        transactionNote = "Reactivate Code ${employee.employeeCode}"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusActiveGreen),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open UPI App", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stylized Simulated QR Code Representation for instant scan
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(110.dp)
                                .background(Color.White)
                        ) {
                            val cellSize = size.width / 9f
                            // Outer corners
                            val dark = Color(0xFF0F172A)
                            // Top left corner finder
                            drawRect(dark, Offset(0f, 0f), Size(cellSize * 3, cellSize * 3))
                            drawRect(Color.White, Offset(cellSize * 0.7f, cellSize * 0.7f), Size(cellSize * 1.6f, cellSize * 1.6f))
                            drawRect(dark, Offset(cellSize * 1.1f, cellSize * 1.1f), Size(cellSize * 0.8f, cellSize * 0.8f))

                            // Top right corner finder
                            drawRect(dark, Offset(cellSize * 6, 0f), Size(cellSize * 3, cellSize * 3))
                            drawRect(Color.White, Offset(cellSize * 6.7f, cellSize * 0.7f), Size(cellSize * 1.6f, cellSize * 1.6f))
                            drawRect(dark, Offset(cellSize * 7.1f, cellSize * 1.1f), Size(cellSize * 0.8f, cellSize * 0.8f))

                            // Bottom left corner finder
                            drawRect(dark, Offset(0f, cellSize * 6), Size(cellSize * 3, cellSize * 3))
                            drawRect(Color.White, Offset(cellSize * 0.7f, cellSize * 6.7f), Size(cellSize * 1.6f, cellSize * 1.6f))
                            drawRect(dark, Offset(cellSize * 1.1f, cellSize * 7.1f), Size(cellSize * 0.8f, cellSize * 0.8f))

                            // Data blocks
                            drawRect(dark, Offset(cellSize * 4, cellSize * 1), Size(cellSize, cellSize))
                            drawRect(dark, Offset(cellSize * 3, cellSize * 4), Size(cellSize * 3, cellSize))
                            drawRect(dark, Offset(cellSize * 4, cellSize * 3), Size(cellSize, cellSize * 3))
                            drawRect(dark, Offset(cellSize * 1, cellSize * 4), Size(cellSize, cellSize))
                            drawRect(dark, Offset(cellSize * 7, cellSize * 4), Size(cellSize, cellSize))
                            drawRect(dark, Offset(cellSize * 5, cellSize * 6), Size(cellSize, cellSize))
                            drawRect(dark, Offset(cellSize * 7, cellSize * 7), Size(cellSize, cellSize))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan & Pay: 9798093650",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input fields: Amount and UTR Number
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Reactivation Fee (₹)") },
                    leadingIcon = {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = NavyPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = utrText,
                    onValueChange = { utrText = it.trim() },
                    label = { Text("UPI Ref / UTR Number (Optional)") },
                    placeholder = { Text("e.g. 427182910291") },
                    leadingIcon = {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = NavyPrimary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 149.0
                        isSubmitting = true
                        onConfirmRenewal(amount, utrText)
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_reactivate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GoldLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify & Reactivate Code (60 Days)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        }
    }
}
