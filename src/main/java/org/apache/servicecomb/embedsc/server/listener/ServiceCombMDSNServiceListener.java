package org.apache.servicecomb.embedsc.server.listener;

import net.posick.mDNS.DNSSDListener;
import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.EmbedSCConstants;
import org.apache.servicecomb.embedsc.server.MicroserviceInstanceService;
import org.apache.servicecomb.embedsc.server.MicroserviceService;
import org.apache.servicecomb.embedsc.server.model.RegisterServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.util.Map;

public class ServiceCombMDSNServiceListener implements DNSSDListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombMDSNServiceListener.class);

    private MicroserviceService microserviceService;
    private MicroserviceInstanceService microserviceInstanceService;

    public ServiceCombMDSNServiceListener(){
        super();
        this.microserviceService = new MicroserviceService();
    }

    public void serviceDiscovered(Object id, ServiceInstance service) {
        LOGGER.debug("Microservice registered to MDNS server {}", service);

        if(service != null && service.getTextAttributes() != null && !service.getTextAttributes().isEmpty()) {
            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
            String registerServiceType = serviceTextAttributesMap.get(EmbedSCConstants.REGISTER_SERVICE_TYPE);

            if (registerServiceType.equals(RegisterServiceType.MICROSERVICE)){
                microserviceService.registerMicroservice(service);
            } else if (registerServiceType.equals(RegisterServiceType.MICROSERVICE_INSTANCE)){
                microserviceInstanceService.registerMicroserviceInstance(service);
            } else if (registerServiceType.equals(RegisterServiceType.MICROSERVICE_SCHEMA)){
                microserviceService.registerSchema(service);
            } else {
                LOGGER.error("Unrecognized service type {} when during registration", registerServiceType);
            }
        }
    }

    // two scenarios: 1. client explicitly call unregister 2. client service process is killed
    public void serviceRemoved(Object id, ServiceInstance service) {
        LOGGER.debug("Microservice unregistered from MDNS server {}", service);

        if(service != null && service.getTextAttributes() != null && !service.getTextAttributes().isEmpty()) {
            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
            String registerServiceType = serviceTextAttributesMap.get(EmbedSCConstants.REGISTER_SERVICE_TYPE);

            if (registerServiceType.equals(RegisterServiceType.MICROSERVICE_INSTANCE.toString())){
                microserviceInstanceService.unregisterMicroserviceInstance(serviceTextAttributesMap.get("serviceId"), serviceTextAttributesMap.get("instanceId"));
            } else {
                LOGGER.error("Unrecognized service type {} during unregistration", registerServiceType);
            }
        }
    }

    public void handleException(Object id, Exception e) {
        LOGGER.error("Running into erros when registering/unregistering to/from MDNS service registry center", e);
    }

    public void receiveMessage(Object id, Message message) {
        // ignore
    }
}
