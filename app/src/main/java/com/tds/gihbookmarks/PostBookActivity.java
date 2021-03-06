package com.tds.gihbookmarks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tds.gihbookmarks.model.Book;
import com.tds.gihbookmarks.util.UserApi;

import java.util.Date;

public class PostBookActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private Button addBookButton;
    private ImageView bookImg1;

    private EditText titleText;
    private EditText authorText;
    private EditText publicationText;
    private EditText editionText;
    private EditText priceText;

    private String currentUserId;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference=db.collection("Books");


    private Uri img1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_book);

        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        titleText=findViewById(R.id.title_text);
        authorText=findViewById(R.id.author_text);
        publicationText=findViewById(R.id.publication_text);
        editionText=findViewById(R.id.edition_text);
        priceText=findViewById(R.id.price_text);

        bookImg1=findViewById(R.id.book_img1);


        addBookButton=findViewById(R.id.add_book);

        addBookButton.setOnClickListener(this);




        bookImg1.setOnClickListener(this);

        if(UserApi.getInstance()!=null){
            currentUserId=UserApi.getInstance().getUserId();

        }
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user=firebaseAuth.getCurrentUser();
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.add_book:
                uploadBook();
                break;
            case R.id.book_img1:
                Intent GalleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                GalleryIntent.setType("image/*");
                startActivityForResult(GalleryIntent,GALLERY_CODE);
                break;

        }
    }

    private void uploadBook() {
        final String title=titleText.getText().toString().trim();
        final String author=authorText.getText().toString().trim();
        final String publication=publicationText.getText().toString().trim();
        final String edition=editionText.getText().toString().trim();
        final String price=priceText.getText().toString().trim();

        if(!TextUtils.isEmpty(title)
           && !TextUtils.isEmpty(publication)
           && !TextUtils.isEmpty(edition)
           && !TextUtils.isEmpty(author)
           && !TextUtils.isEmpty(price)
           && img1!=null

        ){


                final StorageReference filepath=storageReference
                        .child("book_images")
                        .child("image"+ Timestamp.now().getSeconds());
                filepath.putFile(img1)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imgageUrl=uri.toString();
                                        Book book=new Book();
                                        book.setImageUrl1(imgageUrl);
                                        book.setTitle(title);
                                        book.setAuthor(author);
                                        book.setEdition(edition);
                                        book.setExpectedPrice(price);
                                        book.setPublication(publication);
                                        book.setUserId(currentUserId);

                                        book.setDateAdded(new Timestamp(new Date()));

                                        collectionReference.add(book)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                       /* startActivity(new Intent(PostBookActivity.this,HomepageActivity.class));
                                                        finish();*/
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("PostBookActivity", "onFailure: "+e.getMessage());
                                                    }
                                                });


                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("PostBookActivity", "onFailure: "+e.getMessage());
                            }
                        });





        }
        else{
            Snackbar.make(findViewById(R.id.post_book_layout),"Empty Fields",Snackbar.LENGTH_LONG).setAction("Action",null).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK){
            if(data!=null){
                img1=data.getData();
                bookImg1.setImageURI(img1);
            }
        }
    }
}
