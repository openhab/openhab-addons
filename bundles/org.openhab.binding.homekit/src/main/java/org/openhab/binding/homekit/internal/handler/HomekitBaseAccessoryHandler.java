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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.openhab.binding.homekit.internal.action.HomekitPairingActions;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.enums.ServiceType;
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
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelDefinition;
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

    private static final int MIN_CONNECTION_ATTEMPT_DELAY_SECONDS = 2;
    private static final int MAX_CONNECTION_ATTEMPT_DELAY_SECONDS = 600;

    private static final Duration HANDLER_INITIALIZATION_TIMEOUT = Duration.ofSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(HomekitBaseAccessoryHandler.class);
    private final Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
    private final HomekitKeyStore keyStore;

    private boolean isConfigured = false;
    private int connectionAttemptDelay = MIN_CONNECTION_ATTEMPT_DELAY_SECONDS;

    private @Nullable ScheduledFuture<?> connectionAttemptTask;
    private @Nullable CharacteristicReadWriteClient rwService;
    private @Nullable IpTransport ipTransport;

    private @NonNullByDefault({}) Long accessoryId;

    protected static final Gson GSON = new Gson();

    protected final List<Characteristic> eventedCharacteristics = new ArrayList<>();
    protected final HomekitTypeProvider typeProvider;
    protected final TranslationProvider i18nProvider;
    protected final Bundle bundle;

    protected boolean isChildAccessory = false;

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
        try {
            enableEventsOrThrow(false);
        } catch (Exception e) {
            // closing; ignore
        }
        if (connectionAttemptTask instanceof ScheduledFuture<?> task) {
            task.cancel(true);
        }
        if (ipTransport instanceof IpTransport transport) {
            transport.close();
        }
        connectionAttemptTask = null;
        ipTransport = null;
        super.dispose();
    }

    /**
     * Get information about embedded accessories and their respective channels from the /accessories endpoint.
     *
     * @return list of accessories (may be empty)
     * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-homekit-http">HomeKit HTTP</a>
     */
    private void fetchAccessories() {
        try {
            accessories.clear();
            String json = new String(getIpTransport().get(ENDPOINT_ACCESSORIES, CONTENT_TYPE_HAP),
                    StandardCharsets.UTF_8);
            Accessories acc0 = GSON.fromJson(json, Accessories.class);
            if (acc0 instanceof Accessories acc1 && acc1.accessories instanceof List<Accessory> acc2) {
                accessories.putAll(acc2.stream().filter(a -> Objects.nonNull(a.aid))
                        .collect(Collectors.toMap(a -> a.aid, Function.identity())));
            }
            logger.debug("Fetched {} accessories", accessories.size());
            scheduler.submit(this::processAccessories);
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("Communication error '{}' fetching accessories, reconnecting..", e.getMessage());
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("Unexpected error '{}' fetching accessories", e.getMessage());
            }
            logger.debug("Stack trace", e);
        }
    }

    /**
     * Processes the loaded accessories by calling the overloaded abstract methods, then enables eventing,
     * and finally sets thing as online.
     */
    private void processAccessories() {
        Instant timeout = Instant.now().plus(HANDLER_INITIALIZATION_TIMEOUT);
        while (!checkHandlersInitialized() && Instant.now().isBefore(timeout)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return; // shutting down
            }
        }
        onAccessoriesLoaded();
        onRootHandlerReady();
        onThingOnline();
    }

    /**
     * Returns the accessory ID from the 'AccessoryID' configuration parameter.
     *
     * @return the accessory ID, or null if it cannot be determined
     */
    protected @Nullable Long getAccessoryId() {
        if (getConfig().get(CONFIG_ACCESSORY_ID) instanceof BigDecimal accessoryId) {
            try {
                return accessoryId.longValue();
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
                if (unpairInner()) {
                    updateStatus(ThingStatus.REMOVED);
                }
            });
        }
    }

    @Override
    public void initialize() {
        eventedCharacteristics.clear();
        accessories.clear();
        isChildAccessory = getBridge() instanceof Bridge;
        if (!isChildAccessory) {
            scheduleConnectionAttempt();
        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Restores an existing pairing.
     * Updates the thing status accordingly.
     */
    private synchronized boolean verifyPairing() {
        isConfigured = false;
        Long accessoryId = checkedAccessoryId();
        String ipAddress = checkedIpAddress();
        String macAddress = checkedMacAddress();
        String hostName = checkedHostName();
        if (accessoryId == null || ipAddress == null || macAddress == null || hostName == null) {
            return false; // configuration error
        }
        isConfigured = true;

        // check if we have a stored key
        Ed25519PublicKeyParameters accessoryKey = keyStore.getAccessoryKey(macAddress);
        if (accessoryKey == null) {
            logger.debug("No stored pairing credentials for {}", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.not-paired", "Not paired", null));
            return false;
        }

        // create new transport
        if (checkedCreateIpTransport(ipAddress, hostName) == null) {
            return false; // transport creation failed
        }

        // attempt to verify pairing
        try {
            logger.debug("Starting Pair-Verify with existing key for {}", thing.getUID());
            PairVerifyClient client = new PairVerifyClient(getIpTransport(), keyStore.getControllerUUID(),
                    keyStore.getControllerKey(), accessoryKey);

            getIpTransport().setSessionKeys(client.verify());
            rwService = new CharacteristicReadWriteClient(getIpTransport());

            logger.debug("Restored pairing was verified for {}", thing.getUID());
            scheduler.schedule(this::fetchAccessories, MIN_CONNECTION_ATTEMPT_DELAY_SECONDS, TimeUnit.SECONDS);
            return true; // pairing restore succeeded => exit
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IllegalAccessException
                | InvalidCipherTextException | IOException | InterruptedException | TimeoutException
                | ExecutionException | IllegalStateException e) {
            logger.debug("Restored pairing was not verified for {}", thing.getUID(), e);
            // pairing restore failed => exit and perhaps try again later
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, i18nProvider.getText(bundle,
                    "error.pairing-verification-failed", "Pairing / Verification failed", null));
            return false;
        }
    }

    public Map<Long, Accessory> getAccessories() {
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            return bridgeHandler.getAccessories();
        }
        return accessories;
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
     * Schedules a connection attempt.
     */
    protected void scheduleConnectionAttempt() {
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            bridgeHandler.scheduleConnectionAttempt();
        } else {
            ScheduledFuture<?> task = connectionAttemptTask;
            if (task == null || task.isDone() || task.isCancelled()) {
                connectionAttemptTask = scheduler.schedule(this::attemptConnect, connectionAttemptDelay,
                        TimeUnit.SECONDS);
            }
        }
    }

    /**
     * The (re) connection task. Cleans up any prior transport, then attempts to initialize pairing.
     * If successful, resets the retry delay. If not, reschedules itself with an exponentially increased delay.
     */
    private synchronized void attemptConnect() {
        if (ipTransport instanceof IpTransport transport) { // close prior transport (if any)
            transport.close();
            ipTransport = null;
        }
        if (verifyPairing()) {
            connectionAttemptDelay = MIN_CONNECTION_ATTEMPT_DELAY_SECONDS;
            connectionAttemptTask = null;
        } else if (isConfigured) { // config ok but connection failed => try again
            connectionAttemptDelay = Math.min(MAX_CONNECTION_ATTEMPT_DELAY_SECONDS,
                    (int) Math.pow(connectionAttemptDelay, 2));
            connectionAttemptTask = scheduler.schedule(this::attemptConnect, connectionAttemptDelay, TimeUnit.SECONDS);
        }
    }

    /**
     * Gets the IP transport.
     *
     * @throws IllegalAccessException if this is a child accessory or if the transport is not initialized.
     * @return the IpTransport
     */
    protected IpTransport getIpTransport() throws IllegalAccessException, IllegalStateException {
        if (isChildAccessory) {
            throw new IllegalAccessException("Child accessories must delegate to bridge IP transport");
        }
        IpTransport ipTransport = this.ipTransport;
        if (ipTransport == null) {
            throw new IllegalStateException("IP transport not initialized");
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

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        // only non child accessories require pairing support
        return thing.getBridgeUID() != null ? Set.of() : Set.of(HomekitPairingActions.class);
    }

    private @Nullable String checkedIpAddress() {
        Object obj = getConfig().get(CONFIG_IP_ADDRESS);
        if (obj == null || !(obj instanceof String ipAddress) || !IPV4_PATTERN.matcher(ipAddress).matches()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.invalid-ip-address", "Invalid IP address", null));
            return null;
        }
        return ipAddress;
    }

    private @Nullable String checkedMacAddress() {
        if (!(getConfig().get(Thing.PROPERTY_MAC_ADDRESS) instanceof String macAddress) || macAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.missing-mac-address", "Missing MAC address", null));
            return null;
        }
        return macAddress;
    }

    private @Nullable String checkedHostName() {
        Object obj = getConfig().get(CONFIG_HOST_NAME);
        if (obj == null || !(obj instanceof String hostName)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.invalid-host-name", "Invalid fully qualified host name", null));
            return null;
        }
        if (!HOST_PATTERN.matcher(hostName).matches()) {
            logger.warn("Host name '{}' does not match expected pattern; using anyway..", hostName);
        }
        return hostName.replace(" ", "\\032"); // escape mDNS spaces
    }

    private @Nullable Long checkedAccessoryId() {
        accessoryId = getAccessoryId();
        if (accessoryId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.invalid-accessory-id", "Invalid accessory ID", null));
            return null;
        }
        return accessoryId;
    }

    private @Nullable IpTransport checkedCreateIpTransport(String ipAddress, String hostName) {
        try {
            IpTransport ipTransport = new IpTransport(ipAddress, hostName, this);
            this.ipTransport = ipTransport;
            return ipTransport;
        } catch (IOException e) {
            logger.warn("Error '{}' creating transport", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    i18nProvider.getText(bundle, "error.failed-to-connect", "Failed to connect", null));
        }
        return null;
    }

    /**
     * Thing Action that pairs the accessory using the provided pairing code.
     *
     * @param code the pairing code
     * @param withExternalAuthentication true to setup with external authentication e.g. from an app, false otherwise
     */
    public boolean pair(String code, boolean withExternalAuthentication) {
        if (isChildAccessory) {
            logger.warn("Cannot pair child accessory '{}'", thing.getUID());
            return false; // child accessories cannot be paired directly
        }

        if (!PAIRING_CODE_PATTERN.matcher(code).matches()) {
            logger.debug("Pairing code must match XXX-XX-XXX or XXXX-XXXX or XXXXXXXX");
            return false; // invalid pairing code format
        }
        String pairingCode = normalizePairingCode(code);

        isConfigured = false;
        Long accessoryId = checkedAccessoryId();
        String ipAddress = checkedIpAddress();
        String macAddress = checkedMacAddress();
        String hostName = checkedHostName();
        if (accessoryId == null || ipAddress == null || macAddress == null || hostName == null) {
            return false; // configuration error
        }
        isConfigured = true;

        // create new transport
        if (checkedCreateIpTransport(ipAddress, hostName) == null) {
            return false; // transport creation failed
        }

        try {
            logger.debug("Starting Pair-Setup for {}", thing.getUID());
            PairSetupClient pairSetupClient = new PairSetupClient(getIpTransport(), keyStore.getControllerUUID(),
                    keyStore.getControllerKey(), pairingCode, withExternalAuthentication);

            Ed25519PublicKeyParameters accessoryKey = pairSetupClient.pair();
            keyStore.setAccessoryKey(macAddress, accessoryKey);

            logger.debug("Pair-Setup completed; starting Pair-Verify for {}", thing.getUID());
            connectionAttemptDelay = MIN_CONNECTION_ATTEMPT_DELAY_SECONDS; // reset delay on manual pairing
            scheduleConnectionAttempt();
            return true; // pairing succeeded
        } catch (Exception e) {
            // catch all; log all exceptions
            logger.warn("Pairing / verification failed '{}' for {}", e.getMessage(), thing.getUID());
            logger.debug("Stack trace", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, i18nProvider.getText(bundle,
                    "error.pairing-verification-failed", "Pairing / Verification failed", null));
            return false; // pairing failed
        }
    }

    /**
     * Inner method to unpair and clear stored key.
     */
    private boolean unpairInner() {
        if (isChildAccessory) {
            logger.warn("Cannot unpair child accessory '{}'", thing.getUID());
            return false;
        }

        if (!(getConfig().get(Thing.PROPERTY_MAC_ADDRESS) instanceof String macAddress) || macAddress.isBlank()) {
            logger.warn("Cannot unpair accessory '{}' due to missing mac address configuration", thing.getUID());
            return false;
        }
        try {
            PairRemoveClient service = new PairRemoveClient(getIpTransport(), keyStore.getControllerUUID());
            service.remove();
            keyStore.setAccessoryKey(macAddress, null);
            return true;
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException | IllegalAccessException
                | IllegalStateException e) {
            logger.warn("Error '{}' unpairing accessory '{}'", e.getMessage(), thing.getUID());
            return false;
        }
    }

    /**
     * Thing Action that unpairs the accessory.
     */
    public boolean unpair() {
        boolean unpaired = unpairInner();
        if (unpaired) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nProvider.getText(bundle, "error.not-paired", "Not paired", null));
        }
        return unpaired;
    }

    /**
     * Determines if the given throwable is an IOException or TimeoutException, including checking the cause
     * if it is wrapped in an ExecutionException. Used to identify communication-related exceptions that can
     * potentially be recovered.
     *
     * @param throwable the exception to check
     * @return true if it's an IOException or TimeoutException, false otherwise
     */
    protected boolean isCommunicationException(Throwable throwable) {
        return (throwable instanceof IOException || throwable instanceof TimeoutException) ? true
                : (throwable instanceof ExecutionException outer) && (outer.getCause() instanceof Throwable inner)
                        && (inner instanceof IOException || inner instanceof TimeoutException) ? true : false;
    }

    /**
     * Creates properties for the accessory based on the characteristics within the ACCESSORY_INFORMATION
     * service (if any).
     */
    protected void createProperties() {
        Map<Long, Accessory> accessories = getAccessories();
        if (accessories.isEmpty()) {
            return;
        }
        Long accessoryId = getAccessoryId();
        if (accessoryId == null) {
            return;
        }
        Accessory accessory = accessories.get(accessoryId);
        if (accessory == null) {
            return;
        }
        // search for the accessory information service and collect its properties
        for (Service service : accessory.services) {
            if (ServiceType.ACCESSORY_INFORMATION == service.getServiceType()) {
                for (Characteristic characteristic : service.characteristics) {
                    ChannelDefinition channelDef = characteristic.buildAndRegisterChannelDefinition(thing.getUID(),
                            typeProvider, i18nProvider, bundle);
                    if (channelDef != null && FAKE_PROPERTY_CHANNEL_TYPE_UID.equals(channelDef.getChannelTypeUID())) {
                        String name = channelDef.getId();
                        if (channelDef.getLabel() instanceof String value) {
                            thing.setProperty(name, value);
                        }
                    }
                }
                break; // only one accessory information service per accessory
            }
        }
    }

    /**
     * Wrapper to enable or disable events with exception handling.
     *
     * @param enable true to enable events, false to disable
     */
    private void enableEvents(boolean enable) {
        try {
            enableEventsOrThrow(enable);
        } catch (InterruptedException e) {
            // shutting down; do nothing
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("Communication error '{}' subscribing to events, reconnecting..", e.getMessage());
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("Unexpected error '{}' subscribing to events", e.getMessage());
            }
            logger.debug("Stack trace", e);
        }
    }

    /**
     * Inner method to enable or disable events members of the eventedCharacteristics list.
     * All exceptions are thrown upwards to the caller.
     *
     * @param enable true to enable events, false to disable
     * @throws IllegalStateException if this is a child accessory or if the read/write service is not initialized
     * @throws IllegalAccessException if this is a child accessory
     * @throws IOException if there is a communication error
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     * @throws ExecutionException if there is an execution error
     */
    private void enableEventsOrThrow(boolean enable) throws IllegalStateException, IllegalAccessException, IOException,
            InterruptedException, TimeoutException, ExecutionException {
        if (isChildAccessory) {
            logger.warn("Forbidden to enable/disable events on child accessory '{}'", thing.getUID());
            return;
        }
        Service service = new Service();
        service.characteristics = new ArrayList<>();
        service.characteristics.addAll(eventedCharacteristics.stream().map(characteristic -> {
            characteristic.ev = enable;
            return characteristic;
        }).toList());
        if (!service.characteristics.isEmpty()) {
            getRwService().writeCharacteristic(GSON.toJson(service));
            logger.debug("Eventing {}abled for {} channels", enable ? "en" : "dis", service.characteristics.size());
        }
    }

    /**
     * Checks if all handler instances are initialized.
     * Subclasses override this to implement the waiting logic.
     */
    protected abstract boolean checkHandlersInitialized();

    /**
     * Called when the root thing has finished loading the accessories.
     * Subclasses override this to perform any processing required.
     */
    protected abstract void onAccessoriesLoaded();

    /**
     * Called when the root handler is fully online.
     * Subclasses override this to perform any processing required.
     */
    protected abstract void onRootHandlerReady();

    @Override
    public abstract void onEvent(String jsonContent);

    /**
     * Called when the thing is fully online.
     * Enables eventing and updates the thing status to ONLINE.
     * Subclasses override this to perform any extra processing required.
     */
    protected void onThingOnline() {
        enableEvents(true);
        updateStatus(ThingStatus.ONLINE);
    }
}
