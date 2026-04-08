package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.davanok.dvnkquizz.core.domain.entities.Question

@Composable
fun QuestionCard(
    question: Question,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        // TODO
    }
}