package com.example.vardon.recorder;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RecordingsFragment extends Fragment{
    private ListView listView;
    private List<Recording> recordingList = new ArrayList<>();
    private Button btn;
    private Recording selected;
    private String fileName;
    private Boolean isPlaying;
    private MediaPlayer mediaPlayer;
    private boolean isSeekBarChanging;
    private SeekBar seekBar;
    private Timer timer;
    private Chronometer chronometer;
    private long pauseTime;
    private Boolean isReplay = false;
    SQLiteDatabase db;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.recordings_fragment, container, false);

        listView = (ListView) view.findViewById(R.id.list_view);
        create_list();
        show_list();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selected = recordingList.get(position);




                showPlayDialog();


                play();

            }
        });

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "分享");
                menu.add(0, 1, 0, "重命名");
                menu.add(0, 2, 0, "删除");

            }
        });


        return view;
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int position = info.position;
        String fileName = recordingList.get(position).getName();


        switch (item.getItemId()) {
            case 0:
                // 分享操作




                break;

            case 1:
                // 重命名操作
                showSetNameDialog(fileName);

                break;

            case 2:
                // 删除操作
                deleteItem(fileName);
                break;

            default:
                break;
        }

        return super.onContextItemSelected(item);

    }

    private void showPlayDialog(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.play_dialog,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(view).create();

        TextView nameText = (TextView) view.findViewById(R.id.name_text);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer_play);
        TextView timeText = (TextView) view.findViewById(R.id.file_time_text);
        btn = (Button) view.findViewById(R.id.play_btn);
        nameText.setText(selected.getName());
        timeText.setText(selected.getTime());
        isPlaying = true;
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mediaPlayer.reset();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isReplay == true){
                    btn.setBackgroundResource(R.drawable.pause);
                    isReplay = false;
                    isPlaying = true;
                    mediaPlayer.start();
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                }else {

                    if (isPlaying == true) {
                        btn.setBackgroundResource(R.drawable.play);
                        isPlaying = false;
                        mediaPlayer.pause();//暂停状态
                        chronometer.stop();
                        pauseTime = SystemClock.elapsedRealtime();
                        timer.purge();//移除所有任务;

                    } else {
                        btn.setBackgroundResource(R.drawable.pause);
                        isPlaying = true;
                        mediaPlayer.start();
                        chronometer.setBase(chronometer.getBase() + (SystemClock.elapsedRealtime() - pauseTime));
                        chronometer.start();

                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new MySeekBar());


    }

    public void deleteItem(final String name){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("确定删除?");
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                String whereclause = "name = ?" ;

                db.delete("audio", whereclause, new String[] {name});
                File file = new File(getContext().getFilesDir().getAbsolutePath(),name);
                file.delete();
                show_list();
                create_list();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    public void show_list(){
        final MyAdapter myAdapter = new MyAdapter(getActivity(),
                R.layout.list_view_item, recordingList);
        listView.setAdapter(myAdapter);
    }

    public void play(){
        try{
            fileName = selected.getName();
            File file = new File(getContext().getFilesDir().getAbsolutePath(),fileName );
            FileInputStream fis = new FileInputStream(file);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();


            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!isSeekBarChanging){
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        if (seekBar.getProgress() == mediaPlayer.getDuration()){
                            btn.setBackgroundResource(R.drawable.play);
                            chronometer.stop();

                            isReplay = true;
                        }
                    }
                }
            },0,50);

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void create_list() {
        recordingList.clear();
        MainActivity activity = (MainActivity) getActivity();
        db  = activity.getDB();
        Cursor cursor = db.query("audio", null, null, null,
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                recordingList.add(new Recording(name,time,date));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        //滚动时暂停后台定时器
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
            chronometer.stop();

        }
        //滑动结束后新设置值
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(seekBar.getProgress());

            chronometer.setBase(SystemClock.elapsedRealtime()-seekBar.getProgress());
            chronometer.start();
            isPlaying = true;
            mediaPlayer.start();
            btn.setBackgroundResource(R.drawable.pause);

        }
    }
            //修改名字Dialog
    public void showSetNameDialog(final String OldFileName){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.rename_file_dialog,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(view).create();

        final EditText reNameText = (EditText)view.findViewById(R.id.rename_text);
        Button btn = (Button)view.findViewById(R.id.rename_btn);

        reNameText.setHint(OldFileName.replace(".mp3",""));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                reName(reNameText, OldFileName);
                dialog.dismiss();

            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                reName(reNameText, OldFileName);
            }
        });


        dialog.show();
    }
    //文件重命名
    public void reName(EditText reNameText, String oldFileName){
        String newFileName = reNameText.getText().toString();
        if(newFileName.length() !=0){

            newFileName = newFileName+".mp3";
            File oleFile = new File(getContext().getFilesDir().getAbsolutePath(),oldFileName);
            File newFile = new File(getContext().getFilesDir().getAbsolutePath(),newFileName);
            //执行重命名
            oleFile.renameTo(newFile);

        }
        else {
            newFileName = reNameText.getHint().toString()+".mp3";

        }

        //修改数据库
        updateDB(oldFileName, newFileName);

        show_list();
        create_list();

    }
            //修改数据库
    public void updateDB(String oldName, String newName){
        ContentValues values = new ContentValues();

        values.put("name", newName);

        db.update("audio", values, "name = ?", new String[] { oldName });
    }

}
