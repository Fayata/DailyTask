package com.example.dailytask.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailytask.database.DatabaseHelper;
import com.example.dailytask.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    // Variable
    Activity activity;
    JSONArray jsonArray;

    DatabaseHelper databaseHelper;

    // Buat Constructor
    public MainAdapter(Activity activity, JSONArray jsonArray) {
        this.activity = activity;
        this.jsonArray = jsonArray;
    }

    // Buat update array
    public void updateArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main, parent, false);
        // Database
        databaseHelper = new DatabaseHelper(view.getContext());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject object = jsonArray.getJSONObject(position);
            holder.tvText.setText(object.getString("text"));
            holder.tvDate.setText("Deadline: " + object.getString("deadline"));  // Tampilkan tenggat waktu
        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // JSON object
                    JSONObject object = jsonArray.getJSONObject(holder.getAdapterPosition());
                    // Get value dari JSON
                    String sID = object.getString("id");
                    String sText = object.getString("text");
                    // Dialog
                    Dialog dialog = new Dialog(activity);
                    // Set background transparan
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    // Set view
                    dialog.setContentView(R.layout.dialog_main);
                    // Display
                    dialog.show();
                    EditText editText = dialog.findViewById(R.id.edit_text);
                    Button btUpdate = dialog.findViewById(R.id.btn_submit);
                    // Set text sebelumnya
                    editText.setText(sText);
                    // Set update
                    btUpdate.setText("Update");
                    btUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Get text dari edit text
                            String sText = editText.getText().toString().trim();
                            // Update select
                            databaseHelper.update(sID, sText);
                            // Refresh array
                            updateArray(databaseHelper.getArray());
                            // Notif adapter
                            notifyItemChanged(holder.getAdapterPosition());
                            dialog.dismiss();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Get item yang dipilih
                int position = holder.getAdapterPosition();
                try {
                    JSONObject object = jsonArray.getJSONObject(position);
                    String sID = object.getString("id");

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    // Set judul
                    builder.setTitle("Confirm");
                    // Set message
                    builder.setMessage("Yakin untuk menghapus?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            databaseHelper.delete(sID);
                            jsonArray.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, jsonArray.length());
                        }
                    });
                    // Set negative button
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Close
                            dialogInterface.dismiss();
                        }
                    });
                    // Display
                    builder.show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Variable
        TextView tvText, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvText = itemView.findViewById(R.id.tv_text);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}


