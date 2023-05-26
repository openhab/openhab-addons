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
package org.openhab.binding.lgthinq.internal.handler;

import org.openhab.binding.lgthinq.internal.discovery.LGThinqDiscoveryService;

/**
 * The {@link LGThinQBridge}
 *
 * @author Nemer Daud - Initial contribution
 */
public interface LGThinQBridge {
    void registerDiscoveryListener(LGThinqDiscoveryService listener);

    void registryListenerThing(LGThinQAbstractDeviceHandler thing);

    void unRegistryListenerThing(LGThinQAbstractDeviceHandler thing);

    LGThinQAbstractDeviceHandler getThingByDeviceId(String deviceId);
}
