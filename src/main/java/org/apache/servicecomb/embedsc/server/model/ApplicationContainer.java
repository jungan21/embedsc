package org.apache.servicecomb.embedsc.server.model;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.DISCOVER_SERVICE_TYPES;

import net.posick.mDNS.Browse;
import net.posick.mDNS.DNSSDListener;
import net.posick.mDNS.MulticastDNSService;
import org.apache.servicecomb.embedsc.server.listener.ServiceCombMDSNServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContainer.class);

    // Map<appId, Map<serviceName, Map<version, Microservice>>>
    // private Map<String, Map<String, Map<String, Microservice>>> appContainer = new ConcurrentHashMap<>();
    // key:appId
    private Map<String, MicroserviceContainer> apps = new ConcurrentHashMap<>();

    public Map<String, MicroserviceContainer> getApps() {
        return apps;
    }

    public ApplicationContainer() {
        // register event Listener for MDNS service register/unregister events
        try {
            // https://github.com/posicks/mdnsjava/blob/master/README.md
            MulticastDNSService service = new MulticastDNSService();
            Browse browser = new Browse(DISCOVER_SERVICE_TYPES);
            DNSSDListener listener = new ServiceCombMDSNServiceListener();
            service.startServiceDiscovery(browser, listener);
        } catch (IOException e) {
            LOGGER.error("Failed to start Asynchronous Service Discovery Service", e);
        }
    }

    public void setApps(Map<String, MicroserviceContainer> apps) {
        this.apps = apps;
    }

    public MicroserviceContainer getOrCreateMicroserviceContainer(String appId) {
        return apps.computeIfAbsent(appId, id -> new MicroserviceContainer(this, appId));
    }

    public MicroserviceContainer getMicroserviceContainer(String appId) {
        return apps.get(appId);
    }

    public MicroserviceVersionContainer getOrCreateMicroserviceVersionContainer(String appId, String serviceName) {
        MicroserviceContainer microserviceContainer = getOrCreateMicroserviceContainer(appId);
        return microserviceContainer.getOrCreateMicroserviceVersionContainer(serviceName);
    }

    public MicroserviceVersionContainer getMicroserviceVersionContainer(String appId, String serviceName) {
        MicroserviceContainer microserviceContainer = getMicroserviceContainer(appId);
        if (microserviceContainer != null && microserviceContainer.getServices() != null) {
            return microserviceContainer.getServices().get(serviceName);
        }
        return null;
    }

    public ServerMicroservice getOrCreateServerMicroservice(String appId, String serviceName, String version) {
        MicroserviceVersionContainer microserviceVersionContainer = getOrCreateMicroserviceVersionContainer(appId, serviceName);
        return microserviceVersionContainer.getOrCreateServerMicroservice(version);
    }

    public ServerMicroservice getServerMicroservice(String appId, String serviceName, String version) {
        MicroserviceVersionContainer microserviceVersionContainer = getMicroserviceVersionContainer(appId, serviceName);
        if (microserviceVersionContainer != null && microserviceVersionContainer.getVersions() != null){
            return microserviceVersionContainer.getVersions().get(version);
        }
        return null;
    }

    public ServerMicroserviceInstance getOrCreateServerMicroserviceInstance(String appId, String serviceName, String version, String instanceId) {
        ServerMicroservice serverMicroservice = getOrCreateServerMicroservice(appId, serviceName, version);
        return serverMicroservice.getOrCreateServerMicroserviceInstance(instanceId);
    }

    public ServerMicroserviceInstance getServerMicroserviceInstance(String appId, String serviceName, String version, String instancId) {
        ServerMicroservice ServerMicroservice = getServerMicroservice(appId, serviceName, version);
        if (ServerMicroservice != null && ServerMicroservice.getInstances() != null){
            return ServerMicroservice.getInstances().get(instancId);
        }
        return null;
    }

}
