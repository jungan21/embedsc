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
        if(service != null) {
            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
            if (serviceTextAttributesMap != null && !serviceTextAttributesMap.isEmpty()) {
                String registerServiceType = serviceTextAttributesMap.get(ServerRegisterUtil.registerServiceType);
                if (registerServiceType.equals(RegisterServiceType.MICROSERVICE)){
                    microserviceService.registerMicroservice(service);
                } else {
                 //   microserviceInstanceService.registerMicroserviceInstance(service);
                }
            }
        }
    }

    public void serviceRemoved(Object id, ServiceInstance service) {
        LOGGER.debug("Microservice unregistered from MDNS, {}", service);
        // ignore as there is no unregister method for microservice s
    }

    public void handleException(Object id, Exception e) {
        // ignore
    }

    public void receiveMessage(Object id, Message message) {
        // ignore
    }
}
