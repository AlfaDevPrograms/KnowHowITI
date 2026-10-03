package com.khsuiti.knowhow.presentation.feature.about

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.presentation.common.MyAboutIcon
import com.khsuiti.knowhow.presentation.common.MyAboutItem
import com.khsuiti.knowhow.presentation.common.MyText
import com.khsuiti.knowhow.presentation.common.ui.theme.Typography

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
	onNavigateBack: () -> Unit = {}
) {
	val context = LocalContext.current
	var isBackButtonDisabled by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			TopAppBar(
				modifier = Modifier.fillMaxWidth(),
				title = {
					MyText(
						modifier = Modifier,
						text = stringResource(R.string.about),
						maxTextSize = 28.sp,
						textColor = MaterialTheme.colorScheme.onBackground,
						textStyle = Typography.titleSmall
					)
				},
				navigationIcon = {
					IconButton(
						onClick = {
							if (!isBackButtonDisabled) {
								isBackButtonDisabled = true
								onNavigateBack()
							}
						},
						enabled = !isBackButtonDisabled
					) {
						Icon(
							imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
							contentDescription = stringResource(R.string.back),
							tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = Color.Transparent
				)
			)
		},
		containerColor = Color.Transparent
	) { padding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState()),
			contentAlignment = Alignment.Center
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 16.dp, vertical = 16.dp)
			) {
				Image(
					painter = painterResource(R.drawable.logo_transparent),
					contentDescription = stringResource(R.string.app_name),
					contentScale = ContentScale.Fit,
					modifier = Modifier
						.fillMaxWidth(0.7f)
						.aspectRatio(1f)
						.clip(RoundedCornerShape(24.dp))
						.background(
							color = Color.Transparent,
							shape = RoundedCornerShape(24.dp)
						)
						.border(
							width = 1.dp,
							color = MaterialTheme.colorScheme.secondary,
							shape = RoundedCornerShape(24.dp)
						)
				)
				MyText(
					text = stringResource(R.string.app_name),
					textColor = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.padding(top = 8.dp),
					maxTextSize = 32.sp
				)
				MyText(
					text = stringResource(R.string.app_description),
					textColor = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.padding(top = 8.dp),
					maxTextSize = 24.sp,
					maxLines = 8,
					textAlign = TextAlign.Center
				)
				Spacer(modifier = Modifier.height(16.dp))

				val playMarketLinkStar = "https://play.google.com/store/apps/details?id=${context.packageName}"
				MyAboutItem(
					text = stringResource(R.string.play_market_star),
					icon = painterResource(R.drawable.star),
					action = { openLink(context, playMarketLinkStar) }
				)
				Spacer(modifier = Modifier.height(16.dp))
				val downloadApp = stringResource(R.string.download_app)
				val shareApp = stringResource(R.string.share_link)
				MyAboutItem(
					text = stringResource(R.string.share),
					icon = painterResource(R.drawable.share),
					action = {
						val shareIntent = Intent(Intent.ACTION_SEND).apply {
							type = "text/plain"
							putExtra(Intent.EXTRA_TEXT, "$downloadApp: https://play.google.com/store/apps/details?id=${context.packageName}")
						}
						context.startActivity(Intent.createChooser(shareIntent, shareApp))
					}
				)
				val playMarketLink = stringResource(R.string.play_market_link)
				val githubLink = stringResource(R.string.github_link)
				val tgLink = stringResource(R.string.tg_link)
				val vkLink = stringResource(R.string.vk_link)
				val emailLink = stringResource(R.string.email_link)
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(top = 16.dp)
				) {
					val icons = listOf(
						R.drawable.google_play to { openLink(context, playMarketLink) },
						R.drawable.github to { openLink(context, githubLink) },
						R.drawable.telegram to { openLink(context, tgLink) },
						R.drawable.vk to { openLink(context, vkLink) },
						R.drawable.email to { openEmail(context, emailLink) }
					)

					val configuration = LocalConfiguration.current
					val screenWidthDp = configuration.screenWidthDp.dp
					val horizontalPadding = 16.dp * 2
					val availableWidth = screenWidthDp - horizontalPadding
					val spacing = 8.dp
					val iconCount = icons.size
					val totalSpacing = spacing * (iconCount - 1)
					val maxIconWidth = ((availableWidth - totalSpacing) / iconCount).coerceAtMost(64.dp)

					icons.forEach { (resId, action) ->
						MyAboutIcon(
							painter = painterResource(resId),
							action = action,
							size = maxIconWidth
						)
					}
				}
				Spacer(modifier = Modifier.height(16.dp))
				MyText(
					text = stringResource(R.string.version_code),
					textColor = MaterialTheme.colorScheme.onBackground,
					textStyle = Typography.titleSmall,
					maxTextSize = 12.sp
				)
			}
		}
	}
}

fun openLink(context: Context, url: String) {
	val cleanUrl = url.trim()
	val uri = cleanUrl.toUri()
	val intent = Intent(Intent.ACTION_VIEW, uri)
	if (intent.resolveActivity(context.packageManager) != null) {
		context.startActivity(intent)
	} else {
		try {
			val browserIntent = Intent(Intent.ACTION_VIEW, uri)
			browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(browserIntent)
		} catch (e: Exception) {
			Log.e("OpenLink", "${context.getString(R.string.error_open_link)}: $cleanUrl", e)
			Toast.makeText(context, context.getString(R.string.error_open_link), Toast.LENGTH_SHORT).show()
		}
	}
}

fun openEmail(context: Context, email: String) {
	val uri = "mailto:$email".toUri()
	val intent = Intent(Intent.ACTION_SENDTO, uri)
	if (intent.resolveActivity(context.packageManager) != null) {
		context.startActivity(intent)
	}
}
