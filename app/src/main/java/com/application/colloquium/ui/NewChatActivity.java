package com.application.colloquium.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.application.colloquium.R;
import com.application.colloquium.UserClient;
import com.application.colloquium.UserContactAdapter;
import com.application.colloquium.model.User;
import com.application.colloquium.model.UserContacts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NewChatActivity extends AppCompatActivity {

    private static final String TAG = "NewChatActivity" ;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 9001;

    private ListView lstContacts;
    private ProgressBar pgbNewChat;

    private List<String> lstUsers = new ArrayList<>();
    private ArrayList<UserContacts> userContacts = new ArrayList<>();

    private boolean checkContactPermission = false;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference mData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        lstContacts = findViewById(R.id.lv_contacts);
        pgbNewChat = findViewById(R.id.pgb_newchat);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkContactPermission) {
            getContacts();
        }
        else{
            getContactPermission();
        }
    }

    private void getContactPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            checkContactPermission = true;
            getContacts();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    private void getContacts() {

        /*
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        startManagingCursor(cursor);

        String [] from = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone._ID};

        int[] to = {android.R.id.text1,android.R.id.text2};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,cursor,from,to);
        lstContacts.setAdapter(simpleCursorAdapter);
        lstContacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         */


        UserContactAdapter contactAdapter = new UserContactAdapter(this,userContacts);

        lstContacts.setAdapter(contactAdapter);

        fStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult() != null){
                        for(DocumentSnapshot documentSnapshot : task.getResult()){
                            UserContacts contact = new UserContacts();
                            String name = documentSnapshot.getString("name");
                            String phoneNo = documentSnapshot.getString("phoneNo");
                            contact.setName(name);
                            contact.setPhoneNumber(phoneNo);
                            contact.setId(documentSnapshot.getId());
                            userContacts.add(contact);
                            Log.d(TAG,"Contact : "+contact.toString());
                        }
                        Log.d(TAG,"User Contact : "+userContacts.toString());
                        contactAdapter.notifyDataSetChanged();
                        pgbNewChat.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        lstContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Log.d(TAG,"FAuth ID : "+fAuth.getUid());
                User user = ((UserClient)getApplicationContext()).getUser();
                mData.child("User Chats").child(fAuth.getCurrentUser().getUid()).child(userContacts.get(i).getId()).setValue(userContacts.get(i).getName()).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            mData.child("User Chats").child(userContacts.get(i).getId()).child(fAuth.getCurrentUser().getUid()).setValue(user.getName()).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    Intent intent = new Intent(getApplicationContext(),ChatActivity.class);

                                    intent.putExtra("User ID",userContacts.get(i).getId());

                                    startActivity(intent);
                                    Toast.makeText(NewChatActivity.this, "User Chat Created of "+userContacts.get(i).getName(), Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                        else{
                            Log.d(TAG,task.getException().getMessage());
                            Toast.makeText(NewChatActivity.this,task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }
}