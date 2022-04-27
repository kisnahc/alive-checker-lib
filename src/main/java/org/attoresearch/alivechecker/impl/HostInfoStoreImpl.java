package org.attoresearch.alivechecker.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.attoresearch.alivechecker.AliveStatus;
import org.attoresearch.alivechecker.HostInfoStore;
import org.attoresearch.alivechecker.HostInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HostInfoStoreImpl implements HostInfoStore {

    private final Map<String, AliveStatus> hostInfoMap = new ConcurrentHashMap<>();

    private static class InstanceHolder {
        private static final HostInfoStoreImpl INSTANCE = new HostInfoStoreImpl();
    }

    static HostInfoStoreImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private List<HostInfo> getHostInfos() {
        List<HostInfo> hostInfos = new ArrayList<>();

        for (Map.Entry<String, AliveStatus> entry : hostInfoMap.entrySet()) {
            HostInfo hostInfo = new HostInfo(entry.getKey(), entry.getValue());
            hostInfos.add(hostInfo);
        }
        return hostInfos;
    }

    @Override
    public List<HostInfo> getDeadList() {
        return getHostInfos().stream()
                .filter(h -> h.getAliveStatus().equals(AliveStatus.DEAD))
                .collect(Collectors.toList());
    }

    @Override
    public HostInfo get(String hostName) {
        for (Map.Entry<String, AliveStatus> entry : hostInfoMap.entrySet()) {
            if (entry.getKey().equals(hostName)) {
                return new HostInfo(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    @Override
    public List<HostInfo> getAll() {
        return getHostInfos();
    }

    @Override
    public void add(HostInfo hostInfo) {
        validHostInfo(hostInfo);
        hostInfoMap.put(hostInfo.getHostName(), hostInfo.getAliveStatus());
    }

    @Override
    public void add(List<HostInfo> hostInfos) {
        for (HostInfo hostInfo : hostInfos) {
            validHostInfo(hostInfo);
            hostInfoMap.put(hostInfo.getHostName(), hostInfo.getAliveStatus());
        }
    }

    @Override
    public void remove(HostInfo hostInfo) {
        validContainsHostInfo(hostInfo);
        hostInfoMap.remove(hostInfo.getHostName());
    }

    @Override
    public void remove(List<HostInfo> hostInfos) {
        for (HostInfo hostInfo : hostInfos) {
            validContainsHostInfo(hostInfo);
            hostInfoMap.remove(hostInfo.getHostName());
        }
    }

    @Override
    public void clear() {
        if (!hostInfoMap.isEmpty()) {
            hostInfoMap.clear();
        }
    }

    @Override
    public void update(HostInfo hostInfo) {
        hostInfoMap.replace(hostInfo.getHostName(), hostInfo.getAliveStatus());
    }

    /**
     * 중복 추가, 공백 입력 검증 메서드.
     *
     * @param hostInfo
     * @return
     */
    private void validHostInfo(HostInfo hostInfo) {
        if (hostInfoMap.containsKey(hostInfo.getHostName())) {
            throw new IllegalArgumentException("duplicate hostname");
        } else if (hostInfo.getHostName().isBlank()) {
            throw new IllegalArgumentException("can not blank hostname");
        }
    }

    /**
     * 삭제 대상 검증 메서드.
     *
     * @param hostInfo
     * @return
     */
    private void validContainsHostInfo(HostInfo hostInfo) {
        if (!hostInfoMap.containsKey(hostInfo.getHostName())) {
            throw new IllegalArgumentException("It is not included in the removal target.");
        }
    }
}
