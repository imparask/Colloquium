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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.application.colloquium.R;
import com.application.colloquium.UserClient;
import com.application.colloquium.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "Login" ;

    private EditText etPhoneNum,etCode,etFullname;
    private ProgressBar pgbLoginActivity;
    private Button btnVerify,btnLogin;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String verificationID;
    private String phoneNumber;
    private String fullname;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etFullname = findViewById(R.id.et_fullname);
        etPhoneNum = findViewById(R.id.et_phoneno);
        etCode = findViewById(R.id.et_verifycode);
        btnLogin = findViewById(R.id.bt_login);
        btnVerify = findViewById(R.id.bt_verify);
        pgbLoginActivity = findViewById(R.id.pgb_login);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        firebaseLogin();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pgbLoginActivity.setVisibility(View.VISIBLE);

                if(TextUtils.isEmpty(etFullname.toString())){
                    etFullname.setError("Please enter fullname !");
                    return;
                }

                if(TextUtils.isEmpty(etPhoneNum.toString())){
                    etPhoneNum.setError("Enter Valid Phone Number");
                    return;
                }

                fullname = etFullname.getText().toString();
                phoneNumber = "+91"+etPhoneNum.getText().toString();

                sendVerificationCode();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pgbLoginActivity.setVisibility(View.VISIBLE);
                String code = etCode.getText().toString();
                if(TextUtils.isEmpty(code)){
                    etCode.setError("Enter Valid Code");
                    return;
                }
                verifyCode(code);
            }
        });

    }


    private void firebaseLogin(){

        Log.d(TAG,"FirebaseLogin called:..");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationID = s;
                pgbLoginActivity.setVisibility(View.INVISIBLE);
                btnVerify.setClickable(false);
                etCode.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Verification Code Sent Sucessfully..!!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                pgbLoginActivity.setVisibility(View.VISIBLE);
                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    etCode.setText(code);
                    verifyCode(code);
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                pgbLoginActivity.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private void sendVerificationCode(){

        Log.d(TAG,"sendverficationcode called:..");

        //PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneno,60, TimeUnit.SECONDS,LoginActivity.this,mCallbacks);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(fAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code){
        Log.d(TAG,"verifycode called:..");
        PhoneAuthCredential authCredential = PhoneAuthProvider.getCredential(verificationID,code);
        signInWithCredentials(authCredential);
    }

    private void signInWithCredentials(PhoneAuthCredential authCredential) {
        Log.d(TAG,"signInWithCred called:..");
        fAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Login Successful !",Toast.LENGTH_SHORT).show();
                    pgbLoginActivity.setVisibility(View.INVISIBLE);
                    User user = new User(phoneNumber,fullname);
                    fStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    boolean phoneNumberExists = false;
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        String phone = documentSnapshot.getString("phoneNo");
                                        if(phone.equals(phoneNumber)){
                                            //Toast.makeText(getApplicationContext(),"User already exists..!",Toast.LENGTH_SHORT).show();
                                            phoneNumberExists = true;
                                            break;
                                        }
                                    }
                                    if(!phoneNumberExists){
                                        fStore.collection("Users").document(fAuth.getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if(task.isSuccessful()){
                                                    Log.d(TAG,"User Profile Created for "+fullname);
                                                    Toast.makeText(getApplicationContext(),"User profile created !",Toast.LENGTH_SHORT).show();
                                                    //((UserClient)getApplicationContext()).setUser(user);
                                                }
                                                else{
                                                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                }
                            }
                        }
                    });

                }
                else{
                    pgbLoginActivity.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}