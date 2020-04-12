package org.apache.servicecomb.embedsc.client.util;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.REGISTER_SERVICE_TYPE;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.APP_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SERVICE_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SERVICE_NAME;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.VERSION;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMAS;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.STATUS;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.ENVIRONMENT;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.FRAMEWORK_NAME;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_CONTENT;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.ENDPOINTS;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.HOST_NAME;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.MDNS_SERVICE_NAME_SUFFIX;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.MDNS_HOST_NAME_SUFFIX;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.INSTANCE_ID;
import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_ENDPOINT_LIST_SPLITER;

import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.apache.servicecomb.embedsc.server.model.RegisterServiceEnumType;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Framework;
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

    // for Microservice
    public static ServiceInstance convertToMDNSService(String serviceId, Microservice microservice, IpPortManager ipPortManager) {
        try {
            ServiceName serviceName = new ServiceName(microservice.getServiceName()+ MDNS_SERVICE_NAME_SUFFIX);
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceTextAttributesMap = new HashMap<>();
            serviceTextAttributesMap.put(REGISTER_SERVICE_TYPE, RegisterServiceEnumType.MICROSERVICE.name());
            serviceTextAttributesMap.put(APP_ID, microservice.getAppId());
            serviceTextAttributesMap.put(SERVICE_NAME, microservice.getServiceName());
            serviceTextAttributesMap.put(VERSION, microservice.getVersion());
            serviceTextAttributesMap.put(SERVICE_ID, serviceId);
            serviceTextAttributesMap.put(STATUS, microservice.getStatus());

            // have to use special spliter for schema list otherwise, MDNS can't parse the string list properly i.e.  [schema1, schema2]
            // schema1$schema2
            List<String> schemas = microservice.getSchemas();
            StringBuilder sb = new StringBuilder();
            if ( schemas != null && !schemas.isEmpty()) {
                for (String schema : schemas) {
                    sb.append(schema + SCHEMA_ENDPOINT_LIST_SPLITER);
                }
                // remove the last $
                serviceTextAttributesMap.put(SCHEMAS,sb.toString().substring(0, sb.toString().length()-1));
            }
            //serviceTextAttributesMap.put(DESCRIPTION, microservice.getDescription());
            //serviceTextAttributesMap.put(REGISTER_BY, microservice.getRegisterBy());
            //serviceTextAttributesMap.put(PROPERTIES, microservice.getProperties().toString());

            // Framework object has name and value attributes
            //Map<String, String> frameworkMap = new HashMap<>();
            //frameworkMap.put(FRAMEWORK_NAME, microservice.getFramework().getName());
            //frameworkMap.put(VERSION, microservice.getFramework().getVersion());
            //serviceTextAttributesMap.put(FRAMEWORK, frameworkMap.toString());

            // set the timestamp
            // String timestamp = String.valueOf(Instant.now().getEpochSecond());
            // serviceTextAttributesMap.put(TIMESTAMP, timestamp);
            // serviceTextAttributesMap.put(MOD_TIMESTAMP, timestamp);

            // Microservice doesn't have host name, only Microserver Instance has host name. host name can't be null, use a dummy value here
            Name hostname = new Name(microservice.getServiceName() + MDNS_HOST_NAME_SUFFIX);
            return new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), hostname, addresses, serviceTextAttributesMap);

        } catch (TextParseException e) {
            LOGGER.error("microservice {} has invalid name", microservice.getServiceName(), e);
        } catch (UnknownHostException e1) {
            LOGGER.error("microservice {} with Unknown Host {}/", microservice.getServiceName(), ipPortManager.getAvailableAddress().getHostOrIp(), e1);
        }
        return null;
    }

    // for Microservice schema
    public static ServiceInstance convertToMDNSServiceSchema(String serviceId,  String schemaId, String schemaContent, IpPortManager ipPortManager) {

        try {
            ServiceName serviceName = new ServiceName(schemaId + MDNS_SERVICE_NAME_SUFFIX);
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceSchemaTextAttributesMap = new HashMap<>();
            serviceSchemaTextAttributesMap.put(REGISTER_SERVICE_TYPE, RegisterServiceEnumType.MICROSERVICE_SCHEMA.name());
            serviceSchemaTextAttributesMap.put(SERVICE_ID, serviceId);
            serviceSchemaTextAttributesMap.put(SCHEMA_ID, schemaId);
            serviceSchemaTextAttributesMap.put(SCHEMA_CONTENT, schemaContent);

            Name hostname = new Name(schemaId + MDNS_HOST_NAME_SUFFIX);
            // host name can't be null, use a dummy value here
            return new ServiceInstance(serviceName, 0, 0, ipPort.getPort(), hostname, addresses, serviceSchemaTextAttributesMap);

        } catch (TextParseException e) {
            LOGGER.error("microservice has either invalid microserviceId {} OR invalid schemaId {}", serviceId, schemaId, e);
        } catch (UnknownHostException e1) {
            LOGGER.error("microserviceId {} schema registration {} with Unknown Host name {}/", serviceId, schemaId, ipPortManager.getAvailableAddress().getHostOrIp(), e1);
        }
        return null;
    }

    // for Microservie instance
    public static ServiceInstance convertToMDNSServiceInstance(String serviceId, String microserviceInstanceId, MicroserviceInstance microserviceInstance, IpPortManager ipPortManager) {
        try {
            ServiceName serviceName = new ServiceName(microserviceInstanceId + MDNS_SERVICE_NAME_SUFFIX);
            IpPort ipPort = ipPortManager.getAvailableAddress();
            InetAddress[] addresses = new InetAddress[] {InetAddress.getByName(ipPort.getHostOrIp())};

            Map<String, String> serviceInstanceTextAttributesMap = new HashMap<>();
            serviceInstanceTextAttributesMap.put(REGISTER_SERVICE_TYPE, RegisterServiceEnumType.MICROSERVICE_INSTANCE.name());
            serviceInstanceTextAttributesMap.put(SERVICE_ID, serviceId);
            serviceInstanceTextAttributesMap.put(INSTANCE_ID, microserviceInstanceId);
            serviceInstanceTextAttributesMap.put(STATUS, microserviceInstance.getStatus().toString());
            serviceInstanceTextAttributesMap.put(ENVIRONMENT, microserviceInstance.getEnvironment());

            // have to use special spliter for schema list otherwise, MDNS can't parse the string list properly i.e.  [schema1, schema2]
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

            //serviceInstanceTextAttributesMap.put(PROPERTIES, microserviceInstance.getProperties().toString());
            //serviceInstanceTextAttributesMap.put(TIMESTAMP, microserviceInstance.getTimestamp());

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
            framework.setName(frameworkMap.get(FRAMEWORK_NAME));
            framework.setVersion(frameworkMap.get(VERSION));
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

    public static MicroserviceInstance convertToClientMicroserviceInstance(ServerMicroserviceInstance serverMicroserviceInstance) {
        MicroserviceInstance microserviceInstance =  new MicroserviceInstance();
        microserviceInstance.setServiceId(serverMicroserviceInstance.getServiceId());
        microserviceInstance.setInstanceId(serverMicroserviceInstance.getInstanceId());
        microserviceInstance.setHostName(serverMicroserviceInstance.getHostName());
        microserviceInstance.setEndpoints(serverMicroserviceInstance.getEndpoints());
        microserviceInstance.setStatus(MicroserviceInstanceStatus.valueOf(serverMicroserviceInstance.getStatus()));
        microserviceInstance.setTimestamp(serverMicroserviceInstance.getTimestamp());
        microserviceInstance.setProperties(serverMicroserviceInstance.getProperties());
        return microserviceInstance;
    }

    public static String generateServiceId(Microservice microservice){
        String serviceIdStringIndex = String.join("/", microservice.getAppId(), microservice.getServiceName(), microservice.getVersion());
        return UUID.nameUUIDFromBytes(serviceIdStringIndex.getBytes()).toString();
    }

    public static String generateServiceInstanceId(MicroserviceInstance microserviceInstance){
        return UUID.randomUUID().toString();
    }

}
