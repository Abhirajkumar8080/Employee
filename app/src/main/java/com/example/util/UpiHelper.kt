package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UpiHelper {
    const val OWNER_UPI_NUMBER = "9798093650"
    const val OWNER_NAME = "App Owner"
    const val DEFAULT_RENEWAL_FEE = 149.0

    /**
     * Builds the standard NPCI UPI payment URI.
     */
    fun buildUpiUri(
        upiId: String = OWNER_UPI_NUMBER,
        name: String = OWNER_NAME,
        amount: Double = DEFAULT_RENEWAL_FEE,
        transactionNote: String
    ): Uri {
        // Many UPI apps accept pure phone numbers or virtual payment addresses.
        val pa = if (upiId.contains("@")) upiId else "$upiId@upi"
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val uriString = "upi://pay?pa=$pa&pn=${Uri.encode(name)}&am=$formattedAmount&cu=INR&tn=${Uri.encode(transactionNote)}"
        return Uri.parse(uriString)
    }

    /**
     * Attempts to open an installed UPI app (Google Pay, PhonePe, Paytm, BHIM, etc.).
     */
    fun launchUpiPayment(
        context: Context,
        upiId: String = OWNER_UPI_NUMBER,
        name: String = OWNER_NAME,
        amount: Double = DEFAULT_RENEWAL_FEE,
        transactionNote: String
    ): Boolean {
        val upiUri = buildUpiUri(upiId, name, amount, transactionNote)
        val intent = Intent(Intent.ACTION_VIEW, upiUri)
        val chooser = Intent.createChooser(intent, "Pay via UPI App")

        return try {
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No UPI application found. Please copy the UPI ID $OWNER_UPI_NUMBER to pay manually.",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    /**
     * Copies text to Android clipboard and displays confirmation toast.
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied: $text", Toast.LENGTH_SHORT).show()
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return format.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
