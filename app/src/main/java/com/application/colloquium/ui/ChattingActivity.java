package com.application.colloquium.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.colloquium.R;
import com.application.colloquium.UserClient;
import com.application.colloquium.model.PrivateKey;
import com.application.colloquium.model.PublicKey;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.view.View.GONE;
import static com.google.common.math.LongMath.pow;

public class ChattingActivity extends AppCompatActivity {

    private static final String TAG = "ChattingActivity" ;

    private EditText etMessage;
    private TextView tvChat,tvSecret;
    private Button btnSend;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference fData;
    private long prime,primitive;
    private int privateKey;
    private long senderPublicKey,receiverPublicKey,secretKey;
    private ArrayList<PrivateKey> arrSecretKeys;
    private User user;
    private String uid1,uid2;
    private boolean recieverPublicKeyStatus = false;
    private boolean senderPublicKeyStatus = false;
    private IvParameterSpec ivspec = new IvParameterSpec(("fedcba9876543210".getBytes()));
    private byte[] secret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        etMessage = findViewById(R.id.etContent);
        btnSend= findViewById(R.id.btnSendMessage);
        tvChat = findViewById(R.id.tvMessages);
        tvSecret = findViewById(R.id.tvSecret);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fData = FirebaseDatabase.getInstance().getReference();
        user = ((UserClient)getApplicationContext()).getUser();

        uid1= fAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        uid2 = intent.getStringExtra("User ID");
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnSend.setVisibility(View.GONE);

        fetchVariables();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        etMessage.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN)&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

    }

    private void sendMessage(){

        String m = user.getName()+" : "+etMessage.getText().toString();
        if(TextUtils.isEmpty(m)){
            etMessage.setError("Message cannot be empty");
            return;
        }

        try {

            String message = new String(encryptMessage(m));
            Log.d(TAG,"Message : "+message);
            fData.child("User Chats").child(uid1).child(uid2).push().setValue(message).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        fData.child("User Chats").child(uid2).child(uid1).push().setValue(message).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                etMessage.setText("");
                                Toast.makeText( getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Log.d(TAG, task.getException().getMessage());
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private byte[] encryptMessage(String m) throws Exception {

        Log.d(TAG,"Secret Encrypt : "+secret.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret,"AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec,ivspec);
        byte[] cipherText = cipher.doFinal(m.getBytes(StandardCharsets.UTF_8));
        return Base64.encode(cipherText,Base64.NO_WRAP);

    }

    private void fetchVariables() {

        fData.child("User Chats").child(uid1).child(uid2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                prime = Long.parseLong(snapshot.child("Variables").child("prime").getValue().toString());
                primitive = Long.parseLong(snapshot.child("Variables").child("primitive").getValue().toString());
                senderPublicKey = Long.parseLong(snapshot.child("Variables").child("publicKey").getValue().toString());
                Log.d(TAG,"Prime : "+prime+" Primitive : "+primitive+" Sender Public Key : "+senderPublicKey);
                if(senderPublicKey != 0){
                    senderPublicKeyStatus = true;
                }

                fData.child("User Chats").child(uid2).child(uid1).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        receiverPublicKey = Integer.parseInt(snapshot.child("Variables").child("publicKey").getValue().toString());
                        if(receiverPublicKey != 0){
                            recieverPublicKeyStatus = true;
                        }
                        if(senderPublicKeyStatus && recieverPublicKeyStatus){
                            setPrivateKey();
                            btnSend.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if(senderPublicKeyStatus && recieverPublicKeyStatus){
                    setPrivateKey();
                    btnSend.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchChat() {

        fData.child("User Chats").child(uid1).child(uid2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvChat.setText("");
                for(DataSnapshot data : snapshot.getChildren()){
                    if(!(data.getKey().equals("Variables"))){
                        try {
                            Log.d(TAG,"Message Encrypted : "+data.getValue().toString());
                            String message = decryptMessage(data.getValue().toString());
                            Log.d(TAG,"Message Decrypted : "+message);
                            tvChat.append(message+"\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String decryptMessage(String m) throws Exception {

        Log.d(TAG,"Message Decryption : "+m);

        byte[] data = Base64.decode(m, Base64.NO_WRAP);

        Log.d(TAG,"Secret Decrypt : "+secret.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret,"AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
        byte[] decryptedText = cipher.doFinal(data);
        return new String(decryptedText,StandardCharsets.UTF_8);
    }


    private void generateSecretKey() {

        Log.d(TAG,"Receiver Public Key : "+receiverPublicKey);
        Log.d(TAG,"Prime : "+prime);
        Log.d(TAG,"Private Key : "+privateKey);
        Log.d(TAG,"Power : "+(pow(receiverPublicKey,privateKey)));

        secretKey = (pow(receiverPublicKey,privateKey)) % prime;
        tvSecret.setText(""+secretKey);

        secret =  ByteBuffer.allocate(16).putLong(secretKey).array();

        Log.d(TAG,""+secret);

        fetchChat();
        Log.d(TAG,"Secret Key : "+secretKey);

    }


    private void setPrivateKey() {

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
        generateSecretKey();
    }
}