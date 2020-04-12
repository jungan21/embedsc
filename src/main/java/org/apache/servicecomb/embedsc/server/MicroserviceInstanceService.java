package org.apache.servicecomb.embedsc.server;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.SERVICE_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.INSTANCE_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.PROPERTIES;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.STATUS;

import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MicroserviceInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceInstanceService.class);

    public String registerMicroserviceInstance(ServiceInstance mdnsService) {
        //  ServiceName serviceName = new ServiceName(microserviceInstanceId + "._http._tcp.local.");
        String serviceId = null;
        String instanceId = null;

        if (mdnsService != null && mdnsService.getTextAttributes() != null) {
            serviceId = (String) mdnsService.getTextAttributes().get(SERVICE_ID);
            instanceId = (String) mdnsService.getTextAttributes().get(INSTANCE_ID);

            // need to check if this id already exists in server side
            ServerMicroserviceInstance serverMicroserviceInstance = null;
            Map<String, ServerMicroserviceInstance> serverMicroserviceInstanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap().get(serviceId);
            if (serverMicroserviceInstanceMap != null && !serverMicroserviceInstanceMap.isEmpty()) {
                serverMicroserviceInstance = serverMicroserviceInstanceMap.get(instanceId);
            }

            if (serverMicroserviceInstance != null){
                // update existing service instance properties
                Map<String, String> newPropertiesMap = ServerRegisterUtil.convertMapStringToMap((String)mdnsService.getTextAttributes().get(PROPERTIES));
                serverMicroserviceInstance.getProperties().putAll(newPropertiesMap);
                serverMicroserviceInstance.setStatus((String)mdnsService.getTextAttributes().get(STATUS));
                ServerRegisterUtil.buildMappingForMicroserviceInstanceRegistration(serverMicroserviceInstance);
            } else {
                // register new service instance
                // convernt MDNS service instance format to our server side format: ServerMicroserviceInstance
                ServerMicroserviceInstance newServerMicroserviceInstance = ServerRegisterUtil.convertToServerMicroserviceInstance(mdnsService);
                serviceId =  newServerMicroserviceInstance.getServiceId();
                LOGGER.info("register microservice instance , serviceId: {}, instanceId: {}  to server side in-memory map", serviceId, newServerMicroserviceInstance.getInstanceId());

                //for easy query: updaate serverMicroservice instance property and also put ServerMicroserviceInstance into Map<instanceId, ServerMicroserviceInstance>
                ServerMicroservice serverMicroservice = ServerRegisterUtil.getServerMicroserviceMap().get(serviceId);
                serverMicroservice.addInstance(newServerMicroserviceInstance);
                ServerRegisterUtil.getServerMicroserviceInstanceMap().get(newServerMicroserviceInstance.getServiceId()).put(newServerMicroserviceInstance.getInstanceId(), newServerMicroserviceInstance);

                // build mapping for App, Service, Version, ServiceInstance. need appId to build App -> Service -> Version -> Microservice -> Microservice Instance mapping
                newServerMicroserviceInstance.setAppId(serverMicroservice.getAppId());
                newServerMicroserviceInstance.setServiceName(serverMicroservice.getServiceName());
                newServerMicroserviceInstance.setVersion(serverMicroservice.getVersion());
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

    public ServerMicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
        Map<String, ServerMicroserviceInstance>  serverMicroserviceInstanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap().get(serviceId);
        return (serverMicroserviceInstanceMap != null && !serverMicroserviceInstanceMap.isEmpty()) ? serverMicroserviceInstanceMap.get(instanceId) : null;
    }

    public List<ServerMicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
        Map<String, ServerMicroserviceInstance> instanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap().get(providerId);
        return instanceMap == null || instanceMap.isEmpty() ? null : new ArrayList<>(instanceMap.values());
    }

}
