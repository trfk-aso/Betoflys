package org.betofly.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.betofly.app.model.TripCategory

@Composable
fun TripCategoriesRow(
    categories: List<TripCategory>,
    selectedCategory: TripCategory?,
    onCategorySelected: (TripCategory?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (isSelected) null else category
                    onCategorySelected(newSelection)
                },
                label = { Text(category.name.replace("_", " ")) }
            )
        }
    }
}
