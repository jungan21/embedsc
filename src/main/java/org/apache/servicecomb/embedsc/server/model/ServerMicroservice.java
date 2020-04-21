//package org.apache.servicecomb.embedsc.server.model;
//
//import static org.apache.servicecomb.embedsc.EmbedSCConstants.SERVICE_ID;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class ServerMicroservice {
//
//    //private ApplicationContainer applicationContainer;
//
//    private String appId;
//
//    private String serviceName;
//
//    private String version;
//
//    private String serviceId;
//
//    // "FRONT", "BACK"
//    private String level;
//
//    // "UP", "DOWN", "UNKNOWN"
//    private String status;
//
//    // {"allowCrossApp"="false", "dcs"="false"}
//    private Map<String, String> properties;
//
//    // Time.now() Unix sytle 1584468365 ,go service center has sample.
//    private String timestamp;
//
//    private String alias;
//
//    // last time service is updated ...same format as createTimestamp
//    private String modTimestamp;
//
//    // "SDK"
//    private String registerBy;
//
//    private String description;
//
//    private Map<String, String> framework;
//
//    private String environment;
//
//    // key: client siede Microservice property/attribute
//    private Map<String, String> serviceTextAttributesMap = new ConcurrentHashMap<>();
//
//    private Map<String, String> schemaMap = new ConcurrentHashMap<>();
//
//    // list of schema Ids
//    private List<String> schemas;
//
//    // key:instanceId
//    private Map<String, ServerMicroserviceInstance> instances = new ConcurrentHashMap<>();
//
//    public ServerMicroservice() {
//    }
//
//    public ServerMicroservice(String appId, String serviceName, String version) {
//        this.appId = appId;
//        this.serviceName = serviceName;
//        this.version = version;
//    }
//
//    public ServerMicroservice(String appId, String serviceName, String version, Map<String, String> serviceTextAttributes) {
//        this(appId, serviceName, version);
//        this.serviceTextAttributesMap = serviceTextAttributes;
//    }
//
////    public ServerMicroservice(ApplicationContainer applicationContainer, String appId, String serviceName, String version) {
////        this(appId, serviceName, version);
////        this.applicationContainer = applicationContainer;
////    }
//
//    public String getAppId() {
//        return appId;
//    }
//
//    public void setAppId(String appId) {
//        this.appId = appId;
//    }
//
//    public String getServiceName() {
//        return serviceName;
//    }
//
//    public void setServiceName(String serviceName) {
//        this.serviceName = serviceName;
//    }
//
//    public String getVersion() {
//        return version;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }
//
//    public String getServiceId() {
//        return serviceId;
//    }
//
//    public void setServiceId(String serviceId) {
//        this.serviceId = serviceId;
//        this.serviceTextAttributesMap.put(SERVICE_ID, serviceId);
//    }
//
//    public Map<String, ServerMicroserviceInstance> getInstances() {
//        return instances;
//    }
//
//    public void addInstance(ServerMicroserviceInstance instance) {
//       this.instances.put(instance.getInstanceId(), instance);
//    }
//
//    public void removeInstance(String microserviceInstanceId) {
//        this.instances.remove(microserviceInstanceId);
//    }
//
//    public List<String> getSchemas() {
//        return schemas;
//    }
//
//    public void setSchemas(List<String> schemas) {
//        this.schemas = schemas;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public Map<String, String> getProperties() {
//        return properties;
//    }
//
//    public void setProperties(Map<String, String> properties) {
//        this.properties = properties;
//    }
//
//    public String getEnvironment() {
//        return environment;
//    }
//
//    public void setEnvironment(String environment) {
//        this.environment = environment;
//    }
//
//    public void addSchema(String schemaId, String schemaContent) {
//        this.schemaMap.put(schemaId, schemaContent);
//        // Note:can not do this.schemas.add(schemaId). Because schemas List is set by Arrays.asList(). which means it's fixed size
//    }
//
//}
