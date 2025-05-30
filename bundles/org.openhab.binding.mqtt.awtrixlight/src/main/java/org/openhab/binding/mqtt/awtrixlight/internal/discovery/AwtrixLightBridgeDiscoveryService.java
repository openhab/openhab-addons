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

package org.openhab.binding.mqtt.awtrixlight.internal.discovery;

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.handler.AwtrixLightBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link AwtrixLightBridgeDiscoveryService} is responsible for finding awtrix
 * apps and setting them up for the handlers.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixLightBridgeDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    @Nullable
    AwtrixLightBridgeHandler bridgeHandler = null;

    public AwtrixLightBridgeDiscoveryService() {
        super(Set.of(THING_TYPE_APP), 3, true);
    }

    public void appDiscovered(String baseTopic, String appName) {
        AwtrixLightBridgeHandler localHandler = this.bridgeHandler;
        if (localHandler != null) {
            Map<String, String> bridgeProperties = localHandler.getThing().getProperties();
            @Nullable
            String bridgeHardwareId = bridgeProperties.get(PROP_UNIQUEID);
            if (bridgeHardwareId != null) {
                publishApp(localHandler.getThing().getUID(), bridgeHardwareId, baseTopic, appName);
            }
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof AwtrixLightBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
            bridgeHandler.setAppDiscoveryCallback(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.bridgeHandler;
    }

    @Override
    public void deactivate() {
        AwtrixLightBridgeHandler localHandler = this.bridgeHandler;
        if (localHandler != null) {
            localHandler.removeAppDiscoveryCallback();
        }
        super.deactivate();
    }

    @Override
    protected void startScan() {
        // Do nothing. We get results pushed in from the bridge as they come
    }

    void publishApp(ThingUID connectionBridgeUid, String bridgeHardwareId, String basetopic, String appName) {
        if (!"Notification".equals(appName)) {
            String appId = bridgeHardwareId + "-" + appName;
            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_APP, connectionBridgeUid, appName))
                    .withBridge(connectionBridgeUid).withProperty(PROP_APPID, appId)
                    .withProperty(PROP_APP_CONTROLLABLE, false).withProperty(PROP_APPNAME, appName)
                    .withRepresentationProperty(PROP_APPID).withLabel("Awtrix App " + appName).build());
        }
    }
}
