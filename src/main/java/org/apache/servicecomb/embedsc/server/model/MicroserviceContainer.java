package org.apache.servicecomb.embedsc.server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MicroserviceContainer {

    private ApplicationContainer applicationContainer;

    private String appId;

    // Key: serviceName
    private Map<String, MicroserviceVersionContainer> services = new ConcurrentHashMap<>();

    public Map<String, MicroserviceVersionContainer> getServices() {
        return services;
    }

    public MicroserviceContainer(ApplicationContainer applicationContainer, String appId) {
        this.applicationContainer = applicationContainer;
        this.appId = appId;
    }

    public MicroserviceVersionContainer getOrCreateMicroserviceVersionContainer(String serviceName) {
        // return services.computeIfAbsent(serviceName, name -> new MicroserviceVersionContainer(applicationContainer, appId, serviceName));
        if (services.containsKey(serviceName)){
            return services.get(serviceName);
        } else {
            MicroserviceVersionContainer microserviceVersionContainer = new MicroserviceVersionContainer(this.applicationContainer, this.appId, serviceName);
            services.put(serviceName, microserviceVersionContainer);
            return microserviceVersionContainer;
        }
    }

}


