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
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.thing.Thing;

/**
 * The {@link Gateway} Gateway interface
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface Gateway {
    public Thing getThing();

    public String getIpAddress();

    public String getToken();

    public boolean discoverEnabled();

    public void registerDevice(BaseHandler deviceHandler, String deviceId);

    public void unregisterDevice(BaseHandler deviceHandler, String deviceId);

    public void deleteDevice(BaseHandler deviceHandler, String deviceId);

    public boolean isKnownDevice(String deviceId);

    public void websocketUpdate(String update);

    public DirigeraAPI api();

    public Model model();

    public DirigeraDiscoveryManager discovery();
}
