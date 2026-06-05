package com.davanok.dvnkquizz.ui.screens.aboutScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.app_name
import dvnkquizz.sharedui.generated.resources.feature_create_games_description
import dvnkquizz.sharedui.generated.resources.feature_create_games_title
import dvnkquizz.sharedui.generated.resources.feature_live_results_description
import dvnkquizz.sharedui.generated.resources.feature_live_results_title
import dvnkquizz.sharedui.generated.resources.feature_multiplayer_description
import dvnkquizz.sharedui.generated.resources.feature_multiplayer_title
import dvnkquizz.sharedui.generated.resources.ic_launcher_monochrome
import dvnkquizz.sharedui.generated.resources.landing_description
import dvnkquizz.sharedui.generated.resources.landing_title
import dvnkquizz.sharedui.generated.resources.start
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(
    navigateNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Image(
            painter = painterResource(Res.drawable.ic_launcher_monochrome),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.landing_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.landing_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        FeatureCard(
            title = stringResource(Res.string.feature_create_games_title),
            description = stringResource(Res.string.feature_create_games_description)
        )

        Spacer(Modifier.height(12.dp))

        FeatureCard(
            title = stringResource(Res.string.feature_multiplayer_title),
            description = stringResource(Res.string.feature_multiplayer_description)
        )

        Spacer(Modifier.height(12.dp))

        FeatureCard(
            title = stringResource(Res.string.feature_live_results_title),
            description = stringResource(Res.string.feature_live_results_description)
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = navigateNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.start))
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}