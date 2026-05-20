package com.longanizacontrol.mx.util

import java.util.Locale

fun formatKg(value: Double): String = String.format(Locale.US, "%.2f kg", value)
fun formatCurrency(value: Double): String = String.format(Locale.US, "$%.2f MXN", value)
fun toDoubleOrZero(value: String): Double = value.toDoubleOrNull() ?: 0.0
