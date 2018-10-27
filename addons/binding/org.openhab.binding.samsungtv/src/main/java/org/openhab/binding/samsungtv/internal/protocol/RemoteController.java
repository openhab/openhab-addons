/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
