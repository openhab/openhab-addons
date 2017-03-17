/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.test;

/**
 * JSON representation of a device (gateway or subdevice) used in the communication with the server.
 *
 * @author Mihaela Memova
 *
 */
public class JsonDevice {

    private String device_type;
    private int id;
    private String label;

    public JsonDevice(String type, int id, String label) {
        this.device_type = type;
        this.id = id;
        this.label = label;
    }

    public String getType() {
        return device_type;
    }

    public void setType(String type) {
        this.device_type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
