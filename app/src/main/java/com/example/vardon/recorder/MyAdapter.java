package com.example.vardon.recorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends ArrayAdapter<Recording> {
    private int resourceId;

    public MyAdapter(Context context, int textViewResourceId, List<Recording> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Recording recording = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

        TextView name = (TextView) view.findViewById(R.id.name_list);
        name.setText(recording.getName());
        TextView time = (TextView) view.findViewById(R.id.time_list);
        time.setText(recording.getTime());
        TextView date = (TextView) view.findViewById(R.id.date_list);
        date.setText(recording.getDate());
        return view;
    }
}
