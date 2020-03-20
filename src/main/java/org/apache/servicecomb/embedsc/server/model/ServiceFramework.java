package org.apache.servicecomb.embedsc.server.model;

import org.apache.servicecomb.serviceregistry.api.registry.Framework;

public class ServiceFramework {
    private String name;
    private String version;

    public ServiceFramework(String name, String version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public String toString() {
        return "framework{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
