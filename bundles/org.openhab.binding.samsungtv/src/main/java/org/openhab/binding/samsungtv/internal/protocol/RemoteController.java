/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RemoteController} is the base class for handling remote control keys for the Samsung TV.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public abstract class RemoteController implements AutoCloseable {
    protected String host;
    protected int port;
    protected String appName;
    protected String uniqueId;

    public RemoteController(String host, int port, @Nullable String appName, @Nullable String uniqueId) {
        this.host = host;
        this.port = port;
        this.appName = appName != null ? appName : "";
        this.uniqueId = uniqueId != null ? uniqueId : "";
    }

    public abstract void openConnection() throws RemoteControllerException;

    public abstract boolean isConnected();

    public abstract void sendKey(KeyCode key) throws RemoteControllerException;

    public abstract void sendKeys(List<KeyCode> keys) throws RemoteControllerException;

    @Override
    public abstract void close() throws RemoteControllerException;
}
