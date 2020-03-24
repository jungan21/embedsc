package org.apache.servicecomb.embedsc.client;

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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        return microserviceService.getMicroserviceId(appId, microserviceName, versionRule, environment);
    }

    @Override
    public String registerMicroservice(Microservice microservice) {
        String serviceId = microservice.getServiceId();
        if (serviceId== null || serviceId.length() == 0){
            // generate serviceId based on the appId, serviceName and version
            serviceId = UUID.nameUUIDFromBytes(ClientRegisterUtil.generateServiceIndexKey(microservice).getBytes()).toString();
        }

        try {
            ServiceInstance service =ClientRegisterUtil.convertToMDNSServiceInstance(serviceId, microservice, this.ipPortManager);
            // broadcast to MDNS
            this.multicastDNSService.register(service);
            return serviceId;
        } catch (IOException e) {
            LOGGER.error("Failed to register microservice to mdns {}/{}/{}", microservice.getAppId(), microservice.getServiceName(), microservice.getVersion(), e);
        }
        return null;
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
        return false;
    }

    @Override
    public String getSchema(String microserviceId, String schemaId) {
        return null;
    }

    @Override
    public String getAggregatedSchema(String microserviceId, String schemaId) {
        return null;
    }

    @Override
    public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {
        return null;
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
