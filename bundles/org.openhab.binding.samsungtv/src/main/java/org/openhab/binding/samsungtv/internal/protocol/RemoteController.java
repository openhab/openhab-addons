/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.samsungtv.internal.protocol;

import java.util.List;

/**
 * The {@link RemoteController} is the base class for handling remote control keys for teh Samsung TV.
 *
 * @author Arjan Mels - Initial contribution
 */

public abstract class RemoteController implements AutoCloseable {
    protected String host;
    protected int port;
    protected String appName;
    protected String uniqueId;

    public RemoteController(String host, int port, String appName, String uniqueId) {
        this.host = host;
        this.port = port;
        this.appName = appName != null ? appName : "";
        this.uniqueId = uniqueId != null ? uniqueId : "";
    }

    public abstract void openConnection() throws RemoteControllerException;

    public abstract void sendKey(KeyCode key) throws RemoteControllerException;

    public abstract void sendKeys(List<KeyCode> keys) throws RemoteControllerException;

}
