package com.example.vardon.recorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDBHelper extends SQLiteOpenHelper {

        private static final String DBName = "recorder.db";
        private static final String DATA = "audio";

        private static final String CREATE_DATA_TABLE
                = "create table " + DATA + "(id integer primary key autoincrement, name text, time text, date text)";

        private static final String UPDATE_DATA_TABLE
                = "alter table " + DATA + " add height integer";

        public SimpleDBHelper(Context context, int version) {
            super(context, DBName, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DATA_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {


            sqLiteDatabase.execSQL(UPDATE_DATA_TABLE);

            throw new IllegalStateException("unknown oldVersion " + i);
        }

}
