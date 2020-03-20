package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import java.util.Map;

public class MicroserviceVersionContainer {

    private ApplicationContainer applicationContainer;

    private String appId;

    private String serviceName;

    // Key: version
    private Map<String, MicroserviceInstanceContainer> serviceInstancesByAppIdAndServiceNameAndVersion = new ConcurrentHashMapEx<>();

    public MicroserviceVersionContainer(ApplicationContainer applicationContainer, String appId, String serviceName) {
        this.applicationContainer = applicationContainer;
        this.appId = appId;
        this.serviceName = serviceName;
    }

    public  MicroserviceInstanceContainer getOrCreateMicroserviceInstanceContainer(String version) {
        return serviceInstancesByAppIdAndServiceNameAndVersion.computeIfAbsent(serviceName, name -> new MicroserviceInstanceContainer(applicationContainer, appId, serviceName, version));
    }


}
