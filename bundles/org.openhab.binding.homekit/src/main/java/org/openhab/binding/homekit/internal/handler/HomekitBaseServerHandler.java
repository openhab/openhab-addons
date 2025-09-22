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

    protected final Map<Integer, Accessory> accessories = new HashMap<>();

    protected boolean isChildAccessory = false;

    protected @NonNullByDefault({}) CharacteristicReadWriteService rwService;
    protected @NonNullByDefault({}) String pairingCode;
    protected @NonNullByDefault({}) Integer accessoryId;
    protected @NonNullByDefault({}) IpTransport ipTransport;

    protected @Nullable Ed25519PrivateKeyParameters controllerLongTermPrivateKey = null;
    protected @Nullable Ed25519PublicKeyParameters accessoryLongTermPublicKey = null;

    public HomekitBaseServerHandler(Thing thing, HttpClientFactory httpClientFactory) {
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
    protected void getAccessories() {
        try {
            byte[] decrypted = ipTransport.get(ENDPOINT_ACCESSORIES, CONTENT_TYPE_HAP);
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
        if (isChildAccessory) {
            updateStatus(ThingStatus.REMOVED);
        } else {
            updateStatus(ThingStatus.REMOVING);
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
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not connected");
            }
        } else {
            // standalone accessory or brige accessory, so do pairing and session setup here
            isChildAccessory = false;
            try {
                ipTransport = new IpTransport(getConfig().get(CONFIG_IP_V4_ADDRESS).toString());
                scheduler.execute(() -> initializePairing()); // return fast, do pairing in background thread
            } catch (Exception e) {
                logger.warn("Failed to create transport: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to connect to accessory");
            }
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

        restoreLongTermKeys();

        Ed25519PrivateKeyParameters controllerLongTermPrivateKey = this.controllerLongTermPrivateKey;
        Ed25519PublicKeyParameters accessoryLongTermPublicKey = this.accessoryLongTermPublicKey;
        if (controllerLongTermPrivateKey != null && accessoryLongTermPublicKey != null) {
            // Perform Pair-Verify with existing key
            try {
                PairVerifyClient client = new PairVerifyClient(ipTransport, accessoryId.toString(),
                        controllerLongTermPrivateKey, accessoryLongTermPublicKey);

                ipTransport.setSessionKeys(client.verify());
                rwService = new CharacteristicReadWriteService(ipTransport);

                logger.debug("Restored pairing was verified for accessory {}", accessoryId);
                updateStatus(ThingStatus.ONLINE);

                return;
            } catch (Exception e) {
                logger.debug("Restored pairing was not verified for accessory {}", accessoryId);
                this.controllerLongTermPrivateKey = null;
                storeLongTermKeys();
                // fall through to create new pairing
            }
        }

        // Create new controller private key
        controllerLongTermPrivateKey = new Ed25519PrivateKeyParameters(new SecureRandom());
        logger.debug("Created new controller long term private key for accessory {}", accessoryId);

        try {
            // Perform Pair-Setup
            PairSetupClient pairSetupClient = new PairSetupClient(ipTransport, thing.getUID().toString(),
                    controllerLongTermPrivateKey, pairingCode);

            accessoryLongTermPublicKey = pairSetupClient.pair();
            this.accessoryLongTermPublicKey = accessoryLongTermPublicKey;

            // Perform Pair-Verify immediately after Pair-Setup
            PairVerifyClient pairVerifyClient = new PairVerifyClient(ipTransport, accessoryId.toString(),
                    controllerLongTermPrivateKey, accessoryLongTermPublicKey);

            ipTransport.setSessionKeys(pairVerifyClient.verify());
            rwService = new CharacteristicReadWriteService(ipTransport);

            this.controllerLongTermPrivateKey = controllerLongTermPrivateKey;

            storeLongTermKeys();

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
