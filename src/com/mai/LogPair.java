package com.mai;

public class LogPair {
    private Log queryLog, resultLog;

    public LogPair(Log queryLog, Log resultLog) {
        this.queryLog = queryLog;
        this.resultLog = resultLog;
    }

    public Log getQueryLog() {
        return queryLog;
    }

    public Log getResultLog() {
        return resultLog;
    }

    public long getGap() {
        return resultLog.getDate().getTime() - queryLog.getDate().getTime();
    }

}
