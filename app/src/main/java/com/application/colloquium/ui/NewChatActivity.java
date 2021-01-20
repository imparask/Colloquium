package com.application.colloquium.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.application.colloquium.R;
import com.application.colloquium.UserClient;
import com.application.colloquium.UserContactAdapter;
import com.application.colloquium.model.PublicKey;
import com.application.colloquium.model.PrivateKey;
import com.application.colloquium.model.User;
import com.application.colloquium.model.UserContacts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.math.LongMath.pow;


public class NewChatActivity extends AppCompatActivity {

    private static final String TAG = "NewChatActivity" ;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 9001;

    private ListView lstContacts;
    private ProgressBar pgbNewChat;

    private List<String> lstUsers = new ArrayList<>();
    private ArrayList<UserContacts> userContacts = new ArrayList<>();
    private ArrayList<PrivateKey> arrSecretKeys;

    private boolean checkContactPermission = false;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference mData;
    private int privateKey,requestStatus;

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
            if(userContacts.size()>0){
                userContacts.clear();
            }
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

        UserContactAdapter contactAdapter = new UserContactAdapter(this,userContacts);
        lstContacts.setAdapter(contactAdapter);

        List<String> phoneNumbers = new ArrayList<>();

        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (phones.getCount() > 0) {
            while (phones.moveToNext()) {
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumbers.add("+91"+phoneNumber);
            }
        }
        phones.close();

        Log.d(TAG,""+phoneNumbers);

        fStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult() != null){
                        for(DocumentSnapshot documentSnapshot : task.getResult()){
                            UserContacts contact = new UserContacts();
                            String name = documentSnapshot.getString("name");
                            String phoneNo = documentSnapshot.getString("phoneNo");
                            Log.d(TAG,""+phoneNo);
                            if(phoneNumbers.contains(phoneNo)){
                                contact.setName(name);
                                contact.setPhoneNumber(phoneNo);
                                contact.setId(documentSnapshot.getId());
                                userContacts.add(contact);
                                Log.d(TAG,"Contact : "+contact.toString());
                            }
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

                if(!checkChatExists(userContacts.get(i).getId())) {

                    long prime = getPrimeNumber();
                    long primitive = getPrimitiveRoot(prime);
                    long publicKey = getPublicKey(primitive,prime,fAuth.getUid(),userContacts.get(i).getId());
                    Log.d(TAG,"Prime : "+prime);
                    Log.d(TAG,"Primitive : "+primitive);
                    PublicKey pp = new PublicKey(prime, primitive, publicKey);

                    AlertDialog.Builder builder = new AlertDialog.Builder(NewChatActivity.this);
                    builder.setMessage("Send Chat Request to "+userContacts.get(i).getName());
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sendChatRequest(i,pp);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    startActivity(new Intent(NewChatActivity.this,MainActivity.class));
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();

                }
                else{
                    Toast.makeText(getApplicationContext(),"Chat already exists !",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }

            }
        });
    }

    private void sendChatRequest(int pos , PublicKey pk) {

        mData.child("User Chats").child(fAuth.getCurrentUser().getUid()).child(userContacts.get(pos).getId()).child("Variables").setValue(pk).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    requestStatus = 2;
                    mData.child("Request Status").child(userContacts.get(pos).getId()).child(fAuth.getCurrentUser().getUid()).setValue(requestStatus).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Toast.makeText(NewChatActivity.this, "Request Sent Successfully !",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(NewChatActivity.this, MainActivity.class));
                            //Toast.makeText(NewChatActivity.this, "User Chat Created of " + userContacts.get(pos).getName(), Toast.LENGTH_LONG).show();
                            /*
                            mData.child("Request Status").child(fAuth.getCurrentUser().getUid()).child(userContacts.get(pos).getId()).setValue(requestStatus).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                     }
                            });*/
                        }
                    });
                } else {
                    Log.d(TAG, task.getException().getMessage());
                    Toast.makeText(NewChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private boolean checkChatExists(String id) {

        final boolean status[] = {false};

        mData.child("User Chats").child(fAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data:snapshot.getChildren()) {
                    if(data.getKey().equals(id)){
                        status[0] = true;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return status[0];
    }

    private long getPublicKey(long primitive, long prime,String uid1, String uid2) {

        SharedPreferences sharedPreferences = getSharedPreferences("Private Keys", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<PrivateKey>>() {}.getType();
        arrSecretKeys = gson.fromJson(json, type);
        if (arrSecretKeys == null) {
            arrSecretKeys = new ArrayList<>();
        }

        boolean keyStatus = false;
        for(PrivateKey key : arrSecretKeys){
            Log.d(TAG,"Shared secret keys array : "+key);
            if(key.getUid1().equals(uid1) && key.getUid2().equals(uid2)){
                privateKey = key.getSecret();
                keyStatus = true;
                break;
            }
        }
        if(!(keyStatus)){
            Random random = new Random();

            privateKey = random.nextInt(20);

            while(privateKey <=1){
                privateKey = random.nextInt(20) + 1;
            }

            Log.d(TAG,"Private Key : "+privateKey);
            //Log.d(TAG,"Power : "+Math.pow(primitive,secretKey));
            //Log.d(TAG,"PublicKey: "+(pow(primitive,secretKey)) % prime);

            PrivateKey sk = new PrivateKey(uid1,uid2,privateKey);
            arrSecretKeys.add(sk);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            json = gson.toJson(arrSecretKeys);
            editor.putString("task list", json);
            editor.apply();

        }

        /*
        if(uid1.equals("ikbx5ixQcmXVa0xEyoa6xOoRb4X2")){
            privateKey = 4;
        }
        else{
            privateKey = 3;
        }
        */

        Log.d(TAG,"Private Key : "+privateKey);


        return ((pow(primitive,privateKey)) % prime);

    }

    private long getPrimitiveRoot(long prime) {

        /*
        Random random = new Random();

        int n = random.nextInt();
        Log.d(TAG,"Num : "+n);
        while(n <= 1) {
            n = random.nextInt(prime) + 1;
        }

        Log.d(TAG,"Num : "+n);
        while(!(gcd(n, prime) == 1)){
            n = random.nextInt(prime)+1;
            Log.d(TAG,"Num : "+n);
            while(n <= 1) {
                Log.d(TAG,"Num : "+n);
                n = random.nextInt(prime)+1;
            }
        }*/
        return 9;
    }

    private long getPrimeNumber() {

        /*
        int num;
        Random rand = new Random();
        num = rand.nextInt(100)+1;

        Log.d(TAG,"Number : "+num);

        while(num <= 1 ){
            num = rand.nextInt(100) + 1;
        }

        Log.d(TAG,"Number : "+num);
        while (!isPrime(num)) {
            num = rand.nextInt(100) + 1;
            while(num <= 1 ){
                num = rand.nextInt(100) + 1;
            }
        }*/

        return 23;
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch(item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            case R.id.viewPendingReq:
                startActivity(new Intent(getApplicationContext(),PendingRequest.class));
                break;
        }

        return true;
    }
}