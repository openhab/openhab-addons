/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

/**
 * The model representing an Neeo Macro (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoMacro {

    /** The macro key */
    private final String key;

    /** The component type */
    private final String componentType;

    /** The macro name */
    private final String name;

    /** The macro label */
    private final String label;

    /** The associated device name */
    private final String deviceName;

    /** The associated room name */
    private final String roomName;

    /**
     * Instantiates a new neeo macro.
     *
     * @param key the key
     * @param componentType the component type
     * @param name the name
     * @param label the label
     * @param deviceName the device name
     * @param roomName the room name
     */
    public NeeoMacro(String key, String componentType, String name, String label, String deviceName, String roomName) {
        this.key = key;
        this.componentType = componentType;
        this.name = name;
        this.label = label;
        this.deviceName = deviceName;
        this.roomName = roomName;
    }

    /**
     * Gets the macro key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the component type
     *
     * @return the component type
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * Gets the macro name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the macro label
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the associated device name
     *
     * @return the device name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Gets the associated room name
     *
     * @return the room name
     */
    public String getRoomName() {
        return roomName;
    }

    @Override
    public String toString() {
        return "NeeoMacro [key=" + key + ", componentType=" + componentType + ", name=" + name + ", label=" + label
                + ", deviceName=" + deviceName + ", roomName=" + roomName + "]";
    }

}
