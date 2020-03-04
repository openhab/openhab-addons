/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.handler;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.AirMediaConfig;
import org.openhab.binding.freebox.internal.api.model.LanHost;
import org.openhab.binding.freebox.internal.api.model.UPnPAVConfig;
import org.openhab.binding.freebox.internal.config.PlayerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayerHandler} is responsible for handling everything associated to
 * any Freebox Player thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 *         https://github.com/betonniere/freeteuse/
 *         https://github.com/MaximeCheramy/remotefreebox/blob/16e2a42ed7cfcfd1ab303184280564eeace77919/remotefreebox/fbx_descriptor.py
 *         https://dev.freebox.fr/sdk/freebox_player_1.1.4_codes.html
 *
 */
@NonNullByDefault
public class PlayerHandler extends LanHostHandler {
    private final Logger logger = LoggerFactory.getLogger(PlayerHandler.class);

    public PlayerHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected Map<String, String> discoverAttributes() throws FreeboxException {
        final Map<String, String> properties = super.discoverAttributes();
        PlayerConfiguration config = getConfigAs(PlayerConfiguration.class);
        List<LanHost> hosts = bridgeHandler.getLanHosts();
        List<LanHost> matching = hosts.stream().filter(host -> config.hostAddress.equals(host.getIpv4()))
                .collect(Collectors.toList());
        if (!matching.isEmpty()) {
            properties.put(Thing.PROPERTY_MAC_ADDRESS, matching.get(0).getMAC());
            properties.put("Interface", matching.get(0).getInterface());
        }
        return properties;
    }

    @Override
    protected boolean internalHandleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            boolean enable = command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN);
            try {
                switch (channelUID.getIdWithoutGroup()) {
                    case AIRMEDIASTATUS:
                        updateChannelSwitchState(PLAYER_ACTIONS, AIRMEDIASTATUS, enableAirMedia(enable));
                        return true;
                    case UPNPAVSTATUS:
                        updateChannelSwitchState(PLAYER_ACTIONS, UPNPAVSTATUS, enableUPnPAV(enable));
                        return true;
                }
            } catch (FreeboxException e) {
                logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
            }
        }
        return super.internalHandleCommand(channelUID, command);
    }

    public boolean enableAirMedia(boolean enable) throws FreeboxException {
        AirMediaConfig config = new AirMediaConfig();
        config.setEnabled(enable);
        config = bridgeHandler.execute(config, null);
        return config.isEnabled();
        // return bridgeHandler.executePut(FreeboxAirMediaConfigResponse.class, config).isEnabled();
    }

    public boolean enableUPnPAV(boolean enable) throws FreeboxException {
        UPnPAVConfig config = new UPnPAVConfig();
        config.setEnabled(enable);
        config = bridgeHandler.execute(config, null);
        return config.isEnabled();
        // return bridgeHandler.executePut(FreeboxUPnPAVConfigResponse.class, config).isEnabled();
    }

}
