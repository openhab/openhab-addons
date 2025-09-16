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
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteService;
import org.openhab.binding.homekit.internal.hap_services.PairingRemoveService;
import org.openhab.binding.homekit.internal.hap_services.PairingSetupService;
import org.openhab.binding.homekit.internal.hap_services.PairingVerifyService;
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
public abstract class HomekitBaseServerHandler extends BaseThingHandler {

    protected static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(HomekitBaseServerHandler.class);

    protected final HttpTransport httpTransport;
    protected final Map<Integer, Accessory> accessories = new HashMap<>();

    protected boolean isChildAccessory = false;

    protected @NonNullByDefault({}) CharacteristicReadWriteService rwService;
    protected @NonNullByDefault({}) SecureSession session;
    protected @NonNullByDefault({}) String baseUrl;
    protected @NonNullByDefault({}) String pairingCode;
    protected @NonNullByDefault({}) Integer accessoryId;
    protected @NonNullByDefault({}) SessionKeys sessionKeys;

    protected @Nullable Ed25519PrivateKeyParameters controllerPrivateKey = null;

    public HomekitBaseServerHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpTransport = new HttpTransport(httpClientFactory.getCommonHttpClient());
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
                    accessories.putAll(accessoryList.stream().filter(a -> Objects.nonNull(a.aid))
                            .collect(Collectors.toMap(a -> a.aid, Function.identity())));
                }
            } catch (Exception e) {
                logger.warn("Failed to get accessories: {}", e.getMessage());
            }
        }
    }

    /**
     * Extracts the accessory ID from the thing's UID property.
     * The UID is expected to end with "-<accessoryId>".
     *
     * @return the accessory ID, or null if it cannot be determined
     */
    protected @Nullable Integer getAccessoryId() {
        String uidProperty = thing.getProperties().get(PROPERTY_UID);
        if (uidProperty == null) {
            return null;
        }
        int accessoryIdIndex = uidProperty.lastIndexOf("-");
        if (accessoryIdIndex < 0) {
            return null;
        }
        try {
            return Integer.parseInt(uidProperty.substring(accessoryIdIndex + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // this is an abstract thing with no channels, so do nothing
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        if (!isChildAccessory) {
            // unpair and clear stored keys if this is NOT a child accessory
            try {
                new PairingRemoveService(httpTransport, baseUrl, sessionKeys, thing.getUID().toString()).remove();
                this.controllerPrivateKey = null;
                storeControllerPrivateKey();
            } catch (Exception e) {
                logger.warn("Failed to remove pairing for accessory {}", accessoryId);
            }
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            // accessory is hosted by a bridge, so use bridge's pairing session and read/write service
            this.isChildAccessory = true;
            this.session = bridgeHandler.session;
            this.rwService = bridgeHandler.rwService;
            if (this.rwService != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not connected");
            }
        } else {
            // standalone accessory or brige accessory, so do pairing and session setup here
            this.isChildAccessory = false;
            this.baseUrl = "http://" + getConfig().get(CONFIG_IP_V4_ADDRESS).toString();
            scheduler.execute(() -> initializePairing()); // return fast, do pairing in background thread
        }
    }

    /**
     * Restores an existing pairing or creates a new one if necessary.
     * Updates the thing status accordingly.
     */
    private void initializePairing() {
        pairingCode = getConfig().get(CONFIG_PAIRING_CODE).toString();
        accessoryId = getAccessoryId();
        if (accessoryId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid accessory ID");
            return;
        }

        restoreControllerPrivateKey();
        Ed25519PrivateKeyParameters controllerPrivateKey = this.controllerPrivateKey;

        if (controllerPrivateKey != null) {
            // Perform Pair-Verify with existing key
            try {
                this.sessionKeys = new PairingVerifyService(httpTransport, baseUrl, accessoryId.toString(),
                        controllerPrivateKey).verify();

                this.session = new SecureSession(sessionKeys);
                this.rwService = new CharacteristicReadWriteService(httpTransport, session, baseUrl);

                logger.debug("Restored pairing was verified for accessory {}", accessoryId);
                updateStatus(ThingStatus.ONLINE);

                return;
            } catch (Exception e) {
                logger.debug("Restored pairing was not verified for accessory {}", accessoryId);
                this.controllerPrivateKey = null;
                storeControllerPrivateKey();
                // fall through to create new pairing
            }
        }

        // Create new controller private key
        controllerPrivateKey = new Ed25519PrivateKeyParameters(new SecureRandom());
        logger.debug("Created new controller private key for accessory {}", accessoryId);

        try {
            // Perform Pair-Setup
            this.sessionKeys = new PairingSetupService(httpTransport, baseUrl, pairingCode, controllerPrivateKey,
                    thing.getUID().toString()).pair();

            // Perform Pair-Verify immediately after Pair-Setup
            this.sessionKeys = new PairingVerifyService(httpTransport, baseUrl, accessoryId.toString(),
                    controllerPrivateKey).verify();

            this.session = new SecureSession(sessionKeys);
            this.rwService = new CharacteristicReadWriteService(httpTransport, session, baseUrl);
            this.controllerPrivateKey = controllerPrivateKey;
            storeControllerPrivateKey();

            updateStatus(ThingStatus.ONLINE);
            logger.debug("Pairing and verification completed for accessory {}", accessoryId);
        } catch (Exception e) {
            logger.warn("Pairing and verification failed for accessory {}", accessoryId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Pairing failed");
        }
    }

    /**
     * Restores the controller's private key from the thing's properties.
     * The private key is expected to have been stored as a Base64-encoded string.
     */
    private void restoreControllerPrivateKey() {
        String encoded = thing.getProperties().get(PROPERTY_CONTROLLER_PRIVATE_KEY);
        controllerPrivateKey = encoded == null ? null
                : new Ed25519PrivateKeyParameters(Base64.getDecoder().decode(encoded), 0);
    }

    /**
     * Stores the controller's private key in the thing's properties.
     * The private key is stored as a Base64-encoded string.
     */
    private void storeControllerPrivateKey() {
        Ed25519PrivateKeyParameters controllerPrivateKey = this.controllerPrivateKey;
        String property = controllerPrivateKey == null ? null
                : Base64.getEncoder().encodeToString(controllerPrivateKey.getEncoded());
        thing.setProperty(PROPERTY_CONTROLLER_PRIVATE_KEY, property);
    }
}
