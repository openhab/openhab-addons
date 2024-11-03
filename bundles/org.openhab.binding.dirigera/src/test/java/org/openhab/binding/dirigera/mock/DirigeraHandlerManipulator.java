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
package org.openhab.binding.dirigera.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;

/**
 * The {@link DirigeraHandlerManipulator} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DirigeraHandlerManipulator extends DirigeraHandler {

    public DirigeraHandlerManipulator(Bridge bridge, HttpClient insecureClient, Storage<String> bindingStorage,
            DirigeraDiscoveryManager discoveryManager, TimeZoneProvider timeZoneProvider) {
        super(bridge, insecureClient, bindingStorage, discoveryManager, timeZoneProvider);
        // Changes the class of the provider. During initialize this class will be used for instantiation
        super.apiProvider = APIMock.class;
    }
}
