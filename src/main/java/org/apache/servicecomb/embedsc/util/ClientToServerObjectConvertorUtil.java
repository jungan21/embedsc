package org.apache.servicecomb.embedsc.util;

import org.apache.servicecomb.embedsc.server.model.MicroserviceRequest;
import org.apache.servicecomb.embedsc.server.model.ServiceFramework;
import org.apache.servicecomb.embedsc.server.model.ServiceType;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ClientToServerObjectConvertorUtil {

    public static MicroserviceRequest convertToMicroserviceRequest(Microservice microservice){

        String serviceId = microservice.getServiceId();
        if (serviceId== null || serviceId.length() == 0){
            serviceId = UUID.nameUUIDFromBytes(generateServiceIndexKey(microservice).getBytes()).toString();
        }

        Map<String, String> serviceTextAttributesMap = new LinkedHashMap<>();

        serviceTextAttributesMap.put("type", ServiceType.SERVICE.name());
        serviceTextAttributesMap.put("serviceId", serviceId);
        serviceTextAttributesMap.put("appId", microservice.getAppId());
        serviceTextAttributesMap.put("serviceName", microservice.getServiceName());
        serviceTextAttributesMap.put("version", microservice.getVersion());
        serviceTextAttributesMap.put("level", microservice.getLevel());
        serviceTextAttributesMap.put("schemas", microservice.getSchemas().toString());
        serviceTextAttributesMap.put("status", microservice.getStatus());
        serviceTextAttributesMap.put("registerBy", microservice.getRegisterBy());
        serviceTextAttributesMap.put("environment", microservice.getEnvironment());
        serviceTextAttributesMap.put("properties", microservice.getProperties().toString());
        ServiceFramework serviceFramework = new ServiceFramework(microservice.getFramework().getName(), microservice.getFramework().getVersion());
        serviceTextAttributesMap.put("framework", serviceFramework.toString());

        Map<String, String> schemaMap = microservice.getSchemaMap();
        if (schemaMap != null && schemaMap.size() > 0 ){
            for (Map.Entry<String, String> schemaMapEntry : schemaMap.entrySet()) {
                String schemaId = schemaMapEntry.getKey();
                String schemaContent = schemaMapEntry.getValue();
                serviceTextAttributesMap.put("schema_" + schemaId, schemaContent);
            }
        }

        MicroserviceRequest microserviceRequest = new MicroserviceRequest(microservice.getAppId(), microservice.getServiceName(),microservice.getVersion(), serviceTextAttributesMap);
        microserviceRequest.setServiceId(serviceId);
        return microserviceRequest;
    }

    private static String generateServiceIndexKey(Microservice microservice){
        return  String.join("/", microservice.getEnvironment(), microservice.getAppId(), microservice.getServiceName(), microservice.getVersion());
    }

}
