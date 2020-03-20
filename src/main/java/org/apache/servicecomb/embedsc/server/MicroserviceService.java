package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.Lookup;
import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.MicroserviceRequest;
import org.apache.servicecomb.embedsc.server.model.ServiceType;
import org.apache.servicecomb.embedsc.server.model.ServiceFramework;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Arrays;
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
        Lookup lookup = null;
        try {
            ServiceName serviceName = new ServiceName(microserviceName + ".local.");
            lookup = new Lookup(serviceName);
            ServiceInstance[] services = lookup.lookupServices();
            for (ServiceInstance service : services) {
                Map<String, String> serviceTextAttributes = service.getTextAttributes();
                if (serviceTextAttributes != null && serviceTextAttributes.size() > 0) {
                    String returnedType = serviceTextAttributes.get("type");
                    String returnedAppId = serviceTextAttributes.get("appId");
                    String returnedMicroserviceName = serviceTextAttributes.get("serviceName");
                    String returnedVersion = serviceTextAttributes.get("version");
                    String returnedEnvironment = serviceTextAttributes.get("environment");
                    String returnedServiceId = serviceTextAttributes.get("serviceId");
                    if (returnedType.equals(ServiceType.SERVICE.name()) && returnedAppId.equals(appId)
                            && returnedMicroserviceName.equals(microserviceName) && returnedVersion.equals(versionRule)
                            && returnedEnvironment.equals(environment) && returnedServiceId != null && !returnedServiceId.isEmpty()){
                        return returnedServiceId;
                    }

                }
            }
        } catch (Exception e){
            LOGGER.error("failed to query microservice id from mdns {}/{}/{}",
                    appId,
                    microserviceName,
                    versionRule,
                    e);
        } finally {
            if (lookup != null)
            {
                try {
                    lookup.close();
                } catch (IOException e) {
                    LOGGER.error("failed to close lookup object {}", e);
                }
            }
        }
        return null;
    }

    public String registerMicroservice(MicroserviceRequest microserviceRequest) {

        if (applicationContainer.getMicroservicesByAppIdMap(microserviceRequest.getAppId()) == null){
            applicationContainer.get
        }
        // add to ApplicationContainer
        ApplicationContainer applicationContainer = new ApplicationContainer();
        applicationContainer.getMicroservicesByAppIdMap()


        try {
            ServiceName serviceName = new ServiceName(microserviceRequest.getServiceName()+ "._http._tcp.local.");
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            ServiceInstance service = new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), null, addresses, microserviceRequest.getServiceTextAttributes());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("register microservice : {} to mdns", service);
            }
            new MulticastDNSService().register(service);



            return microserviceRequest.getServiceId();
        } catch (IOException e) {
            LOGGER.error("register microservice {}/{}/{} to mdns failed",
                    microserviceRequest.getAppId(),
                    microserviceRequest.getServiceName(),
                    microserviceRequest.getVersion(),
                    e);
        }
        return null;
    }

    // for checkSchemaIdSet() method in MicroserviceRegisterTask.java
    public Microservice getMicroservice(String microserviceId) {
        Lookup lookup = null;
        try {
            ServiceName serviceName = new ServiceName("_http._tcp" + ".local.");
            lookup = new Lookup(serviceName);
            ServiceInstance[] services = lookup.lookupServices();
            for (ServiceInstance service : services) {
                Map<String, String> serviceTextAttributes = service.getTextAttributes();
                if (serviceTextAttributes != null && serviceTextAttributes.size() > 0) {
                    String returnedServiceId = serviceTextAttributes.get("serviceId");
                    if (returnedServiceId.equals(microserviceId)){
                        Microservice microservice = new Microservice();
                        microservice.setServiceId(microserviceId);
                        String returnedSchemas = serviceTextAttributes.get("schemas");
                        if(returnedSchemas != null && returnedSchemas.length() > 2) { // ["schema1", "schema2"]
                            microservice.setSchemas(Arrays.asList(returnedSchemas.substring(1, returnedSchemas.length()-1).split(",")));
                        }
                        return microservice;
                    }
                }
            }
        } catch (Exception e){
            LOGGER.error("failed to query microservice with microserviceId {} from mdns", microserviceId, e);
        } finally {
            if (lookup != null)
            {
                try {
                    lookup.close();
                } catch (IOException e) {
                    LOGGER.error("failed to close lookup object {}", e);
                }
            }
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
