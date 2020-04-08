package org.apache.servicecomb.embedsc.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMicroserviceInstance {

    private ApplicationContainer applicationContainer;

    private String appId;

    private String serviceName;

    private String version;

    private String instanceId;

    private String serviceId;

    private List<String> endpoints = new ArrayList();

    private String hostName;

    private String status;

    private Map<String, String> properties;

    private String timestamp;

    private String modTimestamp;

    // key: client siede Microservice property/attribute
    private Map<String, String> serviceInstanceTextAttributesMap = new ConcurrentHashMap<>();

    public ServerMicroserviceInstance(){}

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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getModTimestamp() {
        return modTimestamp;
    }

    public void setModTimestamp(String modTimestamp) {
        this.modTimestamp = modTimestamp;
    }
}
