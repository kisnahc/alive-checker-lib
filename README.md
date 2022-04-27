# *AliveChecker*
***
#### *멀티스레드 기반의 논블로킹을 이용한 호스트 Alive 상태 체크 모니터링 기능 제공.*

# *Usage*
***
### Table of Contents
- [1. Map to HostInfo From Data](#1-map-to-hostinfo-from-data)
- [2. Add to HostInfoStore](#2-add-to-hostinfostore)
- [3. Get by HostInfoStore](#3-get-by-hostinfostore)
- [4. Delete in HostInfoStore](#4-delete-in-hostinfostore)
- [5. Start Monitoring](#5-start-monitoring)
- [6. Monitoring Running Check](#6-monitoring-running-check)
- [7. Stop Monitoring](#7-stop-monitoring)

## 1. Map to HostInfo From Data
***
- 호스트 상태 체크 모니터링에 추가 또는 삭제하고자 하는 데이터를 아래 모델로 정의.
~~~java
public class HostInfo {

        private String hostName;
        private AliveStatus aliveStatus = AliveStatus.NONE;
    
        public HostInfo(String hostName) {
            this.hostName = hostName;
        }
}
~~~
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    AliveCheckManager aliveCheckManager = new AliveCheckManagerImpl();

    HostInfo hostInfo = aliveCheckManager.createHostInfo("hostName");
~~~

## 2. Add to HostInfoStore
***
- 상태 체크 모니터링 목록에 추가.
- 중복된 데이터를 추가할 경우, 기존 데이터 유지.
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    AliveCheckManager aliveCheckManager = new AliveCheckManagerImpl();

    // 단 한 건의 호스트 추가.
    aliveCheckManager.hostInfoStore().add(hostInfo);
            
    // n개의 호스트를 추가.
    List<HostInfo> hostInfos = new ArrayList<>();

    hostNames.forEach(hostName -> {
        HostInfo hostInfo = new HostInfo(hostName);
        hostInfos.add(hostInfo);
    });
    
    aliveCheckManager.hostInfoStore().add(hostInfos);
~~~

## 3. Get by HostInfoStore
***
- 모니터링 목록에서 호스트 조회.
- 목록에 존재하지 않는 호스트 조회 시 null 반환.
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    AliveCheckManager aliveCheckManager = new AliveCheckManagerImpl(); 

    // 목록 전체 조회.
    List<HostInfo> hostInfos = aliveCheckManager.hostInfoStore.getAll();

    // 목록 단건 조회.
    HostInfo hostInfo = aliveCheckManager.hostInfoStore.get("hostName");
    
    // 호스트의 alive 상태 조회. (아직 조회가 되지 않은 상태라면, NONE 반환)
    hostInfo.getAliveStatus();
~~~

## 4. Remove in HostInfoStore
***
- 상태 체크 모니터링 목록에서 삭제.
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    AliveCheckManager aliveCheckManager = new AliveCheckManagerImpl();
    
    // 단 한 건의 호스트를 삭제. 
    aliveCheckManager.hostInfoStore().remove(hostInfo);

    // n개의 호스트를 삭제.
    aliveCheckManager.hostInfoStore().remove(hostInfos);

    // 목록 전체 삭제
    aliveCheckManager.hostInfoStore().clear();
~~~

## 5. Start Monitoring
***
- 파라미터로 모니터링 실행의 주기와 스레드 생성 개수 설정.
- HostInfoStore 가 비어 있을 경우 실행 불가능.
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    AliveCheckManager aliveCheckManager = new AliveCheckManagerImpl();
    
    int thread = 3;
    
    int intervalMs = 3000;
    
    aliveCheckManager.hostInfoMonitor.start(thread, intervalMs)
~~~

- start() 중복 실행 불가능. (중복 호출 시 마지막에 호출 된 메서드 실행)
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    aliveCheckManager.hostInfoMonitor.start(2, 1000); // 종료.
    aliveCheckManager.hostInfoMonitor.start(3, 3000); // 실행.
~~~

## 6. Monitoring Running Check
***
- 현재 모니터링이 실행 중인지 확인.
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    // 실행중이면 true, 아니면 false
    boolean running = aliveCheckManager.hostInfoMonitor().isRunning();
~~~

## 7. Stop Monitoring
***
- 실행중인 모니터링 종료.
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;

    // 실해중인 쓰레드 즉시 종료.
    aliveCheckManager.hostInfoMonitor.stopNow();
    
    // 기존에 실행중이던 쓰레드 작업이 모두 끝난 뒤 종료.
    aliveCheckManager.hostInfoMonitor.stop();
~~~
- 예외적으로 stop() 호출 직후 바로 start() 호출 시 stopNow() 와 동일하게 즉시 종료됨.
~~~java
import org.attoresearch.alivechecker.AliveCheckManagerImpl;
    
    aliveCheckManager.hostInfoMonitor.stop();
    aliveCheckManager.hostInfoMonitor.start(); // 쓰레드의 남은 작업들은 즉시 종료 된 후 실행. 
~~~
