package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.*;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MicroserviceService{

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceService.class);

    public List<Microservice> getAllMicroservices() {
        return null;
    }

    public String getMicroserviceId(String appId, String microserviceName, String versionRule, String environment) {
        // environment is ignored
        ServerMicroservice serverMicroservice = ServerRegisterUtil.getApplicationContainer().getServerMicroservice(appId, microserviceName, versionRule);
        return serverMicroservice != null ? serverMicroservice.getServiceId() : null;
    }

    public ServerMicroservice getMicroservice(String microserviceId) {
        ServerMicroservice serverMicroservice= ServerRegisterUtil.getServerMicroserviceMap().get(microserviceId);
        return serverMicroservice;
    }

    // TODO: called by ServiceCombMDSNServiceListener when a new service is registered
    // 1 . to build the server side mapping relationship
    // 2.  refer to: LocalServiceRegistryClientImpl.java  和 registry.yaml 文件 业务流 和数据模型
    // 3.  refer to  AbstractServiceRegistry.registerMicroserviceMapping()
    public String registerMicroservice(ServiceInstance mdnsService) {

        // convernt MDNS service format to our server side format: ServerMicroservice
        ServerMicroservice serverMicroservice = ServerRegisterUtil.convertToServerMicroservice(mdnsService);

        LOGGER.info("register microservice : {}/{}/{}/ to server side in-memory map", serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());

        /**
         *  keep track of:
         *   Map <serviceId, ServerMicroservice>
         *   Map <serviceId, Map<instanceId, ServerMicroservice>>
         */

        ServerRegisterUtil.getServerMicroserviceMap().put(serverMicroservice.getServiceId(), serverMicroservice);
        // if serviceId doesn't exist in Map <serviceId, Map<instanceId, ServerMicroservice>>, then create empty Map for Map<instanceId, ServerMicroservice>
        // when register for serviceInstance, we will put the instance into the map
        ServerRegisterUtil.getServerMicroserviceInstanceMap().computeIfAbsent(serverMicroservice.getServiceId(), k -> new ConcurrentHashMap<>());


        // register microservice mappings
        ApplicationContainer applicationContainer = ServerRegisterUtil.getApplicationContainer();
        applicationContainer.getOrCreateServerMicroservice(serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());

        // register to local in-memory map which can reflect the mapping relationship
        // TODO TODO TODO
        ServerRegisterUtil.registerMicroserviceMapping(serverMicroservice);

        // register this microservice instance
        Map<String, ServerMicroserviceInstance> instanceMap = serverMicroservice.getInstances();
        if (instanceMap != null && !instanceMap.isEmpty()){
            for (ServerMicroserviceInstance serverMicroserviceInstance : instanceMap.values()){
                applicationContainer.getOrCreateServerMicroserviceInstance(serverMicroserviceInstance.getAppId(), serverMicroserviceInstance.getServiceName(),
                        serverMicroserviceInstance.getVersion(), serverMicroserviceInstance.getInstanceId());
            }
        }
        // TODO TODO TODO

        return serverMicroservice.getServiceId();

    }


    public Microservice getAggregatedMicroservice(String microserviceId) {
        return null;
    }

    public boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties) {
        return false;
    }

    public boolean isSchemaExist(String microserviceId, String schemaId) {
        return false;
    }

    public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
        return false;
    }

    public String getSchema(String microserviceId, String schemaId) {
        return null;
    }

    public String getAggregatedSchema(String microserviceId, String schemaId) {
        return null;
    }

    public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {

        return null;
    }

}
