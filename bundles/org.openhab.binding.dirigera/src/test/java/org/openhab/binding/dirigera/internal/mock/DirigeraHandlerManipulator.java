/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.mock;

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.dirigera.internal.DirigeraCommandProvider;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryService;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.osgi.framework.BundleContext;

/**
 * The {@link DirigeraHandlerManipulator} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DirigeraHandlerManipulator extends DirigeraHandler {

    public DirigeraHandlerManipulator(Bridge bridge, HttpClient insecureClient, Storage<String> bindingStorage,
            DirigeraDiscoveryService discoveryService) {
        super(bridge, insecureClient, bindingStorage, discoveryService, mock(LocationProvider.class),
                mock(DirigeraCommandProvider.class), mock(BundleContext.class));
        // Changes the class of the provider. During initialize this class will be used for instantiation
        super.apiProvider = DirigeraAPISimu.class;
    }

    /**
     * for unit testing
     */
    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }
}
