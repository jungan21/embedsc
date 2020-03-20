package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import java.util.Map;

public class MicroserviceInstanceContainer {

    private ApplicationContainer applicationContainer;

    private String appId;

    private String serviceName;

    private String version;

    //key: instanceId
    private Map<String, ServerMicroserviceInstance> instanceByInstanceIdMap = new ConcurrentHashMapEx<>();

    public MicroserviceInstanceContainer(ApplicationContainer applicationContainer, String appId, String serviceName, String version) {
        this.applicationContainer = applicationContainer;
        this.appId = appId;
        this.serviceName = serviceName;
        this.version = version;
    }

    public Map<String, ServerMicroserviceInstance> getInstanceByInstanceIdMap() {
        return instanceByInstanceIdMap;
    }

    public void setInstanceByInstanceIdMap(Map<String, ServerMicroserviceInstance> instanceByInstanceIdMap) {
        this.instanceByInstanceIdMap = instanceByInstanceIdMap;
    }
}
