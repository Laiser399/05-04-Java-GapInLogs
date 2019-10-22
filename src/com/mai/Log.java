package com.mai;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static int nextId = 1;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    enum Type {
        Query, Result
    }

    private Date date;
    private Type type;
    private int id;

    public Log(Date date) {
        this(date, nextId++);
    }

    public Log(Date date, int id) {
        this.date = date;
        this.type = Type.Query;
        this.id = id;
    }

    public Log(Date date, Type type, int id) {
        this.date = date;
        this.type = type;
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public Log createResultLog(long gapMs) {
        if (type != Type.Query)
            throw new RuntimeException("Called \"createResultLog\" for result log.");
        return new Log(new Date(date.getTime() + gapMs), Type.Result, id);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(dateFormat.format(date));
        result.append(" - INFO - ");
        if (type == Type.Query) {
            result.append("QUERY FOR ID = ");
        }
        else if (type == Type.Result) {
            result.append("RESULT QUERY FOR ID = ");
        }
        result.append(Integer.toString(id));
        return result.toString();
    }
}
