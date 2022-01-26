package com.example.bubleApp;
import com.example.bubleApp.ModelPdf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import com.example.bubleApp.databinding.ActivityPdfListAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    private ActivityPdfListAdminBinding binding;

    private AdapterPdfAdmin adapterPdfAdmin;
    private ArrayList<ModelPdf> pdfArrayList;

    private String categoryId, categoryTitle;

    private static final String TAG = "PDF_LIST_TAG";


    //private ImageButton backBtn;
/*

    private ImageButton backBtn;
    private TextView titleTv;
    private TextView subTitleTv;

*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //get data form intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        //set PDF category
        binding.subTitleTv.setText(categoryTitle);

        loadPdfList();

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //search as and when type each letter
                try {
                    adapterPdfAdmin.getFilter().filter(charSequence);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged:" + e.getMessage());

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }


    private void loadPdfList() {
        //init list before adding data

        pdfArrayList = new ArrayList<>();
        /*pdfArrayList.add(new ModelPdf("Y59E61GrFPgQEI22yI3conivdN22", "1642972609262", "Cours 1", "Introduction", "1642972529217",
                "https://firebasestorage.googleapis.com/v0/b/buble-app-a010a.appspot.com/o/Documents%2F1642972609262?alt=media&token=dbe24435-c1de-4fb6-94eb-c1d4e47a2701",
                1642972609262L, 0, 0));
        pdfArrayList.add(new ModelPdf("Y59E61GrFPgQEI22yI3conivdN22", "1642972609262", "Cours 1", "Introduction", "1642972529217",
                "https://firebasestorage.googleapis.com/v0/b/buble-app-a010a.appspot.com/o/Documents%2F1642972609262?alt=media&token=dbe24435-c1de-4fb6-94eb-c1d4e47a2701",
                1642972609262L, 0, 0));
        pdfArrayList.add(new ModelPdf("Y59E61GrFPgQEI22yI3conivdN22", "1642972609262", "Cours 1", "Introduction", "1642972529217",
                "https://firebasestorage.googleapis.com/v0/b/buble-app-a010a.appspot.com/o/Documents%2F1642972609262?alt=media&token=dbe24435-c1de-4fb6-94eb-c1d4e47a2701",
                1642972609262L, 0, 0));
        pdfArrayList.add(new ModelPdf("Y59E61GrFPgQEI22yI3conivdN22", "1642972609262", "Cours 1", "Introduction", "1642972529217",
                "https://firebasestorage.googleapis.com/v0/b/buble-app-a010a.appspot.com/o/Documents%2F1642972609262?alt=media&token=dbe24435-c1de-4fb6-94eb-c1d4e47a2701",
                1642972609262L, 0, 0));*/


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Documents");
        ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        pdfArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
/*
                            //get id and title of category
                            String categoryId = "" + ds.child("categoryId").getValue();
                            //add to list
                            pdfArrayList.add(categoryId);*/

                           //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);

                            assert model != null;
                            if (model.getCategoryId().equals(categoryId)){
                                //add to list

                                pdfArrayList.add(model);
                                Log.d(TAG, "onDataChange:" + model.getId() + "" + model.getTitle());
                            }


                        }
                        //setup adapter
                        adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfAdmin);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}