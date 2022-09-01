package com.example.jobfinder.adapters;

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

import com.example.jobfinder.filters.FilterCategory;
import com.example.jobfinder.models.ModelCategory;
import com.example.jobfinder.databinding.RowItemsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    private RowItemsBinding binding;

    private FilterCategory filter;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowItemsBinding.inflate(LayoutInflater.from(context), parent, false);

        return new AdapterCategory.HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        long timestamp = model.getTimestamp();
        String uid = model.getUid();

        holder.itemTv.setText(category);

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this category?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteCategory(model, holder);
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

    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
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
        return categoryArrayList.size();
    }

    public class HolderCategory extends RecyclerView.ViewHolder{
        TextView itemTv;
        ImageButton deleteBtn;


        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            itemTv = binding.itemTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
