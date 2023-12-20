package com.example.dailytask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailytask.adapter.MainAdapter;
import com.example.dailytask.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "MyChannel";
    private int notificationId = 1;

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

        // Database
        databaseHelper = new DatabaseHelper(getApplicationContext());

        // Hapus otomatis tugas yang deadline-nya sudah tercapai
        performAutoDelete();

        // Set layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAdapter(this, databaseHelper.getArray());
        // Set adapter
        recyclerView.setAdapter(adapter);

        // Set click listener di button
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(
                            Color.TRANSPARENT
                    ));
                    dialog.setContentView(R.layout.dialog_main);
                    dialog.show();

                    EditText editText = dialog.findViewById(R.id.edit_text);
                    Button btnPickDate = dialog.findViewById(R.id.btn_pick_date);
                    Button btSubmit = dialog.findViewById(R.id.btn_submit);

                    // Tambahkan kode untuk menampilkan DatePickerDialog
                    btnPickDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                            Calendar selectedDate = new Calendar.Builder()
                                                    .setDate(year, month, day)
                                                    .build();
                                            String formattedDate = dateFormat.format(selectedDate.getTime());
                                            btnPickDate.setText(formattedDate);
                                        }
                                    }, year, month, dayOfMonth);

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
                                String sDeadline = btnPickDate.getText().toString().trim();

                                Log.d("MainActivity", "sText: " + sText);
                                Log.d("MainActivity", "sDate: " + sDate);
                                Log.d("MainActivity", "sDeadline: " + sDeadline);

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
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "Error showing dialog: " + e.getMessage());
                }
            }
        });

        // Set click listener pada button
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

    // Fungsi untuk melakukan hapus otomatis tugas yang deadline-nya sudah tercapai
    private void performAutoDelete() {
        try {
            // Mengambil tugas yang dihapus karena deadline tercapai
            JSONArray deletedTasks = databaseHelper.getExpiredTasks();
            // Menghapus tugas dari database
            databaseHelper.deleteExpiredTasks();
            adapter.updateArray(databaseHelper.getArray());
            adapter.notifyDataSetChanged();

            // Menampilkan notifikasi untuk setiap tugas yang dihapus
            for (int i = 0; i < deletedTasks.length(); i++) {
                JSONObject task = deletedTasks.getJSONObject(i);
                showNotification("Deadline Terlewat", task.getString("text"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error performing auto delete: " + e.getMessage());
        }
    }

    // Fungsi untuk menampilkan notifikasi
    private void showNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId++, builder.build());
    }
}
