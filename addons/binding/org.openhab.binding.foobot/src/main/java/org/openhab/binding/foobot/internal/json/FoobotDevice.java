/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foobot.internal.json;

/**
 * The {@link FoobotDevice} is the Java class used to map the JSON
 * response to the foobot.io request.
 *
 * @author Divya Chauhan - Initial contribution
 * @author George Katsis - Code refactor
 */
public class FoobotDevice {

    private String uuid;

    // private int userId;

    private String mac;

    private String name;

    public FoobotDevice(String uuid, String mac, String name) {
        this.uuid = uuid;
        this.mac = mac;
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
