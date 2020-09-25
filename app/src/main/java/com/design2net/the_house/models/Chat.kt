package com.design2net.the_house.models

class Chat(
        val order_number: String,
        val sku: String,
        val message: String,
        val sustitutos: String,
        val date_sent: String,
        val from_user: String,
        val is_from_employee: Boolean) {
}