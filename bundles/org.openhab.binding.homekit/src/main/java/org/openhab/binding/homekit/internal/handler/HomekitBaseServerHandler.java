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
package org.openhab.binding.homekit.internal.handler;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.services.CharacteristicReadWriteService;
import org.openhab.binding.homekit.internal.services.PairingSetupService;
import org.openhab.binding.homekit.internal.session.SecureSession;
import org.openhab.binding.homekit.internal.session.SessionKeys;
import org.openhab.binding.homekit.internal.transport.HttpTransport;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles I/O with HomeKit server devices -- either simple accessories or bridge accessories that
 * contain child accessories. If the handler is for a HomeKit bridge or a stand alone HomeKit accessory
 * device it performs the pairing and secure session setup. If the handler is for a HomeKit accessory
 * that is part of a bridge, it uses the pairing and session from the bridge handler.
 * Subclasses should override the handleCommand method to handle commands for specific channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBaseServerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitBaseServerHandler.class);

    protected static final Gson GSON = new Gson();
    protected final HttpTransport httpTransport;
    protected final Map<Integer, Accessory> accessories = new HashMap<>();

    protected boolean isChildAccessory = false;

    protected @NonNullByDefault({}) CharacteristicReadWriteService charactersticsManager;
    protected @NonNullByDefault({}) SessionKeys keys;
    protected @NonNullByDefault({}) SecureSession session;
    protected @NonNullByDefault({}) String baseUrl;
    protected @NonNullByDefault({}) String pairingCode;

    public HomekitBaseServerHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpTransport = new HttpTransport(httpClientFactory.getCommonHttpClient());
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            // accessory is hosted by a bridge, so use the bridge's pairing and session
            this.isChildAccessory = true;
            this.keys = bridgeHandler.keys;
            this.session = bridgeHandler.session;
            this.charactersticsManager = bridgeHandler.charactersticsManager;
            if (this.charactersticsManager != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not connected");
            }
        } else {
            // standalone accessory or brige accessory, so do pairing and session setup here
            this.isChildAccessory = false;
            this.baseUrl = "http://" + getConfig().get(CONFIG_IP_V4_ADDRESS).toString();
            this.pairingCode = getConfig().get(CONFIG_PAIRING_CODE).toString();
            try {
                this.keys = new PairingSetupService(httpTransport, pairingCode).pair(baseUrl);
                this.session = new SecureSession(keys);
                this.charactersticsManager = new CharacteristicReadWriteService(httpTransport, session, baseUrl);
                scheduler.submit(() -> getAccessories());
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.error("Failed to initialize HomeKit client", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // override in subclass
    }

    /**
     * Get information about embedded accessories and their respective channels.
     * Uses the /accessories endpoint.
     * Returns an empty list if there was a problem.
     * Requires a valid secure session.
     *
     * @return list of accessories (may be empty)
     * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-homekit-http">HomeKit HTTP</a>
     */
    protected void getAccessories() {
        SecureSession session = this.session;
        if (session != null) {
            try {
                byte[] encrypted = httpTransport.get(baseUrl, ENDPOINT_ACCESSORIES, CONTENT_TYPE_HAP);
                byte[] decrypted = session.decrypt(encrypted);
                Accessories result = GSON.fromJson(new String(decrypted, StandardCharsets.UTF_8), Accessories.class);
                if (result != null && result.accessories instanceof List<Accessory> accessoryList) {
                    accessories.clear();
                    accessories.putAll(accessoryList.stream().filter(a -> Objects.nonNull(a.accessoryId))
                            .collect(Collectors.toMap(a -> a.accessoryId, Function.identity())));
                }
            } catch (Exception e) {
            }
        }
    }
}
