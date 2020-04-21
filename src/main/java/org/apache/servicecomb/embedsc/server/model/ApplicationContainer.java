//package org.apache.servicecomb.embedsc.server.model;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class ApplicationContainer {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContainer.class);
//
//    // Map<appId, Map<serviceName, Map<version, Microservice>>>
//    // private Map<String, Map<String, Map<String, Microservice>>> appContainer = new ConcurrentHashMap<>();
//    // key:appId
//    private Map<String, MicroserviceContainer> apps = new ConcurrentHashMap<>();
//
//    public MicroserviceContainer getOrCreateMicroserviceContainer(String appId) {
//        return apps.computeIfAbsent(appId, id -> new MicroserviceContainer(this, appId));
//    }
//
//    public MicroserviceContainer getMicroserviceContainer(String appId) {
//        return apps.get(appId);
//    }
//
//    public MicroserviceVersionContainer getOrCreateMicroserviceVersionContainer(String appId, String serviceName) {
//        MicroserviceContainer microserviceContainer = getOrCreateMicroserviceContainer(appId);
//        return microserviceContainer.getOrCreateMicroserviceVersionContainer(serviceName);
//    }
//
//    public MicroserviceVersionContainer getMicroserviceVersionContainer(String appId, String serviceName) {
//        MicroserviceContainer microserviceContainer = getMicroserviceContainer(appId);
//        if (microserviceContainer != null && microserviceContainer.getServices() != null) {
//            return microserviceContainer.getServices().get(serviceName);
//        }
//        return null;
//    }
//
//    public ServerMicroservice getOrCreateServerMicroservice(String appId, String serviceName, String version) {
//        MicroserviceVersionContainer microserviceVersionContainer = getOrCreateMicroserviceVersionContainer(appId, serviceName);
//        return microserviceVersionContainer.getOrCreateServerMicroservice(version);
//    }
//
//    public ServerMicroservice getServerMicroservice(String appId, String serviceName, String version) {
//        MicroserviceVersionContainer microserviceVersionContainer = getMicroserviceVersionContainer(appId, serviceName);
//        if (microserviceVersionContainer != null && microserviceVersionContainer.getVersions() != null){
//            return microserviceVersionContainer.getVersions().get(version);
//        }
//        return null;
//    }
//
//}
