package com.application.colloquium.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.colloquium.R;
import com.application.colloquium.UserClient;
import com.application.colloquium.model.PrivateKey;
import com.application.colloquium.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity" ;

    private EditText etMessage;
    private TextView tvShowChat;
    private Button btnSend;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference fData;
    private ArrayList<PrivateKey> arrSecretKeys;
    private User user;
    private String uid;
    private long prime,primitive, publicKey1,publicKey2,secretKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etMessage = findViewById(R.id.et_message);
        btnSend= findViewById(R.id.bt_send);
        tvShowChat = findViewById(R.id.tv_chat);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fData = FirebaseDatabase.getInstance().getReference();
        user = ((UserClient)getApplicationContext()).getUser();

        Intent intent = getIntent();
        uid = intent.getStringExtra("User ID");

    }

    @Override
    protected void onResume() {
        super.onResume();

        prime = -1;
        primitive = -1;
        publicKey1 = -1;
        publicKey2 = -1;


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String m = user.getName()+" : "+etMessage.getText().toString();
                if(TextUtils.isEmpty(m)){
                    etMessage.setError("Message cannot be empty");
                    return;
                }

                String message = encryptMessage(m);
                if(!message.equals("0")) {
                    fData.child("User Chats").child(fAuth.getCurrentUser().getUid()).child(uid).push().setValue(message).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                fData.child("User Chats").child(uid).child(fAuth.getCurrentUser().getUid()).push().setValue(message).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Log.d(TAG, task.getException().getMessage());
                                Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

    }

    private void loadSecretKeys() {
        /*
        SharedPreferences sharedPreferences = getSharedPreferences("Secret Keys", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<Secret>>() {}.getType();
        arrSecretKeys = gson.fromJson(json, type);
        if (arrSecretKeys == null) {
            arrSecretKeys = new ArrayList<>();
        }

        for(Secret sk : arrSecretKeys){
            if(sk.getUid1().equals(uid) && sk.getUid2().equals(fAuth.getCurrentUser().getUid()){
                secretKey = sk.getSecret();
                break;
            }
        }

        Log.d(TAG,"Secret Key : "+secretKey);

         */
    }

    private String encryptMessage(String m) {

        /*
        if(!(prime == -1 || primitive == -1 || publicKey1 == -1 || publicKey2 == -1)){

        }
        else{
            Toast.makeText(getApplicationContext(),"Cannot Encrypt message !!",Toast.LENGTH_LONG).show();
            return "0";
        }

         */

        return m;
    }


    private String decryptMessage(String m) {
        return m;
    }


    private void fetchChat() {

        fData.child("User Chats").child(fAuth.getCurrentUser().getUid()).child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvShowChat.setText("");
                for(DataSnapshot data : snapshot.getChildren()){
                    if(!(data.getKey().equals("Variables"))){
                        Log.d(TAG,"Message Encrypted : "+data.getValue().toString());
                        String message = decryptMessage(data.getValue().toString());
                        Log.d(TAG,"Message Decrypted : "+message);
                        tvShowChat.append(message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void fetchVariables() {

        fData.child("User Chats").child(fAuth.getCurrentUser().getUid()).child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                prime = Integer.parseInt(snapshot.child("Variables").child("prime").getValue().toString());
                primitive = Integer.parseInt(snapshot.child("Variables").child("primitive").getValue().toString());
                publicKey1 = Integer.parseInt(snapshot.child("Variables").child("publicKey").getValue().toString());

                fData.child("User Chats").child(uid).child(fAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        publicKey2 = Integer.parseInt(snapshot.child("Variables").child("publicKey").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Log.d(TAG,"Prime : "+prime+" Primitive : "+primitive+" Public Key1 : "+publicKey1+" Public Key2 : "+publicKey2);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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