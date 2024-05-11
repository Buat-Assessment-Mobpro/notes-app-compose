package com.example.notesappcompose.feature_note.domain.usecases

import com.example.notesappcompose.feature_note.domain.model.InvalidNoteException
import com.example.notesappcompose.feature_note.domain.model.Note
import com.example.notesappcompose.feature_note.domain.repository.NoteRepository

class AddNote(
    private val repository: NoteRepository
) {
    @Throws(InvalidNoteException::class)
    suspend operator fun invoke(note: Note) {
        if (note.title.isBlank()) {
            throw InvalidNoteException("Judul Catatan tidak boleh Kosong!")
        }
        if (note.content.isBlank()) {
            throw InvalidNoteException("Isi Catatan tidak boleh Kosong!")
        }

        repository.insertNote(note)
    }
}