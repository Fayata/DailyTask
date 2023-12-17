package com.example.dailytask;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    //Variable
    Activity activity;
    JSONArray jsonArray;

    DatabaseHelper databaseHelper;

    //buat Constructor
    public MainAdapter(Activity activity, JSONArray jsonArray){
        this.activity = activity;
        this.jsonArray = jsonArray;
    }
    //buat update array
    public void updateArray(JSONArray jsonArray){
        this.jsonArray = jsonArray;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main,parent,false);
        //database
        databaseHelper = new DatabaseHelper(view.getContext());
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            //json object
            JSONObject object = jsonArray.getJSONObject(position);
            //set text
            holder.tvText.setText(object.getString("text"));
            holder.tvDate.setText(object.getString("date"));
        }catch (JSONException e){
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //json object
                    JSONObject object = jsonArray.getJSONObject(
                            holder.getAdapterPosition());
                    //get value dari json
                    String sID = object.getString("id");
                    String sText = object.getString("text");
                    //dialog
                    Dialog dialog = new Dialog(activity);
                    //set background transparan
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    //set view
                    dialog.setContentView(R.layout.dialog_main);
                    //display
                    dialog.show();
                    EditText editText = dialog.findViewById(R.id.edit_text);
                    Button btUpdate = dialog.findViewById(R.id.btn_submit);
                    //set text seblumnya
                    editText.setText(sText);
                    //set update
                    btUpdate.setText("Update");
                    btUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //get text dari edit text
                            String sText = editText.getText().toString().trim();
                            //update select
                            databaseHelper.update(sID, sText);
                            //refresh array
                            updateArray(databaseHelper.getArray());
                            //notif adapter
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
                //get item yang dipilih
                int position = holder.getAdapterPosition();
                try {
                    JSONObject object = jsonArray.getJSONObject(position);
                    String sID = object.getString("id");

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    //set judul
                    builder.setTitle("Confirm");
                    //set message
                    builder.setMessage("Yakin untuk menghapus?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            databaseHelper.delete(sID);
                            jsonArray.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, jsonArray.length());
                        }
                    });
                    //set negative button
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //close
                            dialogInterface.dismiss();
                        }
                    });
                    //display
                    builder.show();

                }catch (JSONException e){
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

    public class ViewHolder extends RecyclerView.ViewHolder{
        //variable
        TextView tvText,tvDate;
        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvText = itemView.findViewById(R.id.tv_text);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}

