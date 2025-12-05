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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import org.openhab.binding.homekit.internal.hapservices.CharacteristicReadWriteClient;
import org.openhab.binding.homekit.internal.hapservices.PairRemoveClient;
import org.openhab.binding.homekit.internal.hapservices.PairSetupClient;
import org.openhab.binding.homekit.internal.hapservices.PairVerifyClient;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.binding.homekit.internal.session.EventListener;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.events.system.StartlevelEvent;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.service.StartLevelService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelDefinition;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles I/O with HomeKit server devices -- either simply accessories, bridge accessories or bridged
 * accessories. If the handler is for a HomeKit bridge or a HomeKit accessory it performs the pairing
 * and secure session setup. If the handler is for a HomeKit bridged accessory, it depends upon the
 * pairing and session of the bridge accessory handler.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public abstract class HomekitBaseAccessoryHandler extends BaseThingHandler implements EventListener, EventSubscriber {

    private static final int MIN_CONNECTION_ATTEMPT_DELAY_SECONDS = 2;
    private static final int MAX_CONNECTION_ATTEMPT_DELAY_SECONDS = 600;
    private static final int MANUAL_REFRESH_DELAY_SECONDS = 3;

    private final Logger logger = LoggerFactory.getLogger(HomekitBaseAccessoryHandler.class);
    private final Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
    private final HomekitKeyStore keyStore;

    private boolean isConfigured = false;
    private int connectionAttemptDelay = MIN_CONNECTION_ATTEMPT_DELAY_SECONDS;

    private volatile @Nullable ScheduledFuture<?> connectionAttemptTask;
    private volatile @Nullable CharacteristicReadWriteClient rwService;
    private volatile @Nullable IpTransport ipTransport;
    private volatile @Nullable ScheduledFuture<?> refreshTask;
    private volatile @Nullable Future<?> manualRefreshTask;

    private @NonNullByDefault({}) Long accessoryId;

    protected static final Gson GSON = new Gson();
    protected static final String THING_STATUS_FMT = "@text/%s [\"%s\"]";

    /**
     * Maps of evented and polled Characteristics.
     * The maps are keyed on the unique "aid,iid" combination to prevent duplicate entries.
     */
    protected static final String AID_IID_FORMAT = "%s,%s";
    protected final Map<String, Characteristic> eventedCharacteristics = new ConcurrentHashMap<>();
    protected final Map<String, Characteristic> polledCharacteristics = new ConcurrentHashMap<>();

    protected final HomekitTypeProvider typeProvider;
    protected final TranslationProvider i18nProvider;
    protected final Bundle bundle;

    protected boolean isBridgedAccessory = false;
    protected final Throttler throttler = new Throttler();

    private @Nullable ServiceRegistration<?> eventSubscription;

    /**
     * A helper class that runs a {@link Callable} and enforces a minimum delay between calls.
     * This is to avoid overwhelming accessories with too many requests in a short time.
     */
    private class Throttler {
        private static final Duration MIN_INTERVAL = Duration.ofSeconds(2);
        private @Nullable Instant notBeforeInstant = null;

        /**
         * Calls the given task. The method is synchronized to ensure that only one HTTP call is
         * executed at a time. It calculates the required delay based on the last call time and
         * sleeps if necessary. And it updates the notBeforeInstant after each call to enforce the
         * delay. It initializes notBeforeInstant if required.
         *
         * @param task the task to be called
         * @return the String result of the task
         * @throws Exception the compiler us to handle any exception, but will actually be more specific
         */
        public synchronized String call(Callable<String> task) throws Exception {
            try {
                Instant next = notBeforeInstant;
                if (next == null) {
                    notBeforeInstant = next = Instant.now().plus(MIN_INTERVAL);
                }
                Duration delay = Duration.between(Instant.now(), next);
                if (delay.isPositive()) {
                    Duration sleepDuration = delay.compareTo(MIN_INTERVAL) < 0 ? delay : MIN_INTERVAL;
                    logger.trace("{} throttling call for {} to respect minimum interval", thing.getUID(),
                            sleepDuration);
                    Thread.sleep(sleepDuration);
                }
                return task.call();
            } finally {
                notBeforeInstant = Instant.now().plus(MIN_INTERVAL);
            }
        }

        public void reset() {
            notBeforeInstant = null;
        }
    }

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
        eventedCharacteristics.clear();
        accessories.clear();
        cancelRefreshTasks();
        if (!isBridgedAccessory) {
            try {
                enableEventsOrThrow(false);
            } catch (Exception e) {
                // closing; ignore
            }
        }
        if (connectionAttemptTask instanceof ScheduledFuture<?> task) {
            task.cancel(true);
        }
        connectionAttemptTask = null;
        if (ipTransport instanceof IpTransport transport) {
            transport.close();
        }
        ipTransport = null;
        if (eventSubscription instanceof ServiceRegistration<?> registration) {
            registration.unregister();
        }
        eventSubscription = null;
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
            String json = throttler.call(() -> new String(getIpTransport().get(ENDPOINT_ACCESSORIES, CONTENT_TYPE_HAP),
                    StandardCharsets.UTF_8));
            Accessories acc0 = GSON.fromJson(json, Accessories.class);
            if (acc0 instanceof Accessories acc1 && acc1.accessories instanceof List<Accessory> acc2) {
                accessories.putAll(acc2.stream().filter(a -> Objects.nonNull(a.aid))
                        .collect(Collectors.toMap(a -> a.aid, Function.identity())));
            }
            logger.debug("{} fetched {} accessories", thing.getUID(), accessories.size());
            scheduler.submit(this::processBridgedThings);
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("{} communication error '{}' fetching accessories, reconnecting..", thing.getUID(),
                        e.getMessage());
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("{} unexpected error '{}' fetching accessories", thing.getUID(), e.getMessage());
            }
            logger.debug("Stack trace", e);
        }
    }

    /**
     * Called after all bridged accessory things are initialized, and processes them by calling the
     * overloaded abstract 'onConnectedThingAccessoriesLoaded' methods, and finally calls the
     * 'onThingOnline' methods (and its eventual overloaded implementations).
     */
    private void processBridgedThings() {
        if (!bridgedThingsInitialized()) {
            logger.warn("{} unexpected error: bridged Things not initialized.", thing.getUID());
        }
        onConnectedThingAccessoriesLoaded();
        onThingOnline();
    }

    /**
     * Returns the accessory ID. For bridges and accessories this is always 1. Whereas for
     * bridged accessories it comes from the thing's configuration parameter value.
     *
     * @return the accessory ID, or null if it cannot be determined
     */
    protected @Nullable Long getAccessoryId() {
        if (isBridgedAccessory) {
            if (getConfig().get(CONFIG_ACCESSORY_ID) instanceof BigDecimal accessoryId) {
                try {
                    return accessoryId.longValue();
                } catch (NumberFormatException e) {
                }
            }
            logger.debug("{} missing or invalid accessory id", thing.getUID());
            return null;
        }
        return 1L;
    }

    @Override
    public void handleRemoval() {
        cancelRefreshTasks();
        if (isBridgedAccessory) {
            updateStatus(ThingStatus.REMOVED);
        } else {
            scheduler.submit(() -> {
                if (unpairInner().startsWith(ACTION_RESULT_OK)) {
                    updateStatus(ThingStatus.REMOVED);
                }
            });
        }
    }

    @Override
    public void initialize() {
        isBridgedAccessory = getBridge() instanceof Bridge;
        if (!isBridgedAccessory) {
            if (alreadyAtStartLevelComplete()) {
                // schedule connection attempt immediately
                scheduleConnectionAttempt();
            } else {
                // delay connection attempt until STARTLEVEL_COMPLETE is signalled via receive() method below
                BundleContext context = bundle.getBundleContext();
                eventSubscription = context.registerService(EventSubscriber.class.getName(), this, null);
            }
        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Return true if STARTLEVEL_COMPLETE has already been acheived.
     * <p>
     * Note: STARTLEVEL_COMPLETE means all Thing handlers are instantiated and their initialize() methods have
     * been called, and the registries for item, thing, and item-channel-links have all been loaded.
     */
    private boolean alreadyAtStartLevelComplete() {
        BundleContext context = bundle.getBundleContext();
        ServiceReference<StartLevelService> reference = context.getServiceReference(StartLevelService.class);
        if (reference != null && context.getService(reference) instanceof StartLevelService service) {
            try {
                return service.getStartLevel() >= StartLevelService.STARTLEVEL_COMPLETE;
            } finally {
                context.ungetService(reference);
            }
        }
        return false;
    }

    /**
     * When an event is received, checks if {@link StartLevelService#STARTLEVEL_COMPLETE} is reached, and if so
     * schedules a connection attempt.
     * <p>
     * Note: STARTLEVEL_COMPLETE means all Thing handlers are instantiated and their initialize() methods have
     * been called, and the registries for item, thing, and item-channel-links have all been loaded.
     */
    @Override
    public void receive(Event event) {
        if (event instanceof StartlevelEvent sle && sle.getStartlevel() >= StartLevelService.STARTLEVEL_COMPLETE) {
            scheduleConnectionAttempt();
        }
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of(StartlevelEvent.TYPE);
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
            logger.debug("{} no stored pairing credentials", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.not-paired");
            return false;
        }

        // create new transport
        if (checkedCreateIpTransport(ipAddress, hostName) == null) {
            return false; // transport creation failed
        }

        // attempt to verify pairing
        try {
            logger.debug("{} starting Pair-Verify with existing key", thing.getUID());
            PairVerifyClient client = new PairVerifyClient(getIpTransport(), keyStore.getControllerUUID(),
                    keyStore.getControllerKey(), accessoryKey);

            getIpTransport().setSessionKeys(client.verify());
            rwService = new CharacteristicReadWriteClient(getIpTransport());
            throttler.reset();

            logger.debug("{} restored pairing was verified", thing.getUID());
            scheduler.schedule(this::fetchAccessories, MIN_CONNECTION_ATTEMPT_DELAY_SECONDS, TimeUnit.SECONDS);
            return true; // pairing restore succeeded => exit
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IllegalAccessException
                | InvalidCipherTextException | IOException | InterruptedException | TimeoutException
                | ExecutionException | IllegalStateException e) {
            logger.debug("{} restored pairing was not verified", thing.getUID(), e);
            // pairing restore failed => exit and perhaps try again later
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    THING_STATUS_FMT.formatted("error.pairing-verification-failed", e.getMessage()));
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
     * @throws IllegalAccessException if this is a bridged accessory or if the transport is not initialized.
     * @return the IpTransport
     */
    protected IpTransport getIpTransport() throws IllegalAccessException, IllegalStateException {
        if (isBridgedAccessory) {
            throw new IllegalAccessException("Bridged accessories must delegate to bridge IP transport");
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
     * @return the CharacteristicReadWriteClient
     */
    protected @Nullable CharacteristicReadWriteClient getRwService() {
        return rwService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        // only bridges and accessories require pairing support
        return isBridgedAccessory ? Set.of() : Set.of(HomekitPairingActions.class);
    }

    private @Nullable String checkedIpAddress() {
        Object obj = getConfig().get(CONFIG_IP_ADDRESS);
        if (obj == null || !(obj instanceof String ipAddress) || !IPV4_PATTERN.matcher(ipAddress).matches()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.invalid-ip-address");
            return null;
        }
        return ipAddress;
    }

    private @Nullable String checkedMacAddress() {
        if (!(getConfig().get(CONFIG_MAC_ADDRESS) instanceof String macAddress) || macAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.missing-mac-address");
            return null;
        }
        return macAddress;
    }

    private @Nullable String checkedHostName() {
        Object obj = getConfig().get(CONFIG_HTTP_HOST_HEADER);
        if (obj == null || !(obj instanceof String hostName)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.invalid-host-name");
            return null;
        }
        if (!HOST_PATTERN.matcher(hostName).matches()) {
            logger.warn("{} host name '{}' does not match expected pattern; using anyway..", thing.getUID(), hostName);
        }
        return hostName.replace(" ", "\\032"); // escape mDNS spaces
    }

    private @Nullable Long checkedAccessoryId() {
        accessoryId = getAccessoryId();
        if (accessoryId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.invalid-accessory-id");
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    THING_STATUS_FMT.formatted("error.failed-to-connect", e.getMessage()));
        }
        return null;
    }

    /**
     * Thing Action that pairs the accessory using the provided pairing code.
     *
     * @param code the pairing code
     * @param withExternalAuthentication true to setup with external authentication e.g. from an app, false otherwise
     *
     * @return OK or ERROR with reason
     */
    public String pair(String code, boolean withExternalAuthentication) {
        if (isBridgedAccessory) {
            logger.warn("{} forbidden to pair a bridged accessory", thing.getUID());
            return ACTION_RESULT_ERROR_FORMAT.formatted("bridged accessory");
        }

        if (!PAIRING_CODE_PATTERN.matcher(code).matches()) {
            logger.debug("{} pairing code must match XXX-XX-XXX or XXXX-XXXX or XXXXXXXX", thing.getUID());
            return ACTION_RESULT_ERROR_FORMAT.formatted("code format");
        }
        String pairingCode = normalizePairingCode(code);

        isConfigured = false;
        Long accessoryId = checkedAccessoryId();
        String ipAddress = checkedIpAddress();
        String macAddress = checkedMacAddress();
        String hostName = checkedHostName();
        if (accessoryId == null || ipAddress == null || macAddress == null || hostName == null) {
            return ACTION_RESULT_ERROR_FORMAT.formatted("config error");
        }
        isConfigured = true;

        if (keyStore.getAccessoryKey(macAddress) != null) {
            return ACTION_RESULT_OK_FORMAT.formatted("already paired"); // OK if already paired
        }

        // create new transport
        if (checkedCreateIpTransport(ipAddress, hostName) == null) {
            return ACTION_RESULT_ERROR_FORMAT.formatted("no transport");
        }

        try {
            logger.debug("{} starting Pair-Setup", thing.getUID());
            PairSetupClient pairSetupClient = new PairSetupClient(getIpTransport(), keyStore.getControllerUUID(),
                    keyStore.getControllerKey(), pairingCode, withExternalAuthentication);

            Ed25519PublicKeyParameters accessoryKey = pairSetupClient.pair();
            keyStore.setAccessoryKey(macAddress, accessoryKey);

            logger.debug("{} completed Pair-Setup; starting Pair-Verify", thing.getUID());
            connectionAttemptDelay = MIN_CONNECTION_ATTEMPT_DELAY_SECONDS; // reset delay on manual pairing
            scheduleConnectionAttempt();
            return ACTION_RESULT_OK; // pairing succeeded
        } catch (Exception e) {
            // catch all; log all exceptions
            logger.debug("{} pairing / verification failed '{}'", thing.getUID(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    THING_STATUS_FMT.formatted("error.pairing-verification-failed", e.getMessage()));
            return ACTION_RESULT_ERROR_FORMAT.formatted("pairing error");
        }
    }

    /**
     * Inner method to unpair and clear stored key.
     *
     * @return OK or ERROR with reason
     */
    private String unpairInner() {
        if (isBridgedAccessory) {
            logger.warn("{} forbidden to unpair a bridged accessory", thing.getUID());
            return ACTION_RESULT_ERROR_FORMAT.formatted("bridged accessory");
        }

        if (!(getConfig().get(CONFIG_MAC_ADDRESS) instanceof String macAddress) || macAddress.isBlank()) {
            logger.warn("{} cannot unpair accessory due to missing mac address configuration", thing.getUID());
            return ACTION_RESULT_ERROR_FORMAT.formatted("config error");
        }

        if (keyStore.getAccessoryKey(macAddress) == null) {
            return ACTION_RESULT_ERROR_FORMAT.formatted("not paired");
        }

        try {
            PairRemoveClient service = new PairRemoveClient(getIpTransport(), keyStore.getControllerUUID());
            service.remove();
            keyStore.setAccessoryKey(macAddress, null);
            return ACTION_RESULT_OK;
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException | IllegalAccessException
                | IllegalStateException e) {
            logger.warn("{} error '{}' unpairing accessory", thing.getUID(), e.getMessage());
            return ACTION_RESULT_ERROR_FORMAT.formatted("unpairing error");
        }
    }

    /**
     * Thing Action that unpairs the accessory.
     *
     * @return OK or ERROR with reason
     */
    public String unpair() {
        String result = unpairInner();
        if (result.startsWith(ACTION_RESULT_OK)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.not-paired");
        }
        return result;
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
        Map<String, String> thingProperties = new HashMap<>(thing.getProperties());
        for (Service service : accessory.services) {
            if (ServiceType.ACCESSORY_INFORMATION == service.getServiceType()) {
                for (Characteristic characteristic : service.characteristics) {
                    ChannelDefinition channelDef = characteristic.buildAndRegisterChannelDefinition(thing.getUID(),
                            typeProvider, i18nProvider, bundle);
                    if (channelDef != null && CHANNEL_TYPE_STATIC.equals(channelDef.getChannelTypeUID())) {
                        // only static ChannelDefinitions contribute to the properties
                        thingProperties.putAll(channelDef.getProperties());
                    }
                }
                break; // only one accessory information service per accessory
            }
        }
        thing.setProperties(thingProperties);
    }

    /**
     * Wrapper to enable or disable eventing for members of the eventedCharacteristics list of the
     * accessory or its bridged accessories, with exception handling.
     *
     * @param enable true to enable events, false to disable
     */
    private void enableEvents(boolean enable) {
        try {
            enableEventsOrThrow(enable);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // shutting down; restore interrupt flag and do nothing
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("{} communication error '{}' subscribing to events, reconnecting..", thing.getUID(),
                        e.getMessage());
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("{} unexpected error '{}' subscribing to events", thing.getUID(), e.getMessage());
            }
            logger.debug("Stack trace", e);
        }
    }

    /**
     * Inner method to enable or disable eventing for members of the eventedCharacteristics list of the
     * accessory or its bridged accessories. All exceptions are thrown upwards to the caller.
     *
     * @param enable true to enable events, false to disable
     * @throws Exception the compiler requires us to handle any error; but it will actually be one of the following:
     * @throws IllegalStateException if this is a bridged accessory or if the read/write service is not initialized,
     * @throws IOException if there is a communication error,
     * @throws InterruptedException if the operation is interrupted,
     * @throws TimeoutException if the operation times out,
     * @throws ExecutionException if there is an execution error
     */
    private void enableEventsOrThrow(boolean enable) throws Exception {
        if (isBridgedAccessory) {
            throw new IllegalStateException("Forbidden to enable/disable events on bridged accessory");
        }
        Service service = new Service();
        service.characteristics = new ArrayList<>();
        service.characteristics.addAll(getEventedCharacteristics().values().stream().map(cxx -> {
            cxx.ev = enable;
            return cxx;
        }).toList());
        if (service.characteristics.isEmpty()) {
            return;
        }
        final CharacteristicReadWriteClient rwService = this.rwService;
        if (rwService == null) {
            throw new IllegalStateException("Read/write service not initialized");
        }
        throttler.call(() -> rwService.writeCharacteristics(GSON.toJson(service)));
        logger.debug("{} eventing {}abled for {} channels", thing.getUID(), enable ? "en" : "dis",
                service.characteristics.size());
    }

    /**
     * Polls all characteristics in the polledCharacteristics list of the accessory or its bridged accessories.
     * Called periodically by the refresh task and on-demand when RefreshType.REFRESH is called.
     */
    private synchronized void refresh() {
        List<String> queries = getPolledCharacteristics().values().stream().filter(c -> c.iid != null && c.aid != null)
                .map(c -> "%s.%s".formatted(c.aid, c.iid)).toList();
        if (queries.isEmpty()) {
            return;
        }
        final CharacteristicReadWriteClient rwService = this.rwService;
        if (rwService == null) {
            throw new IllegalStateException("Read/write service not initialized");
        }
        try {
            String json = throttler.call(() -> rwService.readCharacteristics(String.join(",", queries)));
            onEvent(json);
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("{} communication error '{}' polling accessories, reconnecting..", thing.getUID(),
                        e.getMessage(), e);
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("{} unexpected error '{}' polling accessories", thing.getUID(), e.getMessage(), e);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    THING_STATUS_FMT.formatted("error.polling-error", e.getMessage()));
        }
    }

    /**
     * Checks if all bridged accessory things have the reached status UNKNOWN, OFFLINE, or ONLINE.
     * Subclasses MUST override this to perform the check.
     */
    protected abstract boolean bridgedThingsInitialized();

    /**
     * Called when the connected thing has finished loading the accessories.
     * Subclasses MUST override this to perform any extra processing required.
     */
    protected abstract void onConnectedThingAccessoriesLoaded();

    /**
     * Gets the evented characteristics list for this accessory or its bridged accessories.
     * Subclasses MUST override this to perform any extra processing required.
     *
     * @return map of channel UID to characteristic
     */
    protected abstract Map<String, Characteristic> getEventedCharacteristics();

    /**
     * Gets the polled characteristics list for this accessory or its bridged accessories.
     * Subclasses MUST override this to perform any extra processing required.
     *
     * @return map of channel UID to characteristic
     */
    protected abstract Map<String, Characteristic> getPolledCharacteristics();

    @Override
    public abstract void onEvent(String json);

    /**
     * Called when the thing is fully online. Updates the thing status to ONLINE. And if the
     * thing is not a bridged accessory, enables eventing,and starts the refresh task.
     * Subclasses MAY override this to perform any extra processing required.
     */
    protected void onThingOnline() {
        updateStatus(ThingStatus.ONLINE);
        if (!isBridgedAccessory) {
            enableEvents(true);
            startConnectedThingRefreshTask();
        }
    }

    /**
     * Called when the connected thing handler has been initialized, the pairing verified, the accessories
     * loaded, and the channels and properties created. Sets up a scheduled task to periodically refresh
     * the state of the accessory.
     */
    private void startConnectedThingRefreshTask() {
        if (getConfig().get(CONFIG_REFRESH_INTERVAL) instanceof Object refreshInterval) {
            try {
                int refreshIntervalSeconds = Integer.parseInt(refreshInterval.toString());
                if (refreshIntervalSeconds > 0) {
                    ScheduledFuture<?> task = refreshTask;
                    if (task == null || task.isCancelled() || task.isDone()) {
                        refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, refreshIntervalSeconds,
                                refreshIntervalSeconds, TimeUnit.SECONDS);
                    }
                }
            } catch (NumberFormatException e) {
                // logged below
            }
        }
        if (refreshTask == null) {
            logger.warn("{} invalid refresh interval configuration, polling disabled", thing.getUID());
        }
    }

    /**
     * Cancels the refresh tasks if either is running.
     */
    private void cancelRefreshTasks() {
        if (refreshTask instanceof ScheduledFuture<?> task) {
            task.cancel(true);
        }
        if (manualRefreshTask instanceof Future<?> task) {
            task.cancel(true);
        }
        refreshTask = null;
        manualRefreshTask = null;
    }

    /**
     * Requests a manual refresh by scheduling a refresh task after a short debounce delay. Defers to the
     * bridge handler if this is a bridged accessory. And if a manual refresh task is already scheduled or
     * running, it does nothing more.
     */
    protected void requestManualRefresh() {
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            bridgeHandler.requestManualRefresh();
        } else {
            Future<?> task = manualRefreshTask;
            if (task == null || task.isDone() || task.isCancelled()) {
                manualRefreshTask = scheduler.schedule(this::refresh, MANUAL_REFRESH_DELAY_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Reads characteristic(s) from the accessory. Defers to the bridge handler if this is a bridged accessory.
     *
     * @param query a comma delimited HTTP query string e.g. "1.10,1.11" for aid 1 and iid 10 and 11
     * @return JSON response as String
     * @throws Exception compiler requires us to handle any exception; but actually will be one of the following:
     * @throws ExecutionException if there is an execution error
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException if the operation is interrupted
     * @throws IOException if there is a communication error
     * @throws IllegalStateException if the read/write service is not initialized
     */
    protected String readCharacteristics(String query) throws Exception {
        CharacteristicReadWriteClient rwService = getBridge() instanceof Bridge bridge
                && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler ? bridgeHandler.getRwService()
                        : getRwService();
        if (rwService == null) {
            throw new IllegalStateException("Read/write service not initialized");
        }
        return throttler.call(() -> rwService.readCharacteristics(query));
    }

    /**
     * Writes characteristic(s) to the accessory. Defers to the bridge handler if this is a bridged accessory.
     *
     * @param json the JSON to write
     * @return the JSON response
     * @throws Exception compiler requires us to handle any exception; but actually will be one of the following:
     * @throws ExecutionException if there is an execution error
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException
     * @throws IOException if there is a communication error
     * @throws IllegalStateException if the read/write service is not initialized
     */
    protected String writeCharacteristics(String json) throws Exception {
        CharacteristicReadWriteClient rwService = getBridge() instanceof Bridge bridge
                && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler ? bridgeHandler.getRwService()
                        : getRwService();
        if (rwService == null) {
            throw new IllegalStateException("Read/write service not initialized");
        }
        return throttler.call(() -> rwService.writeCharacteristics(json));
    }
}
