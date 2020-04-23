package org.apache.servicecomb.zeroconfigsc.client;

import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.APP_ID;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.SERVICE_ID;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.SERVICE_NAME;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.VERSION;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.STATUS;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.ENDPOINTS;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.HOST_NAME;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.MDNS_SERVICE_NAME_SUFFIX;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.MDNS_HOST_NAME_SUFFIX;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.INSTANCE_ID;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.SCHEMA_ENDPOINT_LIST_SPLITER;
import static org.apache.servicecomb.zeroconfigsc.ZeroConfigServiceRegistryConstants.UUID_SPLITER;

import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.zeroconfigsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientRegisterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegisterUtil.class);

    public static ServiceInstance convertToMDNSServiceInstance(String serviceId, String microserviceInstanceId, MicroserviceInstance microserviceInstance, IpPortManager ipPortManager, Microservice microservice) {
        try {
            ServiceName serviceName = new ServiceName( microservice.getServiceName() + MDNS_SERVICE_NAME_SUFFIX);
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceInstanceTextAttributesMap = new HashMap<>();
            serviceInstanceTextAttributesMap.put(SERVICE_ID, serviceId);
            serviceInstanceTextAttributesMap.put(INSTANCE_ID, microserviceInstanceId);
            serviceInstanceTextAttributesMap.put(STATUS, microserviceInstance.getStatus().toString());
            serviceInstanceTextAttributesMap.put(APP_ID, microservice.getAppId());
            serviceInstanceTextAttributesMap.put(SERVICE_NAME, microservice.getServiceName());
            serviceInstanceTextAttributesMap.put(VERSION, microservice.getVersion());

            // use special spliter for schema list otherwise, MDNS can't parse the string list properly i.e.  [schema1, schema2]
            // schema1$schema2
            List<String> endpoints = microserviceInstance.getEndpoints();
            StringBuilder sb = new StringBuilder();
            if ( endpoints != null && !endpoints.isEmpty()) {
                for (String endpoint : endpoints) {
                    sb.append(endpoint + SCHEMA_ENDPOINT_LIST_SPLITER);
                }
                // remove the last $
                serviceInstanceTextAttributesMap.put(ENDPOINTS,sb.toString().substring(0, sb.toString().length()-1));
            }

            String hostName = microserviceInstance.getHostName();
            serviceInstanceTextAttributesMap.put(HOST_NAME, hostName);
            Name mdnsHostName = new Name(hostName + MDNS_HOST_NAME_SUFFIX);
            return new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), mdnsHostName, addresses, serviceInstanceTextAttributesMap);
        } catch (TextParseException e) {
            LOGGER.error("microservice instance {} has invalid id", microserviceInstanceId, e);
        } catch (UnknownHostException e1) {
            LOGGER.error("microservice instance {} with Unknown Host name {}/", microserviceInstanceId, ipPortManager.getAvailableAddress().getHostOrIp(), e1);
        }
        return null;
    }

    public static MicroserviceInstance convertToClientMicroserviceInstance(ServerMicroserviceInstance serverMicroserviceInstance) {
        MicroserviceInstance microserviceInstance =  new MicroserviceInstance();
        microserviceInstance.setServiceId(serverMicroserviceInstance.getServiceId());
        microserviceInstance.setInstanceId(serverMicroserviceInstance.getInstanceId());
        microserviceInstance.setHostName(serverMicroserviceInstance.getHostName());
        microserviceInstance.setEndpoints(serverMicroserviceInstance.getEndpoints());
        microserviceInstance.setStatus(MicroserviceInstanceStatus.valueOf(serverMicroserviceInstance.getStatus()));
        return microserviceInstance;
    }

    public static String generateServiceId(Microservice microservice){
        String serviceIdStringIndex = String.join("/", microservice.getAppId(), microservice.getServiceName(), microservice.getVersion());
        return UUID.nameUUIDFromBytes(serviceIdStringIndex.getBytes()).toString().split(UUID_SPLITER)[0];
    }

    public static String generateServiceInstanceId(MicroserviceInstance microserviceInstance){
        return UUID.randomUUID().toString().split(UUID_SPLITER)[0];
    }
}
