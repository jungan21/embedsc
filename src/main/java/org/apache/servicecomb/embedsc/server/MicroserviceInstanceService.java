package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MicroserviceInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceInstanceService.class);

    public String registerMicroserviceInstance(ServiceInstance mdnsService) {
        // convernt MDNS service format to our server side format: ServerMicroserviceInstance
        ServerMicroserviceInstance serverMicroserviceInstance = ServerRegisterUtil.convertToServerMicroserviceInstance(mdnsService);
        String microserviceId =  serverMicroserviceInstance.getServiceId();
        LOGGER.info("register microservice instance : {}/{}/ to server side in-memory map", microserviceId, serverMicroserviceInstance.getInstanceId());

        //for efficient query, we put ServerMicroservice into Map <serviceId, ServerMicroservice>, and create empty Map<instanceId, ServerMicroserviceInstance>
        ServerMicroservice serverMicroservice = ServerRegisterUtil.getServerMicroserviceMap().get(microserviceId);
        serverMicroservice.addInstance(serverMicroserviceInstance);
        ServerRegisterUtil.getServerMicroserviceInstanceMap().get(serverMicroserviceInstance.getServiceId()).put(serverMicroserviceInstance.getInstanceId(), serverMicroserviceInstance);

        // build in-memory mapping relationship for App, Service, Version, ServiceInstance
        // need appId to build App -> Service -> Version -> Microservice -> Microservice Instance mapping
        serverMicroserviceInstance.setAppId(serverMicroservice.getAppId());
        ServerRegisterUtil.buildMappingForMicroserviceInstanceRegistration(serverMicroserviceInstance);

        return serverMicroserviceInstance.getInstanceId();
    }

    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
        return false;
    }

    public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
        return null;
    }

    public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId, Map<String, String> instanceProperties) {
        return false;
    }


    public HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId) {
        return null;
    }

    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {

    }

    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback, AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {

    }

    public List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName, String versionRule) {
        return null;
    }

    public MicroserviceInstances findServiceInstances(String consumerId, String appId, String serviceName, String versionRule, String revision) {
        return null;
    }

    public ServerMicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
        Map<String, ServerMicroserviceInstance>  serverMicroserviceInstanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap().get(serviceId);
        if (serverMicroserviceInstanceMap != null && !serverMicroserviceInstanceMap.isEmpty()){
            return serverMicroserviceInstanceMap.get(instanceId);
        }
        return null;
    }

    public ServiceCenterInfo getServiceCenterInfo() {
        return null;
    }

    public boolean undateMicroserviceInstanceStatus(String microserviceId, String microserviceInstanceId, String status) {
        return false;
    }

}
