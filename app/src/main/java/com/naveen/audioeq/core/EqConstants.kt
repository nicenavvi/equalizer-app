package com.naveen.audioeq.core

/**
 * ఈక్వలైజర్‌కి సంబంధించిన స్థిర విలువలు.
 * యూజర్‌కి అర్థమయ్యేలా ప్రతి బ్యాండ్‌కి ఫ్రీక్వెన్సీ పేరు ఇక్కడ ఉంటుంది.
 */
object EqConstants {
    // 10-బ్యాండ్ ఈక్వలైజర్‌కి స్టాండర్డ్ ఫ్రీక్వెన్సీ లేబుల్స్
    val BAND_LABELS: List<String> = listOf(
        "31Hz", "62Hz", "125Hz", "250Hz", "500Hz",
        "1kHz", "2kHz", "4kHz", "8kHz", "16kHz"
    )
}
