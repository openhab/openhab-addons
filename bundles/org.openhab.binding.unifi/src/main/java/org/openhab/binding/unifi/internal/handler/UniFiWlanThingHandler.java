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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_ENABLE;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_ESSID;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_GUEST_CLIENTS;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_PASSPHRASE;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_QRCODE_ENCODING;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_SECURITY;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_SITE;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_WIRELESS_CLIENTS;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_WLANBAND;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_WPAENC;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_WPAMODE;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiWlanThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.binding.unifi.internal.api.dto.UniFiWirelessClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWlan;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link UniFiWlanThingHandler} is responsible for handling commands and status updates for a wireless network.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class UniFiWlanThingHandler extends UniFiBaseThingHandler<UniFiWlan, UniFiWlanThingConfig> {

    private UniFiWlanThingConfig config = new UniFiWlanThingConfig();

    public UniFiWlanThingHandler(final Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(final UniFiWlanThingConfig config) {
        this.config = config;

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.thing.wlan.offline.configuration_error");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable UniFiWlan getEntity(final UniFiControllerCache cache) {
        return cache.getWlan(config.getWlanId());
    }

    @Override
    protected State getChannelState(final UniFiWlan wlan, final String channelId) {
        final State state;

        switch (channelId) {
            case CHANNEL_ENABLE:
                state = OnOffType.from(wlan.isEnabled());
                break;
            case CHANNEL_ESSID:
                state = StringType.valueOf(wlan.getName());
                break;
            case CHANNEL_SITE:
                final UniFiSite site = wlan.getSite();
                if (site != null && site.getDescription() != null && !site.getDescription().isBlank()) {
                    state = StringType.valueOf(site.getDescription());
                } else {
                    state = UnDefType.UNDEF;
                }
                break;
            case CHANNEL_WIRELESS_CLIENTS:
                state = countClients(wlan, c -> true);
                break;
            case CHANNEL_GUEST_CLIENTS:
                state = countClients(wlan, c -> c.isGuest());
                break;
            case CHANNEL_SECURITY:
                state = StringType.valueOf(wlan.getSecurity());
                break;
            case CHANNEL_WLANBAND:
                state = StringType.valueOf(wlan.getWlanBand());
                break;
            case CHANNEL_WPAENC:
                state = StringType.valueOf(wlan.getWpaEnc());
                break;
            case CHANNEL_WPAMODE:
                state = StringType.valueOf(wlan.getWpaMode());
                break;
            case CHANNEL_PASSPHRASE:
                state = StringType.valueOf(wlan.getXPassphrase());
                break;
            case CHANNEL_QRCODE_ENCODING:
                state = qrcodeEncoding(wlan);
                break;
            default:
                // Unsupported channel; nothing to update
                state = UnDefType.NULL;
        }
        return state;
    }

    private static State countClients(final UniFiWlan wlan, final Predicate<UniFiClient> filter) {
        final UniFiSite site = wlan.getSite();

        if (site == null) {
            return UnDefType.UNDEF;
        } else {
            return new DecimalType(site.getCache().countClients(site,
                    c -> c instanceof UniFiWirelessClient wirelessClient
                            && (wlan.getName() != null && wlan.getName().equals(wirelessClient.getEssid()))
                            && filter.test(c)));
        }
    }

    /**
     * Returns a MERCARD like notation of the Wi-Fi access code. Format:
     * <code>WIFI:S:&lt;SSID>;T:WPA|blank;P:&lt;password>;;</code>
     *
     * @param wlan wlan UniFi entity object containing the data
     * @return MERCARD like Wi-Fi access format
     * @see https://github.com/zxing/zxing/wiki/Barcode-Contents#wi-fi-network-config-android-ios-11
     */
    private static State qrcodeEncoding(final UniFiWlan wlan) {
        final String name = encode(wlan.getName());
        final String xPassphrase = wlan.getXPassphrase();
        final boolean nopass = xPassphrase == null || xPassphrase.isBlank();
        final String mode = nopass ? "nopass" : "WPA";
        final String hidden = wlan.isHideSsid() ? "H:true" : "";
        final String passcode = nopass ? "" : "P:" + encode(xPassphrase);

        return StringType.valueOf(String.format("WIFI:S:%s;T:%s;%s;%s;", name, mode, passcode, hidden));
    }

    private static String encode(final @Nullable String value) {
        return value == null ? "" : value.replaceAll("([\\;,\":])", "\\\\$1");
    }

    @Override
    protected boolean handleCommand(final UniFiController controller, final UniFiWlan entity,
            final ChannelUID channelUID, final Command command) throws UniFiException {
        final String channelID = channelUID.getId();

        if (CHANNEL_ENABLE.equals(channelID) && command instanceof OnOffType && entity.getSite() != null) {
            controller.enableWifi(entity, OnOffType.ON == command);
            return true;
        }
        return false;
    }
}
