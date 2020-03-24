package org.apache.servicecomb.embedsc.server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MicroserviceVersionContainer {

    private ApplicationContainer applicationContainer;

    private String appId;

    private String serviceName;

    // Key: version
    private Map<String, ServerMicroservice> versions = new ConcurrentHashMap<>();

    public MicroserviceVersionContainer(ApplicationContainer applicationContainer, String appId, String serviceName) {
        this.applicationContainer = applicationContainer;
        this.appId = appId;
        this.serviceName = serviceName;
    }

    public Map<String, ServerMicroservice> getVersions() {
        return versions;
    }

    public void setVersions(Map<String, ServerMicroservice> versions) {
        this.versions = versions;
    }

    public ServerMicroservice getOrCreateServerMicroservice(String version) {
        return versions.computeIfAbsent(version, v -> new ServerMicroservice(applicationContainer, appId, serviceName, version));
    }

}
