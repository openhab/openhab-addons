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
package org.openhab.binding.smartthings.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.thing.ThingUID;

/**
 * This interface is responsible sending commands to the Smartthings Hub
 * handlers.
 *
 * @author Bob Raker - Initial contribution
 */
public interface SmartthingsHubCommand {

    /**
     * Send a command to the Smartthings Hub
     *
     * @param path http path which tells Smartthings what to execute
     * @param data data to send
     * @return Response from Smartthings
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public void sendDeviceCommand(@NonNull String path, int timeout, @NonNull String data)
            throws InterruptedException, TimeoutException, ExecutionException;

    public ThingUID getBridgeUID();
}
