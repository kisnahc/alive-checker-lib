package org.attoresearch.alivechecker.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.attoresearch.alivechecker.HostInfoMonitor;
import org.attoresearch.alivechecker.AliveStatus;
import org.attoresearch.alivechecker.HostInfo;
import org.attoresearch.alivechecker.HostInfoStore;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HostInfoMonitorImpl implements HostInfoMonitor {

    private final HostInfoStore hostInfoStore = HostInfoStoreImpl.getInstance();
    private ExecutorService executorService;
    private ForkJoinPool forkJoinTask;
    private ScheduledExecutorService scheduledExecutor;

    private static class InstanceHolder {
        private static final HostInfoMonitorImpl INSTANCE = new HostInfoMonitorImpl();
    }

    public static HostInfoMonitorImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void start(int threadCount, int intervalMs) {
        if (scheduledExecutor != null) {
            stopNow();
        }
        createScheduler(threadCount, intervalMs);
    }

    @Override
    public void stopNow() {
        if (scheduledExecutor != null) {
            stopNowScheduler();
            log.info("Monitoring has Stopped");
        } else {
            throw new IllegalStateException("There are no monitors running.");
        }
    }

    @Override
    public void stop() {
        if (scheduledExecutor != null) {
            stopScheduler();
        } else {
            throw new IllegalStateException("There are no monitors running.");
        }
    }

    @Override
    public int getThreadCount() {
        return forkJoinTask.getPoolSize();
    }

    @Override
    public boolean isRunning() {
        return !scheduledExecutor.isTerminated();
    }

    /**
     * 스케줄러 스레드, 상태 체크 스레드 생성 메서드.
     * 수용 가능한 Dead Host 설정.
     *
     * @param intervalMs
     */
    private void createScheduler(int threadCount, int intervalMs) {
        forkJoinTask = new ForkJoinPool(threadCount);
        executorService = Executors.newFixedThreadPool(threadCount);
        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleWithFixedDelay(new ScheduleRunnable(), 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 스케줄러의 실행 커맨드.
     */
    private void getScheduleCommand() {
        List<HostInfo> hostInfos = hostInfoStore.getAll();
        List<HostInfo> deadList = hostInfoStore.getDeadList();

        List<HostInfo> collect = hostInfos.stream()
                .filter(hostInfo -> hostInfo.getAliveStatus().equals(AliveStatus.NONE) ||
                        hostInfo.getAliveStatus().equals(AliveStatus.ALIVE))
                .collect(Collectors.toList());

        forkJoinTask.submit(() -> collect.parallelStream().forEach(this::aliveCheckTask));

        if (!deadList.isEmpty()) {
            for (HostInfo hostInfo : deadList) {
                executorService.submit(() -> aliveCheckTask(hostInfo));
            }
        }
    }

    /**
     * 상태 체크 쓰레드 할당 메서드.
     *
     * @param hostInfo
     */
    private void aliveCheckTask(HostInfo hostInfo) {
        try {
            boolean reachable = InetAddress.getByName(hostInfo.getHostName()).isReachable(2000);
            if (reachable) {
                updateAliveStatus(hostInfo, AliveStatus.ALIVE);
            } else {
                updateAliveStatus(hostInfo, AliveStatus.DEAD);
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    /**
     * 호스트 상태 업데이트 메세드.
     *
     * @param hostInfo
     * @param status
     */
    private void updateAliveStatus(HostInfo hostInfo, AliveStatus status) {
        hostInfo.setAliveStatus(status);
        hostInfoStore.update(hostInfo);
    }

    /**
     * 스케줄러 즉시 종료 메서드.
     */
    private void stopNowScheduler() {
        executorService.shutdownNow();
        scheduledExecutor.shutdownNow();
        forkJoinTask.shutdownNow();
        log.info("Monitoring has Stopped");
    }

    /**
     * 스케줄러 종료 메서드. (제출된 모든 작업이 완료 후)
     */
    private void stopScheduler() {
        executorService.shutdown();
        scheduledExecutor.shutdown();
        forkJoinTask.shutdown();
        log.info("Monitoring is stopped.");
    }

    private class ScheduleRunnable implements Runnable {

        private ScheduleRunnable() {
        }

        @Override
        public void run() {
            try {
                getScheduleCommand();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
