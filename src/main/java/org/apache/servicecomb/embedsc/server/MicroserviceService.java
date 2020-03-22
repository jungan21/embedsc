package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.util.RegisterUtil;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
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

    private IpPortManager ipPortManager;

    private  static ApplicationContainer applicationContainer = new ApplicationContainer();;

    public MicroserviceService(IpPortManager ipPortManager){
        this.ipPortManager = ipPortManager;
    }

    public List<Microservice> getAllMicroservices() {
        return null;
    }

    public String getMicroserviceId(String appId, String microserviceName, String versionRule, String environment) {
        ServerMicroservice serverMicroservice = RegisterUtil.getApplicationContainer().getServerMicroservice(appId, microserviceName, versionRule);
        if (serverMicroservice != null) {
           return serverMicroservice.getServiceId();
        }

        LOGGER.debug("failed to query microservice id with appId:{}, serviceName: {}, version:{}", appId, microserviceName, versionRule);

        return null;
    }

    public String registerMicroservice(ServerMicroservice serverMicroservice) {

        // how to register Mapping ?   refer to AbstractServiceRegistry.registerMicroserviceMapping()？
        // ApplicationContainer applicationContainer = RegisterUtil.getApplicationContainer();

        try {
            ServiceName serviceName = new ServiceName(serverMicroservice.getServiceName()+ "._http._tcp.local.");
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            // set the timestamp
            serverMicroservice.setTimestamp(String.valueOf(Instant.now().getEpochSecond()));
            serverMicroservice.setModTimestamp(serverMicroservice.getTimestamp());

            ServiceInstance service = new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), null, addresses, serverMicroservice.getServiceTextAttributesMap());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("register microservice : {} to mdns", service);
            }

            // register to  MDNS
            new MulticastDNSService().register(service);

            // keep track of: <serviceId, serverMicroservice> map
            RegisterUtil.getServerMicroserviceMap().put(serverMicroservice.getServiceId(), serverMicroservice);

            // register mappings
            ApplicationContainer applicationContainer = RegisterUtil.getApplicationContainer();
            applicationContainer.getOrCreateServerMicroservice(serverMicroservice.getAppId(), serverMicroservice.getServiceName(), serverMicroservice.getVersion());

            // register to local in-memory map which can reflect the mapping relationship
            RegisterUtil.registerMicroservice(serverMicroservice);

            return serverMicroservice.getServiceId();
        } catch (IOException e) {
            LOGGER.error("register microservice {}/{}/{} to mdns failed",
                    serverMicroservice.getAppId(),
                    serverMicroservice.getServiceName(),
                    serverMicroservice.getVersion(),
                    e);
        }

        return null;
    }

    // for checkSchemaIdSet() method in MicroserviceRegisterTask.java
    public Microservice getMicroservice(String microserviceId) {
        ServerMicroservice serverMicroservice= RegisterUtil.getServerMicroserviceMap().get(microserviceId);
        if(serverMicroservice != null) {
            return RegisterUtil.convertToClientMicroservice(serverMicroservice);
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
