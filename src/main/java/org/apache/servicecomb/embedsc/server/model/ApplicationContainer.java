package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContainer {

    // Map<appId, Map<serviceName, Map<version, Microservice>>>
    private Map<String, Map<String, Map<String, Microservice>>> appContainer = new ConcurrentHashMap<>();

    // return Map<serviceName, Map<version, Microservice>>
    public Map<String, Map<String, Microservice>> getMicroservicesByAppIdMap(String appId) {
         return appContainer.get(appId);
     }

    // return Map<version, Microservice>
    public Map<String, Microservice> getMicroserviceVersionsByAppIdAndServiceNameMap(String appId, String serviceName) {
        Map<String, Map<String, Microservice>> microservicesByAppIdMap = this.getMicroservicesByAppIdMap(appId);
        if (microservicesByAppIdMap != null && !microservicesByAppIdMap.isEmpty()){
            return microservicesByAppIdMap.get(serviceName);
        }
        return null;
    }

    // return Microservice
    public Microservice getMicroserviceByAppIdAndServiceNameAndVersion(String appId, String serviceName, String version) {
        Map<String, Microservice> microserviceVersionsByAppIdAndServiceNameMap = this.getMicroserviceVersionsByAppIdAndServiceNameMap(appId, serviceName);
        if (microserviceVersionsByAppIdAndServiceNameMap != null && !microserviceVersionsByAppIdAndServiceNameMap.isEmpty()) {
            return microserviceVersionsByAppIdAndServiceNameMap.get(version);
        }
        return null;
    }

}
