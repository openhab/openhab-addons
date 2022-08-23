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
package org.openhab.binding.insteon.internal.device;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature.QueryStatus;
import org.openhab.binding.insteon.internal.device.DeviceType.FeatureEntry;
import org.openhab.binding.insteon.internal.device.database.LinkDB;
import org.openhab.binding.insteon.internal.device.database.LinkDBRecord;
import org.openhab.binding.insteon.internal.device.database.ModemDBEntry;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.driver.RequestQueueManager.RequestQueueEntry;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.GroupMessageStateMachine;
import org.openhab.binding.insteon.internal.message.GroupMessageStateMachine.GroupMessageType;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The InsteonDevice class holds known per-device state of a single Insteon device,
 * including the address, what port(modem) to reach it on etc.
 * Note that some Insteon devices de facto consist of two devices (let's say
 * a relay and a sensor), but operate under the same address. Such devices will
 * be represented just by a single InsteonDevice. Their different personalities
 * will then be represented by DeviceFeatures.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class InsteonDevice {
    private static final int BCAST_STATE_TIMEOUT = 2000; // in milliseconds
    private static final int DIRECT_ACK_TIMEOUT = 6000; // in milliseconds
    private static final int FAILED_MSG_COUNT_THRESHOLD = 5;

    private static final Logger logger = LoggerFactory.getLogger(InsteonDevice.class);

    public enum DeviceStatus {
        INITIALIZED,
        POLLING
    }

    private InsteonAddress address = new InsteonAddress();
    private @Nullable Driver driver;
    private @Nullable InsteonDeviceHandler handler;
    private Map<String, DeviceFeature> features = new LinkedHashMap<>();
    private Map<String, Boolean> flags = new HashMap<>();
    private @Nullable ProductData productData;
    private InsteonEngine engine = InsteonEngine.UNKNOWN;
    private DeviceStatus status = DeviceStatus.INITIALIZED;
    private LinkDB linkDB;
    private long pollInterval = -1L; // in milliseconds
    private volatile int failedMsgCount = 0;
    private volatile long lastMsgReceived = 0L;
    private List<Msg> storedMessages = new LinkedList<>();
    private Queue<RequestQueueEntry> deferredQueue = new PriorityQueue<>();
    private Map<Msg, RequestQueueEntry> deferredQueueHash = new HashMap<>();
    private Queue<RequestQueueEntry> requestQueue = new PriorityQueue<>();
    private Map<Msg, RequestQueueEntry> requestQueueHash = new HashMap<>();
    private @Nullable DeviceFeature featureQueried;
    private long lastQuerySent = 0L;
    private Map<Byte, Long> lastBroadcastReceived = new HashMap<>();
    private Map<Integer, GroupMessageStateMachine> groupState = new HashMap<>();

    /**
     * Constructor
     */
    public InsteonDevice() {
        lastMsgReceived = System.currentTimeMillis();
        linkDB = new LinkDB(this);
    }

    // --------------------- simple getters -----------------------------

    public InsteonAddress getAddress() {
        return address;
    }

    public @Nullable Driver getDriver() {
        return driver;
    }

    public @Nullable InsteonDeviceHandler getHandler() {
        return handler;
    }

    public List<DeviceFeature> getFeatures() {
        synchronized (features) {
            return new ArrayList<>(features.values());
        }
    }

    public @Nullable DeviceFeature getFeature(String name) {
        synchronized (features) {
            return features.get(name);
        }
    }

    public boolean hasFeatures() {
        return !getFeatures().isEmpty();
    }

    public boolean hasFeature(String name) {
        return getFeature(name) != null;
    }

    public double getLastMsgValueAsDouble(String name, double defaultValue) {
        DeviceFeature feature = getFeature(name);
        return feature == null ? defaultValue : feature.getLastMsgValueAsDouble(defaultValue);
    }

    public int getLastMsgValueAsInteger(String name, int defaultValue) {
        DeviceFeature feature = getFeature(name);
        return feature == null ? defaultValue : feature.getLastMsgValueAsInteger(defaultValue);
    }

    public @Nullable State getState(String name) {
        DeviceFeature feature = getFeature(name);
        return feature == null ? null : feature.getState();
    }

    public boolean getFlag(String key, boolean def) {
        synchronized (flags) {
            return flags.getOrDefault(key, def);
        }
    }

    public boolean isBatteryPowered() {
        return getFlag("batteryPowered", false);
    }

    public boolean isDeviceSyncEnabled() {
        return getFlag("deviceSyncEnabled", false);
    }

    public boolean isModem() {
        return getFlag("modem", false);
    }

    public boolean hasModemDBEntry() {
        return getFlag("modemDBEntry", false);
    }

    public @Nullable ProductData getProductData() {
        return productData;
    }

    public @Nullable DeviceType getType() {
        ProductData productData = getProductData();
        return productData == null ? null : productData.getDeviceType();
    }

    public InsteonEngine getInsteonEngine() {
        return engine;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public LinkDB getLinkDB() {
        return linkDB;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public boolean isNotResponding() {
        return failedMsgCount >= FAILED_MSG_COUNT_THRESHOLD;
    }

    public @Nullable DeviceFeature getFeatureQueried() {
        synchronized (requestQueue) {
            return featureQueried;
        }
    }

    public List<Msg> getStoredMessages() {
        return storedMessages;
    }

    // --------------------- simple setters -----------------------------

    public void setAddress(InsteonAddress address) {
        this.address = address;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setHandler(InsteonDeviceHandler handler) {
        this.handler = handler;
    }

    public void setFlag(String key, boolean value) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting {} flag for {} to {}", key, address, value);
        }
        synchronized (flags) {
            flags.put(key, value);
        }
    }

    public void setFlags(Map<String, Boolean> flags) {
        flags.forEach((key, value) -> setFlag(key, value));
    }

    public void setHasModemDBEntry(boolean value) {
        setFlag("modemDBEntry", value);
    }

    public void setIsDeviceSyncEnabled(boolean value) {
        setFlag("deviceSyncEnabled", value);
    }

    public void setIsModem(boolean value) {
        setFlag("modem", value);
    }

    public void setProductData(ProductData productData) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting product data for {} to {}", address, productData);
        }
        this.productData = productData;
    }

    public void setType(DeviceType deviceType) {
        ProductData productData = getProductData();
        if (productData != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("setting device type for {} to {}", address, deviceType.getName());
            }
            productData.setDeviceType(deviceType);
        }
    }

    public void setInsteonEngine(InsteonEngine engine) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting insteon engine for {} to {}", address, engine);
        }
        this.engine = engine;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public void setPollInterval(long pollInterval) {
        if (pollInterval > 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("setting poll interval for {} to {}", address, pollInterval);
            }
            this.pollInterval = pollInterval;
        }
    }

    public void setFeatureQueried(@Nullable DeviceFeature featureQueried) {
        synchronized (requestQueue) {
            this.featureQueried = featureQueried;
        }
    }

    /**
     * Returns if this device is awake
     *
     * @return true if device not battery powered or within awake time
     */
    public boolean isAwake() {
        if (isBatteryPowered()) {
            // define awake time based on the stay awake feature state (ON => 4 minutes; OFF => 3 seconds)
            State state = getState(FEATURE_STAY_AWAKE);
            int awakeTime = OnOffType.ON.equals(state) ? 240000 : 3000; // in msec
            if (System.currentTimeMillis() - lastMsgReceived > awakeTime) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns if a received message is a failed command (NACK_OF_DIRECT)
     *
     * @param msg received message to check
     * @return true if message is NACK_OF_DIRECT
     */
    public boolean isFailedCommandMsg(Msg msg) {
        if (msg.isNackOfDirect()) {
            if (logger.isDebugEnabled()) {
                try {
                    int cmd2 = msg.getInt("command2");
                    if (cmd2 == 0xFF) {
                        logger.debug("got a sender device id not in responder database failed command msg: {}", msg);
                    } else if (cmd2 == 0xFE) {
                        logger.debug("got a no load detected failed command msg: {}", msg);
                    } else if (cmd2 == 0xFD) {
                        logger.debug("got an incorrect checksum failed command msg: {}", msg);
                    } else if (cmd2 == 0xFC) {
                        logger.debug("got a database search timeout failed command msg: {}", msg);
                    } else if (cmd2 == 0xFB) {
                        logger.debug("got an illegal value failed command msg: {}", msg);
                    } else {
                        logger.debug("got an unknown failed command msg: {}", msg);
                    }
                } catch (FieldException e) {
                    logger.warn("error parsing msg {}: ", msg, e);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns if a received message is a failure report (0x5C)
     *
     * @param msg received message to check
     * @return true if message cmd is 0x5C
     */
    public boolean isFailureReportMsg(Msg msg) {
        boolean oldState = isNotResponding();
        boolean failure = false;
        try {
            if (msg.getByte("Cmd") == 0x5C) {
                if (logger.isDebugEnabled()) {
                    logger.debug("got a failure report msg: {}", msg);
                }
                failedMsgCount++;
                failure = true;
            } else {
                failedMsgCount = 0;
            }
        } catch (FieldException e) {
            logger.warn("error parsing msg {}: ", msg, e);
        }
        // update thing status if responding state changed
        if (oldState != isNotResponding()) {
            updateStatus();
        }
        return failure;
    }

    /**
     * Returns if device is pollable
     *
     * @return true if has features and not battery powered
     */
    public boolean isPollable() {
        return hasFeatures() && !isBatteryPowered();
    }

    /**
     * Starts polling this device
     */
    public void startPolling() {
        Driver driver = getDriver();
        // start polling if currently disabled
        if (driver != null && status != DeviceStatus.POLLING) {
            int ndbes = driver.getModemDB().getEntries().size();
            driver.getPoller().startPolling(this, ndbes);
            setStatus(DeviceStatus.POLLING);
        }
    }

    /**
     * Stops polling this device
     */
    public void stopPolling() {
        Driver driver = getDriver();
        // stop polling if currently enabled
        if (driver != null && status == DeviceStatus.POLLING) {
            driver.getPoller().stopPolling(this);
            clearRequestQueues();
            setStatus(DeviceStatus.INITIALIZED);
        }
    }

    /**
     * Polls this device
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void doPoll(long delay) {
        DeviceFeature engineFeature = getFeature(FEATURE_INSTEON_ENGINE);
        if (engineFeature != null) {
            // poll insteon engine if unknown or its feature never queried
            if (engine == InsteonEngine.UNKNOWN || engineFeature.getQueryStatus() == QueryStatus.NEVER_QUERIED) {
                engineFeature.doPoll(delay);
                return; // insteon engine needs to be known before enqueueing more messages
            }
            // build this device link db if not complete or should be refreshed
            if (!linkDB.isComplete() || linkDB.shouldRefresh()) {
                Driver driver = getDriver();
                if (driver != null) {
                    driver.buildLinkDB(this, delay);
                }
                return; // link db needs to be complete before enqueueing more messages
            }
        }
        // process deferred queue if not empty
        if (!deferredQueue.isEmpty()) {
            processDeferredQueue(delay);
        }
        // schedule polling of all features
        schedulePoll(delay, feature -> true);
    }

    /**
     * Polls a specific feature for this device
     *
     * @param name name of the feature to poll
     * @param delay scheduling delay (in milliseconds)
     */
    public void doPollFeature(String name, long delay) {
        DeviceFeature feature = getFeature(name);
        if (feature != null) {
            feature.doPoll(delay);
        }
    }

    /**
     * Polls all responder features for this device
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void doPollResponders(long delay) {
        schedulePoll(delay, feature -> feature.hasResponderFeatures());
    }

    /**
     * Schedules polling for this device
     *
     * @param delay scheduling delay (in milliseconds)
     * @param featureFilter feature filter to apply
     */
    private void schedulePoll(long delay, Predicate<DeviceFeature> featureFilter) {
        long spacing = 0;
        for (DeviceFeature feature : getFeatures()) {
            // skip if is event feature or feature filter doesn't match
            if (feature.isEventFeature() || !featureFilter.test(feature)) {
                continue;
            }
            // poll feature with channel handlers or never queried before
            if (feature.hasChannelHandlers() || feature.getQueryStatus() == QueryStatus.NEVER_QUERIED) {
                Msg msg = feature.doPoll(delay + spacing);
                if (msg != null) {
                    spacing += msg.getQuietTime();
                }
            }
        }
        // ping device if no feature polling scheduled to confirm device is responding
        if (spacing == 0) {
            doPollFeature(FEATURE_PING, delay);
        }
    }

    /**
     * Adjusts related features
     *
     * @param controllerAddress the controller address
     * @param controllerGroup the controller group
     * @param controllerConfig the controller channel config
     * @param cmd the cmd to adjust to
     */
    public void adjustRelatedFeatures(InsteonAddress controllerAddress, int controllerGroup,
            InsteonChannelConfiguration controllerConfig, Command cmd) {
        if (!linkDB.isComplete()) {
            return;
        }
        int onLevel = controllerConfig.getOnLevel();
        RampRate rampRate = controllerConfig.getRampRate();
        for (LinkDBRecord record : linkDB.getResponderRecords(controllerAddress, controllerGroup)) {
            for (DeviceFeature feature : getFeatures()) {
                // handle command for responder feature with group matching record component id (data 3)
                if (feature.isResponderFeature() && feature.getGroup() == record.getComponentId()) {
                    InsteonChannelConfiguration config = new InsteonChannelConfiguration();
                    config.setOnLevel(onLevel == -1 ? record.getOnLevel() : onLevel);
                    config.setRampRate(rampRate == null ? record.getRampRate() : rampRate);
                    feature.handleCommand(config, cmd);
                    break;
                }
            }
        }
    }

    /**
     * Polls related features
     *
     * @param controllerAddress the controller address
     * @param controllerGroup the controller group
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedFeatures(InsteonAddress controllerAddress, int controllerGroup, long delay) {
        if (!linkDB.isComplete()) {
            doPollResponders(delay);
            return;
        }
        for (LinkDBRecord record : linkDB.getResponderRecords(controllerAddress, controllerGroup)) {
            for (DeviceFeature feature : getFeatures()) {
                // poll responder feature with group matching record component id (data 3)
                if (feature.isResponderFeature() && feature.getGroup() == record.getComponentId()) {
                    feature.triggerPoll(delay);
                    break;
                }
            }
        }
    }

    /**
     * Updates feature channel configs for this device
     */
    public void updateChannelConfigs() {
        getFeatures().forEach(DeviceFeature::updateChannelConfigs);
    }

    /**
     * Handles incoming message for this device by forwarding
     * it to all features that this device supports
     *
     * @param msg the incoming message
     */
    public void handleMessage(Msg msg) {
        lastMsgReceived = System.currentTimeMillis();
        // determine if message is a failed command (NACK_OF_DIRECT)
        boolean isFailedCommandMsg = isFailedCommandMsg(msg);
        // determine if message is a failure report (0x5C)
        boolean isFailureReportMsg = isFailureReportMsg(msg);
        // store message if no feature define when not failed command and not failure report
        if (!hasFeatures()) {
            if (!isFailedCommandMsg && !isFailureReportMsg) {
                if (logger.isDebugEnabled()) {
                    logger.debug("storing message for unknown device {}", address);
                }
                synchronized (storedMessages) {
                    storedMessages.add(msg);
                }
            }
            return;
        }
        // update all non-status features
        for (DeviceFeature feature : getFeatures()) {
            if (!feature.isStatusFeature()) {
                if (!isFailureReportMsg) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("----- applying message to feature: {}", feature.getName());
                    }
                    if (feature.handleMessage(msg)) {
                        // handled a reply to a query
                        if (logger.isTraceEnabled()) {
                            logger.trace("handled reply of direct: {}", feature.getName());
                        }
                        // mark query as answered and processed
                        feature.setQueryStatus(QueryStatus.QUERY_ANSWERED);
                        feature.setLastQueryMessage(null);
                        setFeatureQueried(null);
                        break;
                    }
                } else {
                    if (feature.isMyDirectAck(msg)) {
                        // reset query as never queried when receiving a failure report reply to it
                        feature.setQueryStatus(QueryStatus.NEVER_QUERIED);
                        feature.setLastQueryMessage(null);
                        setFeatureQueried(null);
                        break;
                    }
                }
            }
        }
        // update all status features (e.g. device last update time)
        for (DeviceFeature feature : getFeatures()) {
            if (feature.isStatusFeature()) {
                if (!isFailureReportMsg) {
                    feature.handleMessage(msg);
                }
            }
        }
    }

    /**
     * Replays a list of messages
     */
    public void replayMessages(List<Msg> messages) {
        for (Msg msg : messages) {
            if (logger.isTraceEnabled()) {
                logger.trace("replaying msg: {}", msg);
            }
            msg.setIsReplayed(true);
            handleMessage(msg);
        }
    }

    /**
     * Processes deferred request entries
     *
     * @param delay time (in milliseconds) to delay before sending message
     */
    public void processDeferredQueue(long delay) {
        synchronized (deferredQueue) {
            while (!deferredQueue.isEmpty()) {
                RequestQueueEntry request = deferredQueue.poll();
                if (request != null) {
                    deferredQueueHash.remove(request.getMessage());
                    request.setExpirationTime(delay);
                    if (logger.isTraceEnabled()) {
                        logger.trace("enqueuing deferred request: {}", request.getFeature().getName());
                    }
                    addRequestQueueEntry(request, delay);
                }
            }
        }
    }

    /**
     * Processes request queue
     *
     * @param now the current time
     * @return time when to schedule the next message (now + quietTime)
     */
    public long processRequestQueue(long now) {
        synchronized (requestQueue) {
            if (requestQueue.isEmpty()) {
                return 0L;
            }
            // check if a feature queried is in progress
            DeviceFeature featureQueried = this.featureQueried;
            if (featureQueried != null) {
                switch (featureQueried.getQueryStatus()) {
                    case QUERY_QUEUED:
                        // wait for feature queried request to be sent
                        if (logger.isDebugEnabled()) {
                            logger.debug("still waiting for {} query to be sent to {}", featureQueried.getName(),
                                    address);
                        }
                        return now + 1000L; // retry in 1000 ms
                    case QUERY_PENDING:
                        // wait for the feature queried response to be processed
                        long expTime = lastQuerySent + DIRECT_ACK_TIMEOUT;
                        if (expTime > now) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("still waiting for {} query reply from {} for another {} msec",
                                        featureQueried.getName(), address, expTime - now);
                            }
                            return now + 500L; // retry in 500 ms
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("gave up waiting for {} query reply from {}", featureQueried.getName(),
                                    address);
                        }
                        // reset feature queried as never queried
                        featureQueried.setQueryStatus(QueryStatus.NEVER_QUERIED);
                        break;
                    case QUERY_ANSWERED:
                        // do nothing, just handle race condition since feature queried was already answered
                        break;
                    default:
                        if (logger.isDebugEnabled()) {
                            logger.debug("unexpected feature {} query status {} for {}", featureQueried.getName(),
                                    featureQueried.getQueryStatus(), address);
                        }
                }
                // reset feature queried
                this.featureQueried = null;
            }
            // take the next request off the queue
            RequestQueueEntry request = requestQueue.poll();
            if (request == null) {
                return 0L;
            }
            // get requested feature and message
            DeviceFeature feature = request.getFeature();
            Msg msg = request.getMessage();
            // remove request from queue hash
            requestQueueHash.remove(msg);
            // set feature queried for non-broadcast request message
            if (!msg.isBroadcast()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("request taken off direct: {} {}", feature.getName(), msg);
                }
                // mark requested feature query status as queued
                feature.setQueryStatus(QueryStatus.QUERY_QUEUED);
                // store requested feature query message
                feature.setLastQueryMessage(msg);
                // set feature queried
                this.featureQueried = feature;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("request taken off bcast: {} {}", feature.getName(), msg);
                }
            }
            try {
                writeMessage(msg);
            } catch (IOException e) {
                logger.warn("message write failed for msg {}", msg, e);
            }
            // figure out when the request queue should be checked next
            RequestQueueEntry nextRequest = requestQueue.peek();
            long quietTime = msg.getQuietTime();
            long nextExpTime = nextRequest == null ? 0L : nextRequest.getExpirationTime();
            long nextTime = Math.max(now + quietTime, nextExpTime);
            if (logger.isDebugEnabled()) {
                logger.debug("next request queue processed in {} msec, quiettime = {}", nextTime - now, quietTime);
            }
            return nextTime;
        }
    }

    /**
     * Enqueues request to be sent at the next possible time
     *
     * @param msg request message to be sent
     * @param feature device feature that sent this message
     */
    public void enqueueRequest(Msg msg, DeviceFeature feature) {
        enqueueDelayedRequest(msg, feature, 0L);
    }

    /**
     * Enqueues request to be sent after a delay
     *
     * @param msg request message to be sent
     * @param feature device feature that sent this message
     * @param delay time (in milliseconds) to delay before sending request
     */
    public void enqueueDelayedRequest(Msg msg, DeviceFeature feature, long delay) {
        RequestQueueEntry request = new RequestQueueEntry(feature, msg, delay);
        feature.setQueryStatus(QueryStatus.QUERY_CREATED);
        if (!isAwake()) {
            addDeferredQueueEntry(request);
        } else {
            addRequestQueueEntry(request, delay);
        }
    }

    /**
     * Adds deferred queue entry
     *
     * @param request request queue entry to add
     */
    private void addDeferredQueueEntry(RequestQueueEntry request) {
        if (logger.isTraceEnabled()) {
            logger.trace("deferring request for sleeping device {}", address);
        }
        synchronized (deferredQueue) {
            Msg msg = request.getMessage();
            RequestQueueEntry prevRequest = deferredQueueHash.get(msg);
            if (prevRequest != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("overwriting existing deferred request: {} {}", request.getFeature().getName(), msg);
                }
                deferredQueue.remove(prevRequest);
                deferredQueueHash.remove(msg);
            }
            deferredQueue.add(request);
            deferredQueueHash.put(msg, request);
        }
    }

    /**
     * Adds request queue entry
     *
     * @param request request queue entry to add
     * @param delay time (in milliseconds) to delay before sending request
     */
    private void addRequestQueueEntry(RequestQueueEntry request, long delay) {
        if (logger.isTraceEnabled()) {
            logger.trace("enqueuing request with delay {} msec", delay);
        }
        synchronized (requestQueue) {
            Msg msg = request.getMessage();
            RequestQueueEntry prevRequest = requestQueueHash.get(msg);
            if (prevRequest != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("overwriting existing request: {} {}", request.getFeature().getName(), msg);
                }
                requestQueue.remove(prevRequest);
                requestQueueHash.remove(msg);
            }
            requestQueue.add(request);
            requestQueueHash.put(msg, request);
        }
        Driver driver = getDriver();
        if (driver != null) {
            driver.getRequestQueueManager().addQueue(this, delay);
        }
    }

    /**
     * Clears request queues
     */
    private void clearRequestQueues() {
        if (logger.isTraceEnabled()) {
            logger.trace("clearing request queues for {}", address);
        }
        synchronized (deferredQueue) {
            deferredQueue.clear();
            deferredQueueHash.clear();
        }
        synchronized (requestQueue) {
            requestQueue.clear();
            requestQueueHash.clear();
        }
    }

    /**
     * Sends a write message request to driver
     *
     * @param msg message to be written
     * @throws IOException
     */
    private void writeMessage(Msg msg) throws IOException {
        Driver driver = getDriver();
        if (driver != null) {
            driver.writeMessage(msg);
        }
    }

    /**
     * Instantiates features based on a device type
     *
     * @param deviceType device type to instantiate features from
     */
    private void instantiateFeatures(DeviceType deviceType) {
        for (FeatureEntry featureEntry : deviceType.getFeatures()) {
            DeviceFeature feature = DeviceFeature.makeDeviceFeature(featureEntry.getType());
            if (feature == null) {
                logger.warn("device type {} references unknown feature type {}", deviceType, featureEntry.getType());
            } else {
                addFeature(feature, featureEntry.getName(), featureEntry.getParameters());
            }
        }
        for (FeatureEntry featureEntry : deviceType.getFeatureGroups()) {
            DeviceFeature feature = getFeature(featureEntry.getName());
            if (feature == null) {
                logger.warn("device type {} references unknown feature group {}", deviceType, featureEntry.getName());
            } else {
                connectFeatures(feature, featureEntry.getConnectedFeatures());
            }
        }
    }

    /**
     * Connects group features to its parent
     *
     * @param groupFeature group feature to connect to
     * @param features connected features part of that group feature
     */
    private void connectFeatures(DeviceFeature groupFeature, List<String> features) {
        for (String name : features) {
            DeviceFeature feature = getFeature(name);
            if (feature == null) {
                logger.warn("feature group {} references unknown feature {}", groupFeature.getName(), name);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("{} connected feature: {}", groupFeature.getName(), feature.getName());
                }
                feature.addParameters(groupFeature.getParameters());
                feature.setGroupFeature(groupFeature);
                feature.setPollHandler(null);
                groupFeature.addConnectedFeature(feature);
            }
        }
    }

    /**
     * Adds feature to this device
     *
     * @param feature feature object to add
     * @param name feature name
     * @param parameters feature parameters
     */
    private void addFeature(DeviceFeature feature, String name, Map<String, String> parameters) {
        feature.setDevice(this);
        feature.setName(name);
        feature.addParameters(parameters);
        feature.initializeQueryStatus();
        synchronized (features) {
            features.put(name, feature);
        }
    }

    /**
     * Updates this device product data
     *
     * @param newData the new product data to use
     */
    public void updateProductData(ProductData newData) {
        ProductData productData = getProductData();
        if (productData == null) {
            // set product data
            setProductData(newData);
            // reset device
            reset();
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("updating product data for {} to {}", address, newData);
            }
            // update product data
            productData.update(newData);
            // update thing properties
            updateProperties();
        }
    }

    /**
     * Updates this device type
     *
     * @param newType the device type to update to
     */

    public void updateType(@Nullable DeviceType newType) {
        DeviceType currentType = getType();
        if (currentType != null && newType != null && !currentType.getName().equals(newType.getName())) {
            if (logger.isTraceEnabled()) {
                logger.trace("updating device type from {} to {} for {}", currentType.getName(), newType.getName(),
                        address);
            }
            // set device type
            setType(newType);
            // reset device
            reset();
        }
    }

    /**
     * Get the state of the state machine that suppresses duplicates for broadcast messages.
     *
     * @param cmd1 the cmd1 from the broadcast message received
     * @return true if the broadcast message is NOT a duplicate
     */
    public boolean getBroadcastState(byte cmd1) {
        synchronized (lastBroadcastReceived) {
            long timeLapse = lastMsgReceived - lastBroadcastReceived.getOrDefault(cmd1, lastMsgReceived);
            if (timeLapse > 0 && timeLapse < BCAST_STATE_TIMEOUT) {
                return false;
            } else {
                lastBroadcastReceived.put(cmd1, lastMsgReceived);
                return true;
            }
        }
    }

    /**
     * Returns the state of the state machine that suppresses duplicates for group messages.
     * The state machine is advance the first time it is called for a message,
     * otherwise return the current state.
     *
     * @param group the insteon group of the broadcast message
     * @param type the group message type that was received
     * @param cmd1 cmd1 from the message received
     * @return true if the group message is NOT a duplicate
     */
    public boolean getGroupState(int group, GroupMessageType type, byte cmd1) {
        synchronized (groupState) {
            GroupMessageStateMachine stateMachine = groupState.get(group);
            if (stateMachine == null) {
                stateMachine = new GroupMessageStateMachine();
                groupState.put(group, stateMachine);
                if (logger.isTraceEnabled()) {
                    logger.trace("{} created group {} state", address, group);
                }
            } else {
                if (lastMsgReceived <= stateMachine.getLastUpdated()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("{} using previous group {} state for {}", address, group, type);
                    }
                    return stateMachine.getPublish();
                }
            }

            if (logger.isTraceEnabled()) {
                logger.trace("{} updating group {} state to {}", address, group, type);
            }
            return stateMachine.action(type, address, group, cmd1);
        }
    }

    /**
     * Initializes this device
     */
    public void initialize() {
        Driver driver = getDriver();
        if (driver == null || !driver.isModemDBComplete() || address.isX10()) {
            return;
        }

        ModemDBEntry dbe = driver.getModemDB().getEntry(address);
        if (dbe != null) {
            ProductData productData = dbe.getProductData();
            if (productData != null) {
                updateProductData(productData);
            }
            if (!hasModemDBEntry()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("device {} found in the modem database.", address);
                }
                setHasModemDBEntry(true);
            }
            if (isPollable()) {
                startPolling();
            }
        } else {
            logger.warn("device {} not found in the modem database. Did you forget to link?", address);
            setHasModemDBEntry(false);
            stopPolling();
        }
    }

    /**
     * Refreshes this device
     */
    public void refresh() {
        initialize();

        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.refresh();
        }
    }

    /**
     * Resets this device
     */
    private void reset() {
        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.reset(this);
        }
    }

    /**
     * Resets this device heartbeat monitor
     */
    public void resetHeartbeatMonitor() {
        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.resetHeartbeatMonitor();
        }
    }

    /**
     * Updates this device thing properties
     */
    public void updateProperties() {
        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.updateProperties(this);
        }
    }

    /**
     * Updates this device thing status
     */
    private void updateStatus() {
        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.updateStatus();
        }
    }

    /**
     * Notifies that the link db has been updated for this device
     */
    public void linkDBUpdated() {
        if (linkDB.isComplete()) {
            // poll database delta feature
            doPollFeature(FEATURE_DATABASE_DELTA, 0L);
            // poll remaining features for this device
            doPoll(0L);
        }
        // update channel configs
        updateChannelConfigs();
        // update thing status
        updateStatus();
    }

    /**
     * Notifies that a message request was sent to this device
     *
     * @param time the time the request was sent
     */
    public void requestSent(long time) {
        synchronized (requestQueue) {
            DeviceFeature featureQueried = this.featureQueried;
            if (featureQueried != null) {
                // set last query sent time
                lastQuerySent = time;
                // mark feature queried as pending
                featureQueried.setQueryStatus(QueryStatus.QUERY_PENDING);
            }
        }
    }

    @Override
    public String toString() {
        String s = address.toString();
        if (productData != null) {
            s += "|" + productData;
        } else {
            s += "|unknown device";
        }
        for (DeviceFeature feature : getFeatures()) {
            s += "|" + feature;
        }
        return s;
    }

    /**
     * Factory method for creating a InsteonDevice from a device address, driver and product data
     *
     * @param driver the device driver
     * @param address the device address
     * @param productData the device product data
     * @return the newly created InsteonDevice
     */
    public static InsteonDevice makeDevice(Driver driver, InsteonAddress address, @Nullable ProductData productData) {
        InsteonDevice device = new InsteonDevice();
        device.setAddress(address);
        device.setDriver(driver);

        if (productData != null) {
            DeviceType deviceType = productData.getDeviceType();
            if (deviceType != null) {
                device.instantiateFeatures(deviceType);
                device.setFlags(deviceType.getFlags());
            }
            int offset = productData.getFirstRecordOffset();
            if (offset != 0) {
                device.getLinkDB().setFirstRecordOffset(offset);
            }
            device.setProductData(productData);
        }

        return device;
    }

    /**
     * Factory method for creating a InsteonDevice from a device address, driver, product data and cache
     *
     * @param driver the device driver
     * @param address the device address
     * @param productData the device product data
     * @param cache the device cache
     * @return the newly created InsteonDevice
     */
    public static InsteonDevice makeDevice(Driver driver, InsteonAddress address, @Nullable ProductData productData,
            @Nullable DeviceCache cache) {
        InsteonDevice device = makeDevice(driver, address,
                productData == null && cache != null ? cache.getProductData() : productData);

        if (cache != null) {
            DeviceCache.load(cache, device);
        }

        return device;
    }
}
