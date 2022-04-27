package org.attoresearch.alivechecker;

import lombok.*;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class HostInfo {

    private String hostName;
    private AliveStatus aliveStatus = AliveStatus.NONE;

    public HostInfo(String hostName) {
        this.hostName = hostName;
    }

}
