package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import java.util.Map;

public class MicroserviceContainer {

    private ApplicationContainer applicationContainer;

    private String appId;

    // Key: serviceName
    private Map<String, MicroserviceVersionContainer> services = new ConcurrentHashMapEx<>();

    public Map<String, MicroserviceVersionContainer> getServices() {
        return services;
    }

    public void setServices(Map<String, MicroserviceVersionContainer> services) {
        this.services = services;
    }

    public MicroserviceContainer(ApplicationContainer applicationContainer, String appId) {
        this.applicationContainer = applicationContainer;
        this.appId = appId;
    }

    public MicroserviceVersionContainer getOrCreateMicroserviceVersionContainer(String serviceName) {
        return services.computeIfAbsent(serviceName, name -> new MicroserviceVersionContainer(applicationContainer, appId, serviceName));
    }

}


