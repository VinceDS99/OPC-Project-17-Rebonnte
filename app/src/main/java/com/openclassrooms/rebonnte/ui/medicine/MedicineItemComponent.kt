package com.openclassrooms.rebonnte.ui.medicine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.openclassrooms.rebonnte.R

@Composable
fun MedicineItem(medicine: Medicine, onClick: () -> Unit) {
    val arrowDescription = stringResource(R.string.medicine_arrow)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = arrowDescription) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = medicine.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.medicine_stock_label, medicine.stock),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
    }
}