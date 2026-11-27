package com.example.nanadrop.ui.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Shared US-dollar formatting so every screen renders money identically, e.g. `$1,250.00`.
 *
 * [NumberFormat] instances are not thread safe, hence the small synchronisation guard.
 */
private val usdCurrencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

/** Formats this amount as a grouped, 2-decimal US dollar string, e.g. `$1,250.00`. */
fun Double.asCurrency(): String = synchronized(usdCurrencyFormat) { usdCurrencyFormat.format(this) }
