package org.apache.servicecomb.embedsc.client;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.MDNS_SERVICE_NAME_SUFFIX;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.client.util.ClientRegisterUtil;
import org.apache.servicecomb.embedsc.server.MicroserviceInstanceService;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
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

    private IpPortManager ipPortManager;
    private MulticastDNSService multicastDNSService;
    private MicroserviceInstanceService microserviceInstanceService;
    private Microservice currentMicroservice;

    public MDNSServiceRegistryClientImpl(ServiceRegistryConfig serviceRegistryConfig){
        this.ipPortManager = new IpPortManager(serviceRegistryConfig);
        try {
            this.multicastDNSService = new MulticastDNSService();
        } catch (IOException e) {
            LOGGER.error("Failed to create MulticastDNSService object", e);
        }
        // for query ONLY, for update, we have to broadcast
        this.microserviceInstanceService = new MicroserviceInstanceService();
    }

    @Override
    public void init() {}

    @Override
    public List<Microservice> getAllMicroservices() {
        // TODO in our new Zero Configuration registration center, it refers to all service instances
       List<Microservice> serverMicroserviceList =  new ArrayList<>();
       return serverMicroserviceList;
    }

    // this method is called before Microservice registration to check whether service with this ID exists or not
    @Override
    public String getMicroserviceId(String appId, String microserviceName, String versionRule, String environment) {
        return this.currentMicroservice != null ? this.currentMicroservice.getServiceId() : null;
    }

    @Override
    public String registerMicroservice(Microservice microservice) {
        // refer to the logic in LocalServiceRegistryClientImpl.java
        String serviceId = microservice.getServiceId();
        if (serviceId == null || serviceId.length() == 0){
            serviceId = ClientRegisterUtil.generateServiceId(microservice);
        }
        // set to local variable so that it can be used to retrieve serviceName/appId/version when registering instance
        this.currentMicroservice = microservice;
        return serviceId;
    }

    @Override
    public Microservice getMicroservice(String microserviceId) {
        return this.currentMicroservice.getServiceId().equals(microserviceId) ? this.currentMicroservice : null;
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
        List<String> schemaList = this.currentMicroservice.getSchemas();
        return this.currentMicroservice.getServiceId().equals(microserviceId) && schemaList != null && schemaList.contains(schemaId);
    }

    @Override
    public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
        return true;
    }

    @Override
    public String getSchema(String microserviceId, String schemaId) {
        Microservice microservice = this.getMicroservice(microserviceId);
        if (microservice == null) {
            LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}", microserviceId);
            return null;
        }
        return microservice.getSchemaMap() != null ? microservice.getSchemaMap().get(schemaId) : null;
    }

    @Override
    public String getAggregatedSchema(String microserviceId, String schemaId) {
        return this.getSchema(microserviceId, schemaId);
    }

    @Override
    public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {
        // this method is called in MicroserviceRegisterTask.java doRegister()
        Holder<List<GetSchemaResponse>> resultHolder = new Holder<>();
        if (currentMicroservice == null) {
            LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}", microserviceId);
            return resultHolder;
        }
        List<GetSchemaResponse> schemas = new ArrayList<>();
        currentMicroservice.getSchemaMap().forEach((key, val) -> {
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
        String instanceId = instance.getInstanceId(); // allow client to set the instanceId
        if (instanceId == null || instanceId.length() == 0){
            instanceId = ClientRegisterUtil.generateServiceInstanceId(instance);
        }

        try {
            // need currentMicroservice object to retrieve serviceName/appID/version attributes for instance to be registered
            ServiceInstance serviceInstance = ClientRegisterUtil.convertToMDNSServiceInstance(serviceId, instanceId, instance, this.ipPortManager, this.currentMicroservice);
            this.multicastDNSService.register(serviceInstance); // broadcast to MDNS
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

        if( microserviceInstance.getProperties().equals(instanceProperties)) {
            throw new IllegalArgumentException("No update to existing instance properties" +  instanceProperties);
        }

        // putAll will update values for keys exist in the map, also add new <key, value> to the map
        microserviceInstance.getProperties().putAll(instanceProperties);

        String serviceInstanceId = this.registerMicroserviceInstance(microserviceInstance);

        return serviceInstanceId != null && !serviceInstanceId.isEmpty();
    }

    @Override
    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
        ServerMicroserviceInstance serverMicroserviceInstance =  this.microserviceInstanceService.findServiceInstance(microserviceId, microserviceInstanceId);

        if (serverMicroserviceInstance == null) {
            LOGGER.error("Failed to unregister microservice instance from mdns server. The instance with servcieId: {} instanceId:{} doesn't exist in MDNS server", microserviceId, microserviceInstanceId);
            return false;
        }

        try {
            // convention to append "._http._tcp.local."
            LOGGER.info("Start unregister microservice instance. The instance with servcieId: {} instanceId:{}", microserviceId, microserviceInstanceId);
            ServiceName mdnsServiceName = new ServiceName(serverMicroserviceInstance.getServiceName() + MDNS_SERVICE_NAME_SUFFIX);
            // broadcast to MDNS
            if(this.multicastDNSService.unregister(mdnsServiceName)){
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
