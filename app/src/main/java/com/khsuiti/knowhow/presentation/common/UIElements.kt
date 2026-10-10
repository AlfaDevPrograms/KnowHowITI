// FinanceComponents.kt
package com.khsuiti.knowhow.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.presentation.common.ui.theme.Typography
import com.khsuiti.knowhow.presentation.feature.settings.SettingsIntent
import com.khsuiti.knowhow.presentation.feature.settings.SettingsViewModel
import kotlin.math.roundToInt

// ==================== ДАННЫЕ ДЛЯ ФИЛЬТРОВ ====================
data class CategoryOption(val id: Long?, val name: String)

// ==================== БАЗОВЫЕ КОМПОНЕНТЫ ====================

@Composable
fun MyButton(
	action: () -> Unit,
	text: String = "",
	@SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	Button(
		onClick = action,
		colors = ButtonDefaults.outlinedButtonColors(
			containerColor = if (isPressed) MaterialTheme.colorScheme.secondary else Color.Transparent,
		),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
		interactionSource = interactionSource,
		modifier = modifier
	) {
		MyText(
			text = text,
			textColor = if (isPressed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.secondary,
		)
	}
}

@Composable
fun MyText(
	modifier: Modifier = Modifier,
	text: String,
	textStyle: TextStyle = Typography.bodyLarge,
	textColor: Color = MaterialTheme.colorScheme.onBackground,
	minTextSize: TextUnit = 8.sp,
	maxTextSize: TextUnit = 16.sp,
	textAlign: TextAlign = TextAlign.Left,
	maxLines: Int = 1,
	overflow: TextOverflow = TextOverflow.Ellipsis,
	autoSize: TextAutoSize = TextAutoSize.StepBased(minTextSize, maxTextSize),
	fontSize: TextUnit = 16.sp,
	minLines: Int = 1,
) {
	Text(
		modifier = modifier,
		text = text,
		style = textStyle,
		color = textColor,
		autoSize = autoSize,
		textAlign = textAlign,
		fontSize = fontSize,
		maxLines = maxLines,
		overflow = overflow,
		minLines = minLines
	)
}

@Composable
fun MyTextButton(
	action: () -> Unit,
	text: String,
	modifier: Modifier = Modifier,
	textAlign: TextAlign = TextAlign.Left,
	shape: Shape = RoundedCornerShape(24.dp)
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	TextButton(
		onClick = action,
		colors = ButtonDefaults.outlinedButtonColors(
			containerColor = if (isPressed) MaterialTheme.colorScheme.secondary else Color.Transparent,
		),
		interactionSource = interactionSource,
		modifier = modifier,
		shape = shape
	) {
		MyText(
			text = text,
			modifier = modifier,
			textColor = if (isPressed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onPrimary,
			textAlign = textAlign
		)
	}
}

@Composable
fun MySwitch(
	modifier: Modifier = Modifier,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	label: String,
	icon: ImageVector? = null,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center,
		modifier = modifier
	) {
		icon?.let {
			Icon(
				imageVector = it,
				contentDescription = null,
				tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2
			)
			Spacer(modifier = Modifier.width(12.dp))
		}
		MyText(
			text = label,
			textStyle = Typography.bodyLarge
		)
		Spacer(modifier = Modifier.weight(1f))
		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange,
			colors = SwitchDefaults.colors(
				checkedBorderColor = MaterialTheme.colorScheme.secondary,
				uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary,
				checkedThumbColor = MaterialTheme.colorScheme.secondary,
				uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
				checkedTrackColor = MaterialTheme.colorScheme.background,
				uncheckedTrackColor = MaterialTheme.colorScheme.background,
			)
		)
	}
}

@Composable
fun MySlider(
	text: String,
	value: Float,
	update: (Float) -> SettingsIntent,
	viewModel: SettingsViewModel
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Icon(
				imageVector = Icons.Rounded.BrightnessLow,
				contentDescription = null,
				tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2
			)
			Spacer(modifier = Modifier.width(12.dp))
			MyText(
				modifier = Modifier.weight(1f),
				text = text,
			)
			MyText(
				text = "${(value * 100).roundToInt()}%",
				maxTextSize = 16.sp,
			)
		}
		Slider(
			value = value,
			onValueChange = { level ->
				viewModel.handleIntent(update(level))
			},
			valueRange = 0f..1f,
			colors = SliderDefaults.colors(
				thumbColor = MaterialTheme.colorScheme.secondary,
				activeTrackColor = MaterialTheme.colorScheme.secondary,
				activeTickColor = MaterialTheme.colorScheme.secondary,
				inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
				inactiveTickColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
			),
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@Composable
fun MyIconButton(
	modifier: Modifier = Modifier,
	icon: ImageVector,
	action: () -> Unit,
	containerColor: Color = Color.Transparent
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	Button(
		onClick = action,
		shape = RoundedCornerShape(24.dp),
		colors = ButtonDefaults.outlinedButtonColors(
			containerColor = if (isPressed) MaterialTheme.colorScheme.secondary else containerColor,
		),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
		interactionSource = interactionSource,
		modifier = modifier,
		contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = if (isPressed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.secondary,
			modifier = Modifier
				.fillMaxSize(0.75f)
				.align(Alignment.CenterVertically)
		)
	}
}

@Composable
fun MyOutlinedTextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	textStyle: TextStyle = Typography.bodyLarge,
	textColor: Color = MaterialTheme.colorScheme.onPrimary,
	textAlign: TextAlign = TextAlign.Start,
	enabled: Boolean = true,
	readOnly: Boolean = false,
	label: @Composable (() -> Unit)? = null,
	placeholder: @Composable (() -> Unit)? = null,
	leadingIcon: @Composable (() -> Unit)? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	isError: Boolean = false,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default,
	singleLine: Boolean = true,
	maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	shape: Shape = RoundedCornerShape(24.dp),
	supportingText: @Composable (() -> Unit)? = null,
	containerColor: Color = Color.Transparent
) {
	val styledTextStyle = textStyle.copy(
		color = textColor,
		textAlign = textAlign,
		shadow = Shadow(
			color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
			offset = Offset(1f, 1f),
			blurRadius = 2f
		)
	)

	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		modifier = modifier,
		textStyle = styledTextStyle,
		enabled = enabled,
		readOnly = readOnly,
		label = label,
		placeholder = placeholder,
		leadingIcon = leadingIcon,
		trailingIcon = trailingIcon,
		isError = isError,
		visualTransformation = visualTransformation,
		keyboardOptions = keyboardOptions,
		keyboardActions = keyboardActions,
		singleLine = singleLine,
		maxLines = maxLines,
		interactionSource = interactionSource,
		colors = OutlinedTextFieldDefaults.colors(
			focusedTextColor = textColor,
			unfocusedTextColor = textColor,
			disabledTextColor = textColor.copy(alpha = 0.5f),
			cursorColor = textColor,
			focusedBorderColor = MaterialTheme.colorScheme.secondary,
			unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary,
			disabledBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
			focusedLabelColor = MaterialTheme.colorScheme.secondary,
			unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
			disabledLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
			selectionColors = TextSelectionColors(
				handleColor = MaterialTheme.colorScheme.secondary,
				backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
			),
			focusedContainerColor = containerColor,
			unfocusedContainerColor = containerColor
		),
		shape = shape,
		supportingText = supportingText
	)
}

@Composable
fun MyFilterChip(
	selected: Boolean,
	onClick: () -> Unit,
	label: String,
	modifier: Modifier = Modifier,
	leadingIcon: ImageVector? = null,
) {
	FilterChip(
		selected = selected,
		onClick = onClick,
		label = {
			MyText(
				text = label,
				textColor = if (selected)
					MaterialTheme.colorScheme.background
				else
					MaterialTheme.colorScheme.onPrimary,
				maxTextSize = 14.sp,
				maxLines = 1,
			)
		},
		leadingIcon = leadingIcon?.let {
			{
				Icon(
					imageVector = it,
					contentDescription = null,
					tint = if (selected)
						MaterialTheme.colorScheme.background
					else
						MaterialTheme.colorScheme.onPrimary,
					modifier = Modifier.size(18.dp)
				)
			}
		},
		shape = RoundedCornerShape(24.dp),
		border = BorderStroke(
			width = 2.dp,
			color = MaterialTheme.colorScheme.secondary
		),
		colors = FilterChipDefaults.filterChipColors(
			containerColor = Color.Transparent,
			labelColor = MaterialTheme.colorScheme.onPrimary,
			selectedContainerColor = MaterialTheme.colorScheme.secondary,
			selectedLabelColor = MaterialTheme.colorScheme.background,
			disabledContainerColor = Color.Transparent,
			disabledLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
		),
		modifier = modifier
	)
}

@Composable
fun MyAboutItem(
	text: String,
	icon: Painter,
	action: () -> Unit,
) {
	var isPressed by remember { mutableStateOf(false) }

	val defaultBackgroundColor = MaterialTheme.colorScheme.surface
	val pressedBackgroundColor = MaterialTheme.colorScheme.secondary

	val backgroundColor by animateColorAsState(
		targetValue = if (isPressed) pressedBackgroundColor else defaultBackgroundColor,
		animationSpec = tween(durationMillis = 50)
	)

	Row(
		modifier = Modifier
			.pointerInput(Unit) {
				detectTapGestures(
					onPress = {
						isPressed = true
						try {
							tryAwaitRelease()
							action()
						} finally {
							isPressed = false
						}
					}
				)
			}
			.background(
				color = backgroundColor,
				shape = RoundedCornerShape(24.dp)
			)
			.border(
				width = 1.dp,
				color = MaterialTheme.colorScheme.secondary,
				shape = RoundedCornerShape(24.dp)
			)
			.fillMaxWidth()
			.height(52.dp)
			.padding(horizontal = 16.dp, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Box(
			modifier = Modifier.weight(1f),
			contentAlignment = Alignment.Center
		) {
			MyText(
				text = text,
				textStyle = MaterialTheme.typography.titleMedium,
				textColor = if (isPressed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
				textAlign = TextAlign.Center,
				maxTextSize = 20.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
		Spacer(Modifier.width(8.dp))
		Icon(
			painter = icon,
			contentDescription = null,
			tint = if (isPressed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary,
			modifier = Modifier.size(24.dp)
		)
	}
}

@Composable
fun MyAboutIcon(
	painter: Painter,
	action: () -> Unit,
	size: Dp
) {
	var isPressed by remember { mutableStateOf(false) }

	val defaultBackgroundColor = MaterialTheme.colorScheme.surface
	val pressedBackgroundColor = MaterialTheme.colorScheme.secondary

	val backgroundColor by animateColorAsState(
		targetValue = if (isPressed) pressedBackgroundColor else defaultBackgroundColor,
		animationSpec = tween(durationMillis = 50)
	)

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.size(size)
			.background(
				color = backgroundColor,
				shape = CircleShape
			)
			.border(
				width = 1.dp,
				color = MaterialTheme.colorScheme.secondary,
				shape = CircleShape
			)
			.pointerInput(Unit) {
				detectTapGestures(
					onPress = {
						isPressed = true
						try {
							tryAwaitRelease()
							action()
						} finally {
							isPressed = false
						}
					}
				)
			}
	) {
		Icon(
			painter = painter,
			contentDescription = null,
			tint = if (isPressed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(10.dp)
		)
	}
}

@Composable
fun CircleImagePicker(
	modifier: Modifier = Modifier,
	text: String,
	icon: ImageVector? = null,
	onImageSelected: (Uri) -> Unit,
	onImageDelete: () -> Unit,
) {
	val pickImageLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia(),
		onResult = { uri ->
			uri?.let(onImageSelected)
		}
	)
	val launchPicker = {
		pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
	}

	val interactionSourceSelect = remember { MutableInteractionSource() }
	val interactionSourceDelete = remember { MutableInteractionSource() }
	val rippleIndication = LocalIndication.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp)
			.padding(start = 6.dp, end = 8.dp),
	) {

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.weight(1f)
				.clip(CircleShape)
				.indication(interactionSourceSelect, rippleIndication)
				.clickable(
					interactionSource = interactionSourceSelect,
					indication = null
				) {
					launchPicker()
				}
				.padding(horizontal = 12.dp, vertical = 12.dp)
		) {
			icon?.let {
				Icon(
					imageVector = it,
					contentDescription = null,
					tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2,
					modifier = Modifier.size(24.dp)
				)
			}
			Spacer(modifier = Modifier.width(8.dp))
			MyText(
				modifier = Modifier.weight(1f),
				text = text,
				textStyle = Typography.bodyLarge,
				maxLines = 1,
				maxTextSize = 16.sp,
				overflow = TextOverflow.Ellipsis
			)
		}

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.size(48.dp)
				.clip(CircleShape)
				.indication(interactionSourceDelete, rippleIndication)
				.clickable(
					interactionSource = interactionSourceDelete,
					indication = null
				) {
					onImageDelete()
				}
		) {
			Icon(
				imageVector = Icons.Rounded.NoPhotography,
				contentDescription = null,
				tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2,
			)
		}
	}
}

@Composable
fun CustomRadioButton(
	text: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.clip(RoundedCornerShape(24.dp))
			.clickable(onClick = onClick)
			.padding(horizontal = 8.dp, vertical = 4.dp)
			.padding(end = 16.dp)
	) {
		RadioButton(
			selected = selected,
			onClick = onClick,
			colors = RadioButtonDefaults.colors(
				selectedColor = MaterialTheme.colorScheme.secondary,
				unselectedColor = MaterialTheme.colorScheme.onPrimary
			)
		)
		MyText(
			text = text,
			fontSize = 20.sp,
			maxTextSize = 20.sp,
			textColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary
		)
	}
}

@Composable
fun DropdownFilter(
	selectedOption: String,
	options: List<String>,
	onOptionSelected: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	var expanded by remember { mutableStateOf(false) }
	var triggerWidth by remember { mutableStateOf(0.dp) }
	val density = LocalDensity.current

	Box(modifier = modifier) {
		Row(
			modifier = Modifier
				.onSizeChanged { size ->
					with(density) {
						triggerWidth = size.width.toDp()
					}
				}
				.border(
					width = 1.dp,
					shape = if (!expanded) {
						RoundedCornerShape(24.dp)
					} else {
						RoundedCornerShape(24.dp, 24.dp, 4.dp, 4.dp)
					},
					color = MaterialTheme.colorScheme.secondary
				)
				.clip(
					shape = if (!expanded) {
						RoundedCornerShape(24.dp)
					} else {
						RoundedCornerShape(24.dp, 24.dp, 4.dp, 4.dp)
					}
				)
				.clickable { expanded = true }
				.padding(horizontal = 16.dp, vertical = 8.dp)
				.background(MaterialTheme.colorScheme.onSurface),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			MyText(
				text = selectedOption,
				fontSize = 18.sp,
				maxTextSize = 18.sp,
				modifier = Modifier.weight(1f),
				textColor = MaterialTheme.colorScheme.onPrimary
			)
			Icon(
				imageVector = Icons.Rounded.ArrowDropDown,
				contentDescription = stringResource(R.string.select_category),
				tint = MaterialTheme.colorScheme.onPrimary
			)
		}

		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
			containerColor = Color.Transparent,
			shadowElevation = 0.dp,
			modifier = Modifier
				.width(triggerWidth)
				.heightIn(max = 300.dp)
				.clip(RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp))
				.background(MaterialTheme.colorScheme.secondary)
		) {
			options.forEach { option ->
				DropdownMenuItem(
					text = {
						MyText(
							text = option,
							maxTextSize = 16.sp,
						)
					},
					onClick = {
						onOptionSelected(option)
						expanded = false
					},
					contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
				)
			}
		}
	}
}

// ==================== КОМПОНЕНТЫ ТЕМЫ ====================

@Composable
fun ThemeSelector(
	currentTheme: ThemeMode,
	onThemeSelected: (ThemeMode) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
	) {
		Row(
			modifier = Modifier.padding(bottom = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = Icons.Rounded.Palette,
				contentDescription = null,
				tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2
			)
			Spacer(modifier = Modifier.width(12.dp))
			MyText(
				text = stringResource(R.string.theme),
				maxTextSize = 16.sp
			)
		}

		ThemeMode.entries.forEach { mode ->
			ThemeOption(
				mode = mode,
				isSelected = currentTheme == mode,
				onClick = { onThemeSelected(mode) }
			)
		}
	}
}

@Composable
fun ThemeOption(
	mode: ThemeMode,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(24.dp))
			.clickable { onClick() },
		verticalAlignment = Alignment.CenterVertically
	) {
		RadioButton(
			selected = isSelected,
			onClick = onClick,
			colors = RadioButtonDefaults.colors(
				selectedColor = com.khsuiti.knowhow.presentation.common.ui.theme.color2,
				unselectedColor =com.khsuiti.knowhow.presentation.common.ui.theme.color3
			)
		)
		Icon(
			imageVector = getThemeIcon(mode),
			contentDescription = null,
			tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2,
			modifier = Modifier.size(24.dp)
		)
		Spacer(modifier = Modifier.width(8.dp))
		MyText(
			text = getThemeDisplayName(mode),
			maxTextSize = 16.sp,
		)
	}
}
@Composable
private fun getThemeIcon(mode: ThemeMode): ImageVector {
	return when (mode) {
		ThemeMode.Light -> Icons.Rounded.LightMode
		ThemeMode.Dark -> Icons.Rounded.DarkMode
		ThemeMode.System -> Icons.Rounded.Settings
	}
}

@Composable
private fun getThemeDisplayName(mode: ThemeMode): String {
	return when (mode) {
		ThemeMode.Light -> stringResource(R.string.theme_light)
		ThemeMode.Dark -> stringResource(R.string.theme_dark)
		ThemeMode.System -> stringResource(R.string.theme_system)
	}
}