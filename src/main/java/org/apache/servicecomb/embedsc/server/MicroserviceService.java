package org.apache.servicecomb.embedsc.server;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.SERVICE_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.PROPERTIES;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_CONTENT;

import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MicroserviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceService.class);

    public List<ServerMicroservice> getAllMicroservices() {
        return new ArrayList<>(ServerRegisterUtil.getServerMicroserviceMap().values());
    }

    public String getMicroserviceId(String appId, String microserviceName, String version) {
        ServerMicroservice serverMicroservice = ServerRegisterUtil.getApplicationContainer().getServerMicroservice(appId, microserviceName, version);
        return serverMicroservice != null ? serverMicroservice.getServiceId() : null;
    }

    public ServerMicroservice getMicroservice(String microserviceId) {
        return ServerRegisterUtil.getServerMicroserviceMap().get(microserviceId);
    }

    //  called by ServiceCombMDSNServiceListener when a new service is registered/broadcasted through MDNS
    public String registerMicroservice(ServiceInstance mdnsService) {

        String serviceId = null;
        // need to check if this id already exists in server side
        if (mdnsService != null && mdnsService.getTextAttributes() != null){
            serviceId = (String) mdnsService.getTextAttributes().get(SERVICE_ID);
            ServerMicroservice serverMicroservice =  ServerRegisterUtil.getServerMicroserviceMap().get(serviceId);

            if (serverMicroservice != null){
                // update existing service properties
                Map<String, String> newPropertiesMap = ServerRegisterUtil.convertMapStringToMap((String)mdnsService.getTextAttributes().get(PROPERTIES));
                serverMicroservice.getProperties().putAll(newPropertiesMap);
                ServerRegisterUtil.buildMappingForMicroserviceRegistration(serverMicroservice);
            } else {
                // register new service
                // convernt MDNS service format to our server side format: ServerMicroservice
                ServerMicroservice newServerMicroservice = ServerRegisterUtil.convertToServerMicroservice(mdnsService);

                LOGGER.info("register microservice : {}/{}/{}/ to server side in-memory map", newServerMicroservice.getAppId(), newServerMicroservice.getServiceName(), newServerMicroservice.getVersion());

                //for easy query, put ServerMicroservice into Map<serviceId, ServerMicroservice>, and create empty Map<instanceId, ServerMicroserviceInstance> for holding ServerMicroserviceInsance
                Map<String, ServerMicroservice> serverMicroserviceMap = ServerRegisterUtil.getServerMicroserviceMap();

                serverMicroserviceMap.put(newServerMicroservice.getServiceId(), newServerMicroservice);
                LOGGER.info("Jun DEBUG serverMicroserviceMap {}", serverMicroserviceMap);
                Map<String, Map<String, ServerMicroserviceInstance>> serverMicroserviceInstanceMap =  ServerRegisterUtil.getServerMicroserviceInstanceMap();
                serverMicroserviceInstanceMap.computeIfAbsent(newServerMicroservice.getServiceId(), k -> new ConcurrentHashMap<>());
                LOGGER.info("Jun DEBUG serverMicroserviceInstanceMap {}", serverMicroserviceInstanceMap);
                // build mapping for App, Service, Version, ServiceInstance objects
                ServerRegisterUtil.buildMappingForMicroserviceRegistration(newServerMicroservice);
            }
        }
        return serviceId;
    }

    public boolean isSchemaExist(String microserviceId, String schemaId) {
        if(microserviceId == null || schemaId == null){
            LOGGER.error("Can't run isSchemaExist check for null microserviceId:{} or schemaId: {}", microserviceId, schemaId);
            return false;
        }

        List<String> schemaList = this.getMicroservice(microserviceId).getSchemas();
        return schemaList != null && schemaList.contains(schemaId);
    }

    public boolean registerSchema(ServiceInstance mdnsService) {
        ServiceName serviceName = mdnsService.getName();

        if (serviceName != null && !serviceName.toString().isEmpty()){
            Map<String, String> serviceSchemaTextAttributes = mdnsService.getTextAttributes();

            if (serviceSchemaTextAttributes != null && !serviceSchemaTextAttributes.isEmpty()){

                String microserviceId = serviceSchemaTextAttributes.get(SERVICE_ID);
                String schemaId = serviceSchemaTextAttributes.get(SCHEMA_ID);
                String schemaContent = serviceSchemaTextAttributes.get(SCHEMA_CONTENT);

                // keep this copy for efficient querying microservice
                ServerMicroservice serverMicroservice = this.getMicroservice(microserviceId);
                serverMicroservice.addSchema(schemaId, schemaContent);

                // keep track of the mapping relationship
                ServerMicroservice serverMicroserviceInMappingContainer = ServerRegisterUtil.getApplicationContainer().
                        getServerMicroservice(serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());
                serverMicroserviceInMappingContainer.addSchema(schemaId, schemaContent);
                return true;
            }
        }
        return false;
    }

}
