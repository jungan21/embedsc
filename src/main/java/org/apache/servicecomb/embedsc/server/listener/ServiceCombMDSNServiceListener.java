package org.apache.servicecomb.embedsc.server.listener;

import net.posick.mDNS.DNSSDListener;
import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.server.MicroserviceInstanceService;
import org.apache.servicecomb.embedsc.server.MicroserviceService;
import org.apache.servicecomb.embedsc.server.model.RegisterServiceType;
import org.apache.servicecomb.embedsc.server.util.ServerRegisterUtil;
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
        LOGGER.debug("Microservice registered to MDNS, {}", service);

        if(service != null && service.getTextAttributes() != null && !service.getTextAttributes().isEmpty()) {
            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
            String registerServiceType = serviceTextAttributesMap.get(ServerRegisterUtil.registerServiceType);

            if (registerServiceType.equals(RegisterServiceType.MICROSERVICE)){
                microserviceService.registerMicroservice(service);
            } else if (registerServiceType.equals(RegisterServiceType.MICROSERVICE_INSTANCE)){
                // TODO: microserviceInstanceService.registerMicroserviceInstance(service);
            } else if (registerServiceType.equals(RegisterServiceType.MICROSERVICE_SCHEMA)){
                // TODO: register schema for microservice
            } else {
                LOGGER.error("Unrecognized service type {}", registerServiceType);
            }
        }

    }

    // TODO
    // two scenarios: 1. client explicitly call unregister 2. client service process is killed
    public void serviceRemoved(Object id, ServiceInstance service) {
        LOGGER.debug("Microservice unregistered from MDNS, {}", service);
        // TODO: remove the service/serviceInstance from in-meory Map
    }

    public void handleException(Object id, Exception e) {
        LOGGER.error("Microservice register/unregister to/from MDNS exception", e);
    }

    public void receiveMessage(Object id, Message message) {
        // ignore
    }
}
