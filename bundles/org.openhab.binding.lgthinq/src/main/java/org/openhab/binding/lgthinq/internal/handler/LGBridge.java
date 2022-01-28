/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.internal.handler;

import org.openhab.binding.lgthinq.internal.LGDeviceThing;
import org.openhab.binding.lgthinq.internal.discovery.LGThinqDiscoveryService;

/**
 * The {@link LGBridge}
 *
 * @author Nemer Daud - Initial contribution
 */
public interface LGBridge {
    void registerDiscoveryListener(LGThinqDiscoveryService listener);

    void registryListenerThing(LGDeviceThing thing);

    void unRegistryListenerThing(LGDeviceThing thing);

    LGDeviceThing getThingByDeviceId(String deviceId);
}
