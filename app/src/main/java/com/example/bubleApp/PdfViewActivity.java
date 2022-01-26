package com.example.bubleApp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bubleApp.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {

    // view binding
    private ActivityPdfViewBinding binding;

    private String bookId;

    private static final String TAG = "PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get boodId from intent that we passed in intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "OnCreate: BookId: " + bookId);


        loadBookDetails();


        // handle clock go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });
    }

    private void loadBookDetails() {

        Log.d(TAG, "loadBookDetails: Get PDF URL ...");
        // Database reference to fet book details e.g. get book url using book id
        // 1/ Get book Url using book Id

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Documents");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // get book url
                String pdfUrl= "" + snapshot.child("url").getValue();

                Log.d(TAG, "loadBookDetails: PDF URL :" + pdfUrl);

                // 2/ Load pdf using that url from firebase storage
                loadBookFromUrl(pdfUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadBookFromUrl(String pdfUrl) {

        Log.d(TAG, "loadBookFromUrl: Get PDF from storage");

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // load pdf using bytes

                // set false to scroll vertical, set true to swipe horizontal
                binding.pdfView.fromBytes(bytes)
                        .swipeHorizontal(true)//set true to scroll vertical
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {

                        // set current and total pages in toolbar subtitle
                        int currentPage = (page + 1); // do +1 bcs it starts from 0
                        binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount);

                        Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);

                    }
                })
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                Log.d(TAG, "onError: " + t.getMessage());
                                Toast.makeText(PdfViewActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                Log.d(TAG, "onError: " + t.getMessage());

                                Toast.makeText(PdfViewActivity.this, "Error on page " + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .load();

                binding.progressBar.setVisibility(View.GONE);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to load book
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}