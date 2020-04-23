package org.apache.servicecomb.zeroconfigsc.server;

import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.zeroconfigsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.zeroconfigsc.server.util.ServerRegisterUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.*;

public class MicroserviceInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceInstanceService.class);

    public String registerMicroserviceInstance(ServiceInstance mdnsService) {
        //  ServiceName serviceName = new ServiceName(microserviceInstanceId + "._http._tcp.local.");

        String instanceId = null;
        if (mdnsService != null && mdnsService.getTextAttributes() != null) {
            String serviceId = (String) mdnsService.getTextAttributes().get(SERVICE_ID);
            String serviceName = (String) mdnsService.getTextAttributes().get(SERVICE_NAME);
            String version = (String) mdnsService.getTextAttributes().get(VERSION);
            instanceId = (String) mdnsService.getTextAttributes().get(INSTANCE_ID);

            if ( serviceId == null || serviceName == null || instanceId == null ) {
                LOGGER.error("serviceId: {} is null OR  instanceId: {} is null OR serviceName: {} is null", serviceId, instanceId, serviceName);
                return null;
            }

            Map<String, ServerMicroserviceInstance> innerInstanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap().computeIfAbsent(serviceId, id -> new ConcurrentHashMap<>());
            // for Client to easily discover the instance's endpoints by serviceName
            List<ServerMicroserviceInstance> innerInstanceByServiceNameList = ServerRegisterUtil.getserverMicroserviceInstanceMapByServiceName().computeIfAbsent(serviceName, name -> new ArrayList<>());

            // convert to service side ServerMicroserviceInstance object
            ServerMicroserviceInstance newServerMicroserviceInstance = ServerRegisterUtil.convertToServerMicroserviceInstance(mdnsService);
            innerInstanceByServiceNameList.add(newServerMicroserviceInstance);

            if (innerInstanceMap.containsKey(instanceId)) {
                // update existing instance
                LOGGER.info("Update existing microservice instance. serviceId: {}, instanceId: {}", serviceId, instanceId);
                innerInstanceMap.get(instanceId).setStatus((String)mdnsService.getTextAttributes().get(STATUS));
            } else {
                // register a new instance for the instance
                LOGGER.info("Register a new instance for  serviceId: {}, instanceId: {}", serviceId, instanceId);
                innerInstanceMap.put(instanceId, newServerMicroserviceInstance);
            }
        }
        return instanceId;
    }

    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
        Map<String, ServerMicroserviceInstance> innerInstanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap().get(microserviceId);
        if (innerInstanceMap != null && innerInstanceMap.containsKey(microserviceInstanceId)){
            ServerMicroserviceInstance instanceToBeRemoved = innerInstanceMap.get(microserviceInstanceId);
            innerInstanceMap.remove(microserviceInstanceId);

            List<ServerMicroserviceInstance> innerInstanceByServiceNameList = ServerRegisterUtil.getserverMicroserviceInstanceMapByServiceName().get(instanceToBeRemoved.getServiceName());
            for (ServerMicroserviceInstance instance : innerInstanceByServiceNameList){
                if (instance.getInstanceId().equals(microserviceInstanceId)){
                    innerInstanceByServiceNameList.remove(instance);
                }
            }
        } else {
            LOGGER.warn("ServiceId: {},  InstanceId: {} doesn't exist in server side", microserviceId, microserviceInstanceId);
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

    public boolean heartbeat(String microserviceId, String microserviceInstanceId) {
       Map<String, ServerMicroserviceInstance>  serverMicroserviceInstanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap().get(microserviceId);
       return serverMicroserviceInstanceMap != null && serverMicroserviceInstanceMap.containsKey(microserviceInstanceId);
    }

}
