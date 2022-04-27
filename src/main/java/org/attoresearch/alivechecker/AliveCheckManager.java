package org.attoresearch.alivechecker;


public interface AliveCheckManager {

    HostInfoMonitor hostInfoMonitor();

    HostInfoStore hostInfoStore();

    HostInfo createHostInfo(String hostName);

}
