package com.example.vardon.recorder;

public class Recording {
    private String name;
    private String time;
    private String date;

    public Recording(String name, String time, String date){
        this.name = name;
        this.time = time;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }
}
