package com.example.notesappcompose.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notesappcompose.R
import com.example.notesappcompose.feature_note.data.data_source.ViewType
import com.example.notesappcompose.feature_note.domain.model.Note
import com.example.notesappcompose.feature_note.navigation.NavScreen
import com.example.notesappcompose.feature_note.presentation.notes.NotesEvent
import com.example.notesappcompose.feature_note.presentation.notes.NotesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotesScreen(
    navController: NavController,
    context: Context,
) {

    Scaffold(
        floatingActionButton = { FloatingButtonScaffold(navController = navController) },
        content = {
            ContentPartScaffold(
                navController = navController,
                context = context
            )
        }
    )
}

@Composable
fun FloatingButtonScaffold(navController: NavController) {
    FloatingActionButton(
        onClick = {
            navController.navigate(NavScreen.AddEditNoteScreen.route)
        },
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
    }
}

@Composable
fun ContentPartScaffold(
    navController: NavController,
    context: Context,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f) // Menggunakan weight agar teks tetap di kiri
            )

            IconButton(
                onClick = { viewModel.onEvent(NotesEvent.ToggleOrderSection) }
            ) {
                Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
            }

            IconButton(
                onClick = {
                    if (state.currentView == ViewType.LIST) {
                        viewModel.onEvent(NotesEvent.ChangeViewToGrid)
                    } else {
                        viewModel.onEvent(NotesEvent.ChangeViewToList)
                    }
                }
            ) {
                if (state.currentView == ViewType.LIST) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "List View")
                } else {
                    Icon(imageVector = Icons.Default.GridView, contentDescription = "Grid View")
                }
            }
        }



        AnimatedVisibility(
            visible = state.isOrderSectionVisible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            OrderSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                noteOrder = state.noteOrder,
                onOrderChange = {
                    viewModel.onEvent(NotesEvent.Order(it))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.currentView) {
            ViewType.LIST -> {
                NotesList(
                    notes = state.notes,
                    navController = navController,
                    context = context,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
            }
            ViewType.GRID -> {
                NotesGrid(
                    notes = state.notes,
                    navController = navController,
                    context = context,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
            }
        }

    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    navController: NavController,
    context: Context,
    viewModel: NotesViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(notes) { note ->
            NoteItemUI(
                note = note,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(
                            NavScreen.AddEditNoteScreen.route
                                    + "?noteId=${note.id}&noteColor=${note.color}"
                        )
                    },
                onShareClicked = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, note.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                onDeleteClicked = {
                    viewModel.onEvent(NotesEvent.DeleteNote(note))
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Note Deleted!",
                            actionLabel = "Undo"
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                viewModel.onEvent(NotesEvent.RestoreNote)
                            }
                            else -> {}
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotesGrid(
    notes: List<Note>,
    navController: NavController,
    context: Context,
    viewModel: NotesViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        items(notes) { note ->
            NoteItemUI(
                note = note,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(
                            NavScreen.AddEditNoteScreen.route
                                    + "?noteId=${note.id}&noteColor=${note.color}"
                        )
                    },
                onShareClicked = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, note.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                onDeleteClicked = {
                    viewModel.onEvent(NotesEvent.DeleteNote(note))
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Note Deleted!",
                            actionLabel = "Undo"
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                viewModel.onEvent(NotesEvent.RestoreNote)
                            }
                            else -> {}
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
