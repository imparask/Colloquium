package com.application.colloquium;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.application.colloquium.model.UserContacts;
import com.application.colloquium.ui.NewChatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserContactAdapter extends ArrayAdapter<UserContacts> {


    public UserContactAdapter(Context context, ArrayList<UserContacts> userContacts) {
        super(context, 0, userContacts);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        UserContacts userContact = getItem(position);


        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_contact_info, parent, false);
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.tv_Name);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tv_PhoneNum);

        tvName.setText(userContact.getName());
        tvHome.setText(userContact.getPhoneNumber());


        return convertView;
    }
}
