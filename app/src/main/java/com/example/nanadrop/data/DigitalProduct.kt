package com.example.nanadrop.data

data class DigitalProduct(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val description: String,
    val platform: String
)

object ProductRepository {
    val products = listOf(
        DigitalProduct(
            id = "1",
            title = "Minecraft Java & Bedrock Key",
            category = "Keys",
            price = 29.99,
            description = "Official global activation key for both Java and Bedrock editions on PC. Instant delivery.",
            platform = "PC / Microsoft"
        ),
        DigitalProduct(
            id = "2",
            title = "Counter-Strike 2 Prime Status",
            category = "Accounts",
            price = 14.99,
            description = "Clean Steam account with CS2 Prime Status enabled. Full email access included.",
            platform = "Steam"
        ),
        DigitalProduct(
            id = "3",
            title = "Spotify Premium 12 Months",
            category = "Subscriptions",
            price = 24.50,
            description = "Individual subscription upgrade for your personal account. Ad-free music and offline mode.",
            platform = "All Devices"
        ),
        DigitalProduct(
            id = "4",
            title = "Cyberpunk 2077: Phantom Liberty",
            category = "Keys",
            price = 39.99,
            description = "Digital activation code for GOG/Steam. Includes base game and expansion pack.",
            platform = "Steam / GOG"
        ),
        DigitalProduct(
            id = "5",
            title = "ChatGPT Plus Shared Access (1 Mo)",
            category = "Subscriptions",
            price = 9.99,
            description = "30-day access to GPT-4o and advanced reasoning models with high usage limits.",
            platform = "Web / Mobile"
        ),
        DigitalProduct(
            id = "6",
            title = "Roblox 2000 Robux Gift Card",
            category = "Keys",
            price = 25.00,
            description = "Digital voucher code redeemable worldwide for 2000 Robux and exclusive virtual item.",
            platform = "Roblox"
        )
    )

    fun getProductById(id: String): DigitalProduct? {
        return products.find { it.id == id }
    }
}