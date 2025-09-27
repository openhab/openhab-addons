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
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteService;
import org.openhab.binding.homekit.internal.hap_services.PairRemoveClient;
import org.openhab.binding.homekit.internal.hap_services.PairSetupClient;
import org.openhab.binding.homekit.internal.hap_services.PairVerifyClient;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
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

    protected final Map<Integer, Accessory> accessories = new HashMap<>();

    protected boolean isChildAccessory = false;

    protected @NonNullByDefault({}) CharacteristicReadWriteService rwService;
    protected @NonNullByDefault({}) String pairingCode;
    protected @NonNullByDefault({}) Integer accessoryId;
    protected @NonNullByDefault({}) IpTransport ipTransport;

    protected @Nullable Ed25519PrivateKeyParameters controllerLongTermPrivateKey = null;
    protected @Nullable Ed25519PublicKeyParameters accessoryLongTermPublicKey = null;

    public HomekitBaseServerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        if (!isChildAccessory) {
            try {
                ipTransport.close();
            } catch (Exception e) {
            }
        }
        super.dispose();
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
    private void fetchAccessories() {
        try {
            String json = new String(ipTransport.get(ENDPOINT_ACCESSORIES, CONTENT_TYPE_HAP), StandardCharsets.UTF_8);
            Accessories container = GSON.fromJson(json, Accessories.class);
            if (container != null && container.accessories instanceof List<Accessory> accessoryList) {
                accessories.clear();
                accessories.putAll(accessoryList.stream().filter(a -> Objects.nonNull(a.aid))
                        .collect(Collectors.toMap(a -> a.aid, Function.identity())));
            }
            logger.debug("Fetched {} accessories", accessories.size());
            scheduler.submit(() -> accessoriesLoaded()); // notify subclass in scheduler thread
        } catch (Exception e) {
            logger.debug("Failed to get accessories", e);
        }
    }

    /**
     * Called when the thing handler has been initialized, the pairing verified, and the accessories have been loaded.
     * Subclasses override this to perform any processing required.
     * This method is called in the context of a scheduler thread, to avoid blocking operations.
     */
    protected abstract void accessoriesLoaded();

    /**
     * Extracts the accessory ID from the 'Accessory UID' property.
     *
     * @return the accessory ID, or null if it cannot be determined
     */
    protected @Nullable Integer getAccessoryId() {
        String accessoryUid = thing.getProperties().get(PROPERTY_ACCESSORY_UID);
        if (accessoryUid != null) {
            try {
                return Integer.parseInt(new ThingUID(accessoryUid).getId());
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    @Override
    public void handleRemoval() {
        if (isChildAccessory) {
            updateStatus(ThingStatus.REMOVED);
        } else {
            scheduler.submit(() -> {
                // unpair and clear stored keys if this is NOT a child accessory
                try {
                    PairRemoveClient service = new PairRemoveClient(ipTransport, thing.getUID().toString());
                    service.remove();
                    accessoryLongTermPublicKey = null;
                    storeLongTermKeys();
                    updateStatus(ThingStatus.REMOVED);
                } catch (Exception e) {
                    logger.warn("Failed to remove pairing for accessory {}", accessoryId);
                }
            });
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            // accessory is hosted by a bridge, so use bridge's pairing session and read/write service
            isChildAccessory = true;
            ipTransport = bridgeHandler.ipTransport;
            rwService = bridgeHandler.rwService;
            if (rwService != null) {
                fetchAccessories();
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not connected");
            }
        } else {
            // standalone accessory or bridge accessory, so do pairing and session setup here
            isChildAccessory = false;
            Object host = getConfig().get(CONFIG_HOST);
            if (host == null || !(host instanceof String hostString) || hostString.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid host");
                return;
            }
            try {
                ipTransport = new IpTransport(hostString);
            } catch (Exception e) {
                logger.debug("Failed to create transport", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to connect to accessory");
                return;
            }
            scheduler.execute(() -> initializePairing()); // return fast, do pairing in background thread
        }
    }

    /**
     * Restores an existing pairing or creates a new one if necessary.
     * Updates the thing status accordingly.
     */
    private void initializePairing() {
        Object pairingConfig = getConfig().get(CONFIG_PAIRING_CODE);
        if (pairingConfig == null || !(pairingConfig instanceof String pairingCode) || pairingCode.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid pairing code");
            return;
        }
        this.pairingCode = pairingCode;
        this.accessoryId = getAccessoryId();
        if (accessoryId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid accessory ID");
            return;
        }

        restoreLongTermKeys();
        Ed25519PrivateKeyParameters controllerLongTermPrivateKey = this.controllerLongTermPrivateKey;
        Ed25519PublicKeyParameters accessoryLongTermPublicKey = this.accessoryLongTermPublicKey;

        if (controllerLongTermPrivateKey != null && accessoryLongTermPublicKey != null) {
            try {
                logger.debug("Starting Pair-Verify with existing key for accessory {}", accessoryId);
                PairVerifyClient client = new PairVerifyClient(ipTransport, accessoryId.toString(),
                        controllerLongTermPrivateKey, accessoryLongTermPublicKey);

                ipTransport.setSessionKeys(client.verify());
                rwService = new CharacteristicReadWriteService(ipTransport);

                logger.debug("Restored pairing was verified for accessory {}", accessoryId);
                fetchAccessories();
                updateStatus(ThingStatus.ONLINE);

                return;
            } catch (Exception e) {
                logger.debug("Restored pairing was not verified for accessory {}", accessoryId, e);
                this.controllerLongTermPrivateKey = null;
                storeLongTermKeys();
                // fall through to create new pairing
            }
        }

        // Create new controller private key
        controllerLongTermPrivateKey = new Ed25519PrivateKeyParameters(new SecureRandom());
        logger.debug("Created new controller long term private key for accessory {}", accessoryId);

        try {
            logger.debug("Starting Pair-Setup for accessory {}", accessoryId);
            PairSetupClient pairSetupClient = new PairSetupClient(ipTransport, thing.getUID().toString(),
                    controllerLongTermPrivateKey, pairingCode);

            accessoryLongTermPublicKey = pairSetupClient.pair();
            this.accessoryLongTermPublicKey = accessoryLongTermPublicKey;

            logger.debug("Pair-Setup completed; starting Pair-Verify for accessory {}", accessoryId);

            // Perform Pair-Verify immediately after Pair-Setup
            PairVerifyClient pairVerifyClient = new PairVerifyClient(ipTransport, accessoryId.toString(),
                    controllerLongTermPrivateKey, accessoryLongTermPublicKey);

            ipTransport.setSessionKeys(pairVerifyClient.verify());
            rwService = new CharacteristicReadWriteService(ipTransport);

            this.controllerLongTermPrivateKey = controllerLongTermPrivateKey;

            logger.debug("Pairing and verification completed for accessory {}", accessoryId);
            storeLongTermKeys();
            fetchAccessories();
            updateStatus(ThingStatus.ONLINE);

        } catch (Exception e) {
            logger.warn("Pairing and verification failed for accessory {}", accessoryId, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Pairing / Verification failed");
        }
    }

    /**
     * Restores the controller's private key from the thing's properties.
     * The private key is expected to have been stored as a Base64-encoded string.
     */
    private void restoreLongTermKeys() {
        String encoded = thing.getProperties().get(PROPERTY_CONTROLLER_PRIVATE_KEY);
        controllerLongTermPrivateKey = encoded == null ? null
                : new Ed25519PrivateKeyParameters(Base64.getDecoder().decode(encoded), 0);

        encoded = thing.getProperties().get(PROPERTY_ACCESSORY_PUBLIC_KEY);
        accessoryLongTermPublicKey = encoded == null ? null
                : new Ed25519PublicKeyParameters(Base64.getDecoder().decode(encoded), 0);
    }

    /**
     * Stores the controller's private key in the thing's properties.
     * The private key is stored as a Base64-encoded string.
     */
    private void storeLongTermKeys() {
        Ed25519PrivateKeyParameters controllerKey = this.controllerLongTermPrivateKey;
        String property = controllerKey == null ? null : Base64.getEncoder().encodeToString(controllerKey.getEncoded());
        thing.setProperty(PROPERTY_CONTROLLER_PRIVATE_KEY, property);

        Ed25519PublicKeyParameters accessoryKey = this.accessoryLongTermPublicKey;
        property = accessoryKey == null ? null : Base64.getEncoder().encodeToString(accessoryKey.getEncoded());
        thing.setProperty(PROPERTY_ACCESSORY_PUBLIC_KEY, property);
    }
}
