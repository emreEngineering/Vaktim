package com.project.vaktim.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.vaktim.data.model.LocationData
import com.project.vaktim.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelector(
    currentCity: String,
    currentCountry: String,
    currentDistrict: String,
    onLocationChanged: (String, String, String) -> Unit
) {
    var selectedCountry by remember { mutableStateOf(currentCountry) }
    var selectedCity by remember { mutableStateOf(currentCity) }
    var selectedDistrict by remember { mutableStateOf(currentDistrict) }
    var isEditing by remember { mutableStateOf(false) }

    val displayLocation = if (currentDistrict.isNotBlank()) {
        "$currentDistrict, $currentCity, $currentCountry"
    } else {
        "$currentCity, $currentCountry"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                // Ülke Dropdown
                LocationDropdown(
                    label = "Ülke",
                    options = LocationData.getCountries(),
                    selectedOption = selectedCountry,
                    onOptionSelected = { country ->
                        selectedCountry = country
                        selectedCity = ""
                        selectedDistrict = ""
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Şehir Dropdown
                val cities = LocationData.getCities(selectedCountry)
                LocationDropdown(
                    label = "Şehir",
                    options = cities,
                    selectedOption = selectedCity,
                    onOptionSelected = { city ->
                        selectedCity = city
                        selectedDistrict = ""
                    },
                    enabled = selectedCountry.isNotBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // İlçe Dropdown
                val districts = LocationData.getDistricts(selectedCountry, selectedCity)
                LocationDropdown(
                    label = "İlçe (Opsiyonel)",
                    options = districts,
                    selectedOption = selectedDistrict,
                    onOptionSelected = { district ->
                        selectedDistrict = district
                    },
                    enabled = selectedCity.isNotBlank(),
                    allowEmpty = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (selectedCity.isNotBlank() && selectedCountry.isNotBlank()) {
                            isEditing = false
                            onLocationChanged(selectedCity, selectedCountry, selectedDistrict)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedCity.isNotBlank() && selectedCountry.isNotBlank()
                ) {
                    Text("Vakitleri Getir", color = MidnightNavy, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = displayLocation,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Konumu Değiştir",
                            color = GoldMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                selectedCountry = currentCountry
                                selectedCity = currentCity
                                selectedDistrict = currentDistrict
                                isEditing = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    allowEmpty: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredOptions = remember(options, searchQuery) {
        if (searchQuery.isBlank()) options
        else options.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = if (expanded) searchQuery else selectedOption.ifBlank { if (allowEmpty) "Seçiniz (Opsiyonel)" else "Seçiniz" },
            onValueChange = { searchQuery = it },
            label = { Text(label, color = GoldMuted) },
            readOnly = !expanded,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = if (selectedOption.isBlank()) TextMuted else TextWhite,
                cursorColor = GoldPrimary,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = GoldMuted.copy(alpha = 0.5f),
                disabledTextColor = TextMuted.copy(alpha = 0.5f),
                disabledBorderColor = GoldMuted.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = enabled
                ),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchQuery = ""
            },
            modifier = Modifier
                .background(NavyCard)
                .heightIn(max = 250.dp)
        ) {
            if (allowEmpty) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "— Seçim yok —",
                            color = TextMuted,
                            fontWeight = FontWeight.Light
                        )
                    },
                    onClick = {
                        onOptionSelected("")
                        expanded = false
                        searchQuery = ""
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = if (option == selectedOption) GoldPrimary else TextWhite,
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                        searchQuery = ""
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
