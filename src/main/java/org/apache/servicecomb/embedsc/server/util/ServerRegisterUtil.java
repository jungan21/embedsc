package org.apache.servicecomb.embedsc.server.util;

import net.posick.mDNS.ServiceInstance;

import org.apache.servicecomb.embedsc.server.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ServerRegisterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegisterUtil.class);

    //Constants
    public static final String[] discoverServiceTypes = new String[] {"_http._tcp."}; // "_http._tcp.": Web pages
    public static final String registerServiceType = "registerServiceType";

    // Microservice && MicroserviceInstance Container
    private static ApplicationContainer appContainer = new ApplicationContainer();

    // key: serviceID
    private static Map<String, ServerMicroservice> serverMicroserviceMap = new ConcurrentHashMap<>();

    // 1st key: serviceId, 2nd key: instanceId
    private static Map<String, Map<String, ServerMicroserviceInstance>>  serverMicroserviceInstanceMap = new ConcurrentHashMap<>();

    public static ApplicationContainer getApplicationContainer() {
        return appContainer;
    }

    public static Map<String, ServerMicroservice>  getServerMicroserviceMap() {
        return serverMicroserviceMap;
    }

    public static Map<String, Map<String, ServerMicroserviceInstance>>  getServerMicroserviceInstanceMap() {
        return serverMicroserviceInstanceMap;
    }

    public static ServerMicroservice convertToServerMicroservice(ServiceInstance service){

        Map<String, String> serviceTextAttributesMap = service.getTextAttributes();

        if (serviceTextAttributesMap != null && !serviceTextAttributesMap.isEmpty()){
            ServerMicroservice serverMicroservice =  new ServerMicroservice();
            serverMicroservice.setServiceId(serviceTextAttributesMap.get("serviceId"));
            serverMicroservice.setAppId(serviceTextAttributesMap.get("appId"));
            serverMicroservice.setServiceName(serviceTextAttributesMap.get("serviceName"));
            serverMicroservice.setLevel(serviceTextAttributesMap.get("level"));
            serverMicroservice.setAlias(serviceTextAttributesMap.get("alias"));
            serverMicroservice.setVersion(serviceTextAttributesMap.get("version"));
            serverMicroservice.setTimestamp(serviceTextAttributesMap.get("timestamp"));
            serverMicroservice.setModTimestamp(serviceTextAttributesMap.get("modTimestamp"));
            serverMicroservice.setStatus(serviceTextAttributesMap.get("status"));
            serverMicroservice.setRegisterBy(serviceTextAttributesMap.get("registerBy"));
            serverMicroservice.setEnvironment(serviceTextAttributesMap.get("environment"));
            serverMicroservice.setDescription(serviceTextAttributesMap.get("description"));

            // ["schema1", "schema2"] just schema name list
            String schemaString = serviceTextAttributesMap.get("schemas");
            if (schemaString != null && schemaString.length() > 2) {
                serverMicroservice.setSchemas(Arrays.asList(schemaString.substring(1, schemaString.length() - 1).split(",")));
            }

            // framework,  properties and schemaMap are Map type
            serverMicroservice.setFramework(convertMapStringToMap(serviceTextAttributesMap.get("framework")));
            serverMicroservice.setProperties(convertMapStringToMap(serviceTextAttributesMap.get("properties")));

            /**
             *
             * NOTE: because the schemaMap is NOT set when registering for Micrservice
             *  schemaMap Map<String, String> Map<schemaId, content>
             *  serverMicroservice.setSchemaMap(convertMapStringToMap(serviceTextAttributesMap.get("schemaMap")));
             */

            /**
             * NOTE: because the instance is NOT set when registering for Micrservice
             *  extract instance object
             *  String instanceString = serviceTextAttributesMap.get("instance");
             *  serverMicroservice.addInstance(buildServerMicroserviceInstanceFromMapString(instanceString));
             */

            return serverMicroservice;
        }
        return null;
    }

    public static ServerMicroserviceInstance convertToServerMicroserviceInstance(ServiceInstance serviceInstance){
        Map<String, String> serviceInstanceTextAttributesMap = serviceInstance.getTextAttributes();

        if (serviceInstanceTextAttributesMap != null && !serviceInstanceTextAttributesMap.isEmpty()){
            return buildServerMicroserviceInstanceFromMap(serviceInstanceTextAttributesMap);
        }
        return null;
    }

    public static void buildMappingForMicroserviceRegistration(ServerMicroservice serverMicroservice) {
        ApplicationContainer applicationContainer = getApplicationContainer();

        MicroserviceVersionContainer microserviceVersionContainer = applicationContainer.getOrCreateMicroserviceVersionContainer(serverMicroservice.getAppId(), serverMicroservice.getServiceName());
        if ( microserviceVersionContainer != null && microserviceVersionContainer.getVersions() != null){
            microserviceVersionContainer.getVersions().put(serverMicroservice.getVersion(), serverMicroservice);
        }

    }

    public static void buildMappingForMicroserviceInstanceRegistration(ServerMicroserviceInstance serverMicroserviceInstance) {
        ApplicationContainer applicationContainer = getApplicationContainer();

        ServerMicroservice serverMicroservice = applicationContainer.getOrCreateServerMicroservice(serverMicroserviceInstance.getAppId(), serverMicroserviceInstance.getServiceName(), serverMicroserviceInstance.getVersion());
        if ( serverMicroservice != null && serverMicroservice.getInstances() != null){
            serverMicroservice.getInstances().put(serverMicroserviceInstance.getInstanceId(), serverMicroserviceInstance);
        }

    }


    public static Map<String, String> convertMapStringToMap(String mapString){
        if(mapString != null && mapString.length() > 2){
            Map<String, String> map = new HashMap<>();
            String[]  keyValuePairArray = mapString.substring(1, mapString.length() -1 ).split(",");
            for (String keyValuePairString : keyValuePairArray){
                if(keyValuePairString != null && keyValuePairString.length() > 0 && keyValuePairString.contains("=")) {
                    map.put(keyValuePairString.split("=")[0], keyValuePairString.split("=")[1]);
                }
            }
            return map;
        }
        return null;
    }

    private static ServerMicroserviceInstance buildServerMicroserviceInstanceFromMapString (String mapString) {
        Map<String, String> map = convertMapStringToMap(mapString);
        if (map != null && !map.isEmpty()) {
            return buildServerMicroserviceInstanceFromMap(map);
        }
        return null;
    }

    private static ServerMicroserviceInstance buildServerMicroserviceInstanceFromMap (Map<String, String> map) {
        if (map != null && !map.isEmpty()) {
            ServerMicroserviceInstance serverMicroserviceInstance = new ServerMicroserviceInstance();
            serverMicroserviceInstance.setInstanceId(map.get("instanceId"));
            serverMicroserviceInstance.setServiceId(map.get("serviceId"));
            serverMicroserviceInstance.setAppId(map.get("appId"));
            serverMicroserviceInstance.setServiceName(map.get("ServiceName"));
            serverMicroserviceInstance.setStatus(map.get("status"));
            serverMicroserviceInstance.setVersion(map.get("version"));
            serverMicroserviceInstance.setHostName(map.get("hostName"));
            // TODO: serverMicroserviceInstance.setHealthCheck();

            // ["rest://127.0.0.1:8080", "rest://127.0.0.1:8081"]
            String endPointsString = map.get("endpoints");
            if (endPointsString != null && endPointsString.length() > 2) {
                serverMicroserviceInstance.setEndpoints(Arrays.asList(endPointsString.substring(1, endPointsString.length() - 1).split(",")));
            }

            // properties are Map type
            serverMicroserviceInstance.setProperties(convertMapStringToMap(map.get("properties")));

            return serverMicroserviceInstance;
        }
        return null;
    }

//    public static Microservice convertToClientMicroservice(ServerMicroservice serverMicroservice) {
//        Microservice microservice = new Microservice();
//
//        microservice.setServiceId(serverMicroservice.getServiceId());
//        microservice.setAppId(serverMicroservice.getAppId());
//        microservice.setServiceName(serverMicroservice.getServiceName());
//        microservice.setVersion(serverMicroservice.getVersion());
//        microservice.setLevel(serverMicroservice.getLevel());
//        microservice.setAlias(serverMicroservice.getAlias());
//        microservice.setSchemas(serverMicroservice.getSchemas());
//        microservice.setStatus(serverMicroservice.getStatus());
//        microservice.setRegisterBy(serverMicroservice.getRegisterBy());
//        microservice.setEnvironment(serverMicroservice.getEnvironment());
//        microservice.setDescription(serverMicroservice.getDescription());
//
//        Framework framework = new Framework();
//        framework.setName(serverMicroservice.getFramework().get("name"));
//        framework.setVersion(serverMicroservice.getFramework().get("version"));
//        microservice.setFramework(framework);
//
//        microservice.setProperties(serverMicroservice.getProperties());
//
//        for (Map.Entry<String, String> entry : serverMicroservice.getSchemaMap().entrySet()){
//            microservice.addSchema(entry.getKey(), entry.getValue());
//        }
//
//        return microservice;
//    }

}
