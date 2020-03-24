package org.apache.servicecomb.embedsc.server.util;

import net.posick.mDNS.Browse;
import net.posick.mDNS.DNSSDListener;
import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;

import org.apache.servicecomb.embedsc.server.listener.ServiceCombMDSNServiceListener;
import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.MicroserviceVersionContainer;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.embedsc.server.model.ServerMicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.Framework;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ServerRegisterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegisterUtil.class);

    //Constants
    public static  String[] discoverServiceTypes = new String[] {"_http._tcp."}; // "_http._tcp.": Web pages
    public static String registerServiceType = "registerServiceType";

    // Microservice && MicroserviceInstance Container
    private static ApplicationContainer appContainer = new ApplicationContainer();

    // key: serviceID
    private static Map<String, ServerMicroservice>  serverMicroserviceMap = new HashMap<>();

    public static ApplicationContainer getApplicationContainer() {
        return appContainer;
    }

    public static Map<String, ServerMicroservice>  getServerMicroserviceMap() {
        return serverMicroserviceMap;
    }

    public static ServerMicroservice convertToServerMicroservice(ServiceInstance service){

        Map<String, String> serviceTextAttributesMap = service.getTextAttributes();

        if (serviceTextAttributesMap != null && !serviceTextAttributesMap.isEmpty()){
            ServerMicroservice serverMicroservice =  new ServerMicroservice();
            serverMicroservice.setServiceId(serviceTextAttributesMap.get("serviceId"));
            serverMicroservice.setAppId(serviceTextAttributesMap.get("appId"));
            serverMicroservice.setServiceName(serviceTextAttributesMap.get("ServiceName"));
            serverMicroservice.setLevel(serviceTextAttributesMap.get("level"));
            serverMicroservice.setAlias(serviceTextAttributesMap.get("alias"));
            serverMicroservice.setVersion(serviceTextAttributesMap.get("version"));
            serverMicroservice.setTimestamp(serviceTextAttributesMap.get("timestamp"));
            serverMicroservice.setModTimestamp(serviceTextAttributesMap.get("modTimestamp"));
            serverMicroservice.setStatus(serviceTextAttributesMap.get("status"));
            serverMicroservice.setRegisterBy(serviceTextAttributesMap.get("registerBy"));
            serverMicroservice.setEnvironment(serviceTextAttributesMap.get("environment"));
            serverMicroservice.setDescription(serviceTextAttributesMap.get("description"));
            // TODO: serverMicroservice.setInstances(Map);

            // ["schema1", "schema2"]
            String schemaString = serviceTextAttributesMap.get("schemas");
            if (schemaString != null && schemaString.length() > 2) {
                serverMicroservice.setSchemas(Arrays.asList(schemaString.substring(1, schemaString.length() - 1).split(",")));
            }

            // framework,  properties and schemaMap are Map type
            serverMicroservice.setFramework(convertMapStringToMap(serviceTextAttributesMap.get("framework")));
            serverMicroservice.setProperties(convertMapStringToMap(serviceTextAttributesMap.get("properties")));
            // schemaMap Map<String, String> Map<schemaId, content>
            serverMicroservice.setSchemaMap(convertMapStringToMap(serviceTextAttributesMap.get("schemaMap")));

            return serverMicroservice;
        }
        return null;
    }

    public static ServerMicroserviceInstance convertToServerMicroserviceInstance(ServiceInstance serviceInstance){
        Map<String, String> serviceInstanceTextAttributesMap = serviceInstance.getTextAttributes();

        if (serviceInstanceTextAttributesMap != null && !serviceInstanceTextAttributesMap.isEmpty()){
            ServerMicroserviceInstance serverMicroserviceInstance =  new ServerMicroserviceInstance();
            serverMicroserviceInstance.setInstanceId(serviceInstanceTextAttributesMap.get("instanceId"));
            serverMicroserviceInstance.setServiceId(serviceInstanceTextAttributesMap.get("serviceId"));
            serverMicroserviceInstance.setAppId(serviceInstanceTextAttributesMap.get("appId"));
            serverMicroserviceInstance.setServiceName(serviceInstanceTextAttributesMap.get("ServiceName"));
            serverMicroserviceInstance.setStatus(serviceInstanceTextAttributesMap.get("status"));
            serverMicroserviceInstance.setVersion(serviceInstanceTextAttributesMap.get("version"));
            serverMicroserviceInstance.setHostName(serviceInstanceTextAttributesMap.get("hostName"));
            // TODO: serverMicroserviceInstance.setHealthCheck();

            // ["rest://127.0.0.1:8080", "rest://127.0.0.1:8081"]
            String endPointsString = serviceInstanceTextAttributesMap.get("endpoints");
            if (endPointsString != null && endPointsString.length() > 2) {
                serverMicroserviceInstance.setEndpoints(Arrays.asList(endPointsString.substring(1, endPointsString.length() - 1).split(",")));
            }

            // properties are Map type
            serverMicroserviceInstance.setProperties(convertMapStringToMap(serviceInstanceTextAttributesMap.get("properties")));

            return serverMicroserviceInstance;
        }
        return null;
    }


    public static Microservice convertToClientMicroservice(ServerMicroservice serverMicroservice) {
        Microservice microservice = new Microservice();

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
        microservice.setDescription(serverMicroservice.getDescription());

        Framework framework = new Framework();
        framework.setName(serverMicroservice.getFramework().get("name"));
        framework.setVersion(serverMicroservice.getFramework().get("version"));
        microservice.setFramework(framework);

        microservice.setProperties(serverMicroservice.getProperties());

        for (Map.Entry<String, String> entry : serverMicroservice.getSchemaMap().entrySet()){
            microservice.addSchema(entry.getKey(), entry.getValue());
        }

        return microservice;
    }

    // TODO: build server side Mapping
    public static void registerMicroservice(ServerMicroservice serverMicroservice) {
        ApplicationContainer applicationContainer = ServerRegisterUtil.getApplicationContainer();
        ServerMicroservice registeredServerMicroservice = applicationContainer.getOrCreateServerMicroservice(
                serverMicroservice.getAppId(),
                serverMicroservice.getServiceName(),
                serverMicroservice.getVersion());

        MicroserviceVersionContainer microserviceVersionContainer = applicationContainer.getMicroserviceVersionContainer(serverMicroservice.getAppId(),serverMicroservice.getServiceName());
        microserviceVersionContainer.getVersions().put(serverMicroservice.getVersion(), serverMicroservice);


    }

    // TODO: How, When, and Where to start/call this method?
    public static void startAsynchronousServiceDiscoveryService(){
        try {
            // https://github.com/posicks/mdnsjava/blob/master/README.md
            MulticastDNSService service = new MulticastDNSService();
            Browse browser = new Browse(ServerRegisterUtil.discoverServiceTypes);
            DNSSDListener listener = new ServiceCombMDSNServiceListener();
            service.startServiceDiscovery(browser, listener);
        } catch (IOException e) {
            LOGGER.error("Failed to start Asynchronous Service Discovery Service", e);
        }
    }

    private static Map<String, String> convertMapStringToMap(String mapString){
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

}
