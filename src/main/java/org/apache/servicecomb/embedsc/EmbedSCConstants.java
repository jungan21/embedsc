package org.apache.servicecomb.embedsc;

public interface EmbedSCConstants {

    // MDNS Related
    String REGISTER_SERVICE_TYPE = "registerServiceType";
    String MDNS_SERVICE_NAME_SUFFIX = "._http._tcp.local.";
    String MDNS_HOST_NAME_SUFFIX = ".local.";
    String[] DISCOVER_SERVICE_TYPES = new String[] {"_http._tcp."}; // "_http._tcp.": Web pages
    // int SCHEMA_CONTENT_CHUNK_SIZE_IN_BYTE = 1200;

    // Microservice Attributes
    String APP_ID = "appId";
    String SERVICE_NAME = "serviceName";
    String VERSION = "version";
    String SERVICE_ID = "serviceId";
    String LEVEL = "level";
    String ALIAS = "alias";
    String SCHEMAS = "schemas";
    String STATUS = "status";
    String DESCRIPTION = "description";
    String REGISTER_BY = "registerBy";
    String ENVIRONMENT = "environment";
    String ENVIRONMENT_DEVELOPMENT = "development";
    String PROPERTIES = "properties";
    String FRAMEWORK = "framework";
    String FRAMEWORK_NAME = "name";
    String TIMESTAMP = "timestamp";
    String MOD_TIMESTAMP = "modTimestamp";

    // Microservice Schema Attributes
    String SCHEMA_ID = "schemaId";
    String SCHEMA_CONTENT = "schemaContent";
    String SCHEMA_CONTENT_PLACEHOLDER = "{}";

    // Microservice Instance Attributes
    String INSTANCE_ID = "instanceId";
    String ENDPOINTS = "endpoints";
    String HOST_NAME = "hostName";
    String INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK = "OK";

    //others
    String SPLITER_COMMA = ",";
    String SPLITER_MAP_KEY_VALUE = "=";
    String SCHEMA_ENDPOINT_LIST_SPLITER = "$";
}
