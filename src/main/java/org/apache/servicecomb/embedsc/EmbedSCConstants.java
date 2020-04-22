package org.apache.servicecomb.embedsc;

public interface EmbedSCConstants {

    // MDNS Related
    String MDNS_SERVICE_NAME_SUFFIX = "._http._tcp.local.";
    String MDNS_HOST_NAME_SUFFIX = ".local.";
    String[] DISCOVER_SERVICE_TYPES = new String[]
            {
                    "_http._tcp.",              // Web pages
                    "_printer._sub._http._tcp", // Printer configuration web pages
                    "_org.smpte.st2071.device:device_v1.0._sub._mdc._tcp",  // SMPTE ST2071 Devices
                    "_org.smpte.st2071.service:service_v1.0._sub._mdc._tcp"  // SMPTE ST2071 Services
            };

    // Microservice Attributes
    String APP_ID = "appId";
    String SERVICE_NAME = "serviceName";
    String VERSION = "version";
    String SERVICE_ID = "serviceId";
//    String LEVEL = "level";
//    String ALIAS = "alias";
//    String SCHEMAS = "schemas";
    String STATUS = "status";
//    String DESCRIPTION = "description";
//    String REGISTER_BY = "registerBy";
//    String ENVIRONMENT = "environment";
//    String ENVIRONMENT_DEVELOPMENT = "development";
//    String PROPERTIES = "properties";
//    String FRAMEWORK = "framework";
//    String FRAMEWORK_NAME = "name";
//    String TIMESTAMP = "timestamp";
//    String MOD_TIMESTAMP = "modTimestamp";
//
//    // Microservice Schema Attributes
//    String SCHEMA_ID = "schemaId";
//    String SCHEMA_CONTENT = "schemaContent";
//    String SCHEMA_CONTENT_PLACEHOLDER = "{}";

    // Microservice Instance Attributes
    String INSTANCE_ID = "instanceId";
    String ENDPOINTS = "endpoints";
    String HOST_NAME = "hostName";
    String INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK = "OK";
    String ENDPOINT_PREFIX_REST = "rest";
    String ENDPOINT_PREFIX_HTTP = "http";

    //others
//    String SPLITER_COMMA = ",";
    String SPLITER_MAP_KEY_VALUE = "=";
    String SCHEMA_ENDPOINT_LIST_SPLITER = "$";
    String UUID_SPLITER = "-";
}
