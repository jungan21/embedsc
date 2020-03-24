package org.apache.servicecomb.embedsc.client.util;

import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.TextParseException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ClientRegisterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegisterUtil.class);

    public static ServiceInstance convertMicroserviceToMDNSServiceInstance(String serviceId, Microservice microservice, IpPortManager ipPortManager) {
        try {
            ServiceName serviceName = new ServiceName(microservice.getServiceName()+ "._http._tcp.local.");
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceTextAttributesMap = new HashMap<>();
            serviceTextAttributesMap.put("appId", microservice.getAppId());
            serviceTextAttributesMap.put("serviceName", microservice.getServiceName());
            serviceTextAttributesMap.put("version", microservice.getVersion());
            serviceTextAttributesMap.put("serviceId", serviceId);
            serviceTextAttributesMap.put("level", microservice.getLevel());
            serviceTextAttributesMap.put("alias", microservice.getAlias());
            serviceTextAttributesMap.put("schemas", microservice.getSchemas().toString());
            serviceTextAttributesMap.put("status", microservice.getStatus());
            serviceTextAttributesMap.put("description", microservice.getDescription());
            serviceTextAttributesMap.put("registerBy", microservice.getRegisterBy());
            serviceTextAttributesMap.put("environment", microservice.getEnvironment());
            serviceTextAttributesMap.put("properties", microservice.getProperties().toString());
            // Framework object has name and value attributes
            Map<String, String> framework = new HashMap<>();
            framework.put("name", microservice.getFramework().getName());
            framework.put("version", microservice.getFramework().getVersion());
            serviceTextAttributesMap.put("framework", framework.toString());
            // set the timestamp
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            serviceTextAttributesMap.put("timestamp", timestamp);
            serviceTextAttributesMap.put("modTimestamp", timestamp);

            // Map<schemaId, schemaContent> too big
            // serviceTextAttributesMap.put("schemaMap", microservice.getSchemaMap().toString());

            ServiceInstance service = new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), null, addresses, serviceTextAttributesMap);
            return service;

        } catch (TextParseException e) {
            LOGGER.error("microservice {} has invalid name", microservice.getServiceName(), e);
        } catch (UnknownHostException e1) {
            LOGGER.error("microservice {} with Unknown Host {}/", microservice.getServiceName(), ipPortManager.getAvailableAddress().getHostOrIp(), e1);
        }
        return null;
    }

    public static ServiceInstance convertMicroserviceInstanceToMDNSServiceInstance(String microserviceInstanceId, MicroserviceInstance microserviceInstance, IpPortManager ipPortManager) {
        try {
            ServiceName serviceName = new ServiceName(microserviceInstanceId + "._http._tcp.local.");
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceInstanceTextAttributesMap = new HashMap<>();
            serviceInstanceTextAttributesMap.put("serviceId", microserviceInstance.getServiceId());
            serviceInstanceTextAttributesMap.put("status", microserviceInstance.getStatus().toString());
            serviceInstanceTextAttributesMap.put("environment", microserviceInstance.getEnvironment());
            serviceInstanceTextAttributesMap.put("properties", microserviceInstance.getProperties().toString());
            serviceInstanceTextAttributesMap.put("timestamp", microserviceInstance.getTimestamp());
            serviceInstanceTextAttributesMap.put("endpoints", microserviceInstance.getEndpoints().toString());
            serviceInstanceTextAttributesMap.put("hostName", microserviceInstance.getHostName());
            // TODO: toString() method doesn't exist in the HealthCheck class
            serviceInstanceTextAttributesMap.put("healthCheck", microserviceInstance.getHealthCheck().toString());

            ServiceInstance microserviceServiceInstance = new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), null, addresses, serviceInstanceTextAttributesMap);
            return microserviceServiceInstance;

        } catch (TextParseException e) {
            LOGGER.error("microservice instance {} has invalid id", microserviceInstanceId, e);
        } catch (UnknownHostException e1) {
            LOGGER.error("microservice instance {} with Unknown Host name {}/", microserviceInstanceId, ipPortManager.getAvailableAddress().getHostOrIp(), e1);
        }
        return null;
    }


    public static String generateServiceIndexKey(Microservice microservice){
        return  String.join("/", microservice.getEnvironment(), microservice.getAppId(), microservice.getServiceName(), microservice.getVersion());
    }

    //    public static ServerMicroservice convertMicroserviceToServerMicroservice(Microservice microservice){
//        ServerMicroservice serverMicroservice =  new ServerMicroservice();
//
//        // first time register Microservice, need to generate serviceId
//        String serviceId = microservice.getServiceId();
//        if (serviceId== null || serviceId.length() == 0){
//            serviceId = UUID.nameUUIDFromBytes(generateServiceIndexKey(microservice).getBytes()).toString();
//        }
//
//        serverMicroservice.setServiceId(serviceId);
//        serverMicroservice.setAppId(microservice.getAppId());
//        serverMicroservice.setServiceName(microservice.getServiceName());
//        serverMicroservice.setVersion(microservice.getVersion());
//        serverMicroservice.setLevel(microservice.getLevel());
//        serverMicroservice.setAlias(microservice.getAlias());
//        serverMicroservice.setSchemas(microservice.getSchemas());
//        serverMicroservice.setStatus(microservice.getStatus());
//        serverMicroservice.setRegisterBy(microservice.getRegisterBy());
//        serverMicroservice.setEnvironment(microservice.getEnvironment());
//        serverMicroservice.setProperties(microservice.getProperties());
//        serverMicroservice.setDescription(microservice.getDescription());
//
//        // Framework object has name and value attributes
//        Map<String, String> framework = new HashMap<>();
//        framework.put("name", microservice.getFramework().getName());
//        framework.put("version", microservice.getFramework().getVersion());
//        serverMicroservice.setFramework(framework);
//
//        serverMicroservice.setSchemaMap(microservice.getSchemaMap());
//
//        return serverMicroservice;
//    }

}
