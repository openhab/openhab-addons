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

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.KEY_CODE;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.action.PlayerActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager.Player;
import org.openhab.binding.freeboxos.internal.config.PlayerConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inet.ipaddr.IPAddress;

/**
 * The {@link PlayerHandler} is responsible for handling everything associated to any Freebox Player thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerHandler extends HostHandler {
    private static final List<String> VALID_REMOTE_KEYS = Arrays.asList("red", "green", "blue", "yellow", "power",
            "list", "tv", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "vol_inc", "vol_dec", "mute", "prgm_inc",
            "prgm_dec", "prev", "bwd", "play", "rec", "fwd", "next", "up", "right", "down", "left", "back", "swap",
            "info", "epg", "mail", "media", "help", "options", "pip", "ok", "home");

    private final Logger logger = LoggerFactory.getLogger(PlayerHandler.class);
    private @Nullable IPAddress ipAddress;

    public PlayerHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        super.initializeProperties(properties);
        Player player = getManager(PlayerManager.class).getDevice(getClientId());
        properties.put(Thing.PROPERTY_MODEL_ID, player.deviceModel().name());
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (KEY_CODE.equals(channelId) && command instanceof StringType) {
            sendKey(command.toString(), false, 1);
            return true;
        }

        return super.internalHandleCommand(channelId, command);
    }

    @Override
    public void updateConnectivityChannels(LanHost host) {
        super.updateConnectivityChannels(host);
        ipAddress = host.getIpv4();
    }

    public void sendKey(String key, boolean longPress, int count) {
        String aKey = key.toLowerCase();
        IPAddress ip = ipAddress;
        if (ip == null) {
            logger.warn("Player IP is unknown");
        } else if (VALID_REMOTE_KEYS.contains(aKey)) {
            String remoteCode = (String) getConfig().get(PlayerConfiguration.REMOTE_CODE);
            if (remoteCode != null) {
                try {
                    getManager(PlayerManager.class).sendKey(ip.toCanonicalString(), remoteCode, aKey, longPress, count);
                } catch (FreeboxException e) {
                    logger.warn("Error sending key: {}", e.getMessage());
                }
            } else {
                logger.warn("A remote code must be configured in the on the player thing.");
            }
        } else {
            logger.warn("Key '{}' is not a valid key expression", key);
        }
    }

    public void sendMultipleKeys(String keys) {
        String[] keyChain = keys.split(",");
        Arrays.stream(keyChain).forEach(key -> {
            sendKey(key, false, 1);
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(PlayerActions.class);
    }
}
