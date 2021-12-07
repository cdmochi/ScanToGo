package com.example.scantogo.enumeration

import com.google.mlkit.vision.barcode.Barcode

enum class QRType(val type: Int, val typeName: String = "") {
    EMAIL(Barcode.TYPE_EMAIL, "EMAIL"),
    PHONE(Barcode.TYPE_PHONE, "PHONE"),
    URI(Barcode.TYPE_URL, "URL"),
    CONTACT_INFO(Barcode.TYPE_CONTACT_INFO, "CONTACT_INFO"),
    TEXT(Barcode.TYPE_TEXT, "TEXT"),
    SMS(Barcode.TYPE_SMS, "SMS")
}