package org.apache.servicecomb.embedsc.server.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * {
 *   "serviceId": "769d5439888854928cf37efac7bab2b69f8b4a46",
 *   "appId": "springmvc",
 *   "serviceName": "helloworldprovider",
 *   "version": "0.0.1",
 *   "level": "FRONT",
 *   "schemas": [
 *     "helloworldprovider"
 *   ],
 *   "status": "UP",
 *   "properties": {
 *     "allowCrossApp": "false",
 *     "dcs": "false",
 *     "ddm": "false",
 *     "dms": "false"
 *   },
 *   "timestamp": "1584468365",
 *   "modTimestamp": "1584468365",
 *   "registerBy": "SDK",
 *   "framework": {
 *     "name": "servicecomb-java-chassis",
 *     "version": "CSE:2.3.69;ServiceComb:1.2.0.B006"
 *   }
 * }
 */

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
    private Map<String, String> serviceTextAttributesMap = new HashMap<>();

    private Map<String, String> schemaMap;

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
        this.serviceTextAttributesMap.put("serviceId", serviceId);
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
        this.serviceTextAttributesMap.put("framework", framework.toString());
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
