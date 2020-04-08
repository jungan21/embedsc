package org.apache.servicecomb.embedsc.server.util;

import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.MicroserviceVersionContainer;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.*;

public class ServerRegisterUtil {

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
            serverMicroservice.setServiceId(serviceTextAttributesMap.get(SERVICE_ID));
            serverMicroservice.setAppId(serviceTextAttributesMap.get(APP_ID));
            serverMicroservice.setServiceName(serviceTextAttributesMap.get(SERVICE_NAME));
            serverMicroservice.setLevel(serviceTextAttributesMap.get(LEVEL));
            serverMicroservice.setAlias(serviceTextAttributesMap.get(ALIAS));
            serverMicroservice.setVersion(serviceTextAttributesMap.get(VERSION));
            serverMicroservice.setTimestamp(serviceTextAttributesMap.get(TIMESTAMP));
            serverMicroservice.setModTimestamp(serviceTextAttributesMap.get(MOD_TIMESTAMP));
            serverMicroservice.setStatus(serviceTextAttributesMap.get(STATUS));
            serverMicroservice.setRegisterBy(serviceTextAttributesMap.get(REGISTER_BY));
            serverMicroservice.setEnvironment(serviceTextAttributesMap.get(ENVIRONMENT));
            serverMicroservice.setDescription(serviceTextAttributesMap.get(DESCRIPTION));

            // ["schema1", "schema2"] just schema name list
            String schemaString = serviceTextAttributesMap.get(SCHEMAS);
            if (schemaString != null && schemaString.length() > 2) {
                serverMicroservice.setSchemas(Arrays.asList(schemaString.substring(1, schemaString.length() - 1).split(",")));
            }

            // framework,  properties and schemaMap are Map type
            serverMicroservice.setFramework(convertMapStringToMap(serviceTextAttributesMap.get(FRAMEWORK)));
            serverMicroservice.setProperties(convertMapStringToMap(serviceTextAttributesMap.get(PROPERTIES)));

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
            String[]  keyValuePairArray = mapString.substring(1, mapString.length() -1 ).split(SPLITER_COMMA);
            for (String keyValuePairString : keyValuePairArray){
                if(keyValuePairString != null && keyValuePairString.length() > 0 && keyValuePairString.contains(SPLITER_MAP_KEY_VALUE)) {
                    map.put(keyValuePairString.split(SPLITER_MAP_KEY_VALUE)[0], keyValuePairString.split(SPLITER_MAP_KEY_VALUE)[1]);
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
            serverMicroserviceInstance.setInstanceId(map.get(INSTANCE_ID));
            serverMicroserviceInstance.setServiceId(map.get(SERVICE_ID));
            serverMicroserviceInstance.setAppId(map.get(APP_ID));
            serverMicroserviceInstance.setServiceName(map.get(SERVICE_NAME));
            serverMicroserviceInstance.setStatus(map.get(STATUS));
            serverMicroserviceInstance.setVersion(map.get(VERSION));
            serverMicroserviceInstance.setHostName(map.get(HOST_NAME));

            // ["rest://127.0.0.1:8080", "rest://127.0.0.1:8081"]
            String endPointsString = map.get(ENDPOINTS);
            if (endPointsString != null && endPointsString.length() > 2) {
                serverMicroserviceInstance.setEndpoints(Arrays.asList(endPointsString.substring(1, endPointsString.length() - 1).split(SPLITER_COMMA)));
            }

            // properties are Map type
            serverMicroserviceInstance.setProperties(convertMapStringToMap(map.get(PROPERTIES)));

            return serverMicroserviceInstance;
        }
        return null;
    }

}
