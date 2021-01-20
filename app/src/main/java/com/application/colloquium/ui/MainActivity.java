package com.application.colloquium.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.colloquium.R;
import com.application.colloquium.UserClient;
import com.application.colloquium.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity" ;

    private ListView lstChats;
    private ProgressBar pgbMain;
    private TextView tvNoRecentChats;
    private FloatingActionButton fabNewChat;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference fData;
    private User user;
    private List<String> userChatids = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        lstChats = findViewById(R.id.lv_recentchats);
        fabNewChat = findViewById(R.id.fab_addchat);
        pgbMain = findViewById(R.id.pgb_Main);
        tvNoRecentChats = findViewById(R.id.tv_noChats);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fData = FirebaseDatabase.getInstance().getReference();

        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),NewChatActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        pgbMain.setVisibility(View.VISIBLE);
        tvNoRecentChats.setVisibility(View.INVISIBLE);
        getUserDetails();
    }

    private void getUserDetails() {

        DocumentReference userInfoRef = fStore.collection("Users").document(fAuth.getCurrentUser().getUid());
        userInfoRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete : sucessfully got the user details");
                    user = task.getResult().toObject(User.class);
                    ((UserClient)getApplicationContext()).setUser( task.getResult().toObject(User.class));
                    fetchUserChats();
                }
            }
        });

    }

    private void fetchUserChats() {

        ArrayAdapter<String> recentChatsAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,userNames);
        lstChats.setAdapter(recentChatsAdapter);

        fData.child("User Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d(TAG,""+snapshot.getValue().toString());
                if(userChatids.size()>0){
                    userChatids.clear();
                    userNames.clear();
                }
                for(DataSnapshot data:snapshot.child(fAuth.getCurrentUser().getUid()).getChildren()){
                    String userId = data.getKey();
                    if(snapshot.child(userId).child(fAuth.getCurrentUser().getUid()).child("Variables").child("publicKey").getValue() != null){
                        userChatids.add(userId);
                    }
                }
                fStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null ) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    String id = documentSnapshot.getId();
                                    if (userChatids.contains(id)) {
                                        userNames.add(documentSnapshot.get("name").toString());
                                    }
                                }
                            }
                        }

                        recentChatsAdapter.notifyDataSetChanged();
                        pgbMain.setVisibility(View.INVISIBLE);
                        if (userNames.size() > 0) {
                            tvNoRecentChats.setVisibility(View.INVISIBLE);
                        }
                        else{
                            tvNoRecentChats.setVisibility(View.VISIBLE);

                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        lstChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                intent.putExtra("User ID", userChatids.get(i));
                startActivity(intent);

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
    public void onBackPressed() {
        //super.onBackPressed();
    }
}