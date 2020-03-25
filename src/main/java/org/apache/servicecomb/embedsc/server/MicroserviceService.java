package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
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

    public String getMicroserviceId(String appId, String microserviceName, String version) {
        ServerMicroservice serverMicroservice = ServerRegisterUtil.getApplicationContainer().getServerMicroservice(appId, microserviceName, version);
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

        //for efficient query, we put ServerMicroservice into Map <serviceId, ServerMicroservice>, and create empty Map<instanceId, ServerMicroserviceInstance>
        ServerRegisterUtil.getServerMicroserviceMap().put(serverMicroservice.getServiceId(), serverMicroservice);
        ServerRegisterUtil.getServerMicroserviceInstanceMap().computeIfAbsent(serverMicroservice.getServiceId(), k -> new ConcurrentHashMap<>());

        // build in-memory mapping relationship for App, Service, Version, ServiceInstance
        ServerRegisterUtil.buildMappingForMicroserviceRegistration(serverMicroservice);

        // no need to copy Go service-center logic to create/register instance here
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

    public boolean registerSchema(ServiceInstance mdnsService) {

        ServiceName serviceName = mdnsService.getName();
        if (serviceName != null && !serviceName.toString().isEmpty()){
            //  sample serviceName for schema:  microserviceId_schemaId._http._tcp.local.
            String mdnsServiceName = serviceName.toString().split("\\._")[0]; // microserviceId_schemaId
            Map<String, String> serviceSchemaTextAttributes = mdnsService.getTextAttributes();

            String totalChunkNumber = null;

            if (serviceSchemaTextAttributes != null && !serviceSchemaTextAttributes.isEmpty()){
                totalChunkNumber = serviceSchemaTextAttributes.get("totalChunkNumber");

                // indicate this a full schemaContent (no split), otherwise, we have to join all schemaContentChunk together for build full schemaContent
                if (totalChunkNumber == null){
                    String microserviceId = serviceSchemaTextAttributes.get("serviceId");
                    String schemaId = serviceSchemaTextAttributes.get("schemaId");
                    String schemaContent = serviceSchemaTextAttributes.get("schemaContent");

                    // keep this copy for efficient querying microservice
                    ServerMicroservice serverMicroservice = this.getMicroservice(microserviceId);
                    serverMicroservice.addSchema(schemaId, schemaContent);

                    // keep track of the mapping relationship
                    ServerMicroservice serverMicroserviceInContainer = ServerRegisterUtil.getApplicationContainer().getServerMicroservice(serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());
                    serverMicroserviceInContainer.addSchema(schemaId, schemaContent);

                    return true;
                } else {
                    // TODO find all schemaConentChunks for this <microserviceId, schemaId> and combine them together

                }
            }
        }

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
