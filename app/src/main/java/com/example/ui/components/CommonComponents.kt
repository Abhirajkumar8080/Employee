package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CodeStatus
import com.example.data.model.Employee
import com.example.ui.theme.*
import com.example.util.UpiHelper
import java.io.File

@Composable
fun StatusBadge(
    status: CodeStatus,
    remainingDays: Long,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text, icon) = when (status) {
        CodeStatus.ACTIVE -> Quadruple(
            Color(0xFFE8F5E9),
            StatusActiveGreen,
            "ACTIVE (${remainingDays}d left)",
            Icons.Default.CheckCircle
        )
        CodeStatus.EXPIRING_SOON -> Quadruple(
            Color(0xFFFFF8E1),
            StatusWarningAmber,
            "EXPIRING (${remainingDays}d left)",
            Icons.Default.Warning
        )
        CodeStatus.BAND_EXPIRED -> Quadruple(
            Color(0xFFFFEBEE),
            StatusExpiredRed,
            "CODE BAND (EXPIRED)",
            Icons.Default.Cancel
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun CodeDisplayPill(
    code: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyPrimary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
        modifier = modifier.clickable { onCopy() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "Code",
                tint = NavyPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy Code",
                tint = NavyPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun ExpiryTrackerCard(
    employee: Employee,
    onRenewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = employee.getCodeStatus()
    val remainingDays = employee.getRemainingDays()
    val progress = (remainingDays.toFloat() / 60f).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                CodeStatus.BAND_EXPIRED -> Color(0xFFFFF1F2)
                CodeStatus.EXPIRING_SOON -> Color(0xFFFFFBEB)
                CodeStatus.ACTIVE -> Color(0xFFF0FDF4)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            when (status) {
                CodeStatus.BAND_EXPIRED -> StatusExpiredRed
                CodeStatus.EXPIRING_SOON -> StatusWarningAmber
                CodeStatus.ACTIVE -> StatusActiveGreen.copy(alpha = 0.5f)
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (status == CodeStatus.BAND_EXPIRED) Icons.Default.Lock else Icons.Default.Timer,
                        contentDescription = null,
                        tint = when (status) {
                            CodeStatus.BAND_EXPIRED -> StatusExpiredRed
                            CodeStatus.EXPIRING_SOON -> StatusWarningAmber
                            CodeStatus.ACTIVE -> StatusActiveGreen
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "60-Day Payment Code Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
                StatusBadge(status = status, remainingDays = remainingDays)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when (status) {
                    CodeStatus.BAND_EXPIRED -> StatusExpiredRed
                    CodeStatus.EXPIRING_SOON -> StatusWarningAmber
                    CodeStatus.ACTIVE -> StatusActiveGreen
                },
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (status == CodeStatus.BAND_EXPIRED) {
                        "Payment overdue > 60 days. Code locked!"
                    } else {
                        "$remainingDays days remaining until expiry"
                    },
                    fontSize = 12.sp,
                    color = if (status == CodeStatus.BAND_EXPIRED) StatusExpiredRed else Color(0xFF64748B),
                    fontWeight = if (status == CodeStatus.BAND_EXPIRED) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = "Valid till: ${UpiHelper.formatDateShort(employee.validUntilDate)}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (employee.lastPaymentDate != null) {
                Text(
                    text = "Last payment logged: ${UpiHelper.formatDate(employee.lastPaymentDate)}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Expiry / Renew Action Callout
            if (status == CodeStatus.BAND_EXPIRED || status == CodeStatus.EXPIRING_SOON) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRenewClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == CodeStatus.BAND_EXPIRED) StatusExpiredRed else StatusWarningAmber
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("renew_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (status == CodeStatus.BAND_EXPIRED) {
                            "Code Band Hai - Pay UPI to Owner & Chalu Karein"
                        } else {
                            "Renew Code Now (Owner UPI: 9798093650)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerUpiCard(
    onCopyUpi: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = NavyDark
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GoldAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = NavyDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "App Owner Official UPI",
                        fontSize = 11.sp,
                        color = GoldLight
                    )
                    Text(
                        text = UpiHelper.OWNER_UPI_NUMBER,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            FilledTonalButton(
                onClick = onCopyUpi,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GoldAccent,
                    contentColor = NavyDark
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DocumentAttachmentBox(
    title: String,
    uriString: String?,
    onPickImage: () -> Unit,
    onViewImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (!uriString.isNullOrBlank()) {
                    AsyncImage(
                        model = File(uriString),
                        contentDescription = title,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                            .clickable { onViewImage() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (!uriString.isNullOrBlank()) "Document Attached ✓" else "No document attached",
                        fontSize = 11.sp,
                        color = if (!uriString.isNullOrBlank()) StatusActiveGreen else Color(0xFF94A3B8)
                    )
                }
            }

            Row {
                if (!uriString.isNullOrBlank()) {
                    IconButton(
                        onClick = onViewImage,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View",
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onPickImage,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (!uriString.isNullOrBlank()) Icons.Default.Edit else Icons.Default.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!uriString.isNullOrBlank()) "Change" else "Attach",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
