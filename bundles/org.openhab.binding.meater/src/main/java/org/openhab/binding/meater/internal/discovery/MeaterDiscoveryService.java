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
package org.openhab.binding.meater.internal.discovery;

import static org.openhab.binding.meater.internal.MeaterBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meater.internal.MeaterConfiguration;
import org.openhab.binding.meater.internal.handler.MeaterBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link MeaterDiscoveryService} searches for available
 * Meater probes discoverable through MEATER REST API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class MeaterDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int SEARCH_TIME = 2;
    private @Nullable MeaterBridgeHandler handler;

    public MeaterDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MeaterBridgeHandler) {
            this.handler = (MeaterBridgeHandler) handler;
            i18nProvider = ((MeaterBridgeHandler) handler).getI18nProvider();
            localeProvider = ((MeaterBridgeHandler) handler).getLocaleProvider();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        MeaterBridgeHandler bridgeHandler = this.handler;
        if (bridgeHandler != null) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            bridgeHandler.getMeaterThings().entrySet().stream().forEach(thing -> {
                thingDiscovered(
                        DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_MEATER_PROBE, bridgeUID, thing.getKey()))
                                .withLabel("@text/discovery.probe.label").withBridge(bridgeUID)
                                .withProperty(MeaterConfiguration.DEVICE_ID_LABEL, thing.getKey())
                                .withRepresentationProperty(MeaterConfiguration.DEVICE_ID_LABEL).build());
            });
        }
    }
}
