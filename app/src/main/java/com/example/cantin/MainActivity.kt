package com.example.cantin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cantin.ui.theme.CantinTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- Data Classes ---
data class MenuItem(
    val id: Int,
    val name: String,
    val price: Int,
    val imageResId: Int,
    var isFavorite: Boolean = false
)

data class CartItem(val menuItem: MenuItem, var quantity: Int)

data class UserProfile(
    val name: String,
    val phoneNumber: String,
    val email: String
)

// --- ViewModel ---
class CartViewModel : ViewModel() {
    private val _menuItems = MutableStateFlow(DUMMY_MENU_ITEMS)
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile(name = "Rose", phoneNumber = "+62-895-619-768-687", email = "carolina123@gmail.com"))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun updateUserProfile(newName: String, newPhoneNumber: String) {
        _userProfile.update { it.copy(name = newName, phoneNumber = newPhoneNumber) }
    }

    fun toggleFavorite(itemId: Int) {
        _menuItems.update { currentItems ->
            currentItems.map {
                if (it.id == itemId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }

    fun getMenuItem(itemId: Int): MenuItem? {
        return _menuItems.value.find { it.id == itemId }
    }

    fun addToCart(item: MenuItem, quantity: Int = 1) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.menuItem.id == item.id }
            if (existingItem != null) {
                existingItem.quantity += quantity
                currentCart
            } else {
                currentCart + CartItem(item, quantity)
            }
        }
    }

    fun increaseQuantity(itemId: Int) {
        _cartItems.update { currentCart ->
            currentCart.map {
                if (it.menuItem.id == itemId) it.copy(quantity = it.quantity + 1) else it
            }
        }
    }

    fun decreaseQuantity(itemId: Int) {
        _cartItems.update { currentCart ->
            val item = currentCart.find { it.menuItem.id == itemId }
            if (item != null && item.quantity > 1) {
                currentCart.map { if (it.menuItem.id == itemId) it.copy(quantity = it.quantity - 1) else it }
            } else {
                currentCart.filter { it.menuItem.id != itemId }
            }
        }
    }

    val totalItemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }

    val totalPrice: Int
        get() = _cartItems.value.sumOf { it.menuItem.price * it.quantity }
}

val DUMMY_MENU_ITEMS = listOf(
    MenuItem(1, "Nasi Ayam Geprek", 15000, R.drawable.nasi_ayam_geprek),
    MenuItem(2, "Nasi Goreng", 15000, R.drawable.nasi_goreng),
    MenuItem(3, "Nasi Soto Ayam", 10000, R.drawable.nasi_soto_ayam)
)

val PrimaryOrangeColor = Color(0xFFF07E1C)
val CardOrangeColor = Color(0xFFFF9800)

object Destinations {
    const val HOME_ROUTE = "home"
    const val PROFILE_ROUTE = "profile"
    const val FAVORITE_ROUTE = "favorite"
    const val ORDERS_ROUTE = "orders"
    const val EDIT_PROFILE_ROUTE = "edit_profile"
    const val MESSAGE_ROUTE = "message_inbox"
    const val ACTIVITY_ROUTE = "activity"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        setContent {
            CantinTheme {
                CantinApp()
            }
        }
    }
}

@Composable
fun CantinApp() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted: Do something
        } else {
            // Permission Denied: Do something
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        bottomBar = { MyCantenBottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.HOME_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.HOME_ROUTE) {
                HomeScreen(
                    cartViewModel = cartViewModel,
                    onProfileClick = { navController.navigate(Destinations.PROFILE_ROUTE) }
                )
            }
            composable(Destinations.PROFILE_ROUTE) {
                ProfilkuScreen(
                    cartViewModel = cartViewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { navController.navigate(Destinations.EDIT_PROFILE_ROUTE) },
                    onFavoritesClick = { navController.navigate(Destinations.FAVORITE_ROUTE) },
                    onRateProductClick = {},
                    onActivityClick = { navController.navigate(Destinations.ACTIVITY_ROUTE) }
                )
            }
            composable(Destinations.FAVORITE_ROUTE) {
                FavoritScreen(
                    cartViewModel = cartViewModel,
                    navController = navController
                )
            }
            composable(Destinations.ORDERS_ROUTE) {
                KeranjangScreen(
                    cartViewModel = cartViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Destinations.EDIT_PROFILE_ROUTE) {
                UbahProfilScreen(
                    cartViewModel = cartViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Destinations.MESSAGE_ROUTE) { PlaceholderScreen("Halaman Riwayat Pesan") }
            composable(Destinations.ACTIVITY_ROUTE) {
                AktivitaskuScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun MyCantenBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar(
        containerColor = Color.White,
        modifier = Modifier.shadow(elevation = 8.dp)
    ) {
        BottomBarIcon(icon = Icons.Default.Home, contentDescription = "Home", isSelected = currentRoute == Destinations.HOME_ROUTE) { navController.navigateTo(Destinations.HOME_ROUTE) }
        BottomBarIcon(icon = Icons.Default.Favorite, contentDescription = "Favorit", isSelected = currentRoute == Destinations.FAVORITE_ROUTE) { navController.navigateTo(Destinations.FAVORITE_ROUTE) }
        BottomBarIcon(icon = Icons.Default.Receipt, contentDescription = "Pesanan", isSelected = currentRoute == Destinations.ORDERS_ROUTE) { navController.navigateTo(Destinations.ORDERS_ROUTE) }
        BottomBarIcon(icon = Icons.Default.Person, contentDescription = "Akun", isSelected = currentRoute == Destinations.PROFILE_ROUTE) { navController.navigateTo(Destinations.PROFILE_ROUTE) }
    }
}

@Composable
fun RowScope.BottomBarIcon(icon: ImageVector, contentDescription: String, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.weight(1f)) {
        Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(28.dp), tint = if (isSelected) PrimaryOrangeColor else Color.Gray)
    }
}

fun NavController.navigateTo(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    CantinTheme {
        CantinApp()
    }
}

@Composable
fun HomeScreen(cartViewModel: CartViewModel, onProfileClick: () -> Unit) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val menuItems by cartViewModel.menuItems.collectAsState()
    val userProfile by cartViewModel.userProfile.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = if (cartItems.isNotEmpty()) 80.dp else 0.dp)) {
            item { HeaderHome(userProfile.name, onProfileClick) }
            item { SearchBar() }
            item { KategoriChips() }

            items(menuItems, key = { it.id }) { item ->
                val cartItem = cartItems.find { it.menuItem.id == item.id }
                MenuItemCard(
                    item = item,
                    quantity = cartItem?.quantity ?: 0,
                    isFavorite = item.isFavorite,
                    onAddToCart = { cartViewModel.addToCart(item) },
                    onIncrease = { cartViewModel.increaseQuantity(item.id) },
                    onDecrease = { cartViewModel.decreaseQuantity(item.id) },
                    onToggleFavorite = {
                        cartViewModel.toggleFavorite(item.id)
                        if (!item.isFavorite) {
                            NotificationHelper.showFavoriteNotification(context)
                        }
                    }
                )
            }
        }
        if (cartItems.isNotEmpty()) {
            CartBanner(modifier = Modifier.align(Alignment.BottomCenter), count = cartViewModel.totalItemCount, price = cartViewModel.totalPrice)
        }
    }
}

@Composable
fun CartBanner(modifier: Modifier = Modifier, count: Int, price: Int) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardOrangeColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("$count Item", color = Color.White, fontWeight = FontWeight.Bold)
                Text("tersimpan dikeranjang", color = Color.White, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Rp $price", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ShoppingBasket, contentDescription = "Keranjang", tint = Color.White)
            }
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    isFavorite: Boolean,
    onAddToCart: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = item.imageResId), contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold)
                Text("Rp ${item.price}", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavorite) Color.Red else Color.Gray)
            }
            if (quantity == 0) {
                Button(onClick = onAddToCart, colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrangeColor)) {
                    Text("Tambah")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(PrimaryOrangeColor, RoundedCornerShape(4.dp))) {
                    IconButton(onClick = onDecrease) { Text("-", color = Color.White, fontWeight = FontWeight.Bold) }
                    Text("$quantity", color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onIncrease) { Text("+", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeranjangScreen(cartViewModel: CartViewModel, onBackClick: () -> Unit) {
    val cartItems by cartViewModel.cartItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryOrangeColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Keranjangmu masih kosong")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems, key = { it.menuItem.id }) { cartItem ->
                        CartListItem(
                            item = cartItem.menuItem,
                            quantity = cartItem.quantity,
                            onIncrease = { cartViewModel.increaseQuantity(cartItem.menuItem.id) },
                            onDecrease = { cartViewModel.decreaseQuantity(cartItem.menuItem.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CheckoutSection(totalPrice = cartViewModel.totalPrice)
            }
        }
    }
}

@Composable
fun CartListItem(
    item: MenuItem,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = item.imageResId),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold)
            Text("Rp ${item.price}", color = Color.Gray, fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text("$quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun CheckoutSection(totalPrice: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardOrangeColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total: Rp $totalPrice", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(onClick = { /* TODO: Handle checkout */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                Text("Checkout", color = PrimaryOrangeColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritScreen(cartViewModel: CartViewModel, navController: NavController) {
    val menuItems by cartViewModel.menuItems.collectAsState()
    val favoriteItems = menuItems.filter { it.isFavorite }
    val cartItems by cartViewModel.cartItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryOrangeColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (favoriteItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada item favorit")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(favoriteItems, key = { it.id }) { item ->
                        val cartItem = cartItems.find { it.menuItem.id == item.id }
                        MenuItemCard(
                            item = item,
                            quantity = cartItem?.quantity ?: 0,
                            isFavorite = item.isFavorite,
                            onAddToCart = { cartViewModel.addToCart(item) },
                            onIncrease = { cartViewModel.increaseQuantity(item.id) },
                            onDecrease = { cartViewModel.decreaseQuantity(item.id) },
                            onToggleFavorite = { cartViewModel.toggleFavorite(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UbahProfilScreen(cartViewModel: CartViewModel, onBackClick: () -> Unit) {
    // Placeholder for now
    PlaceholderScreen(text = "Ubah Profil")
}


@Composable
fun HeaderHome(name: String, onProfileClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(PrimaryOrangeColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Halo, $name", color = Color.White, fontSize = 18.sp)
                Text("Mau pesan apa hari ini?", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onProfileClick)
            ) {}
        }
        Spacer(modifier = Modifier.weight(1f))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = 40.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CardOrangeColor)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Image(painter = painterResource(id = R.drawable.logo_mycanten), "MyCanten Logo", modifier = Modifier.height(60.dp), contentScale = ContentScale.Fit)
                Text("MyCanten", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Pesan Mudah, Kenyang Cepat", fontSize = 14.sp, color = Color.White)
            }
        }
    }
    Spacer(modifier = Modifier.height(80.dp))
}

@Composable
fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, "Cari", tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Makanan, Minuman, dan Jajanan", color = Color.Gray)
    }
}

@Composable
fun KategoriChips() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = PrimaryOrangeColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
        ) { Text("Makanan", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), textAlign = TextAlign.Center) }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
        ) { Text("Minuman", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), textAlign = TextAlign.Center) }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
        ) { Text("Jajanan", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), textAlign = TextAlign.Center) }
    }
}
