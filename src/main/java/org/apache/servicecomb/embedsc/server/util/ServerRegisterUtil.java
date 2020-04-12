package org.apache.servicecomb.embedsc.server.util;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_ENDPOINT_LIST_SPLITER;

import net.posick.mDNS.Browse;
import net.posick.mDNS.DNSSDListener;
import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import org.apache.servicecomb.embedsc.EmbedSCConstants;
import org.apache.servicecomb.embedsc.server.MicroserviceInstanceService;
import org.apache.servicecomb.embedsc.server.MicroserviceService;
import org.apache.servicecomb.embedsc.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.*;

public class ServerRegisterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegisterUtil.class);

    // Microservice && MicroserviceInstance Container
    private static volatile ApplicationContainer appContainer;

    private static MicroserviceService microserviceService;
    private static MicroserviceInstanceService microserviceInstanceService;

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

    public static synchronized void init() {
        appContainer = new ApplicationContainer();
        microserviceService = new MicroserviceService();
        microserviceInstanceService = new MicroserviceInstanceService();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            startAsyncListenerForRegisteredServices();

        });

    }

    public static ServerMicroservice convertToServerMicroservice(ServiceInstance service){

        Map<String, String> serviceTextAttributesMap = service.getTextAttributes();

        if (serviceTextAttributesMap != null && !serviceTextAttributesMap.isEmpty()){
            ServerMicroservice serverMicroservice =  new ServerMicroservice();
            serverMicroservice.setServiceId(serviceTextAttributesMap.get(SERVICE_ID));
            serverMicroservice.setAppId(serviceTextAttributesMap.get(APP_ID));
            serverMicroservice.setServiceName(serviceTextAttributesMap.get(SERVICE_NAME));
            serverMicroservice.setVersion(serviceTextAttributesMap.get(VERSION));
            serverMicroservice.setStatus(serviceTextAttributesMap.get(STATUS));
            serverMicroservice.setEnvironment(serviceTextAttributesMap.get(ENVIRONMENT));

//            serverMicroservice.setTimestamp(serviceTextAttributesMap.get(TIMESTAMP));
//            serverMicroservice.setModTimestamp(serviceTextAttributesMap.get(MOD_TIMESTAMP));
//            serverMicroservice.setLevel(serviceTextAttributesMap.get(LEVEL));
//            serverMicroservice.setAlias(serviceTextAttributesMap.get(ALIAS));
//            serverMicroservice.setRegisterBy(serviceTextAttributesMap.get(REGISTER_BY));
//            serverMicroservice.setDescription(serviceTextAttributesMap.get(DESCRIPTION));

            // "schema1$schema2"
            String schemasString = serviceTextAttributesMap.get(SCHEMAS);
            if ( schemasString != null && !schemasString.isEmpty()){
                if (schemasString.contains(SCHEMA_ENDPOINT_LIST_SPLITER)){
                    serverMicroservice.setSchemas(Arrays.asList(schemasString.split("\\$")));
                } else {
                    List<String> list  = new ArrayList<>();
                    list.add(schemasString);
                    serverMicroservice.setSchemas(list);
                }
            }
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

            // rest://127.0.0.1:8080$rest://127.0.0.1:8081
            String endPointsString = map.get(ENDPOINTS);
            if ( endPointsString != null && !endPointsString.isEmpty()){
                if (endPointsString.contains(SCHEMA_ENDPOINT_LIST_SPLITER)){
                    serverMicroserviceInstance.setEndpoints(Arrays.asList(endPointsString.split("\\$")));
                } else {
                    List<String> list  = new ArrayList<>();
                    list.add(endPointsString);
                    serverMicroserviceInstance.setEndpoints(list);
                }
            }

            // properties are Map type
           // serverMicroserviceInstance.setProperties(convertMapStringToMap(map.get(PROPERTIES)));

            return serverMicroserviceInstance;
        }
        return null;
    }

    private static void startAsyncListenerForRegisteredServices () {
        String[] serviceTypes = new String[]
                {
                        "_http._tcp.",              // Web pages
                        "_printer._sub._http._tcp", // Printer configuration web pages
                        "_org.smpte.st2071.device:device_v1.0._sub._mdc._tcp",  // SMPTE ST2071 Devices
                        "_org.smpte.st2071.service:service_v1.0._sub._mdc._tcp"  // SMPTE ST2071 Services
                };

            try {

                MulticastDNSService service = new MulticastDNSService();
                service.startServiceDiscovery(new Browse(serviceTypes), new DNSSDListener() {

                    // called when a service is registered to MDNS
                    public void serviceDiscovered(Object id, ServiceInstance service) {
                        LOGGER.info("Microservice is registered to MDNS server {}", service);
                        if(service != null && service.getTextAttributes() != null && !service.getTextAttributes().isEmpty()) {
                            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
                            String registerServiceType = serviceTextAttributesMap.get(EmbedSCConstants.REGISTER_SERVICE_TYPE);
                            switch (RegisterServiceEnumType.valueOf(registerServiceType)) {
                                case MICROSERVICE:
                                    microserviceService.registerMicroservice(service);
                                    break;
                                case MICROSERVICE_INSTANCE:
                                    microserviceInstanceService.registerMicroserviceInstance(service);
                                    break;
                                case MICROSERVICE_SCHEMA:
                                    microserviceService.registerSchema(service);
                                    break;
                                default:
                                    LOGGER.error("Unrecognized service type {} when during registration", registerServiceType);
                                    break;
                            }
                        } else {
                            LOGGER.error("Failed to register service as service: {} is null OR service's text attributes is null", service);
                        }
                    }

                    // called when a service is unregistered from MDNS OR service process is killed
                    public void serviceRemoved(Object id, ServiceInstance service) {
                        LOGGER.info("Microservice is unregistered from MDNS server {}", service);
                        if(service != null && service.getTextAttributes() != null && !service.getTextAttributes().isEmpty()) {
                            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
                            String registerServiceType = serviceTextAttributesMap.get(EmbedSCConstants.REGISTER_SERVICE_TYPE);

                            switch (RegisterServiceEnumType.valueOf(registerServiceType)) {
                                case MICROSERVICE_INSTANCE:
                                    microserviceInstanceService.unregisterMicroserviceInstance(serviceTextAttributesMap.get("serviceId"), serviceTextAttributesMap.get("instanceId"));
                                    break;
                                default:
                                    // Note: Based on our business logic, only unregister instance is supported for now
                                    LOGGER.error("Unrecognized service type {} when during unregistration", registerServiceType);
                                    break;
                            }
                        } else {
                            LOGGER.error("Failed to unregister service as service: {} is null OR service's text attributes is null", service);
                        }
                    }

                    public void handleException(Object id, Exception e) {
                        LOGGER.error("Running into errors when registering/unregistering to/from MDNS service registry center", e);
                    }

                    public void receiveMessage(Object id, Message message) {
                        // ignore
                    }
                });

            } catch (IOException ioException) {
                LOGGER.error("Failed to start Async Service Register/Unregister Listener for MDNS service registry center",ioException);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
                LOGGER.error("Thread.Sleep Failed", interruptedException);
            }
    }

}
