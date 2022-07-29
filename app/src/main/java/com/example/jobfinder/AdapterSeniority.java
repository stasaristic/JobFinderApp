package com.example.jobfinder;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.databinding.RowItemsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterSeniority extends RecyclerView.Adapter<AdapterSeniority.HolderSeniority> implements Filterable {

    private Context context;
    public ArrayList<ModelSeniority> seniorityArrayList, filterList;

    private RowItemsBinding binding;

    private FilterSeniority filter;

    public AdapterSeniority(Context context, ArrayList<ModelSeniority> seniorityArrayList) {
        this.context = context;
        this.seniorityArrayList = seniorityArrayList;
        this.filterList = seniorityArrayList;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterSeniority(filterList, this);
        }
        return filter;
    }

    @NonNull
    @Override
    public AdapterSeniority.HolderSeniority onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowItemsBinding.inflate(LayoutInflater.from(context), parent, false);

        return new AdapterSeniority.HolderSeniority(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSeniority.HolderSeniority holder, int position) {
        ModelSeniority model = seniorityArrayList.get(position);
        String id = model.getId();
        String seniority = model.getSeniority();
        long timestamp = model.getTimestamp();
        String uid = model.getUid();

        holder.itemTv.setText(seniority);

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this job level of seniority?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteSeniority(model, holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private void deleteSeniority(ModelSeniority model, HolderSeniority holder) {
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Seniority");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to delete
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return seniorityArrayList.size();
    }

    public class HolderSeniority extends RecyclerView.ViewHolder {
        TextView itemTv;
        ImageButton deleteBtn;

        public HolderSeniority(@NonNull View itemView) {
            super(itemView);

            itemTv = binding.itemTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
