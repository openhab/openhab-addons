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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteClient;
import org.openhab.binding.homekit.internal.hap_services.PairRemoveClient;
import org.openhab.binding.homekit.internal.hap_services.PairSetupClient;
import org.openhab.binding.homekit.internal.hap_services.PairVerifyClient;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.binding.homekit.internal.session.EventListener;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.osgi.framework.Bundle;
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
public abstract class HomekitBaseAccessoryHandler extends BaseThingHandler implements EventListener {

    protected static final Gson GSON = new Gson();

    private static final int MIN_CONNECTION_DELAY_SECONDS = 2;
    private static final int MAX_CONNECTION_DELAY_SECONDS = 600;

    private final Logger logger = LoggerFactory.getLogger(HomekitBaseAccessoryHandler.class);

    protected final Map<Integer, Accessory> accessories = new HashMap<>();
    protected final HomekitTypeProvider typeProvider;
    protected final HomekitKeyStore keyStore;
    protected final TranslationProvider i18nProvider;
    protected final Bundle bundle;

    protected boolean isChildAccessory = false;

    private int connectionDelaySeconds = MIN_CONNECTION_DELAY_SECONDS;

    private @Nullable ScheduledFuture<?> connectionTask;
    private @Nullable CharacteristicReadWriteClient rwService;
    private @Nullable IpTransport ipTransport;

    protected @NonNullByDefault({}) String pairingCode;
    protected @NonNullByDefault({}) Integer accessoryId;

    public HomekitBaseAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider, HomekitKeyStore keyStore,
            TranslationProvider translationProvider, Bundle bundle) {
        super(thing);
        this.typeProvider = typeProvider;
        this.keyStore = keyStore;
        this.i18nProvider = translationProvider;
        this.bundle = bundle;
    }

    @Override
    public void dispose() {
        unsubscribeEvents();
        cancelConnectionTask();
        if (!isChildAccessory && ipTransport instanceof IpTransport ipTransport) {
            ipTransport.close();
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
            String json = new String(getIpTransport().get(ENDPOINT_ACCESSORIES, CONTENT_TYPE_HAP),
                    StandardCharsets.UTF_8);
            Accessories acc0 = GSON.fromJson(json, Accessories.class);
            if (acc0 instanceof Accessories acc1 && acc1.accessories instanceof List<Accessory> acc2) {
                accessories.clear();
                accessories.putAll(acc2.stream().filter(a -> Objects.nonNull(a.aid))
                        .collect(Collectors.toMap(a -> a.aid, Function.identity())));
            }
            logger.debug("Fetched {} accessories", accessories.size());
            scheduler.submit(this::accessoriesLoaded); // notify subclass in scheduler thread
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException
                | IllegalAccessException e) {
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
        logger.debug("Missing or invalid accessory id");
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
                    PairRemoveClient service = new PairRemoveClient(getIpTransport(), keyStore.getControllerUUID());
                    service.remove();
                    String mac = getThing().getProperties().get(Thing.PROPERTY_MAC_ADDRESS);
                    if (mac != null) {
                        keyStore.setAccessoryKey(mac, null);
                    } else {
                        logger.warn("Could not clear key for '{}' due to missing mac address", thing.getUID());
                    }
                    updateStatus(ThingStatus.REMOVED);
                } catch (IOException | InterruptedException | TimeoutException | ExecutionException
                        | IllegalAccessException e) {
                    logger.warn("Failed to remove pairing for '{}'", thing.getUID());
                }
            });
        }
    }

    @Override
    public void initialize() {
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            // accessory is hosted by a bridge, so use bridge's pairing session and read/write service
            isChildAccessory = true;
            try {
                bridgeHandler.getRwService(); // ensure that bridge has a read/write service
                fetchAccessories();
                updateStatus(ThingStatus.ONLINE);
            } catch (IllegalAccessException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        i18nProvider.getText(bundle, "error.bridge-not-connected", "Bridge not connected", null));
            }
        } else {
            // standalone accessory or bridge accessory, so do pairing and session setup here
            isChildAccessory = false;
            startConnectionTask();
        }
    }

    /**
     * Restores an existing pairing or creates a new one if necessary.
     * Updates the thing status accordingly.
     */
    private synchronized void initializePairing() {
        Object host = getConfig().get(CONFIG_HOST);
        if (host == null || !(host instanceof String hostString) || !HOST_PATTERN.matcher(hostString).matches()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.invalid-host", "Invalid host", null));
            return;
        }

        Object pairingConfig = getConfig().get(CONFIG_PAIRING_CODE);
        if (pairingConfig == null || !(pairingConfig instanceof String pairingConfigString)
                || !PAIRING_CODE_PATTERN.matcher(pairingConfigString).matches()) {
            logger.debug("Pairing code must match XXX-XX-XXX or XXXX-XXXX or XXXXXXXX");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.invalid-pairing-code", "Invalid pairing code", null));
            return;
        }
        pairingCode = normalizePairingCode(pairingConfigString);

        accessoryId = getAccessoryId();
        if (accessoryId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.invalid-accessory-id", "Invalid accessory ID", null));
            return;
        }

        final String macAddress = getThing().getProperties().get(Thing.PROPERTY_MAC_ADDRESS);
        if (macAddress == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.missing-mac-address", "Missing MAC address", null));
            return;
        }

        // create new transport
        try {
            ipTransport = new IpTransport(hostString);
        } catch (IOException e) {
            logger.debug("Failed to create transport", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    i18nProvider.getText(bundle, "error.failed-to-connect", "Failed to connect", null));
            return;
        }

        if (keyStore.getAccessoryKey(macAddress) != null) {
            try {
                logger.debug("Starting Pair-Verify with existing key for {}", thing.getUID());
                PairVerifyClient client = new PairVerifyClient(getIpTransport(), keyStore.getControllerUUID(),
                        keyStore.getControllerKey(), Objects.requireNonNull(keyStore.getAccessoryKey(macAddress)));

                getIpTransport().setSessionKeys(client.verify());
                rwService = new CharacteristicReadWriteClient(getIpTransport());

                logger.debug("Restored pairing was verified for {}", thing.getUID());
                cancelConnectionTask();
                fetchAccessories();
                updateStatus(ThingStatus.ONLINE);
                return; // pairing restore succeeded => exit
            } catch (NoSuchAlgorithmException | NoSuchProviderException | IllegalAccessException
                    | InvalidCipherTextException | IOException | InterruptedException | TimeoutException
                    | ExecutionException e) {
                logger.debug("Restored pairing was not verified for {}", thing.getUID(), e);
                // pairing restore failed => continue to create new pairing
            }
        }

        try {
            logger.debug("Starting Pair-Setup for {}", thing.getUID());
            PairSetupClient pairSetupClient = new PairSetupClient(getIpTransport(), keyStore.getControllerUUID(),
                    keyStore.getControllerKey(), pairingCode);

            Ed25519PublicKeyParameters accessoryKey = pairSetupClient.pair();
            logger.debug("Pair-Setup completed; starting Pair-Verify for {}", thing.getUID());

            // Perform Pair-Verify immediately after Pair-Setup
            PairVerifyClient pairVerifyClient = new PairVerifyClient(getIpTransport(), keyStore.getControllerUUID(),
                    keyStore.getControllerKey(), accessoryKey);

            getIpTransport().setSessionKeys(pairVerifyClient.verify());
            rwService = new CharacteristicReadWriteClient(getIpTransport());
            keyStore.setAccessoryKey(macAddress, accessoryKey);

            logger.debug("Pairing and verification completed for {}", thing.getUID());
            cancelConnectionTask();
            fetchAccessories();
            updateStatus(ThingStatus.ONLINE);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IllegalAccessException
                | InvalidCipherTextException | IOException | InterruptedException | TimeoutException
                | ExecutionException e) {
            logger.warn("Pairing / verification failed '{}' for {}", e.getMessage(), thing.getUID());
            logger.debug("Stack trace", e);
            startConnectionTask();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, i18nProvider.getText(bundle,
                    "error.pairing-verification-failed", "Pairing / verification failed", null));
        }
    }

    public Collection<Accessory> getAccessories() {
        return accessories.values();
    }

    /**
     * Normalize XXX-XX-XXX or XXXX-XXXX or XXXXXXXX to XXX-XX-XXX
     */
    private String normalizePairingCode(String input) throws IllegalArgumentException {
        // remove all non-digit character formatting
        String digits = input.replaceAll("\\D", "");
        if (digits.length() != 8) {
            throw new IllegalArgumentException("Input must contain exactly 8 digits");
        }
        // re-format as XXX-XX-XXX
        return String.format("%s-%s-%s", digits.substring(0, 3), digits.substring(3, 5), digits.substring(5, 8));
    }

    /**
     * Starts a task to attempt (re) connection.
     * The delay increases exponentially up to a maximum of 10 minutes.
     * If this handler is a child of a bridge, it delegates to the bridge handler.
     */
    protected void startConnectionTask() {
        if (isChildAccessory && getBridge() instanceof Bridge bridge
                && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            bridgeHandler.startConnectionTask();
        } else {
            ScheduledFuture<?> task = connectionTask;
            if (task != null) {
                task.cancel(false);
            }
            IpTransport ipTransport = this.ipTransport;
            if (ipTransport != null) { // clean up prior transport if any
                this.ipTransport = null;
                ipTransport.close();
            }
            connectionTask = scheduler.schedule(this::initializePairing, connectionDelaySeconds, TimeUnit.SECONDS);
            connectionDelaySeconds = Math.min(connectionDelaySeconds * connectionDelaySeconds,
                    MAX_CONNECTION_DELAY_SECONDS);
        }
    }

    /**
     * Stops the (re) connect task if it is running. Resets the retry exponent.
     */
    private void cancelConnectionTask() {
        ScheduledFuture<?> task = connectionTask;
        if (task != null) {
            task.cancel(false);
        }
        connectionTask = null;
        connectionDelaySeconds = MIN_CONNECTION_DELAY_SECONDS;
    }

    /**
     * Gets the IP transport.
     *
     * @throws IllegalAccessException if this is a child accessory or if the transport is not initialized.
     * @return the IpTransport
     */
    protected IpTransport getIpTransport() throws IllegalAccessException {
        if (isChildAccessory) {
            throw new IllegalAccessException("Child accessories must delegate to bridge IP transport");
        }
        IpTransport ipTransport = this.ipTransport;
        if (ipTransport == null) {
            throw new IllegalAccessException("IP transport not initialized");
        }
        return ipTransport;
    }

    /**
     * Gets the read/write service.
     *
     * @throws IllegalAccessException if this is a child accessory or if the service is not initialized
     * @return the CharacteristicReadWriteClient
     */
    protected CharacteristicReadWriteClient getRwService() throws IllegalAccessException {
        if (isChildAccessory) {
            throw new IllegalAccessException("Child accessories must delegate to bridge read/write service");
        }
        CharacteristicReadWriteClient rwService = this.rwService;
        if (rwService == null) {
            throw new IllegalAccessException("Read/write service not initialized");
        }
        return rwService;
    }

    /**
     * Subscribes to events from the IP transport.
     */
    protected void subscribeEvents() {
        try {
            getIpTransport().subscribe(this);
        } catch (IllegalAccessException e) {
            logger.debug("Failed to subscribe to events '{}", e.getMessage());
        }
    }

    /**
     * Unsubscribes from events from the IP transport.
     */
    protected void unsubscribeEvents() {
        try {
            getIpTransport().unsubscribe(this);
        } catch (IllegalAccessException e) {
            logger.debug("Failed to unsubscribe from events '{}", e.getMessage());
        }
    }

    @Override
    public void onEvent(String jsonContent) {
        // default implementation does nothing; subclasses must override
    }
}
