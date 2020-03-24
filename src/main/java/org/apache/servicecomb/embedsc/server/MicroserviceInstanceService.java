package org.apache.servicecomb.embedsc.server;

import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MicroserviceInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceInstanceService.class);

    public String registerMicroserviceInstance(ServerMicroserviceInstance serverMicroserviceInstance) {
//        try {
//            ServiceName serviceName = new ServiceName(serverMicroserviceInstance.getInstanceId() + "._http._tcp.local.");
//            Name hostname = new Name(serverMicroserviceInstance.getHostName() + ".local.");
//            IpPort ipPort = ipPortManager.getAvailableAddress();
//            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};
//            ServiceInstance serviceInstance = new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), hostname, addresses, "");
//
//            if (LOGGER.isDebugEnabled()) {
//                LOGGER.debug("register microservice instance: {} to mdns", serviceInstance);
//            }
//
//            ServiceInstance registeredService = new MulticastDNSService().register(serviceInstance);
//            return instance.getInstanceId();
//        } catch (IOException e) {
//            LOGGER.error("register microservice instance to mdns {} failed", instance, e);
//        }
        return null;
    }

    public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
        return false;
    }

    public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
        return null;
    }

    public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId, Map<String, String> instanceProperties) {
        return false;
    }


    public HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId) {
        return null;
    }

    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {

    }

    public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback, AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {

    }

    public List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName, String versionRule) {
        return null;
    }

    public MicroserviceInstances findServiceInstances(String consumerId, String appId, String serviceName, String versionRule, String revision) {
        return null;
    }

    public MicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
        return null;
    }

    public ServiceCenterInfo getServiceCenterInfo() {
        return null;
    }

    public boolean undateMicroserviceInstanceStatus(String microserviceId, String microserviceInstanceId, String status) {
        return false;
    }

}
