package org.apache.servicecomb.embedsc.util;

import org.apache.servicecomb.embedsc.server.model.ApplicationContainer;
import org.apache.servicecomb.embedsc.server.model.ServerMicroservice;
import org.apache.servicecomb.serviceregistry.api.registry.Framework;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterUtil {

    private static ApplicationContainer appContainer = new ApplicationContainer();

    // key: serviceID
    private static Map<String, ServerMicroservice>  serverMicroserviceMap = new HashMap<>();

    public static ApplicationContainer getApplicationContainer() {
        return appContainer;
    }

    public static Map<String, ServerMicroservice>  getServerMicroserviceMap() {
        return serverMicroserviceMap;
    }

    public static ServerMicroservice convertToServerMicroservice(Microservice microservice){
        ServerMicroservice serverMicroservice =  new ServerMicroservice();

        // first time register Microservice, need to generate serviceId
        String serviceId = microservice.getServiceId();
        if (serviceId== null || serviceId.length() == 0){
            serviceId = UUID.nameUUIDFromBytes(generateServiceIndexKey(microservice).getBytes()).toString();
        }

        serverMicroservice.setServiceId(serviceId);
        serverMicroservice.setAppId(microservice.getAppId());
        serverMicroservice.setServiceName(microservice.getServiceName());
        serverMicroservice.setVersion(microservice.getVersion());
        serverMicroservice.setLevel(microservice.getLevel());
        serverMicroservice.setAlias(microservice.getAlias());
        serverMicroservice.setSchemas(microservice.getSchemas());
        serverMicroservice.setStatus(microservice.getStatus());
        serverMicroservice.setRegisterBy(microservice.getRegisterBy());
        serverMicroservice.setEnvironment(microservice.getEnvironment());
        serverMicroservice.setProperties(microservice.getProperties());
        serverMicroservice.setDescription(microservice.getDescription());

        // Framework object has name and value attributes
        Map<String, String> framework = new HashMap<>();
        framework.put("name", microservice.getFramework().getName());
        framework.put("version", microservice.getFramework().getVersion());
        serverMicroservice.setFramework(framework);

        serverMicroservice.setSchemaMap(microservice.getSchemaMap());

        return serverMicroservice;
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

    private static String generateServiceIndexKey(Microservice microservice){
        return  String.join("/", microservice.getEnvironment(), microservice.getAppId(), microservice.getServiceName(), microservice.getVersion());
    }

}
