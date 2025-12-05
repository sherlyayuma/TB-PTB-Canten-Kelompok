package com.example.cantin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilkuScreen(cartViewModel: CartViewModel, onBackClick: () -> Unit, onEditClick: () -> Unit) {
    val userProfile by cartViewModel.userProfile.collectAsState()

    Scaffold(
        topBar = { ProfilkuAppBar(onBackClick = onBackClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item { ProfileHeader(name = userProfile.name, email = userProfile.email, onEditClick = onEditClick) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { Text("Akun", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }
            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
            itemsIndexed(ACCOUNT_OPTIONS) { index, option ->
                OptionRow(text = option, onClick = { /* Handle click for $option */ })
                if (index < ACCOUNT_OPTIONS.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 16.dp))
                }
            }
            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilkuAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Profilku", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White) } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryOrangeColor)
    )
}

@Composable
fun ProfileHeader(name: String, email: String, onEditClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Default.Person, "Avatar", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(8.dp).fillMaxSize())
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit Profil", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun OptionRow(text: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = text, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val ACCOUNT_OPTIONS = listOf(
    "Aktivitasku", "Favoritku", "Metode Pembayaran",
    "Pusat Bantuan", "Keamanan Akun", "Beri Rating"
)
