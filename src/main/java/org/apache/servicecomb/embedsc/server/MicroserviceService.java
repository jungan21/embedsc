package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.*;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MicroserviceService{

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceService.class);

    private static ApplicationContainer applicationContainer = new ApplicationContainer();

    public List<Microservice> getAllMicroservices() {
        return null;
    }

    public String getMicroserviceId(String appId, String microserviceName, String versionRule, String environment) {
        ServerMicroservice serverMicroservice = ServerRegisterUtil.getApplicationContainer().getServerMicroservice(appId, microserviceName, versionRule);
        if (serverMicroservice != null) {
           return serverMicroservice.getServiceId();
        }

        LOGGER.debug("failed to query microservice id with appId:{}, serviceName: {}, version:{}", appId, microserviceName, versionRule);

        return null;
    }


    // TODO: called by ServiceCombMDSNServiceListener when a new service is registered
    // 1 . to build the server side mapping relationship
    // 2.  refer to: LocalServiceRegistryClientImpl.java  和 registry.yaml 文件 业务流 和数据模型
    public String registerMicroservice(ServiceInstance mdnsService) {

        // how to register Mapping ?   refer to AbstractServiceRegistry.registerMicroserviceMapping()？
        // ApplicationContainer applicationContainer = RegisterUtil.getApplicationContainer();

        ServerMicroservice serverMicroservice = ServerRegisterUtil.convertToServerMicroservice(mdnsService);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("register microservice : {}/{}/{}/ to server side in-memory map", serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());
        }

        // keep track of: <serviceId, serverMicroservice> map
        ServerRegisterUtil.getServerMicroserviceMap().put(serverMicroservice.getServiceId(), serverMicroservice);

        // register mappings
        ApplicationContainer applicationContainer = ServerRegisterUtil.getApplicationContainer();
        applicationContainer.getOrCreateServerMicroservice(serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());

        // register to local in-memory map which can reflect the mapping relationship
        ServerRegisterUtil.registerMicroservice(serverMicroservice);

        return serverMicroservice.getServiceId();
    }

    // for checkSchemaIdSet() method in MicroserviceRegisterTask.java
    public Microservice getMicroservice(String microserviceId) {
        ServerMicroservice serverMicroservice= ServerRegisterUtil.getServerMicroserviceMap().get(microserviceId);
        if(serverMicroservice != null) {
            return ServerRegisterUtil.convertToClientMicroservice(serverMicroservice);
        }
        return null;
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
