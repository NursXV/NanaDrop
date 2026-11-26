package com.example.nanadrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nanadrop.data.ProductRepository
import com.example.nanadrop.ui.components.SettingsBottomSheet
import com.example.nanadrop.ui.screen.CartScreen
import com.example.nanadrop.ui.screen.CatalogScreen
import com.example.nanadrop.ui.screen.DetailsScreen
import com.example.nanadrop.ui.screen.DealsScreen
import com.example.nanadrop.ui.screen.ListingDetailsScreen
import com.example.nanadrop.ui.screen.MarketplaceScreen
import com.example.nanadrop.ui.theme.NanaDropTheme
import com.example.nanadrop.ui.viewmodel.DealsViewModel
import com.example.nanadrop.ui.viewmodel.MarketplaceViewModel
import com.example.nanadrop.ui.viewmodel.StoreViewModel


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val storeViewModel: StoreViewModel by viewModels()
    private val marketplaceViewModel: MarketplaceViewModel by viewModels {
        MarketplaceViewModel.Factory(application)
    }
    private val dealsViewModel: DealsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by marketplaceViewModel.isDarkMode.collectAsState()
            var showSettingsSheet by remember { mutableStateOf(false) }

            NanaDropTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentRoute) {
                                        "marketplace" -> "NanaDrop Marketplace"
                                        "deals" -> "Special Game Deals"
                                        "cart" -> "Shopping Cart"
                                        "catalog" -> "Digital Product Catalog"
                                        "listing_details/{listingId}" -> "Listing Details"
                                        else -> "NanaDrop"
                                    }
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { showSettingsSheet = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Settings Menu"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (currentRoute == "marketplace" || currentRoute == "deals" || currentRoute == "cart") {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Marketplace") },
                                    label = { Text("Marketplace") },
                                    selected = currentRoute == "marketplace",
                                    onClick = {
                                        if (currentRoute != "marketplace") {
                                            navController.navigate("marketplace")
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LocalOffer, contentDescription = "Deals") },
                                    label = { Text("Deals") },
                                    selected = currentRoute == "deals",
                                    onClick = {
                                        if (currentRoute != "deals") {
                                            navController.navigate("deals")
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                                    label = { Text("Cart") },
                                    selected = currentRoute == "cart",
                                    onClick = {
                                        if (currentRoute != "cart") {
                                            navController.navigate("cart")
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "marketplace",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("catalog") {
                            CatalogScreen(
                                viewModel = storeViewModel,
                                onProductClick = { productId ->
                                    navController.navigate("details/$productId")
                                }
                            )
                        }
                        composable("marketplace") {
                            MarketplaceScreen(
                                viewModel = marketplaceViewModel,
                                storeViewModel = storeViewModel,
                                onNavigateToCart = { navController.navigate("cart") },
                                onListingClick = { listingId ->
                                    navController.navigate("listing_details/$listingId")
                                }
                            )
                        }
                        composable("deals") {
                            DealsScreen(viewModel = dealsViewModel)
                        }
                        composable(
                            route = "details/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId")
                            val product = ProductRepository.products.find { it.id == productId }
                            DetailsScreen(
                                product = product,
                                onBackClick = { navController.popBackStack() },
                                onAddToCart = { prod ->
                                    storeViewModel.addToCart(prod)
                                }
                            )
                        }
                        composable(
                            route = "listing_details/{listingId}",
                            arguments = listOf(navArgument("listingId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
                            ListingDetailsScreen(
                                listingId = listingId,
                                marketplaceViewModel = marketplaceViewModel,
                                storeViewModel = storeViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("cart") {
                            CartScreen(
                                viewModel = storeViewModel,
                                marketplaceViewModel = marketplaceViewModel,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToOrders = { navController.navigate("orders") }
                            )
                        }
                    }

                    if (showSettingsSheet) {
                        SettingsBottomSheet(
                            viewModel = marketplaceViewModel,
                            onDismissRequest = { showSettingsSheet = false }
                        )
                    }
                }
            }
        }
    }
}
