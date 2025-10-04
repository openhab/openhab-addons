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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteClient;
import org.openhab.binding.homekit.internal.hap_services.PairRemoveClient;
import org.openhab.binding.homekit.internal.hap_services.PairSetupClient;
import org.openhab.binding.homekit.internal.hap_services.PairVerifyClient;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
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

    // pattern matcherfor pairing code XXX-XX-XXX
    protected static final Pattern PAIRING_CODE_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{3}$");

    // pattern matcher for host ipv4 address 123.123.123.123:12345
    protected static final Pattern HOST_PATTERN = Pattern.compile(
            "^(((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)):(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]?\\d{1,4})$");

    private final Logger logger = LoggerFactory.getLogger(HomekitBaseServerHandler.class);

    protected final Map<Integer, Accessory> accessories = new HashMap<>();
    protected final HomekitTypeProvider typeProvider;

    protected boolean isChildAccessory = false;

    protected @NonNullByDefault({}) CharacteristicReadWriteClient rwService;
    protected @NonNullByDefault({}) String pairingCode;
    protected @NonNullByDefault({}) Integer accessoryId;
    protected @NonNullByDefault({}) IpTransport ipTransport;
    protected @NonNullByDefault({}) byte[] clientPairingId;

    protected @Nullable Ed25519PrivateKeyParameters controllerLongTermSecretKey = null;
    protected @Nullable Ed25519PublicKeyParameters accessoryLongTermPublicKey = null;

    public HomekitBaseServerHandler(Thing thing, HomekitTypeProvider typeProvider) {
        super(thing);
        this.typeProvider = typeProvider;
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
            Accessories acc0 = GSON.fromJson(json, Accessories.class);
            if (acc0 instanceof Accessories acc1 && acc1.accessories instanceof List<Accessory> acc2) {
                accessories.clear();
                accessories.putAll(acc2.stream().filter(a -> Objects.nonNull(a.aid))
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
                    PairRemoveClient service = new PairRemoveClient(ipTransport, clientPairingId);
                    service.remove();
                    accessoryLongTermPublicKey = null;
                    storeLongTermKeys();
                    updateStatus(ThingStatus.REMOVED);
                } catch (Exception e) {
                    logger.warn("Failed to remove pairing for {}", thing.getUID());
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
            if (host == null || !(host instanceof String hostString) || !HOST_PATTERN.matcher(hostString).matches()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid host");
                return;
            }
            try {
                ipTransport = new IpTransport(hostString);
            } catch (Exception e) {
                logger.debug("Failed to create transport", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to connect");
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
        if (pairingConfig == null || !(pairingConfig instanceof String pairingCode)
                || !PAIRING_CODE_PATTERN.matcher(pairingCode).matches()) {
            logger.debug("Pairing code must match XXX-XX-XXX");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid pairing code");
            return;
        }
        this.pairingCode = pairingCode;
        try {
            clientPairingId = CryptoUtils.sha64(thing.getUID().toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.debug("Eroor creating client Id", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error creating client Id");
            return;
        }
        this.accessoryId = getAccessoryId();
        if (accessoryId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid accessory ID");
            return;
        }

        restoreLongTermKeys();
        Ed25519PrivateKeyParameters controllerLongTermSecretKey = this.controllerLongTermSecretKey;
        Ed25519PublicKeyParameters accessoryLongTermPublicKey = this.accessoryLongTermPublicKey;

        if (controllerLongTermSecretKey != null && accessoryLongTermPublicKey != null) {
            try {
                logger.debug("Starting Pair-Verify with existing key for {}", thing.getUID());
                PairVerifyClient client = new PairVerifyClient(ipTransport, clientPairingId,
                        controllerLongTermSecretKey, accessoryLongTermPublicKey);

                ipTransport.setSessionKeys(client.verify());
                rwService = new CharacteristicReadWriteClient(ipTransport);

                logger.debug("Restored pairing was verified for {}", thing.getUID());
                fetchAccessories();
                updateStatus(ThingStatus.ONLINE);

                return;
            } catch (Exception e) {
                logger.debug("Restored pairing was not verified for {}", thing.getUID(), e);
                this.controllerLongTermSecretKey = null;
                storeLongTermKeys();
                // fall through to create new pairing
            }
        }

        // Create new controller private key
        controllerLongTermSecretKey = new Ed25519PrivateKeyParameters(new SecureRandom());
        logger.debug("Created new controller long term private key for {}", thing.getUID());

        try {
            logger.debug("Starting Pair-Setup for {}", thing.getUID());
            PairSetupClient pairSetupClient = new PairSetupClient(ipTransport, clientPairingId,
                    controllerLongTermSecretKey, pairingCode);

            accessoryLongTermPublicKey = pairSetupClient.pair();
            this.accessoryLongTermPublicKey = accessoryLongTermPublicKey;

            logger.debug("Pair-Setup completed; starting Pair-Verify for {}", thing.getUID());

            // Perform Pair-Verify immediately after Pair-Setup
            PairVerifyClient pairVerifyClient = new PairVerifyClient(ipTransport, clientPairingId,
                    controllerLongTermSecretKey, accessoryLongTermPublicKey);

            ipTransport.setSessionKeys(pairVerifyClient.verify());
            rwService = new CharacteristicReadWriteClient(ipTransport);

            this.controllerLongTermSecretKey = controllerLongTermSecretKey;

            logger.debug("Pairing and verification completed for {}", thing.getUID());
            storeLongTermKeys();
            fetchAccessories();
            updateStatus(ThingStatus.ONLINE);

        } catch (Exception e) {
            logger.warn("Pairing / verification failed for {}", thing.getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Pairing / verification failed");
        }
    }

    /**
     * Restores the controller's private key from the thing's properties.
     * The private key is expected to have been stored as a Base64-encoded string.
     */
    private void restoreLongTermKeys() {
        String encoded = thing.getProperties().get(PROPERTY_CONTROLLER_PRIVATE_KEY);
        controllerLongTermSecretKey = encoded == null ? null
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
        Ed25519PrivateKeyParameters controllerKey = this.controllerLongTermSecretKey;
        String property = controllerKey == null ? null : Base64.getEncoder().encodeToString(controllerKey.getEncoded());
        thing.setProperty(PROPERTY_CONTROLLER_PRIVATE_KEY, property);

        Ed25519PublicKeyParameters accessoryKey = this.accessoryLongTermPublicKey;
        property = accessoryKey == null ? null : Base64.getEncoder().encodeToString(accessoryKey.getEncoded());
        thing.setProperty(PROPERTY_ACCESSORY_PUBLIC_KEY, property);
    }

    public Collection<Accessory> getAccessories() {
        return accessories.values();
    }
}
