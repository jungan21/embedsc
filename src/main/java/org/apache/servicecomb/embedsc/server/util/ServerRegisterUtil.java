package org.apache.servicecomb.embedsc.server.util;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.SCHEMA_ENDPOINT_LIST_SPLITER;

import net.posick.mDNS.*;
import org.apache.servicecomb.embedsc.server.MicroserviceInstanceService;
import org.apache.servicecomb.embedsc.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.servicecomb.embedsc.EmbedSCConstants.*;

public class ServerRegisterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegisterUtil.class);

    private static MicroserviceInstanceService microserviceInstanceService;

    // 1st key: serviceId, 2nd key: instanceId
    private static Map<String, Map<String, ServerMicroserviceInstance>>  serverMicroserviceInstanceMap = new ConcurrentHashMap<>();

    // 1st key: serviceName, 2nd key: Version
    private static Map<String, List<ServerMicroserviceInstance>>  serverMicroserviceInstanceMapByServiceName = new ConcurrentHashMap<>();

    public static Map<String, Map<String, ServerMicroserviceInstance>>  getServerMicroserviceInstanceMap() {
        return serverMicroserviceInstanceMap;
    }

    public static Map<String, List<ServerMicroserviceInstance>>  getserverMicroserviceInstanceMapByServiceName() {
        return serverMicroserviceInstanceMapByServiceName;
    }

    public static synchronized void init() {
        microserviceInstanceService = new MicroserviceInstanceService();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            startAsyncListenerForRegisteredServices();
        });

    }

    public static ServerMicroserviceInstance convertToServerMicroserviceInstance(ServiceInstance serviceInstance){
        Map<String, String> serviceInstanceTextAttributesMap = serviceInstance.getTextAttributes();

        if (serviceInstanceTextAttributesMap != null && !serviceInstanceTextAttributesMap.isEmpty()){
            return buildServerMicroserviceInstanceFromMap(serviceInstanceTextAttributesMap);
        }
        return null;
    }

    private static ServerMicroserviceInstance buildServerMicroserviceInstanceFromMap (Map<String, String> map) {
        if (map != null && !map.isEmpty()) {
            ServerMicroserviceInstance serverMicroserviceInstance = new ServerMicroserviceInstance();
            serverMicroserviceInstance.setInstanceId(map.get(INSTANCE_ID));
            serverMicroserviceInstance.setServiceId(map.get(SERVICE_ID));
            serverMicroserviceInstance.setStatus(map.get(STATUS));
            serverMicroserviceInstance.setHostName(map.get(HOST_NAME));

            serverMicroserviceInstance.setAppId(map.get(APP_ID));
            serverMicroserviceInstance.setServiceName(map.get(SERVICE_NAME));
            serverMicroserviceInstance.setVersion(map.get(VERSION));

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

            return serverMicroserviceInstance;
        }
        return null;
    }

    public static List<ServerMicroserviceInstance> getServiceInstancesByServiceName (String serviceName) {
        return serverMicroserviceInstanceMapByServiceName.get(serviceName);
    }

    public static List<String> getLatestVersionServiceInstanceEndpointsByServiceName (String serviceName) {

        List<String> instanceEndpointList = new ArrayList<>();
        List<ServerMicroserviceInstance> serverMicroserviceInstanceList = getServiceInstancesByServiceName(serviceName);

        Comparator<ServerMicroserviceInstance> versionComparator = new Comparator<ServerMicroserviceInstance>() {
            public int compare(ServerMicroserviceInstance s1, ServerMicroserviceInstance s2) {
                // descending order e.g. [0.0.3, 0.0.2, 0.0.1 ....]
                return s2.getVersion().compareToIgnoreCase(s1.getVersion());
            }
        };

        if (serverMicroserviceInstanceList != null && !serverMicroserviceInstanceList.isEmpty()) {
            Collections.sort(serverMicroserviceInstanceList, versionComparator);
            instanceEndpointList.addAll(serverMicroserviceInstanceList.get(0).getEndpoints());
        }
        return instanceEndpointList;
    }

    public static List<String> getServiceInstanceEndpointsByServiceNameAndVersion (String serviceName, String version) {
        List<String> instanceEndpointList = new ArrayList<>();
        List<ServerMicroserviceInstance> serverMicroserviceInstanceList = getServiceInstancesByServiceName(serviceName);
        for (ServerMicroserviceInstance instance : serverMicroserviceInstanceList){
            if (instance.getVersion().equalsIgnoreCase(version)){
                instanceEndpointList.addAll(instance.getEndpoints());
            }

        }
        return instanceEndpointList;
    }

    public static List<String> lookupServiceInstanceEndpointsByServiceName(String serviceName) throws IOException {
        List<String> endpointsList = new ArrayList<>();
        ServiceName mdnsServiceName  = new ServiceName(serviceName + MDNS_SERVICE_NAME_SUFFIX);
        Lookup lookup = null;
        try {
            lookup = new Lookup(mdnsServiceName);
            ServiceInstance[] services = lookup.lookupServices();
            for (ServiceInstance service : services) {
                Map<String, String> attributesMap = service.getTextAttributes();
                if (attributesMap != null && attributesMap.containsKey(ENDPOINTS)) {
                    endpointsList.add(attributesMap.get(ENDPOINTS));
                }
            }
        } finally {
            if (lookup != null) {
                try {
                    lookup.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return endpointsList;
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
                        if(service != null && service.getTextAttributes() != null && !service.getTextAttributes().isEmpty()) {
                            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
                            microserviceInstanceService.registerMicroserviceInstance(service);
                            LOGGER.info("Microservice Instance is registered to MDNS server {}", serviceTextAttributesMap);

                            // for debug start register
                            Map<String, Map<String, ServerMicroserviceInstance>> instanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap();
                            System.out.println("Jun Debug instanceMap register: " + instanceMap);
                            Map<String, List<ServerMicroserviceInstance>> instanceByNameMap = ServerRegisterUtil.getserverMicroserviceInstanceMapByServiceName();
                            System.out.println("Jun Debug instanceByNameMap register: " + instanceByNameMap);
                            // for debug start register

                        } else {
                            LOGGER.error("Failed to register service instance. Because service: {} is null OR service's text attributes: {} is null", service, service.getTextAttributes());
                        }
                    }

                    // called when a service is unregistered from MDNS OR service process is killed
                    public void serviceRemoved(Object id, ServiceInstance service) {
                        LOGGER.info("Going to unregister a service instance {}", service);
                        if(service != null && service.getTextAttributes() != null && !service.getTextAttributes().isEmpty()) {
                            Map<String, String> serviceTextAttributesMap = service.getTextAttributes();
                            microserviceInstanceService.unregisterMicroserviceInstance(serviceTextAttributesMap.get(SERVICE_ID), serviceTextAttributesMap.get(INSTANCE_ID));
                            LOGGER.info("Microservice Instance is unregistered from MDNS server {}", service.getTextAttributes());

                            // for debug start unregister
                            Map<String, Map<String, ServerMicroserviceInstance>> instanceMap = ServerRegisterUtil.getServerMicroserviceInstanceMap();
                            System.out.println("Jun Debug instanceMap unregister: " + instanceMap);
                            Map<String, List<ServerMicroserviceInstance>> instanceByNameMap = ServerRegisterUtil.getserverMicroserviceInstanceMapByServiceName();
                            System.out.println("Jun Debug instanceByNameMap unregister: " + instanceByNameMap);
                            // for debug start unregister
                        } else {
                            LOGGER.error("Failed to unregister service as service: {} is null OR service's text attributes is null", service.getTextAttributes());
                        }
                    }

                    public void handleException(Object id, Exception e) {
                        LOGGER.error("Running into errors when registering/unregistering to/from MDNS service registry center", e);
                    }

                    public void receiveMessage(Object id, Message message) {
                        //LOGGER.warn("Ignore receivedMessage from MDNS");
                    }
                });

            } catch (IOException ioException) {
                LOGGER.error("Failed to start Async Service Register/Unregister Listener for MDNS service registry center",ioException);
            }
    }
}
