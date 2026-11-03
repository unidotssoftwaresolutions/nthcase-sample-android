package com.nthcase.sample.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppEvent
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.ThemeMode
import com.nthcase.sample.ui.screens.CartScreen
import com.nthcase.sample.ui.screens.FormScreen
import com.nthcase.sample.ui.screens.GestureScreen
import com.nthcase.sample.ui.screens.GiftCardsScreen
import com.nthcase.sample.ui.screens.ItemScreen
import com.nthcase.sample.ui.screens.LabScreen
import com.nthcase.sample.ui.screens.OrdersScreen
import com.nthcase.sample.ui.screens.SettingsScreen
import com.nthcase.sample.ui.screens.ShopScreen
import com.nthcase.sample.ui.screens.SignInScreen
import com.nthcase.sample.ui.screens.SlowScreen
import com.nthcase.sample.ui.screens.WebViewScreen
import com.nthcase.sample.ui.theme.MossgroveTheme
import kotlinx.coroutines.launch

object Routes {
    const val SIGN_IN = "signin"
    const val SHOP = "shop"
    const val CART = "cart"
    const val LAB = "lab"
    const val SETTINGS = "settings"
    const val ITEM = "item/{id}"
    const val FORM = "form"
    const val GESTURES = "gestures"
    const val WEBVIEW = "webview"
    const val SLOW = "slow"
    const val ORDERS = "orders"
    const val GIFT_CARDS = "giftcards"

    fun item(id: String) = "item/$id"

    val tabs = setOf(SHOP, CART, LAB, SETTINGS)
}

/** Shows a snackbar from anywhere: a message, and an optional action. */
fun interface Snack {
    fun show(message: String, actionLabel: String?, onAction: (() -> Unit)?)
}

fun Snack.show(message: String) = show(message, null, null)

val LocalSnack = staticCompositionLocalOf<Snack> { Snack { _, _, _ -> } }

@Composable
fun MossgroveApp() {
    val themeMode = AppState.themeMode
    LaunchedEffect(themeMode) {
        val night = when (themeMode) {
            ThemeMode.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.Dark -> AppCompatDelegate.MODE_NIGHT_YES
        }
        if (AppCompatDelegate.getDefaultNightMode() != night) AppCompatDelegate.setDefaultNightMode(night)
    }
    MossgroveTheme {
        val nav = rememberNavController()
        val snackbarHost = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val snack = remember {
            Snack { message, actionLabel, onAction ->
                scope.launch {
                    snackbarHost.currentSnackbarData?.dismiss()
                    // Long (10 s), so a test step after the action still finds it on screen.
                    val result = snackbarHost.showSnackbar(message, actionLabel, duration = SnackbarDuration.Long)
                    if (result == SnackbarResult.ActionPerformed) onAction?.invoke()
                }
            }
        }

        LaunchedEffect(Unit) {
            AppState.events.collect { event ->
                when (event) {
                    AppEvent.OpenOrders -> nav.navigate(Routes.ORDERS)
                    AppEvent.SignedOut -> nav.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                }
            }
        }

        // A deep link opens its item as soon as someone is signed in: at once, or right after the sign-in screen.
        val pending = AppState.pendingItemId
        val signedIn = AppState.signedIn
        LaunchedEffect(pending, signedIn) {
            if (pending != null && signedIn) {
                AppState.pendingItemId = null
                nav.navigate(Routes.item(pending)) { launchSingleTop = true }
            }
        }

        val backStack by nav.currentBackStackEntryAsState()
        val route = backStack?.destination?.route

        CompositionLocalProvider(LocalSnack provides snack) {
            Scaffold(
                modifier = Modifier.testIdRoot(),
                contentWindowInsets = WindowInsets(0),
                bottomBar = { if (route in Routes.tabs) BottomBar(nav, route) },
                snackbarHost = {
                    SnackbarHost(snackbarHost) { data ->
                        Snackbar(
                            modifier = Modifier.testTag(TestIds.SNACKBAR),
                            action = data.visuals.actionLabel?.let { label ->
                                {
                                    TextButton(
                                        onClick = { data.performAction() },
                                        modifier = Modifier.testTag(TestIds.SNACKBAR_ACTION),
                                    ) { Text(label, color = MaterialTheme.colorScheme.inversePrimary) }
                                }
                            },
                        ) { Text(data.visuals.message, modifier = Modifier.testTag(TestIds.SNACKBAR_TEXT)) }
                    }
                },
            ) { padding ->
                NavHost(
                    navController = nav,
                    startDestination = if (AppState.signedIn) Routes.SHOP else Routes.SIGN_IN,
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding),
                ) {
                    composable(Routes.SIGN_IN) {
                        SignInScreen(onSignedIn = {
                            nav.navigate(Routes.SHOP) { popUpTo(Routes.SIGN_IN) { inclusive = true } }
                        })
                    }
                    composable(Routes.SHOP) { ShopScreen(onOpenItem = { nav.navigate(Routes.item(it.toString())) }) }
                    composable(
                        Routes.ITEM,
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        ItemScreen(
                            rawId = entry.arguments?.getString("id").orEmpty(),
                            onBack = { nav.upOrShop() },
                            onViewCart = { nav.switchTab(Routes.CART) },
                        )
                    }
                    composable(Routes.CART) {
                        CartScreen(
                            onBrowse = { nav.switchTab(Routes.SHOP) },
                            onViewOrders = { nav.navigate(Routes.ORDERS) },
                        )
                    }
                    composable(Routes.LAB) { LabScreen(onOpen = { nav.navigate(it) }) }
                    composable(Routes.SETTINGS) { SettingsScreen() }
                    composable(Routes.FORM) { FormScreen(onBack = { nav.upOrShop() }) }
                    composable(Routes.GESTURES) { GestureScreen(onBack = { nav.upOrShop() }) }
                    composable(Routes.WEBVIEW) { WebViewScreen(onBack = { nav.upOrShop() }) }
                    composable(Routes.SLOW) { SlowScreen(onBack = { nav.upOrShop() }) }
                    composable(Routes.ORDERS) {
                        OrdersScreen(onBack = { nav.upOrShop() }, onShop = { nav.switchTab(Routes.SHOP) })
                    }
                    composable(Routes.GIFT_CARDS) { GiftCardsScreen(onBack = { nav.upOrShop() }) }
                }
            }
        }
    }
}

private fun NavHostController.upOrShop() {
    if (!popBackStack()) navigate(Routes.SHOP)
}

/**
 * A tab always opens at its first screen (SPEC.md §Navigation): no per-tab history is kept or restored, so a scenario
 * that taps a tab lands on the same screen in every framework.
 */
private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(Routes.SHOP)
        launchSingleTop = true
    }
}

@Composable
private fun BottomBar(nav: NavHostController, route: String?) {
    val count = AppState.cart.itemCount
    NavigationBar {
        Tab(nav, route, Routes.SHOP, "Shop", Icons.Shop, TestIds.NAV_SHOP)
        NavigationBarItem(
            selected = route == Routes.CART,
            onClick = { nav.switchTab(Routes.CART) },
            icon = {
                BadgedBox(badge = {
                    if (count > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.onTertiary) {
                            Text(count.toString())
                        }
                    }
                }) { Icon(Icons.Cart, contentDescription = null) }
            },
            label = { Text("Cart") },
            // Material clears a tab icon's semantics, badge included, so the count is said by the tab itself
            modifier = Modifier
                .testTag(TestIds.NAV_CART)
                .semantics { contentDescription = if (count > 0) "Cart, $count ${if (count == 1) "item" else "items"}" else "Cart" },
        )
        Tab(nav, route, Routes.LAB, "Lab", Icons.Lab, TestIds.NAV_LAB)
        Tab(nav, route, Routes.SETTINGS, "Settings", Icons.Settings, TestIds.NAV_SETTINGS)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Tab(
    nav: NavHostController,
    current: String?,
    route: String,
    label: String,
    icon: ImageVector,
    id: String,
) {
    NavigationBarItem(
        selected = current == route,
        onClick = { nav.switchTab(route) },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        modifier = Modifier.testTag(id),
    )
}
