package com.example.dailytask;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailytask.adapter.MainAdapter;
import com.example.dailytask.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton btAdd;

    DatabaseHelper databaseHelper;
    MainAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        btAdd = findViewById(R.id.bt_add);

        //database
        databaseHelper = new DatabaseHelper(getApplicationContext());

        //set layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAdapter(this, databaseHelper.getArray());
        //set adapter
        recyclerView.setAdapter(adapter);

        //set clicklistener di button
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(
                        Color.TRANSPARENT
                ));
                //set view
                dialog.setContentView(R.layout.dialog_main);
                //display dialog
                dialog.show();

                EditText editText = dialog.findViewById(R.id.edit_text);
                Button btnPickDate = dialog.findViewById(R.id.btn_pick_date);
                Button btSubmit = dialog.findViewById(R.id.btn_submit);

                // Tambahkan kode untuk menampilkan DatePickerDialog
                btnPickDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Mendapatkan tanggal saat ini
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                        // Membuat DatePickerDialog
                        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                        // Format tanggal yang dipilih
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                        Calendar selectedDate = new Calendar.Builder()
                                                .setDate(year, month, day)
                                                .build();
                                        String formattedDate = dateFormat.format(selectedDate.getTime());
                                        // Menampilkan tanggal yang dipilih
                                        btnPickDate.setText(formattedDate);
                                    }
                                }, year, month, dayOfMonth);

                        // Menampilkan DatePickerDialog
                        datePickerDialog.show();
                    }
                });

                //set click listener pada button submit
                btSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            //text dari edit text
                            String sText = editText.getText().toString().trim();
                            String sDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                            String sDeadline = btnPickDate.getText().toString().trim();  // Ambil tenggat waktu dari Button

                            if (!sText.isEmpty() && !sDeadline.equals("Pilih Tanggal")) {
                                //Insert database
                                databaseHelper.insert(sText, sDate, sDeadline);
                                adapter.updateArray(databaseHelper.getArray());
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(MainActivity.this, "Isi semua kolom dengan benar", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //set click listener pada button
        btAdd.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Yakin menghapus semuanya?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Clear semua value
                        databaseHelper.truncate();
                        adapter.updateArray(databaseHelper.getArray());
                        recyclerView.setAdapter(adapter);
                    }
                });
                //negative button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                return false;
            }
        });
    }
}

