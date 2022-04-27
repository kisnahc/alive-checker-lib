package org.attoresearch.alivechecker;

import java.util.List;

public interface HostInfoStore {

    HostInfo get(String hostName);

    void add(HostInfo hostInfo);

    void add(List<HostInfo> hostInfo);

    void remove(HostInfo hostInfo);

    void remove(List<HostInfo> hostInfo);

    void clear();

    List<HostInfo> getAll();

    List<HostInfo>getDeadList();

    void update(HostInfo hostInfo);
}
