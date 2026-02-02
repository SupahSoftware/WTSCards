package com.wtscards.data.model

data class Order(
    val id: String,
    val name: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val shippingType: String? = null,
    val shippingCost: Long = 0,
    val status: String = "New",
    val createdAt: Long,
    val trackingNumber: String? = null,
    val discount: Int = 0,
    val cards: List<Card> = emptyList(),
    val length: Double = 0.0,
    val width: Double = 0.0,
    val height: Double = 0.0,
    val pounds: Int = 0,
    val ounces: Int = 0,
    val totalOverride: Long? = null
)

object OrderStatus {
    const val NEW = "New"
    const val LABEL_CREATED = "Label created"
    const val SHIPPED = "Shipped"

    val allStatuses = listOf(NEW, LABEL_CREATED, SHIPPED)
}
