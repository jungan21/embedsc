package org.apache.servicecomb.embedsc.client.util;

import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.server.model.RegisterServiceType;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Framework;
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
import java.util.UUID;

public class ClientRegisterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegisterUtil.class);

    public static final String registerServiceType = "registerServiceType";

    public static ServiceInstance convertToMDNSServiceInstance(String serviceId, Microservice microservice, IpPortManager ipPortManager) {
        try {
            ServiceName serviceName = new ServiceName(microservice.getServiceName()+ "._http._tcp.local.");
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceTextAttributesMap = new HashMap<>();
            serviceTextAttributesMap.put(registerServiceType, RegisterServiceType.MICROSERVICE.toString());
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

            // TODO: Map<schemaId, schemaContent> too big
            // serviceTextAttributesMap.put("schemaMap", microservice.getSchemaMap().toString());

            // TODO: check, we may need to remove follow instance piece of code
            MicroserviceInstance microserviceInstance = microservice.getInstance();
            if (microserviceInstance != null) {
                StringBuilder builder = new StringBuilder();
                String microserviceInstanceString =  builder.append('{')
                        .append("instanceId='").append(microserviceInstance.getInstanceId()).append('\'')
                        .append(", serviceId='").append(microserviceInstance.getInstanceId()).append( '\'')
                        .append(", endpoints=").append(microserviceInstance.getEndpoints()).append( '\'')
                        .append( ", hostName='").append(microserviceInstance.getHostName()).append('\'')
                        .append(", status=").append(microserviceInstance.getStatus()).append('\'')
                        .append(", properties=").append( microserviceInstance.getProperties()).append('\'')
                        .append(", timestamp='").append(microserviceInstance.getTimestamp()).append('\'')
                        .append('}')
                        .toString();
                serviceTextAttributesMap.put("instance",  microserviceInstanceString);
            }

            ServiceInstance service = new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), null, addresses, serviceTextAttributesMap);

            return service;

        } catch (TextParseException e) {
            LOGGER.error("microservice {} has invalid name", microservice.getServiceName(), e);
        } catch (UnknownHostException e1) {
            LOGGER.error("microservice {} with Unknown Host {}/", microservice.getServiceName(), ipPortManager.getAvailableAddress().getHostOrIp(), e1);
        }
        return null;
    }

    public static ServiceInstance convertToMDNSServiceInstance(String microserviceInstanceId, MicroserviceInstance microserviceInstance, IpPortManager ipPortManager) {
        try {
            ServiceName serviceName = new ServiceName(microserviceInstanceId + "._http._tcp.local.");
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceInstanceTextAttributesMap = new HashMap<>();
            serviceInstanceTextAttributesMap.put(registerServiceType, RegisterServiceType.MICROSERVICE_INSTANCE.toString());
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

    public static ServiceInstance convertToMDNSServiceInstance(String microserviceId, String schemaId, String schemaContent, IpPortManager ipPortManager) {

        try {
            ServiceName serviceName = new ServiceName(microserviceId + "_" + schemaId + "._http._tcp.local.");
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceSchemaTextAttributesMap = new HashMap<>();
            serviceSchemaTextAttributesMap.put(registerServiceType, RegisterServiceType.MICROSERVICE_SCHEMA.toString());
            serviceSchemaTextAttributesMap.put("serviceId", microserviceId);
            serviceSchemaTextAttributesMap.put("schemaId", schemaId);
            serviceSchemaTextAttributesMap.put("schemaContent", schemaContent);

            ServiceInstance microserviceServiceSchema = new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), null, addresses, serviceSchemaTextAttributesMap);
            return microserviceServiceSchema;

        } catch (TextParseException e) {
            LOGGER.error("microservice has either invalid microserviceId {} OR invalid schemaId {}", microserviceId, schemaId, e);
        } catch (UnknownHostException e1) {
            LOGGER.error("microserviceId {} schema registration {} with Unknown Host name {}/", microserviceId, schemaId, ipPortManager.getAvailableAddress().getHostOrIp(), e1);
        }
        return null;
    }

    public static String generateServiceId(Microservice microservice){
        String serviceIdStringIndex = String.join("/", microservice.getAppId(), microservice.getServiceName(), microservice.getVersion());
        return UUID.nameUUIDFromBytes(serviceIdStringIndex.getBytes()).toString();
    }

    // TODO should be baesd on the both serviceId and schemaId
    public static String generateServiceSchemaId(String serviceId, String schemaId){
        String schemaIdStringIndex = String.join("/", serviceId, schemaId);
        return UUID.nameUUIDFromBytes(schemaIdStringIndex.getBytes()).toString();
    }

    // convert retured server side object to client object
    public static Microservice convertToClientMicroservice(ServerMicroservice serverMicroservice){
        Microservice microservice =  new Microservice();

        microservice.setServiceId(serverMicroservice.getServiceId());
        microservice.setAppId(serverMicroservice.getAppId());
        microservice.setServiceName(serverMicroservice.getServiceName());
        microservice.setVersion(serverMicroservice.getVersion());
        microservice.setLevel(serverMicroservice.getLevel());
        microservice.setAlias(serverMicroservice.getAlias());
        microservice.setSchemas(serverMicroservice.getSchemas());
        microservice.setStatus(serverMicroservice.getStatus());
        microservice.setRegisterBy(serverMicroservice.getRegisterBy());
        microservice.setEnvironment(serverMicroservice.getEnvironment());
        microservice.setProperties(serverMicroservice.getProperties());
        microservice.setDescription(serverMicroservice.getDescription());

        // Framework object has name and value attributes
        Map<String, String> frameworkMap = serverMicroservice.getFramework();
        if (frameworkMap != null && !frameworkMap.isEmpty()) {
            Framework framework = new Framework();
            framework.setName(frameworkMap.get("name"));
            framework.setVersion(frameworkMap.get("version"));
            microservice.setFramework(framework);
        }

        Map<String, String> schemaMap = microservice.getSchemaMap();
        if (schemaMap != null && !schemaMap.isEmpty()){
            for (Map.Entry<String, String> entry : schemaMap.entrySet())
                // fill out Map<schemaId, schemaContent> and List<schemaId>
                microservice.addSchema(entry.getKey(), entry.getValue());
        }

        return microservice;
    }

}
