package com.khsuiti.knowhow.presentation.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.data.local.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
	viewModel: CatalogViewModel,
	modifier: Modifier = Modifier,
	onSelectSubject: (String) -> Unit = {}
) {
	val state by viewModel.state.collectAsState()
	val focusManager = LocalFocusManager.current
	val systemDark = isSystemInDarkTheme()
	val themeMode by viewModel.themeMode.collectAsState()

	val isDark = when (themeMode) {
		ThemeMode.System -> systemDark
		ThemeMode.Light  -> false
		ThemeMode.Dark   -> true
	}

	LaunchedEffect(Unit) {
		viewModel.handleIntent(CatalogIntent.LoadCatalog)
	}
	Scaffold(
		containerColor = Color.Transparent
	) { innerPadding ->
		Column(
			modifier = modifier
				.fillMaxSize()
				.padding(innerPadding)
				.padding(horizontal = 16.dp)
		) {
			Spacer(modifier = Modifier.height(12.dp))

			// Title & Subtitle
			Text(
				text = stringResource(R.string.catalog_title),
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)
			Text(
				text = stringResource(R.string.catalog_subtitle),
				style = MaterialTheme.typography.bodyMedium,
				color = Color.Gray
			)

			Spacer(modifier = Modifier.height(12.dp))

			// Search Bar
			OutlinedTextField(
				value = state.searchQuery,
				onValueChange = { viewModel.handleIntent(CatalogIntent.SearchQueryChanged(it)) },
				placeholder = {
					Text(
						text = stringResource(R.string.search_catalog_placeholder),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
					)
				},
				leadingIcon = {
					Icon(
						imageVector = Icons.Rounded.Search,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.secondary
					)
				},
				singleLine = true,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
				keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
				shape = RoundedCornerShape(20.dp),
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = MaterialTheme.colorScheme.secondary,
					unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
					focusedContainerColor = MaterialTheme.colorScheme.surface,
					unfocusedContainerColor = MaterialTheme.colorScheme.surface
				),
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(16.dp))

			// Popular Categories
			Text(
				text = stringResource(R.string.popular_categories),
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.height(8.dp))

			LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				items(state.popularCategories) { cat ->
					val isSelected = (cat == "Все предметы" && state.selectedCategory == null) ||
							(cat == state.selectedCategory)
					FilterChip(
						selected = isSelected,
						onClick = { viewModel.handleIntent(CatalogIntent.SelectCategoryFilter(cat)) },
						label = { Text(text = cat, fontSize = 13.sp) },
						shape = RoundedCornerShape(16.dp),
						colors = FilterChipDefaults.filterChipColors(
							selectedContainerColor = MaterialTheme.colorScheme.secondary,
							selectedLabelColor = Color.White,
							containerColor = MaterialTheme.colorScheme.surface,
							labelColor = MaterialTheme.colorScheme.onSurface
						)
					)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			// Stats row
			val filteredSubjects = state.subjects.filter { item ->
				val matchesSearch = state.searchQuery.isEmpty() ||
						item.subjectName.contains(state.searchQuery, ignoreCase = true) ||
						item.description.contains(state.searchQuery, ignoreCase = true)
				val matchesCat = state.selectedCategory == null || item.subjectName.equals(state.selectedCategory, ignoreCase = true)
				matchesSearch && matchesCat
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = stringResource(R.string.subject_count_format, filteredSubjects.size),
					style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.onBackground
				)
				Text(
					text = stringResource(R.string.sort_popular),
					style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.secondary
				)
			}

			Spacer(modifier = Modifier.height(12.dp))

			// Cards
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(16.dp),
				modifier = Modifier.fillMaxSize()
			) {
				items(filteredSubjects) { item ->
					SubjectCardItem(
						item = item,
						onClick = { onSelectSubject(item.service.idService) },
						isDark = isDark
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectCardItem(
	item: SubjectCatalogItem,
	onClick: () -> Unit,
	isDark: Boolean
) {
	Card(
		shape = RoundedCornerShape(20.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				AsyncImage(
					model = item.photoUrl,
					contentDescription = null,
					modifier = Modifier
						.size(60.dp)
						.clip(CircleShape),
					contentScale = ContentScale.Crop,
					error = painterResource(id = R.drawable.logo)
				)

				Spacer(modifier = Modifier.width(12.dp))

				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = item.subjectName,
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onSurface
					)

					Text(
						text = item.description,
						style = MaterialTheme.typography.bodySmall,
						color = if (isDark) Color.Gray else MaterialTheme.colorScheme.onSurface,
						maxLines = 2
					)

					Row(verticalAlignment = Alignment.CenterVertically) {
						repeat(5) {
							Icon(
								imageVector = Icons.Rounded.Star,
								contentDescription = null,
								tint = Color(0xFFFFC107),
								modifier = Modifier.size(14.dp)
							)
						}
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "${item.rating}",
							style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "(${item.reviewCount} отзывов)",
							style = MaterialTheme.typography.bodySmall,
							color = if (isDark) Color.Gray else MaterialTheme.colorScheme.onSurface
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(12.dp))

			// Subtopic Chips
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(6.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				item.subtopics.forEach { sub ->
					Surface(
						shape = RoundedCornerShape(12.dp),
						color = MaterialTheme.colorScheme.surfaceVariant
					) {
						Text(
							text = sub,
							style = MaterialTheme.typography.labelMedium,
							color = MaterialTheme.colorScheme.secondary,
							modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			// Footer Row
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Column {
					Text(
						text = stringResource(R.string.tutors_label),
						style = MaterialTheme.typography.labelSmall,
						color = if (isDark) Color.Gray else MaterialTheme.colorScheme.onSurface
					)
					Text(
						text = stringResource(R.string.tutors_count_format, item.tutorCount),
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.secondary
					)
				}

				Column {
					Text(
						text = stringResource(R.string.level_label),
						style = MaterialTheme.typography.labelSmall,
						color = if (isDark) Color.Gray else MaterialTheme.colorScheme.onSurface
					)
					Text(
						text = item.gradeLevel,
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onSurface
					)
				}

				Button(
					onClick = onClick,
					shape = RoundedCornerShape(16.dp),
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
				) {
					Text(
						text = stringResource(R.string.open_button),
						color = Color.White,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}
}
