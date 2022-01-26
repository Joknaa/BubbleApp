package com.example.bubleApp;
import static com.example.bubleApp.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;



public class MyApplication extends Application {
    @Override
    public void onCreate(){

        super.onCreate();
    }
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        // format timestamp to dd/mm/
        //android.text.format.
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        return date;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {

        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting" +bookTitle+"..." );//e.g. Deleting Book ABC ...
        progressDialog.show();


        Log.d(TAG, "deleteBook: Deleting from storage...");

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    @Override
                    public void onSuccess(Void unused){
                        Log.d(TAG, "onSuccess: Deleted from storage");
                        Log.d(TAG, "onSuccess: Now deleting info from db");

                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Documents");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>(){
                                    @Override
                                    public void onSuccess(Void unused){
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Document Deleted Successfully...", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener(){
                                    @Override
                                    public void onFailure(@NonNull Exception e){
                                        Log.d(TAG, "onFailure: Failed to delete from db due to "+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e){
                        Log.d(TAG, "onFailure: Failed to delete from storage due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

   public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {

       String TAG = "PDF_SIZE_TAG";
       StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
       ref.getMetadata()
               .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                   @Override
                   public void onSuccess(StorageMetadata storageMetadata) {

                       //get size in bytes
                       double bytes = storageMetadata.getSizeBytes();
                       double kb = bytes / 1024;
                       double mb = kb / 1024;
                       if (mb >= 1) {
                           sizeTv.setText(String.format("%.2f", mb) + "MB");
                       } else if (kb >= 1) {
                           sizeTv.setText(String.format("%.2f", kb) + "KB");
                       } else {
                           sizeTv.setText(String.format("%.2f", bytes) + "bytes");
                       }
                   }

               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Log.d(TAG, "onFailure: Failed to delete from storage due to" + e.getMessage());
                   }
               });

   }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar){
        String TAG = "PDF_LOAD_SINGLE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                Log.d(TAG, "onSuccess: "+pdfTitle+" successfully got the file");


               pdfView.fromBytes(bytes)
                       .pages(0) //show only first page
                       .spacing(0)
                       .swipeHorizontal(false)
                       .enableSwipe(false)
                       .onError(new OnErrorListener(){
                    @Override
                    public void onError(Throwable t){
                        //hide progress
                      progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onSError: "+t.getMessage());

                    }
                })
                       .onPageError(new OnPageErrorListener(){
                   @Override
                   public void onPageError(int page, Throwable t) {
                       progressBar.setVisibility(View.INVISIBLE);
                       Log.d(TAG, "onPageError: "+t.getMessage());
                   }
                }) .onLoad(new OnLoadCompleteListener() {
                   @Override
                   public void loadComplete(int nbPages) {
                       //pdf loaded
                       //hide progress
                       progressBar.setVisibility(View.INVISIBLE);
                       Log.d(TAG, "loadComplete: Pdf loaded");

                   }
               })
                       .load();
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e){
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onFailure: Failed to get the file due to"+e.getMessage());
            }
        });

    }

    public static void loadCategory(String categoryId, TextView categoryTv){
        //get category using categoryId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get category
                        String category = ""+snapshot.child("category").getValue();

                        //set to category text view
                        categoryTv.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void incrementBookViewCount(String bookId){
        // 1/ Get book view count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Documents");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // get view count
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        // in case of null, replace with 0
                        if (viewsCount.equals("") || viewsCount.equals("null")){
                            viewsCount = "0";
                        }

                        // 2/ Increment view count
                        long newViewCount = Long.parseLong(viewsCount) + 1;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Documents");
                        reference.child(bookId).updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl){

        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book ...");

        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);

        //Progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading " + nameWithExtension + "...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // download from firebase storage using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Document Downloaded");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving Book ...");
                        saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to : " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download due to : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded document");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context,"Saved to Downloads Folder", Toast.LENGTH_LONG).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to download folder");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);

        } catch (Exception e) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to download folder due to " + e.getMessage());
            Toast.makeText(context, "Failed saving to downloads folder due to "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }


    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementing book download Count");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Documents");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloading Count: " + downloadsCount);

                        if (downloadsCount.equals("") || downloadsCount.equals("null")){
                            downloadsCount = "0";
                        }

                        //convert to long and increment 1
                        long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloading Count: " +newDownloadsCount);


                        // setup data to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        // update new incremented downloads count to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Documents");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Download Count updated ...");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads count due to: " + e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
