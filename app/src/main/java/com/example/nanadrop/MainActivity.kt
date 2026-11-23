package com.example.nanadrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nanadrop.data.ProductRepository
import com.example.nanadrop.ui.screen.CartScreen
import com.example.nanadrop.ui.screen.CatalogScreen
import com.example.nanadrop.ui.screen.DetailsScreen
import com.example.nanadrop.ui.screen.DealsScreen
import com.example.nanadrop.ui.screen.MarketplaceScreen
import com.example.nanadrop.ui.theme.NanaDropTheme
import com.example.nanadrop.ui.viewmodel.DealsViewModel
import com.example.nanadrop.ui.viewmodel.MarketplaceViewModel
import com.example.nanadrop.ui.viewmodel.StoreViewModel


class MainActivity : ComponentActivity() {
    private val storeViewModel: StoreViewModel by viewModels()
    private val marketplaceViewModel: MarketplaceViewModel by viewModels {
        MarketplaceViewModel.Factory(application)
    }
    private val dealsViewModel: DealsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NanaDropTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute == "catalog" || currentRoute == "marketplace" || currentRoute == "deals" || currentRoute == "cart") {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Catalog") },
                                    label = { Text("Catalog") },
                                    selected = currentRoute == "catalog",
                                    onClick = {
                                        if (currentRoute != "catalog") {
                                            navController.navigate("catalog") {
                                                popUpTo("catalog") { inclusive = true }
                                            }
                                        }
                                    }
                                )
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
                        startDestination = "catalog",
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
                            MarketplaceScreen(viewModel = marketplaceViewModel)
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
                        composable("cart") {
                            CartScreen(
                                viewModel = storeViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}