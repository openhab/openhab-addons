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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.ActivePlayerActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.player.Player;
import org.openhab.binding.freeboxos.internal.api.player.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.player.PlayerStatus;
import org.openhab.binding.freeboxos.internal.api.player.PlayerSystemConfiguration;
import org.openhab.binding.freeboxos.internal.api.system.SystemConfig;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.framework.BundleContext;

/**
 * The {@link ActivePlayerHandler} is responsible for handling everything associated to Freebox Player with api
 * capabilities.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ActivePlayerHandler extends PlayerHandler {

    public ActivePlayerHandler(Thing thing, AudioHTTPServer audioHTTPServer, String ipAddress,
            BundleContext bundleContext) {
        super(thing, audioHTTPServer, ipAddress, bundleContext);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        super.initializeProperties(properties);
        Player player = getManager(PlayerManager.class).getDevice(getClientId());
        if (player.isReachable()) {
            PlayerSystemConfiguration config = getManager(PlayerManager.class).getConfig(player.getId());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getSerial());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getFirmwareVersion());
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            PlayerStatus status = getManager(PlayerManager.class).getPlayerStatus(getClientId());
            updateChannelString(PLAYER_STATUS, PLAYER_STATUS, status.getPowerState().name());
            updateChannelString(PLAYER_STATUS, PACKAGE, status.getForegroundApp().getPackage());
        }
    }

    @Override
    protected Optional<SystemConfig> getDeviceConfig() throws FreeboxException {
        // PlayerConfiguration config = getConfigAs(PlayerConfiguration.class);
        // PlayerManager playerManager = getManager(PlayerManager.class);
        // return Optional.ofNullable(playerManager.getConfig(config.id));
        return Optional.empty();
    }

    @Override
    protected void internalCallReboot() throws FreeboxException {
        getManager(PlayerManager.class).reboot(getClientId());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(ActivePlayerActions.class);
    }
}
