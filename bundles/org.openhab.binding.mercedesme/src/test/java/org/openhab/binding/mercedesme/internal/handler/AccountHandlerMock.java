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
package org.openhab.binding.mercedesme.internal.handler;

import static org.mockito.Mockito.mock;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.discovery.MercedesMeDiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;

import com.daimler.mbcarkit.proto.Client.ClientMessage;

/**
 * {@link AccountHandlerMock} to retrieve and collect commands from {@link VehicleHandler}
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerMock extends AccountHandler {
    private static VolatileStorageService storageService = new VolatileStorageService();
    private static LocaleProvider localeProvider = new LocaleProvider() {

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    };

    JSONObject command = new JSONObject();

    public AccountHandlerMock() {
        super(mock(Bridge.class), mock(MercedesMeDiscoveryService.class), mock(HttpClient.class),
                mock(LocaleProvider.class), mock(StorageService.class), mock(NetworkAddressService.class));
        config = Optional.of(new AccountConfiguration());
    }

    public AccountHandlerMock(Bridge b, @Nullable String storedObject) {
        super(b, mock(MercedesMeDiscoveryService.class), mock(HttpClient.class), localeProvider, storageService,
                mock(NetworkAddressService.class));
        if (storedObject != null) {
            Storage<String> storage = storageService.getStorage(Constants.BINDING_ID);
            storage.put("a@b.c", storedObject);
        }
        config = Optional.of(new AccountConfiguration());
    }

    @Override
    public void registerVin(String vin, VehicleHandler handler) {
    }

    @Override
    public void getVehicleCapabilities(String vin) {
    }

    @Override
    public void sendCommand(@Nullable ClientMessage cm) {
        if (cm != null) {
            command = ProtoConverter.clientMessage2Json(cm);
        }
    }

    public AccountConfiguration getConfigT() {
        return new AccountConfiguration();
    }

    public JSONObject getCommand() {
        return command;
    }

    public void connect() {
        super.ws.onConnect(mock(Session.class));
    }
}
