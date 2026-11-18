package com.cyrilsx.russiancases.service;

public class ResourceNotFoundException extends RuntimeException {

    private final String resource;
    private final transient Object id;

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
        this.resource = resource;
        this.id = id;
    }

    public String getResource() {
        return resource;
    }

    public Object getId() {
        return id;
    }
}
