package com.application.colloquium.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.application.colloquium.PendingRequestAdapter;
import com.application.colloquium.R;
import com.application.colloquium.model.PrivateKey;
import com.application.colloquium.model.PublicKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

import static com.google.common.math.LongMath.pow;

public class PendingRequest extends AppCompatActivity implements PendingRequestAdapter.customButtonListener {

    private static final String TAG = "PendingRequest" ;

    private ListView listView;
    private PendingRequestAdapter pendingRequestAdapter;
    private ArrayList<String> requestNames = new ArrayList<>();
    private ArrayList<String> requestPersonID = new ArrayList<>();
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference fData;

    private long prime;
    private long primitive;
    private int privateKey;
    private long senderPublicKey,receiverPublicKey,secretKey;
    private ArrayList<PrivateKey> arrSecretKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_request);

        listView = findViewById(R.id.lvPendingReq);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fData = FirebaseDatabase.getInstance().getReference();

        getPendingRequests();
    }

    private void getPendingRequests() {

        pendingRequestAdapter = new PendingRequestAdapter(this,requestNames);
        pendingRequestAdapter.setCustomButtonListener(PendingRequest.this);
        listView.setAdapter(pendingRequestAdapter);
        Log.d(TAG,""+listView.getAdapter().getCount());
        Log.d(TAG,"getPendingRequests: called..");

        fData.child("Request Status").child(fAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.d(TAG,"Pending Requests: called..");
                for(DataSnapshot data : snapshot.getChildren()){
                    String uid = data.getKey();
                    Log.d(TAG,"UID : "+uid);
                    fStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG,"Pending Requests: Users called..");
                            for(QueryDocumentSnapshot data : task.getResult()){
                                if(data.getId().equals(uid)){
                                    requestNames.add(data.getString("name"));
                                    requestPersonID.add(data.getId());
                                }
                            }
                            //Log.d(TAG,"Request Name : "+requestNames+" Request ID : "+requestPersonID);
                            pendingRequestAdapter.notifyDataSetChanged();
                            //Log.d(TAG,""+listView.getAdapter().getCount());
                        }
                    });
                }

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

    @Override
    public void onButtonClickListener(int position, String value) {

            setPrivatePublicKeys(position);
            clearPendingStatus(position);
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }

    private void clearPendingStatus(int position) {

        fData.child("Request Status").child(fAuth.getCurrentUser().getUid()).child(requestPersonID.get(position)).removeValue();
        fData.child("Request Status").child(requestPersonID.get(position)).child(fAuth.getCurrentUser().getUid()).removeValue();
    }

    private long getPrimitiveNumber() {
        return 9;
    }

    private long getPrimeNumber() {
        return 23;
    }

    private void setPrivatePublicKeys(int position) {

        String uid1 = fAuth.getCurrentUser().getUid();
        String uid2 = requestPersonID.get(position);
        prime = getPrimeNumber();
        primitive = getPrimitiveNumber();

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




        senderPublicKey = ((pow(primitive,privateKey)) % prime);
        PublicKey pp = new PublicKey(prime, primitive, senderPublicKey);
        fData.child("User Chats").child(uid1).child(uid2).child("Variables").setValue(pp).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Log.d(TAG,"Public Key successfully set ! for "+uid2);
            }
        });

    }

}