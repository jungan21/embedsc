package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import java.util.Map;

public class ApplicationContainer {
;
    // Map<appId, Map<serviceName, Map<version, Microservice>>>
    //private Map<String, Map<String, Map<String, Microservice>>> appContainer = new ConcurrentHashMap<>();

    // key:appId
    private Map<String, MicroserviceContainer> apps = new ConcurrentHashMapEx<>();

    public Map<String, MicroserviceContainer> getApps() {
        return apps;
    }

    public void setApps(Map<String, MicroserviceContainer> apps) {
        this.apps = apps;
    }

    public MicroserviceContainer getOrCreateMicroserviceContainer(String appId) {
        return apps.computeIfAbsent(appId, id -> new MicroserviceContainer(this, appId));
    }

    public MicroserviceVersionContainer getOrCreateMicroserviceVersions(String appId, String serviceName) {
        MicroserviceContainer microserviceContainer = getOrCreateMicroserviceContainer(appId);
        return microserviceContainer.getOrCreateMicroserviceVersionContainer(serviceName);
    }

}
