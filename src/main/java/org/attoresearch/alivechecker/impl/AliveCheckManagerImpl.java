package org.attoresearch.alivechecker.impl;

import lombok.NoArgsConstructor;
import org.attoresearch.alivechecker.HostInfoMonitor;
import org.attoresearch.alivechecker.AliveCheckManager;
import org.attoresearch.alivechecker.HostInfo;
import org.attoresearch.alivechecker.HostInfoStore;

@NoArgsConstructor
public class AliveCheckManagerImpl implements AliveCheckManager {

    @Override
    public HostInfoMonitor hostInfoMonitor() {
        return HostInfoMonitorImpl.getInstance();
    }

    @Override
    public HostInfoStore hostInfoStore() {
        return HostInfoStoreImpl.getInstance();
    }

    @Override
    public HostInfo createHostInfo(String hostName) {
        return new HostInfo(hostName);
    }

}
