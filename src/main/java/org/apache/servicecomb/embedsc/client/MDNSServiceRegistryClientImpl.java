package org.apache.servicecomb.embedsc.client;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.MDNS_SERVICE_NAME_SUFFIX;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_CONTENT_PLACEHOLDER;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.client.util.ClientRegisterUtil;
import org.apache.servicecomb.embedsc.server.MicroserviceInstanceService;
import org.apache.servicecomb.embedsc.server.MicroserviceService;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.api.registry.*;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.version.Version;
import org.apache.servicecomb.serviceregistry.version.VersionRule;
import org.apache.servicecomb.serviceregistry.version.VersionRuleUtils;
import org.apache.servicecomb.serviceregistry.version.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MDNSServiceRegistryClientImpl implements ServiceRegistryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MDNSServiceRegistryClientImpl.class);

    public static final String MDNS_REGISTRY_KEY = "mdns.registry";

    private IpPortManager ipPortManager;
    private MulticastDNSService multicastDNSService;

    private MicroserviceService microserviceService;
    private MicroserviceInstanceService microserviceInstanceService;

    public MDNSServiceRegistryClientImpl(ServiceRegistryConfig serviceRegistryConfig){
        this.ipPortManager = new IpPortManager(serviceRegistryConfig);
        try {
            this.multicastDNSService = new MulticastDNSService();
        } catch (IOException e) {
            LOGGER.error("Failed to create MulticastDNSService object", e);
        }
        // for querying ONLY
        this.microserviceService = new MicroserviceService();
        this.microserviceInstanceService = new MicroserviceInstanceService();
    }

    @Override
    public void init() {}

    @Override
    public List<Microservice> getAllMicroservices() {
       List<ServerMicroservice> serverMicroserviceList =  microserviceService.getAllMicroservices();
       List<Microservice> resultMicroserviceList = new ArrayList<>();
       for (ServerMicroservice serverMicroservice : serverMicroserviceList) {
           resultMicroserviceList.add(ClientRegisterUtil.convertToClientMicroservice(serverMicroservice));
       }
       return resultMicroserviceList;
    }

    @Override
    public String getMicroserviceId(String appId, String microserviceName, String versionRule, String environment) {
        return microserviceService.getMicroserviceId(appId, microserviceName, versionRule);
    }

    @Override
    public String registerMicroservice(Microservice microservice) {
        // refer to the logic in LocalServiceRegistryClientImpl.java
        String serviceId = microservice.getServiceId();
        if (serviceId == null || serviceId.length() == 0){
            // follow Go service-center logic to generate serviceId based on the appId, serviceName and version
            serviceId = ClientRegisterUtil.generateServiceId(microservice);
        }

        try {
            ServiceInstance service = ClientRegisterUtil.convertToMDNSService(serviceId, microservice, this.ipPortManager);
            // broadcast to MDNS
            this.multicastDNSService.register(service);
        } catch (IOException e) {
            LOGGER.error("Failed to register microservice to mdns {}/{}/{}", microservice.getAppId(), microservice.getServiceName(), microservice.getVersion(), e);
            return null;
        }
        return serviceId;
    }

    @Override
    public Microservice getMicroservice(String microserviceId) {
        ServerMicroservice serverMicroservice = microserviceService.getMicroservice(microserviceId);
        return serverMicroservice != null ? ClientRegisterUtil.convertToClientMicroservice(serverMicroservice) : null;
    }

    @Override
    public Microservice getAggregatedMicroservice(String microserviceId) {
        return this.getMicroservice(microserviceId);
    }

    @Override
    public boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties) {
        Microservice microservice = this.getMicroservice(microserviceId);
        if(microservice == null) {
            return false;
        }
        // putAll will update values for keys exist in the map, also add new <key, value> to the map
        microservice.getProperties().putAll(serviceProperties);

        String serviceId = this.registerMicroservice(microservice);

        return serviceId != null && !serviceId.isEmpty();
    }

    @Override
    public boolean isSchemaExist(String microserviceId, String schemaId) {
        return this.microserviceService.isSchemaExist(microserviceId, schemaId);
    }

    @Override
    public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
        Microservice microservice = this.getMicroservice(microserviceId);
        if (microservice == null){
            LOGGER.error("Invalid serviceId! Failed to retrieve Microservice for serviceId {}", microserviceId);
            return false;
        }

        LOGGER.info("MDNS Service registration center doesn't record the schemaContent for schemaId:\n {}", schemaId);

        // for Consumer to retrieve the provider's schemas TODO: add rest endpoint for consumer to get Map content
        ServerRegisterUtil.schemasMap.put(schemaId, schemaContent);

        // set it to placehold value: {} so that it won't break the main servicecomb service registration logic
        schemaContent = SCHEMA_CONTENT_PLACEHOLDER;
        ServiceInstance serviceSchema = ClientRegisterUtil.convertToMDNSServiceSchema(microserviceId, schemaId, schemaContent, this.ipPortManager);

        // broadcast to MDNS
        try {
            this.multicastDNSService.register(serviceSchema);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to register schemaId: {} for microserviceId: {} to MDNS. The schema content is: {}", schemaId, microserviceId, schemaContent, e);
            return false;
        }
    }

    @Override
    public String getSchema(String microserviceId, String schemaId) {
        Microservice microservice = this.getMicroservice(microserviceId);
        if (microservice == null) {
            LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}", microserviceId);
            return null;
        }

        Map<String, String> schemaMap = microservice.getSchemaMap();

        if (schemaMap != null && !schemaMap.isEmpty()){
            return schemaMap.get(schemaId);
        }
        return null;
    }

    @Override
    public String getAggregatedSchema(String microserviceId, String schemaId) {
        return this.getSchema(microserviceId, schemaId);
    }

    @Override
    public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {
        // this method is called in MicroserviceRegisterTask.java doRegister()
        Holder<List<GetSchemaResponse>> resultHolder = new Holder<>();
        Microservice microservice = this.getMicroservice(microserviceId);
        if (microservice == null) {
            LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}", microserviceId);
            return resultHolder;
        }
        List<GetSchemaResponse> schemas = new ArrayList<>();
        microservice.getSchemaMap().forEach((key, val) -> {
            GetSchemaResponse schema = new GetSchemaResponse();
            schema.setSchema(val);
            schema.setSchemaId(key);
            schema.setSummary(Hashing.sha256().newHasher().putString(val, Charsets.UTF_8).hash().toString());
            schemas.add(schema);
        });
        resultHolder.setStatusCode(Response.Status.OK.getStatusCode()).setValue(schemas);
        return resultHolder;
    }

    @Override
    public String registerMicroserviceInstance(MicroserviceInstance instance) {

        String serviceId = instance.getServiceId();
        // allow client to set the instanceId
        String instanceId = instance.getInstanceId();

        if ( instanceId == null || instanceId.length() == 0){
            instanceId = ClientRegisterUtil.generateServiceInstanceId(instance);
        }

        try {
            ServiceInstance serviceInstance = ClientRegisterUtil.convertToMDNSServiceInstance(serviceId, instanceId, instance, this.ipPortManager);
            // broadcast to MDNS
            this.multicastDNSService.register(serviceInstance);
        } catch (IOException e) {
            LOGGER.error("Failed to register microservice instance to mdns. servcieId: {} instanceId:{}", serviceId, instanceId,  e);
            return null;
        }
        return instanceId;
    }

    @Override
    public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
        List<MicroserviceInstance> microserviceInstanceResult = new ArrayList<>();
        List<ServerMicroserviceInstance>  serverMicroserviceInstanceList = this.microserviceInstanceService.getMicroserviceInstance(consumerId, providerId);
        if (serverMicroserviceInstanceList == null || serverMicroserviceInstanceList.isEmpty()) {
            LOGGER.error("Invalid serviceId: {}", providerId);
            return microserviceInstanceResult;
        }

        for (ServerMicroserviceInstance serverMicroserviceInstance : serverMicroserviceInstanceList){
            microserviceInstanceResult.add(ClientRegisterUtil.convertToClientMicroserviceInstance(serverMicroserviceInstance));
            return microserviceInstanceResult;
        }
        return microserviceInstanceResult;
    }

    @Override
    public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId, Map<String, String> instanceProperties) {
        MicroserviceInstance microserviceInstance = this.findServiceInstance(microserviceId, microserviceInstanceId);
        if(microserviceInstance == null) {
            LOGGER.error("Invalid microserviceId, microserviceId: {} OR microserviceInstanceId, microserviceInstanceId: {}", microserviceId, microserviceInstanceId);
            return false;
        }

        if(  microserviceInstance.getProperties().equals(instanceProperties)) {
            throw new IllegalArgumentException("No update to existing instance properties" +  instanceProperties);
        }

        // putAll will update values for keys exist in the map, also add new <key, value> to the map
        microserviceInstance.getProperties().putAll(instanceProperties);

        String serviceInstanceId = this.registerMicroserviceInstance(microserviceInstance);

        return serviceInstanceId != null && !serviceInstanceId.isEmpty();
    }

    @Override
    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {

        MicroserviceInstance microserviceInstance = this.findServiceInstance(microserviceId, microserviceInstanceId);
        if (microserviceInstance == null) {
            LOGGER.error("Failed to unregister microservice instance from mdns server. The instance with servcieId: {} instanceId:{} doesn't exist in MDNS server", microserviceId, microserviceInstanceId);
            return false;
        }

        try {
            // convention to append "._http._tcp.local."
            ServiceName serviceName = new ServiceName(microserviceInstanceId + MDNS_SERVICE_NAME_SUFFIX);
            // broadcast to MDNS
            if(this.multicastDNSService.unregister(serviceName)){
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to unregister microservice instance from mdns server. servcieId: {} instanceId:{}", microserviceId, microserviceInstanceId,  e);
            return false;
        }
        return false;
    }

    @Override
    public HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId) {
        HeartbeatResponse response = new HeartbeatResponse();
        if (this.microserviceInstanceService.heartbeat(microserviceId, microserviceInstanceId)) {
            response.setMessage(INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK);
            response.setOk(true);
        }
        return response;
    }

    @Override
    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {}

    @Override
    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback, AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {}

    @Override
    public List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName, String versionRule) {
        MicroserviceInstances instances = findServiceInstances(consumerId, appId, serviceName, versionRule, null);
        if (instances.isMicroserviceNotExist()) {
            return null;
        }
        return instances.getInstancesResponse().getInstances();
    }

    @Override
    public MicroserviceInstances findServiceInstances(String consumerId, String appId, String serviceName, String strVersionRule, String revision) {
        int currentRevision = 1;

        List<MicroserviceInstance> allInstances = new ArrayList<>();
        MicroserviceInstances microserviceInstances = new MicroserviceInstances();
        FindInstancesResponse response = new FindInstancesResponse();
        if (revision != null && currentRevision == Integer.parseInt(revision)) {
            microserviceInstances.setNeedRefresh(false);
            return microserviceInstances;
        }

        microserviceInstances.setRevision(String.valueOf(currentRevision));
        VersionRule versionRule = VersionRuleUtils.getOrCreate(strVersionRule);
        Microservice latestMicroservice = findLatest(appId, serviceName, versionRule);
        if (latestMicroservice == null) {
            microserviceInstances.setMicroserviceNotExist(true);
            return microserviceInstances;
        }

        Version latestVersion = VersionUtils.getOrCreate(latestMicroservice.getVersion());
        for (Microservice microservice : this.getAllMicroservices()) {
            if (!isSameMicroservice(microservice, appId, serviceName)) {
                continue;
            }

            Version version = VersionUtils.getOrCreate(microservice.getVersion());
            if (!versionRule.isMatch(version, latestVersion)) {
                continue;
            }

            List<MicroserviceInstance> microserviceInstanceList = this.getMicroserviceInstance(null, microservice.getServiceId());
            allInstances.addAll(microserviceInstanceList);
        }
        response.setInstances(allInstances);
        microserviceInstances.setInstancesResponse(response);

        return microserviceInstances;
    }



    private Microservice findLatest(String appId, String serviceName, VersionRule versionRule) {
        Version latestVersion = null;
        Microservice latest = null;
        List<Microservice> microserviceList = this.getAllMicroservices();
        for (Microservice microservice : microserviceList) {
            if (!isSameMicroservice(microservice, appId, serviceName)) {
                continue;
            }
            Version version = VersionUtils.getOrCreate(microservice.getVersion());
            if (!versionRule.isAccept(version)) {
                continue;
            }
            if (latestVersion == null || version.compareTo(latestVersion) > 0) {
                latestVersion = version;
                latest = microservice;
            }
        }

        return latest;
    }

    private boolean isSameMicroservice(Microservice microservice, String appId, String serviceName) {
        return microservice.getAppId().equals(appId) && microservice.getServiceName().equals(serviceName);
    }

    @Override
    public MicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
        ServerMicroserviceInstance  serverMicroserviceInstance = this.microserviceInstanceService.findServiceInstance(serviceId, instanceId);
        if (serverMicroserviceInstance == null) {
            LOGGER.error("Invalid serviceId OR instanceId! Failed to retrieve Microservice Instance for serviceId {} and instanceId {}", serviceId, instanceId);
            return null;
        }
        return ClientRegisterUtil.convertToClientMicroserviceInstance(serverMicroserviceInstance);
    }

    @Override
    public ServiceCenterInfo getServiceCenterInfo() {
        ServiceCenterInfo info = new ServiceCenterInfo();
        info.setVersion("1.0.0");
        info.setBuildTag("20200501");
        info.setRunMode("dev");
        info.setApiVersion("1.0.0");
        info.setConfig(new ServiceCenterConfig());
        return info;
    }

    @Override
    public boolean updateMicroserviceInstanceStatus(String microserviceId, String instanceId, MicroserviceInstanceStatus status) {
        if (null == status) {
            throw new IllegalArgumentException("null status is now allowed");
        }

        MicroserviceInstance microserviceInstance = this.findServiceInstance(microserviceId, instanceId);

        if(microserviceInstance == null) {
            throw new IllegalArgumentException("Invalid microserviceId=" +  microserviceId + "OR instanceId=" + instanceId);
        }

        if (status.equals(microserviceInstance.getStatus())){
            throw new IllegalArgumentException("service instance status is same as server side existing status: " +  microserviceInstance.getStatus().toString());
        }

        LOGGER.debug("update status of microservice instance: {}", status);
        microserviceInstance.setStatus(status);
        String serviceInstanceId = this.registerMicroserviceInstance(microserviceInstance);
        return serviceInstanceId != null && !serviceInstanceId.isEmpty();
    }

}
