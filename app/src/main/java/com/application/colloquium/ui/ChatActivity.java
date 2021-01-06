package com.application.colloquium.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.colloquium.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity" ;

    private EditText etMessage;
    private TextView tvShowChat;
    private Button btnSend;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference mData;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etMessage = findViewById(R.id.et_message);
        btnSend= findViewById(R.id.bt_send);
        tvShowChat = findViewById(R.id.tv_chat);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        uid = intent.getStringExtra("User ID");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString();
                if(TextUtils.isEmpty(message)){
                    etMessage.setError("Message cannot be empty");
                    return;
                }

                mData.child("User Chats").child(fAuth.getCurrentUser().getUid()).child(uid).push().setValue(message).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Log.d(TAG,task.getException().getMessage());
                            Toast.makeText(ChatActivity.this,task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}