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

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dirigera.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.DirigeraHandlerFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link HandlerFactoryMock} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HandlerFactoryMock extends DirigeraHandlerFactory {
    public HandlerFactoryMock() {
        super(mock(StorageService.class), mock(NetworkAddressService.class), new DiscoveryMangerMock(),
                DirigeraBridgeProvider.TZP);
    }

    @Override
    public @Nullable ThingHandler createHandler(Thing thing) {
        return super.createHandler(thing);
    }
}
