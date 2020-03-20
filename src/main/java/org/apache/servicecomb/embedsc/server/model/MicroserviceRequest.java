package org.apache.servicecomb.embedsc.server.model;

import java.util.Map;

public class MicroserviceRequest {

    private String appId;

    private String serviceName;

    private String serviceId;

    private String version;

    private Map<String, String> serviceTextAttributes;

    public MicroserviceRequest(String appId, String serviceName, String version, Map<String, String> serviceTextAttributes) {
        this.appId = appId;
        this.serviceName = serviceName;
        this.version = version;
        this.serviceTextAttributes = serviceTextAttributes;
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

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getServiceTextAttributes() {
        return serviceTextAttributes;
    }

    public void setServiceTextAttributes(Map<String, String> serviceTextAttributes) {
        this.serviceTextAttributes = serviceTextAttributes;
    }
}
