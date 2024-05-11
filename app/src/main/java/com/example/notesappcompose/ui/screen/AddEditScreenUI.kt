package com.example.notesappcompose.ui.screen

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notesappcompose.feature_note.domain.model.Note
import com.example.notesappcompose.feature_note.presentation.add_edit_note.AddEditNoteEvent
import com.example.notesappcompose.feature_note.presentation.add_edit_note.AddEditNoteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreenUI(
    navController: NavController,
    noteColor: Int,
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val noteBgAnimation = remember {
        Animatable(
            Color(
                if (noteColor != -1) noteColor else viewModel.noteColor.value
            )
        )
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditNoteViewModel.UiEvent.ShowSnackbar -> {
                    SnackbarHostState().showSnackbar(
                        message = event.message
                    )
                }

                is AddEditNoteViewModel.UiEvent.SaveNote -> {
                    navController.navigateUp()
                }

                else -> {

                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.End,

                modifier = Modifier.fillMaxWidth()
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(AddEditNoteEvent.SaveNote)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan Note")
                }
            }

        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(noteBgAnimation.value)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Note.noteColors.forEach { color ->
                        val colorInt = color.toArgb()

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(15.dp, CircleShape)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = 4.dp,
                                    color = if (viewModel.noteColor.value == colorInt) {
                                        Color.Black
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .clickable {
                                    scope.launch {
                                        noteBgAnimation.animateTo(
                                            targetValue = Color(colorInt),
                                            animationSpec = tween(durationMillis = 500)
                                        )
                                    }

                                    viewModel.onEvent(AddEditNoteEvent.ChangeColor(colorInt))
                                }

                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                HintUI(
                    text = titleState.text,
                    hint = titleState.hint,
                    onValueChange = {
                        viewModel.onEvent(AddEditNoteEvent.EnteredTitle(it))
                    },
                    onFocusChange = {
                        viewModel.onEvent(AddEditNoteEvent.ChangeTitleFocus(it))
                    },
                    isHintVisible = titleState.isHintVisible,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))

                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray
                )

                Spacer(Modifier.height(16.dp))

                HintUI(
                    text = contentState.text,
                    hint = contentState.hint,
                    onValueChange = {
                        viewModel.onEvent(AddEditNoteEvent.EnteredContent(it))
                    },
                    onFocusChange = {
                        viewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))
                    },
                    isHintVisible = contentState.isHintVisible,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxHeight()
                )
            }

        }
    )
}
