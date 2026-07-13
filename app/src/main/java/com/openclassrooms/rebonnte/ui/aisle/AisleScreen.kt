package com.openclassrooms.rebonnte.ui.aisle

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme

@Composable
fun AisleScreen(viewModel: AisleViewModel) {
    val aisles by viewModel.aisles.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val arrowDescription = stringResource(R.string.aisle_arrow)

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(aisles) { aisle ->
            AisleItem(
                aisle = aisle,
                onClick = { startDetailActivity(context, aisle.name) },
                arrowDescription = arrowDescription
            )
        }
    }
}

@Composable
fun AisleItem(aisle: Aisle, onClick: () -> Unit, arrowDescription: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = arrowDescription) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = aisle.name, style = MaterialTheme.typography.bodyMedium)
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
    }
}

private fun startDetailActivity(context: Context, name: String) {
    val intent = Intent(context, AisleDetailActivity::class.java).apply {
        putExtra("nameAisle", name)
    }
    context.startActivity(intent)
}

@Preview(showBackground = true, name = "AisleScreen — Liste de rayons")
@Composable
fun AisleScreenContentPreview() {
    RebonnteTheme {
        Column {
            AisleItem(aisle = Aisle("Rayon Analgésiques"), onClick = {})
            AisleItem(aisle = Aisle("Rayon Antibiotiques"), onClick = {})
            AisleItem(aisle = Aisle("Rayon Vitamines"), onClick = {})
        }
    }
}