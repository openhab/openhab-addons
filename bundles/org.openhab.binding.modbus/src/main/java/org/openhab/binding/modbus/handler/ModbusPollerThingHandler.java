/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.handler;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.internal.AtomicStampedValue;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.binding.modbus.internal.config.ModbusPollerConfiguration;
import org.openhab.binding.modbus.internal.handler.ModbusDataThingHandler;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusFailureCallback;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ModbusPollerThingHandler} is responsible for polling Modbus slaves. Errors and data is delegated to
 * child thing handlers inheriting from {@link ModbusReadCallback} -- in practice: {@link ModbusDataThingHandler}.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusPollerThingHandler extends BaseBridgeHandler {

    /**
     * {@link ModbusReadCallback} that delegates all tasks forward.
     *
     * All instances of {@linkplain ReadCallbackDelegator} are considered equal, if they are connected to the same
     * bridge. This makes sense, as the callback delegates
     * to all child things of this bridge.
     *
     * @author Sami Salonen - Initial contribution
     *
     */
    private class ReadCallbackDelegator
            implements ModbusReadCallback, ModbusFailureCallback<ModbusReadRequestBlueprint> {

        private volatile @Nullable AtomicStampedValue<PollResult> lastResult;

        public synchronized void handleResult(PollResult result) {
            // Ignore all incoming data and errors if configuration is not correct
            if (hasConfigurationError() || disposed) {
                return;
            }
            if (config.getCacheMillis() >= 0) {
                AtomicStampedValue<PollResult> localLastResult = this.lastResult;
                if (localLastResult == null) {
                    this.lastResult = new AtomicStampedValue<>(System.currentTimeMillis(), result);
                } else {
                    localLastResult.update(System.currentTimeMillis(), result);
                    this.lastResult = localLastResult;
                }
            }
            logger.debug("Thing {} received response {}", thing.getUID(), result);
            notifyChildren(result);
            if (result.failure != null) {
                Exception error = result.failure.getCause();
                assert error != null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Error with read: %s: %s", error.getClass().getName(), error.getMessage()));
            } else {
                resetCommunicationError();
            }
        }

        @Override
        public synchronized void handle(AsyncModbusReadResult result) {
            // Casting to allow registers.orElse(null) below..
            Optional<@Nullable ModbusRegisterArray> registers = (Optional<@Nullable ModbusRegisterArray>) result
                    .getRegisters();
            lastPolledDataCache.set(registers.orElse(null));
            handleResult(new PollResult(result));
        }

        @Override
        public synchronized void handle(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
            handleResult(new PollResult(failure));
        }

        private void resetCommunicationError() {
            ThingStatusInfo statusInfo = thing.getStatusInfo();
            if (ThingStatus.OFFLINE.equals(statusInfo.getStatus())
                    && ThingStatusDetail.COMMUNICATION_ERROR.equals(statusInfo.getStatusDetail())) {
                updateStatus(ThingStatus.ONLINE);
            }
        }

        /**
         * Update children data if data is fresh enough
         *
         * @param oldestStamp oldest data that is still passed to children
         * @return whether data was updated. Data is not updated when it's too old or there's no data at all.
         */
        @SuppressWarnings("null")
        public boolean updateChildrenWithOldData(long oldestStamp) {
            return Optional.ofNullable(this.lastResult).map(result -> result.copyIfStampAfter(oldestStamp))
                    .map(result -> {
                        logger.debug("Thing {} reusing cached data: {}", thing.getUID(), result.getValue());
                        notifyChildren(result.getValue());
                        return true;
                    }).orElse(false);
        }

        private void notifyChildren(PollResult pollResult) {
            @Nullable
            AsyncModbusReadResult result = pollResult.result;
            @Nullable
            AsyncModbusFailure<ModbusReadRequestBlueprint> failure = pollResult.failure;
            childCallbacks.forEach(handler -> {
                if (result != null) {
                    handler.onReadResult(result);
                } else if (failure != null) {
                    handler.handleReadError(failure);
                }
            });
        }

        /**
         * Rest data caches
         */
        public void resetCache() {
            lastResult = null;
        }
    }

    /**
     * Immutable data object to cache the results of a poll request
     */
    private class PollResult {

        public final @Nullable AsyncModbusReadResult result;
        public final @Nullable AsyncModbusFailure<ModbusReadRequestBlueprint> failure;

        PollResult(AsyncModbusReadResult result) {
            this.result = result;
            this.failure = null;
        }

        PollResult(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
            this.result = null;
            this.failure = failure;
        }

        @Override
        public String toString() {
            return failure == null ? String.format("PollResult(result=%s)", result)
                    : String.format("PollResult(failure=%s)", failure);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(ModbusPollerThingHandler.class);

    private static final List<String> SORTED_READ_FUNCTION_CODES = ModbusBindingConstantsInternal.READ_FUNCTION_CODES
            .keySet().stream().sorted().collect(Collectors.toUnmodifiableList());

    private @NonNullByDefault({}) ModbusPollerConfiguration config;
    private long cacheMillis;
    private volatile @Nullable PollTask pollTask;
    private volatile @Nullable ModbusReadRequestBlueprint request;
    private volatile boolean disposed;
    private volatile List<ModbusDataThingHandler> childCallbacks = new CopyOnWriteArrayList<>();
    private volatile AtomicReference<@Nullable ModbusRegisterArray> lastPolledDataCache = new AtomicReference<>();
    private @NonNullByDefault({}) ModbusCommunicationInterface comms;

    private ReadCallbackDelegator callbackDelegator = new ReadCallbackDelegator();

    private @Nullable ModbusReadFunctionCode functionCode;

    public ModbusPollerThingHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels, no commands
    }

    private @Nullable ModbusEndpointThingHandler getEndpointThingHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge is null");
            return null;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not online");
            return null;
        }

        ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            logger.debug("Bridge handler is null");
            return null;
        }

        if (handler instanceof ModbusEndpointThingHandler) {
            ModbusEndpointThingHandler slaveEndpoint = (ModbusEndpointThingHandler) handler;
            return slaveEndpoint;
        } else {
            logger.debug("Unexpected bridge handler: {}", handler);
            return null;
        }
    }

    @Override
    public synchronized void initialize() {
        if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            // If the bridge was online then first change it to offline.
            // this ensures that children will be notified about the change
            updateStatus(ThingStatus.OFFLINE);
        }
        this.callbackDelegator.resetCache();
        comms = null;
        request = null;
        disposed = false;
        logger.trace("Initializing {} from status {}", this.getThing().getUID(), this.getThing().getStatus());
        try {
            config = getConfigAs(ModbusPollerConfiguration.class);
            String type = config.getType();
            if (!ModbusBindingConstantsInternal.READ_FUNCTION_CODES.containsKey(type)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("No function code found for type='%s'. Was expecting one of: %s", type,
                                String.join(", ", SORTED_READ_FUNCTION_CODES)));
                return;
            }
            functionCode = ModbusBindingConstantsInternal.READ_FUNCTION_CODES.get(type);
            switch (functionCode) {
                case READ_INPUT_REGISTERS:
                case READ_MULTIPLE_REGISTERS:
                    if (config.getLength() > ModbusConstants.MAX_REGISTERS_READ_COUNT) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                                "Maximum of %d registers can be polled at once due to protocol limitations. Length %d is out of bounds.",
                                ModbusConstants.MAX_REGISTERS_READ_COUNT, config.getLength()));
                        return;
                    }
                    break;
                case READ_COILS:
                case READ_INPUT_DISCRETES:
                    if (config.getLength() > ModbusConstants.MAX_BITS_READ_COUNT) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                                "Maximum of %d coils/discrete inputs can be polled at once due to protocol limitations. Length %d is out of bounds.",
                                ModbusConstants.MAX_BITS_READ_COUNT, config.getLength()));
                        return;
                    }
                    break;
            }
            cacheMillis = this.config.getCacheMillis();
            registerPollTask();
        } catch (EndpointNotInitializedException e) {
            logger.debug("Exception during initialization", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                    .format("Exception during initialization: %s (%s)", e.getMessage(), e.getClass().getSimpleName()));
        } finally {
            logger.trace("initialize() of thing {} '{}' finished", thing.getUID(), thing.getLabel());
        }
    }

    @Override
    public synchronized void dispose() {
        logger.debug("dispose()");
        // Mark handler as disposed as soon as possible to halt processing of callbacks
        disposed = true;
        unregisterPollTask();
        this.callbackDelegator.resetCache();
        comms = null;
        lastPolledDataCache.set(null);
    }

    /**
     * Unregister poll task.
     *
     * No-op in case no poll task is registered, or if the initialization is incomplete.
     */
    public synchronized void unregisterPollTask() {
        logger.trace("unregisterPollTask()");
        if (config == null) {
            return;
        }
        PollTask localPollTask = this.pollTask;
        if (localPollTask != null) {
            logger.debug("Unregistering polling from ModbusManager");
            comms.unregisterRegularPoll(localPollTask);
        }
        this.pollTask = null;
        request = null;
        comms = null;
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * Register poll task
     *
     * @throws EndpointNotInitializedException in case the bridge initialization is not complete. This should only
     *             happen in transient conditions, for example, when bridge is initializing.
     */
    @SuppressWarnings("null")
    private synchronized void registerPollTask() throws EndpointNotInitializedException {
        logger.trace("registerPollTask()");
        if (pollTask != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            logger.debug("pollTask should be unregistered before registering a new one!");
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, String.format("Bridge '%s' is offline",
                    Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>")));
            logger.debug("No bridge handler available -- aborting init for {}", this);
            return;
        }
        ModbusCommunicationInterface localComms = slaveEndpointThingHandler.getCommunicationInterface();
        if (localComms == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, String.format(
                    "Bridge '%s' not completely initialized", Optional.ofNullable(getBridge()).map(b -> b.getLabel())));
            logger.debug("Bridge not initialized fully (no communication interface) -- aborting init for {}", this);
            return;
        }
        this.comms = localComms;
        ModbusReadFunctionCode localFunctionCode = functionCode;
        if (localFunctionCode == null) {
            return;
        }

        ModbusReadRequestBlueprint localRequest = new ModbusReadRequestBlueprint(slaveEndpointThingHandler.getSlaveId(),
                localFunctionCode, config.getStart(), config.getLength(), config.getMaxTries());
        this.request = localRequest;

        if (config.getRefresh() <= 0L) {
            logger.debug("Not registering polling with ModbusManager since refresh disabled");
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Not polling");
        } else {
            logger.debug("Registering polling with ModbusManager");
            pollTask = localComms.registerRegularPoll(localRequest, config.getRefresh(), 0, callbackDelegator,
                    callbackDelegator);
            assert pollTask != null;
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private boolean hasConfigurationError() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        return statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR;
    }

    @Override
    public synchronized void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged for {}. Reseting handler", this.getThing().getUID());
        this.dispose();
        this.initialize();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ModbusDataThingHandler) {
            this.childCallbacks.add((ModbusDataThingHandler) childHandler);
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ModbusDataThingHandler) {
            this.childCallbacks.remove(childHandler);
        }
    }

    /**
     * Return {@link ModbusReadRequestBlueprint} represented by this thing.
     *
     * Note that request might be <code>null</code> in case initialization is not complete.
     *
     * @return modbus request represented by this poller
     */
    public @Nullable ModbusReadRequestBlueprint getRequest() {
        return request;
    }

    /**
     * Get communication interface associated with this poller
     *
     * @return
     */
    public ModbusCommunicationInterface getCommunicationInterface() {
        return comms;
    }

    /**
     * Refresh the data
     *
     * If data or error was just recently received (i.e. cache is fresh), return the cached response.
     */
    public void refresh() {
        ModbusReadRequestBlueprint localRequest = this.request;
        if (localRequest == null) {
            return;
        }
        ModbusRegisterArray possiblyMutatedCache = lastPolledDataCache.get();
        AtomicStampedValue<PollResult> lastPollResult = callbackDelegator.lastResult;
        if (lastPollResult != null && possiblyMutatedCache != null) {
            AsyncModbusReadResult lastSuccessfulPollResult = lastPollResult.getValue().result;
            if (lastSuccessfulPollResult != null) {
                ModbusRegisterArray lastRegisters = ((Optional<@Nullable ModbusRegisterArray>) lastSuccessfulPollResult
                        .getRegisters()).orElse(null);
                if (lastRegisters != null && !possiblyMutatedCache.equals(lastRegisters)) {
                    // Register has been mutated in between by a data thing that writes "individual bits"
                    // Invalidate cache for a fresh poll
                    callbackDelegator.resetCache();
                }
            }
        }

        long oldDataThreshold = System.currentTimeMillis() - cacheMillis;
        boolean cacheWasRecentEnoughForUpdate = cacheMillis > 0
                && this.callbackDelegator.updateChildrenWithOldData(oldDataThreshold);
        if (cacheWasRecentEnoughForUpdate) {
            logger.debug(
                    "Poller {} received refresh() and cache was recent enough (age at most {} ms). Reusing old response",
                    getThing().getUID(), cacheMillis);
        } else {
            // cache expired, poll new data
            logger.debug("Poller {} received refresh() but the cache is not applicable. Polling new data",
                    getThing().getUID());
            ModbusCommunicationInterface localComms = comms;
            if (localComms != null) {
                localComms.submitOneTimePoll(localRequest, callbackDelegator, callbackDelegator);
            }
        }
    }

    public AtomicReference<@Nullable ModbusRegisterArray> getLastPolledDataCache() {
        return lastPolledDataCache;
    }
}
