package org.apache.servicecomb.embedsc.server.model;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.FRAMEWORK;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SERVICE_ID;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMicroservice {

    private ApplicationContainer applicationContainer;

    private String appId;

    private String serviceName;

    private String version; // "1.3.0"

    private String serviceId;

    private String level; // "FRONT", "BACK"

    private List<String> schemas; // list of schema names

    private String status; // "UP", "DOWN", "UNKNOWN"

    private Map<String, String> properties; // {"allowCrossApp"="false", "dcs"="false"}

    private String timestamp; // Time.now() Unix sytle 1584468365 ,go service center has sample.

    private String alias;

    private String modTimestamp; // last time service is updated ...same format as createTimestamp

    private String registerBy; // "SDK"

    private String description;

    private Map<String, String> framework;

    private String environment;

    // key: client siede Microservice property/attribute
    private Map<String, String> serviceTextAttributesMap = new ConcurrentHashMap<>();

    private Map<String, String> schemaMap = new ConcurrentHashMap<>();

    // key:instanceId
    private Map<String, ServerMicroserviceInstance> instances = new ConcurrentHashMap<>();

    public ServerMicroservice() {
    }

    public ServerMicroservice(String appId, String serviceName, String version) {
        this.appId = appId;
        this.serviceName = serviceName;
        this.version = version;
    }

    public ServerMicroservice(String appId, String serviceName, String version, Map<String, String> serviceTextAttributes) {
        this(appId, serviceName, version);
        this.serviceTextAttributesMap = serviceTextAttributes;
    }

    public ServerMicroservice(ApplicationContainer applicationContainer, String appId, String serviceName, String version) {
        this(appId, serviceName, version);
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

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
        this.serviceTextAttributesMap.put(SERVICE_ID, serviceId);
    }

    public Map<String, String> getServiceTextAttributesMap() {
        return serviceTextAttributesMap;
    }

    public void setServiceTextAttributesMap(Map<String, String> serviceTextAttributesMap) {
        this.serviceTextAttributesMap = serviceTextAttributesMap;
    }

    public Map<String, ServerMicroserviceInstance> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, ServerMicroserviceInstance> instances) {
        this.instances = instances;
    }

    public void addInstance(ServerMicroserviceInstance instance) {
       this.instances.put(instance.getServiceId(), instance);
    }

    public void removeInstance(String microserviceInstanceId) {
        this.instances.remove(microserviceInstanceId);
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getModTimestamp() {
        return modTimestamp;
    }

    public void setModTimestamp(String modTimestamp) {
        this.modTimestamp = modTimestamp;
    }

    public String getRegisterBy() {
        return registerBy;
    }

    public void setRegisterBy(String registerBy) {
        this.registerBy = registerBy;
    }

    public Map<String, String> getFramework() {
        return framework;
    }

    public void setFramework(Map<String, String> framework) {
        this.framework = framework;
        this.serviceTextAttributesMap.put(FRAMEWORK, framework.toString());
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Map<String, String> getSchemaMap() {
        return schemaMap;
    }

    public void setSchemaMap(Map<String, String> schemaMap) {
        this.schemaMap = schemaMap;
    }

    public String getSchema(String schemaId) {
        return schemaMap.get(schemaId);
    }

    public void addSchema(String schemaId, String schemaContent) {
        this.schemaMap.put(schemaId, schemaContent);
        this.schemas.add(schemaId);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ServerMicroserviceInstance getOrCreateServerMicroserviceInstance(String instanceId) {
        return instances.computeIfAbsent(instanceId, ins -> new ServerMicroserviceInstance(applicationContainer, appId, serviceName, version, instanceId));
    }
}
