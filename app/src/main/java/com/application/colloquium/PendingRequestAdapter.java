package com.application.colloquium;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class PendingRequestAdapter extends ArrayAdapter<String> {

    customButtonListener customListener;

    public interface customButtonListener {
        public void onButtonClickListener(int position,String value);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }


    public PendingRequestAdapter(@NonNull Context context, ArrayList<String> requests) {
        super(context, 0,requests);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pending_request_view,parent, false);

            TextView text = (TextView) convertView.findViewById(R.id.tvRequesterName);
            Button button = (Button) convertView.findViewById(R.id.btnApprove);


            text.setText(getItem(position));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customListener != null) {
                        customListener.onButtonClickListener(position,getItem(position));
                    }

                }
            });

        }

        return convertView;
    }
}
