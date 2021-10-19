package com.example.note.listeners;

import com.example.note.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);

}
