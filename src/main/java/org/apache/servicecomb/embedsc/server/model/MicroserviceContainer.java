package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;

import java.util.Map;

public class MicroserviceContainer {

    private ApplicationContainer applicationContainer;

    private String appId;

    // Key: serviceName
    private Map<String, MicroserviceVersionContainer> versionsByServiceName = new ConcurrentHashMapEx<>();

    public MicroserviceContainer(ApplicationContainer applicationContainer, String appId) {
        this.applicationContainer = applicationContainer;
        this.appId = appId;
    }

    public Map<String, MicroserviceVersionContainer> getVersionsByServiceName() {
        return versionsByServiceName;
    }

    public void setVersionsByServiceName(Map<String, MicroserviceVersionContainer> versionsByServiceName) {
        this.versionsByServiceName = versionsByServiceName;
    }

    public  MicroserviceVersionContainer getOrCreateMicroserviceVersionContainer(String serviceName) {
        return versionsByServiceName.computeIfAbsent(serviceName, name -> new MicroserviceVersionContainer(applicationContainer, appId, serviceName));
    }

}


