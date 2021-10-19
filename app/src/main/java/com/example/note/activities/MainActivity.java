package com.example.note.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.note.R;
import com.example.note.activities.CreateNoteActivity;
import com.example.note.adapters.NotesAdapter;
import com.example.note.database.NotesDatabase;
import com.example.note.entities.Note;
import com.example.note.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE=1;
    public static final int REQUEST_CODE_UPDATE_NOTE=2;
    public static final int REQUEST_CODE_SHOW_NOTES=3;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private ConstraintLayout constraintLayout,constraintLayoutDeleteNote;
    private LinearLayout deletelayout;
    private AlertDialog dialogDeleteNote;

    private int OnClickImage=0;
    private ImageView imageViewDeveloper;
    private Note note;

    private int noteClickedPosition=-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain=findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                    new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        notesRecyclerView=findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter=new NotesAdapter(noteList);
        notesRecyclerView.setAdapter(notesAdapter);
        constraintLayout=findViewById(R.id.constraintdeveloper);
        imageViewDeveloper=findViewById(R.id.imageDeveloper);

        getNotes(REQUEST_CODE_SHOW_NOTES);


        notesAdapter.setOnClickListener(new NotesAdapter.OnNoteListener() {
            @Override
            public void OnClick(View view, int position) {
                //getNotes(REQUEST_CODE_UPDATE_NOTE);
                note=noteList.get(position);
                noteClickedPosition = position;
                Intent intent=new Intent(MainActivity.this, CreateNoteActivity.class);
                intent.putExtra("isViewOrUpdate", true);
                intent.putExtra("note",note);
                startActivity(intent);
            }

            @Override
            public void OnLongClick(int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        note=noteList.get(position);
                        noteClickedPosition = position;
                        DeleteNoteTask task = new DeleteNoteTask();
                        task.execute();
                    }
                });
                }
        });

        Toast.makeText(MainActivity.this, "WORKED1", Toast.LENGTH_SHORT).show();

        //getNotes(REQUEST_CODE_UPDATE_NOTE);
    }

    /*private void showDeleteDialog(){
        if(dialogDeleteNote==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            View view= LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote=builder.create();
            if(dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            note=noteList.get(noteClickedPosition);
                            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(note);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent=new Intent();
                            intent.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }


                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteNote.dismiss();
                }
            });
        }

        dialogDeleteNote.show();
    }*/

    @SuppressLint("StaticFieldLeak")
    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().deleteNote(note);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            getNotes(10);

            notesAdapter.notifyDataSetChanged();
        }
    }


    public void getNotes(final int requestCode){

        Toast.makeText(MainActivity.this, "WORKED2", Toast.LENGTH_SHORT).show();
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getNotesDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                //Toast.makeText(MainActivity.this, "requestCode"+requestCode, Toast.LENGTH_SHORT).show();
                if(requestCode==REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    //Log.d("Test","1 "+noteList.size()+"");
                    notesAdapter.notifyDataSetChanged();
                }else if(requestCode==REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyDataSetChanged();
                    //Log.d("Test","2 "+noteList.size()+"");
                    notesRecyclerView.smoothScrollToPosition(0);
                }
                else if(requestCode==REQUEST_CODE_UPDATE_NOTE){
                    //noteList.remove(noteClickedPosition);
                    //noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                    notesAdapter.notifyDataSetChanged();
                    //Log.d("Test","3 "+noteList.size()+"");
                    //notesAdapter.notifyDataSetChanged();
                }
                else {
                    noteList.remove(noteClickedPosition);
                    notesAdapter.notifyDataSetChanged();
                }

            }
        }
        new GetNotesTask().execute();

    }

    public void showAlertDialogButtonClicked(View view) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AlertDialog");
        builder.setMessage("Would you like to continue learning how to use Android alerts?");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "No Delete", Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(MainActivity.this, "WORKED3", Toast.LENGTH_SHORT).show();
        Log.d("TEST!!!", "onActivityResult: "+requestCode+"  "+resultCode);

        getNotes(REQUEST_CODE_UPDATE_NOTE);
        if(requestCode==REQUEST_CODE_ADD_NOTE && resultCode==RESULT_OK){
            //Toast.makeText(MainActivity.this, "WORK ADD", Toast.LENGTH_SHORT).show();
            getNotes(REQUEST_CODE_ADD_NOTE);
            getNotes(REQUEST_CODE_UPDATE_NOTE);
        }else if(requestCode==REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK){
            if(data!=null){
                //Toast.makeText(MainActivity.this, "Work UPDATE", Toast.LENGTH_SHORT).show();
                getNotes(REQUEST_CODE_UPDATE_NOTE);
            }
        }
        else{
            Log.d("TEST!!!", "onActivityResult: "+requestCode+"  "+resultCode);
        }
    }

    public void onClickimagedeveloper(View view) {
        if(OnClickImage==0){
            constraintLayout.setVisibility(View.VISIBLE);
            constraintLayout.animate().alpha(1).setDuration(1000);
            imageViewDeveloper.animate().alpha(1).rotation(360).setDuration(2500);
            constraintLayout.setAlpha(1);
            OnClickImage++;
        }
        else if(OnClickImage==1){
            constraintLayout.setVisibility(View.INVISIBLE);
            constraintLayout.animate().alpha(0).setDuration(1000);
            imageViewDeveloper.animate().alpha(0).rotation(-360).setDuration(2500);
            constraintLayout.setAlpha(0);
            OnClickImage=0;
        }
    }

    public void onClickcalendar(View view) {
        
    }

}

