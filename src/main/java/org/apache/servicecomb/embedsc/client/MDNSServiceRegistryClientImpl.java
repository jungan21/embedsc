package org.apache.servicecomb.embedsc.client;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.client.util.ClientRegisterUtil;
import org.apache.servicecomb.embedsc.server.MicroserviceInstanceService;
import org.apache.servicecomb.embedsc.server.MicroserviceService;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.api.registry.*;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
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

    private MicroserviceService microserviceService;
    private MicroserviceInstanceService microserviceInstanceService;

    public MDNSServiceRegistryClientImpl(ServiceRegistryConfig serviceRegistryConfig, InstanceCacheManager instanceCacheManager){
        this.ipPortManager = new IpPortManager(serviceRegistryConfig, instanceCacheManager);
        try {
            this.multicastDNSService = new MulticastDNSService();
        } catch (IOException e) {
            LOGGER.error("Failed to create MulticastDNSService object", e);
        }
        this.microserviceService = new MicroserviceService();
        this.microserviceInstanceService = new MicroserviceInstanceService();
    }

    @Override
    public void init() {

    }

    @Override
    public List<Microservice> getAllMicroservices() {
        return microserviceService.getAllMicroservices();
    }

    @Override
    public String getMicroserviceId(String appId, String microserviceName, String versionRule, String environment) {
        return microserviceService.getMicroserviceId(appId, microserviceName, versionRule);
    }

    @Override
    public String registerMicroservice(Microservice microservice) {
        String serviceId = microservice.getServiceId();
        if (serviceId == null || serviceId.length() == 0){
            // follow Go service center logic to generate serviceId based on the appId, serviceName and version
            serviceId = ClientRegisterUtil.generateServiceId(microservice);
        }

        try {
            ServiceInstance service = ClientRegisterUtil.convertToMDNSServiceInstance(serviceId, microservice, this.ipPortManager);
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
        return null;
    }

    @Override
    public boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties) {
        return false;
    }

    @Override
    public boolean isSchemaExist(String microserviceId, String schemaId) {
        return false;
    }

    @Override
    public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
        Microservice microservice = this.getMicroservice(microserviceId);
        if (microservice == null) {
            LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}", microserviceId);
            return false;
        }

        try {


            // from mdns source code
//            NetworkProcessor.java
//                    ===
//            // Normally MTU size is 1500, but can be up to 9000 for jumbo frames.
//            public static final int DEFAULT_MTU = 1500;
//
//            DatagramProcessor.java
//                    ===
//            // The default UDP datagram payload size
//            protected int maxPayloadSize = 512;
//
//            // Determine maximum mDNS Payload size
//
//            maxPayloadSize = mtu (default 1500) - 40 /* IPv6 Header Size */- 8 /* UDP Header */;

            // suggest

            // TODO:check schemaContent size if it's > 512 bytes (UDP limits), have to chunk the data
            // TODO: Querier define default UDP size is 512
            // https://tools.ietf.org/html/rfc6762#page-51 UTF-8 format
            // https://tools.ietf.org/html/rfc6762#page-46  payload size ~1500 bytes DNS UDP: 512, MDNS UDP: 1500
            ServiceInstance service = ClientRegisterUtil.convertToMDNSServiceInstance(microserviceId, schemaId, schemaContent, this.ipPortManager);
            // broadcast to MDNS

            this.multicastDNSService.register(service);
        } catch (IOException e) {
            LOGGER.error("Failed to register schemaId: {} for microserviceId: {} to MDNS. The schema content is: {}",schemaId, microserviceId, schemaContent, e);
            return false;
        }
        return true;
    }

    @Override
    public String getSchema(String microserviceId, String schemaId) {
        String schemaContent = null;
        Microservice microservice = this.getMicroservice(microserviceId);
        if (microservice == null) {
            LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}", microserviceId);
            return schemaContent;
        }

        Map<String, String> schemaMap = microservice.getSchemaMap();
        if (schemaMap != null && !schemaMap.isEmpty()){
            schemaContent = schemaMap.get(schemaId);
        }
        return schemaContent;
    }

    @Override
    public String getAggregatedSchema(String microserviceId, String schemaId) {
        return null;
    }

    @Override
    public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {
        // this method is called as part of the doRegister() method within MicroserviceRegisterTask.java
        Microservice microservice = this.getMicroservice(microserviceId);
        if (microservice == null) {
            LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}", microserviceId);
        }
        List<GetSchemaResponse> schemas = new ArrayList<>();
        microservice.getSchemaMap().forEach((key, val) -> {
            GetSchemaResponse schema = new GetSchemaResponse();
            schema.setSchema(val);
            schema.setSchemaId(key);
            schema.setSummary(Hashing.sha256().newHasher().putString(val, Charsets.UTF_8).hash().toString());
            schemas.add(schema);
        });
        Holder<List<GetSchemaResponse>> resultHolder = new Holder<>();
        resultHolder.setStatusCode(Response.Status.OK.getStatusCode()).setValue(schemas);
        return resultHolder;
    }

    @Override
    public String registerMicroserviceInstance(MicroserviceInstance instance) {
        return null;
        //return microserviceInstanceService.registerMicroserviceInstance(instance);
    }

    @Override
    public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
        return null;
    }

    @Override
    public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId, Map<String, String> instanceProperties) {
        return false;
    }

    @Override
    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
        return false;
    }

    @Override
    public HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId) {
        return null;
    }

    @Override
    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {

    }

    @Override
    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback, AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {

    }

    @Override
    public List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName, String versionRule) {
        return null;
    }

    @Override
    public MicroserviceInstances findServiceInstances(String consumerId, String appId, String serviceName, String versionRule, String revision) {
        return null;
    }

    @Override
    public MicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
        return null;
    }

    @Override
    public ServiceCenterInfo getServiceCenterInfo() {
        return null;
    }

    @Override
    public boolean undateMicroserviceInstanceStatus(String microserviceId, String microserviceInstanceId, String status) {
        return false;
    }

}
