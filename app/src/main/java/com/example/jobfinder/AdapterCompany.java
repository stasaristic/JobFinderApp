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

public class AdapterCompany extends RecyclerView.Adapter<AdapterCompany.HolderCompany> implements Filterable {

    private Context context;
    public ArrayList<ModelCompany> companyArrayList, filterList;

    // view binding
    private RowItemsBinding binding;

    // instance of filter class
    private FilterCompany filter;


    public AdapterCompany(Context context, ArrayList<ModelCompany> companyArrayList) {
        this.context = context;
        this.companyArrayList = companyArrayList;
        this.filterList = companyArrayList;
    }

    @NonNull
    @Override
    public AdapterCompany.HolderCompany onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_companies.xml
        binding = RowItemsBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCompany(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCompany.HolderCompany holder, int position) {
        //get data
        ModelCompany model = companyArrayList.get(position);
        String id = model.getId();
        String company = model.getCompany();
        long timestamp = model.getTimestamp();
        String uid = model.getUid();

        //set data
        holder.itemTv.setText(company);

        //handle click, delete company
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this company?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // begin delete
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteCompany(model, holder);
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

    private void deleteCompany(ModelCompany model, HolderCompany holder) {
        // get id of company to delete
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Companies");
        ref.child(id)
            .removeValue()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    // deleted successfully
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
        return companyArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterCompany(filterList, this);
        }
        return filter;
    }

    /*View holder class to hold UI views for row_companies.xml*/
    class HolderCompany extends RecyclerView.ViewHolder {

        //ui views of row_companies.xml
        TextView itemTv;
        ImageButton deleteBtn;

        public HolderCompany(@NonNull View itemView) {
            super(itemView);

            //init ui views
            itemTv = binding.itemTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
