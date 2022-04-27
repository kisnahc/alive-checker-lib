package org.attoresearch.alivechecker;

public interface HostInfoMonitor {

    void start(int threadCount, int intervalMs);

    boolean isRunning();

    void stopNow() throws InterruptedException;

    void stop();

    int getThreadCount();
}