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

public class AdapterType extends RecyclerView.Adapter<AdapterType.HolderType> implements Filterable {

    private Context context;
    public ArrayList<ModelType> typeArrayList, filterList;

    private RowItemsBinding binding;

    private FilterType filter;

    public AdapterType(Context context, ArrayList<ModelType> typeArrayList) {
        this.context = context;
        this.typeArrayList = typeArrayList;
        this.filterList = typeArrayList;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterType(filterList, this);
        }
        return filter;
    }

    @NonNull
    @Override
    public AdapterType.HolderType onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowItemsBinding.inflate(LayoutInflater.from(context), parent, false);

        return new AdapterType.HolderType(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterType.HolderType holder, int position) {
        ModelType model = typeArrayList.get(position);
        String id = model.getId();
        String type = model.getType();
        long timestamp = model.getTimestamp();
        String uid = model.getUid();

        holder.itemTv.setText(type);

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this type of job?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteType(model, holder);
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

    private void deleteType(ModelType model, HolderType holder) {
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Types");
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
        return typeArrayList.size();
    }

    public class HolderType extends RecyclerView.ViewHolder {

        TextView itemTv;
        ImageButton deleteBtn;

        public HolderType(@NonNull View itemView) {
            super(itemView);

            itemTv = binding.itemTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
