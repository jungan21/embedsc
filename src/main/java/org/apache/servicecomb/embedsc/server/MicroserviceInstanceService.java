package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
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
        //  ServiceName serviceName = new ServiceName(microserviceInstanceId + "._http._tcp.local.");
        String serviceId = null;
        String instanceId = null;

        if (mdnsService != null && mdnsService.getTextAttributes() != null) {
            serviceId = (String) mdnsService.getTextAttributes().get("serviceId");
            instanceId = (String) mdnsService.getTextAttributes().get("instanceId");

            // need to check if this id already exists in server side
            ServerMicroserviceInstance serverMicroserviceInstance = ServerRegisterUtil.getServerMicroserviceInstanceMap().get(serviceId).get(instanceId);

            // update existing service instance properties
            if (serverMicroserviceInstance != null){
                Map<String, String> newPropertiesMap = ServerRegisterUtil.convertMapStringToMap((String)mdnsService.getTextAttributes().get("properties"));
                serverMicroserviceInstance.getProperties().putAll(newPropertiesMap);
                serverMicroserviceInstance.setStatus((String)mdnsService.getTextAttributes().get("status"));
                ServerRegisterUtil.buildMappingForMicroserviceInstanceRegistration(serverMicroserviceInstance);
            } else {
                // register new service instance
                // convernt MDNS service instance format to our server side format: ServerMicroserviceInstance
                ServerMicroserviceInstance newServerMicroserviceInstance = ServerRegisterUtil.convertToServerMicroserviceInstance(mdnsService);
                serviceId =  newServerMicroserviceInstance.getServiceId();
                LOGGER.info("register microservice instance : {}/{}/ to server side in-memory map", serviceId, newServerMicroserviceInstance.getInstanceId());

                //for easy query: updaate serverMicroservice instance property and also put ServerMicroserviceInstance into Map<instanceId, ServerMicroserviceInstance>
                ServerMicroservice serverMicroservice = ServerRegisterUtil.getServerMicroserviceMap().get(serviceId);
                serverMicroservice.addInstance(newServerMicroserviceInstance);
                ServerRegisterUtil.getServerMicroserviceInstanceMap().get(newServerMicroserviceInstance.getServiceId()).put(newServerMicroserviceInstance.getInstanceId(), newServerMicroserviceInstance);

                // build mapping for App, Service, Version, ServiceInstance. need appId to build App -> Service -> Version -> Microservice -> Microservice Instance mapping
                newServerMicroserviceInstance.setAppId(serverMicroservice.getAppId());
                ServerRegisterUtil.buildMappingForMicroserviceInstanceRegistration(newServerMicroserviceInstance);

            }
        }

        return instanceId;
    }

    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {

        // delete it from  map
        ServerMicroservice serverMicroservice = ServerRegisterUtil.getServerMicroserviceMap().get(microserviceId);
        if (serverMicroservice != null && serverMicroservice.getInstances() != null){
            serverMicroservice.removeInstance(microserviceInstanceId);
        }
        ServerRegisterUtil.getServerMicroserviceInstanceMap().get(microserviceId).remove(microserviceInstanceId);

        // delete it from mapping container
        ApplicationContainer applicationContainer = ServerRegisterUtil.getApplicationContainer();
        ServerMicroservice serverMicroserviceInMapping = applicationContainer.getServerMicroservice(serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());
        Map<String, ServerMicroserviceInstance> instancesMap =  serverMicroserviceInMapping.getInstances();
        if(instancesMap != null && instancesMap.containsKey(microserviceInstanceId)){
            instancesMap.remove(microserviceInstanceId);
        }
        return true;
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
        return (serverMicroserviceInstanceMap != null && !serverMicroserviceInstanceMap.isEmpty()) ? serverMicroserviceInstanceMap.get(instanceId) : null;
    }

    public ServiceCenterInfo getServiceCenterInfo() {
        return null;
    }

    public boolean undateMicroserviceInstanceStatus(String microserviceId, String microserviceInstanceId, String status) {
        return false;
    }

}
