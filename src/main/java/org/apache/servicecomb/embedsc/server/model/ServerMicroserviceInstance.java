package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.serviceregistry.api.registry.HealthCheck;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerMicroserviceInstance {

    private ApplicationContainer applicationContainer;

    private String appId;

    private String serviceName;

    private String version;

    private String instanceId;

    private String serviceId;

    private List<String> endpoints = new ArrayList();

    private String hostName; // e.g. DESKTOP-Q2K46AO

    private String status;

    private Map<String, String> properties;

    private HealthCheck healthCheck;


    public ServerMicroserviceInstance(String appId, String serviceName, String version, String instanceId) {
        this.appId = appId;
        this.serviceName = serviceName;
        this.version = version;
        this.instanceId = instanceId;
    }

    public ServerMicroserviceInstance(ApplicationContainer applicationContainer, String appId, String serviceName, String version, String instanceId) {
        this(appId, serviceName, version, instanceId);
        this.applicationContainer = applicationContainer;
    }

}
