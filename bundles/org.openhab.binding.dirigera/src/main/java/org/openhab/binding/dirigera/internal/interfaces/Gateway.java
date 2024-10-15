/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseDeviceHandler;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.binding.dirigera.internal.network.RestAPI;

/**
 * The {@link Gateway} Gateway interface
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface Gateway {
    public void registerDevice(BaseDeviceHandler deviceHandler);

    public void unregisterDevice(BaseDeviceHandler deviceHandler);

    public void deleteDevice(BaseDeviceHandler deviceHandler);

    public RestAPI api();

    public Model model();

    public String getToken();

    public String getIpAddress();

    public void newDevice(String id);

    public void websocketUpdate(JSONObject update);

    public void newScene(String id, String name);
}
