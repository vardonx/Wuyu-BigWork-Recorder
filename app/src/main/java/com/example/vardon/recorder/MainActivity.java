package com.example.vardon.recorder;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button record_view;
    private Button saved_view;
    SQLiteDatabase db;
    private static SimpleDBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment(new RecordFragment());
        saved_view = (Button)findViewById(R.id.saved_view);
        record_view = (Button)findViewById(R.id.record_view);

        dbHelper = new SimpleDBHelper(this, 3);
        dbHelper.getWritableDatabase();


        saved_view.setOnClickListener(this);
        record_view.setOnClickListener(this);

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_layout, fragment);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_view:
                changeBotton(saved_view, record_view);
                replaceFragment(new RecordFragment());
                break;
            case R.id.saved_view:
                changeBotton(record_view, saved_view);
                replaceFragment(new RecordingsFragment());;
                break;
        }
    }
    public void changeBotton(Button before, Button after){
        after.setBackgroundResource(R.drawable.bg);
        before.setBackgroundResource(R.drawable.btn_bg);
        after.setEnabled(false);
        before.setEnabled(true);
    }
    public static SQLiteDatabase getDB() {
        return dbHelper.getWritableDatabase();
    }
}
