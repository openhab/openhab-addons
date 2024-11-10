/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonLegacyChannelConfiguration;
import org.openhab.binding.insteon.internal.device.LegacyDeviceType.FeatureGroup;
import org.openhab.binding.insteon.internal.transport.LegacyDriver;
import org.openhab.binding.insteon.internal.transport.message.LegacyGroupMessageStateMachine;
import org.openhab.binding.insteon.internal.transport.message.LegacyGroupMessageStateMachine.GroupMessage;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.core.types.Command;
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
 */
@NonNullByDefault
public class LegacyDevice {
    private final Logger logger = LoggerFactory.getLogger(LegacyDevice.class);

    public enum DeviceStatus {
        INITIALIZED,
        POLLING
    }

    /** need to wait after query to avoid misinterpretation of duplicate replies */
    private static final int QUIET_TIME_DIRECT_MESSAGE = 2000;
    /** how far to space out poll messages */
    private static final int TIME_BETWEEN_POLL_MESSAGES = 1500;

    private DeviceAddress address = InsteonAddress.UNKNOWN;
    private long pollInterval = -1L; // in milliseconds
    private @Nullable LegacyDriver driver = null;
    private Map<String, LegacyDeviceFeature> features = new HashMap<>();
    private @Nullable String productKey = null;
    private volatile long lastTimePolled = 0L;
    private volatile long lastMsgReceived = 0L;
    private boolean isModem = false;
    private PriorityQueue<@Nullable QEntry> mrequestQueue = new PriorityQueue<>();
    private @Nullable LegacyDeviceFeature featureQueried = null;
    private long lastQueryTime = 0L;
    private boolean hasModemDBEntry = false;
    private DeviceStatus status = DeviceStatus.INITIALIZED;
    private Map<Integer, LegacyGroupMessageStateMachine> groupState = new HashMap<>();
    private Map<String, Object> deviceConfigMap = new HashMap<>();

    /**
     * Constructor
     */
    public LegacyDevice() {
        lastMsgReceived = System.currentTimeMillis();
    }

    public boolean hasProductKey() {
        return productKey != null;
    }

    public @Nullable String getProductKey() {
        return productKey;
    }

    public boolean hasModemDBEntry() {
        return hasModemDBEntry;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public DeviceAddress getAddress() {
        return address;
    }

    public @Nullable LegacyDriver getDriver() {
        return driver;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public boolean isModem() {
        return isModem;
    }

    public @Nullable LegacyDeviceFeature getFeature(String name) {
        return features.get(name);
    }

    public Map<String, LegacyDeviceFeature> getFeatures() {
        return features;
    }

    public boolean hasProductKey(String key) {
        String productKey = this.productKey;
        return productKey != null && productKey.equals(key);
    }

    public boolean hasValidPollingInterval() {
        return pollInterval > 0;
    }

    public long getPollOverDueTime() {
        return lastTimePolled - lastMsgReceived;
    }

    public boolean hasAnyListeners() {
        synchronized (features) {
            for (LegacyDeviceFeature feature : features.values()) {
                if (feature.hasListeners()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public void setHasModemDBEntry(boolean hasModemDBEntry) {
        this.hasModemDBEntry = hasModemDBEntry;
    }

    public void setAddress(DeviceAddress address) {
        this.address = address;
    }

    public void setDriver(LegacyDriver driver) {
        this.driver = driver;
    }

    public void setIsModem(boolean isModem) {
        this.isModem = isModem;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public void setPollInterval(long pollInterval) {
        logger.trace("setting poll interval for {} to {} ", address, pollInterval);
        if (pollInterval > 0) {
            this.pollInterval = pollInterval;
        }
    }

    public void setFeatureQueried(@Nullable LegacyDeviceFeature featureQueried) {
        synchronized (mrequestQueue) {
            this.featureQueried = featureQueried;
        }
    }

    public void setDeviceConfigMap(Map<String, Object> deviceConfigMap) {
        this.deviceConfigMap = deviceConfigMap;
    }

    public Map<String, Object> getDeviceConfigMap() {
        return deviceConfigMap;
    }

    public @Nullable LegacyDeviceFeature getFeatureQueried() {
        synchronized (mrequestQueue) {
            return featureQueried;
        }
    }

    /**
     * Removes feature listener from this device
     *
     * @param itemName name of the feature listener to remove
     * @return true if a feature listener was successfully removed
     */
    public boolean removeFeatureListener(String itemName) {
        boolean removedListener = false;
        synchronized (features) {
            for (Iterator<Entry<String, LegacyDeviceFeature>> it = features.entrySet().iterator(); it.hasNext();) {
                LegacyDeviceFeature feature = it.next().getValue();
                if (feature.removeListener(itemName)) {
                    removedListener = true;
                }
            }
        }
        return removedListener;
    }

    /**
     * Invoked to process an openHAB command
     *
     * @param driver The driver to use
     * @param config The item configuration
     * @param command The actual command to execute
     */
    public void processCommand(LegacyDriver driver, InsteonLegacyChannelConfiguration config, Command command) {
        logger.debug("processing command {} features: {}", command, features.size());
        synchronized (features) {
            for (LegacyDeviceFeature feature : features.values()) {
                if (feature.isReferencedByItem(config.getChannelName())) {
                    feature.handleCommand(config, command);
                }
            }
        }
    }

    /**
     * Execute poll on this device: create an array of messages,
     * add them to the request queue, and schedule the queue
     * for processing.
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void doPoll(long delay) {
        long now = System.currentTimeMillis();
        List<QEntry> list = new ArrayList<>();
        synchronized (features) {
            int spacing = 0;
            for (LegacyDeviceFeature feature : features.values()) {
                if (feature.hasListeners()) {
                    Msg msg = feature.makePollMsg();
                    if (msg != null) {
                        list.add(new QEntry(feature, msg, now + delay + spacing));
                        spacing += TIME_BETWEEN_POLL_MESSAGES;
                    }
                }
            }
        }
        if (list.isEmpty()) {
            return;
        }
        synchronized (mrequestQueue) {
            for (QEntry qe : list) {
                mrequestQueue.add(qe);
            }
        }
        LegacyRequestManager instance = LegacyRequestManager.instance();
        if (instance != null) {
            instance.addQueue(this, now + delay);
        } else {
            logger.warn("request queue manager is null");
        }

        if (!list.isEmpty()) {
            lastTimePolled = now;
        }
    }

    /**
     * Handle incoming message for this device by forwarding
     * it to all features that this device supports
     *
     * @param msg the incoming message
     */
    public void handleMessage(Msg msg) {
        lastMsgReceived = System.currentTimeMillis();
        synchronized (features) {
            // first update all features that are
            // not status features
            for (LegacyDeviceFeature feature : features.values()) {
                if (!feature.isStatusFeature()) {
                    logger.debug("----- applying message to feature: {}", feature.getName());
                    if (feature.handleMessage(msg)) {
                        // handled a reply to a query,
                        // mark it as processed
                        logger.trace("handled reply of direct: {}", feature);
                        setFeatureQueried(null);
                        break;
                    }
                }
            }
            // then update all the status features,
            // e.g. when the device was last updated
            for (LegacyDeviceFeature feature : features.values()) {
                if (feature.isStatusFeature()) {
                    feature.handleMessage(msg);
                }
            }
        }
    }

    /**
     * Called by the RequestQueueManager when the queue has expired
     *
     * @param timeNow
     * @return time when to schedule the next message (timeNow + quietTime)
     */
    public long processRequestQueue(long timeNow) {
        synchronized (mrequestQueue) {
            if (mrequestQueue.isEmpty()) {
                return 0L;
            }
            LegacyDeviceFeature featureQueried = this.featureQueried;
            if (featureQueried != null) {
                // A feature has been queried, but
                // the response has not been digested yet.
                // Must wait for the query to be processed.
                long delta = timeNow - (lastQueryTime + featureQueried.getDirectAckTimeout());
                if (delta < 0) {
                    logger.debug("still waiting for query reply from {} for another {} usec", address, -delta);
                    return timeNow + 2000L; // retry soon
                } else {
                    logger.debug("gave up waiting for query reply from device {}", address);
                }
            }
            QEntry qe = mrequestQueue.poll(); // take it off the queue!
            if (qe == null) {
                return 0L;
            }
            if (!qe.getMsg().isBroadcast()) {
                logger.debug("qe taken off direct: {} {}", qe.getFeature(), qe.getMsg());
                lastQueryTime = timeNow;
                // mark feature as pending
                qe.getFeature().setQueryStatus(LegacyDeviceFeature.QueryStatus.QUERY_PENDING);
                // also mark this queue as pending so there is no doubt
                this.featureQueried = qe.getFeature();
            } else {
                logger.debug("qe taken off bcast: {} {}", qe.getFeature(), qe.getMsg());
            }
            long quietTime = qe.getMsg().getQuietTime();
            qe.getMsg().setQuietTime(500L); // rate limiting downstream!
            try {
                writeMessage(qe.getMsg());
            } catch (IOException e) {
                logger.warn("message write failed for msg {}", qe.getMsg(), e);
            }
            // figure out when the request queue should be checked next
            QEntry qnext = mrequestQueue.peek();
            long nextExpTime = (qnext == null ? 0L : qnext.getExpirationTime());
            long nextTime = Math.max(timeNow + quietTime, nextExpTime);
            logger.debug("next request queue processed in {} msec, quiettime = {}", nextTime - timeNow, quietTime);
            return nextTime;
        }
    }

    /**
     * Enqueues message to be sent at the next possible time
     *
     * @param msg message to be sent
     * @param feature device feature that sent this message (so we can associate the response message with it)
     */
    public void enqueueMessage(Msg msg, LegacyDeviceFeature feature) {
        enqueueDelayedMessage(msg, feature, 0);
    }

    /**
     * Enqueues message to be sent after a delay
     *
     * @param msg message to be sent
     * @param feature device feature that sent this message (so we can associate the response message with it)
     * @param delay time (in milliseconds) to delay before enqueuing message
     */
    public void enqueueDelayedMessage(Msg msg, LegacyDeviceFeature feature, long delay) {
        long now = System.currentTimeMillis();
        synchronized (mrequestQueue) {
            mrequestQueue.add(new QEntry(feature, msg, now + delay));
        }
        if (!msg.isBroadcast()) {
            msg.setQuietTime(QUIET_TIME_DIRECT_MESSAGE);
        }
        logger.trace("enqueing direct message with delay {}", delay);
        LegacyRequestManager instance = LegacyRequestManager.instance();
        if (instance != null) {
            instance.addQueue(this, now + delay);
        } else {
            logger.warn("request queue manger instance is null");
        }
    }

    private void writeMessage(Msg msg) throws IOException {
        LegacyDriver driver = this.driver;
        if (driver != null) {
            driver.writeMessage(msg);
        }
    }

    private void instantiateFeatures(LegacyDeviceType deviceType) {
        for (Entry<String, String> entry : deviceType.getFeatures().entrySet()) {
            LegacyDeviceFeature feature = LegacyDeviceFeature.makeDeviceFeature(entry.getValue());
            if (feature == null) {
                logger.warn("device type {} references unknown feature: {}", deviceType, entry.getValue());
            } else {
                addFeature(entry.getKey(), feature);
            }
        }
        for (Entry<String, FeatureGroup> entry : deviceType.getFeatureGroups().entrySet()) {
            FeatureGroup featureGroup = entry.getValue();
            @Nullable
            LegacyDeviceFeature feature = LegacyDeviceFeature.makeDeviceFeature(featureGroup.getType());
            if (feature == null) {
                logger.warn("device type {} references unknown feature group: {}", deviceType, featureGroup.getType());
            } else {
                addFeature(entry.getKey(), feature);
                connectFeatures(entry.getKey(), feature, featureGroup.getFeatures());
            }
        }
    }

    private void connectFeatures(String name, LegacyDeviceFeature groupFeature, ArrayList<String> groupFeatures) {
        for (String featureName : groupFeatures) {
            @Nullable
            LegacyDeviceFeature feature = features.get(featureName);
            if (feature == null) {
                logger.warn("feature group {} references unknown feature {}", name, featureName);
            } else {
                logger.debug("{} connected feature: {}", name, feature);
                groupFeature.addConnectedFeature(feature);
            }
        }
    }

    private void addFeature(String name, LegacyDeviceFeature feature) {
        feature.setDevice(this);
        synchronized (features) {
            features.put(name, feature);
        }
    }

    /**
     * Get the state of the state machine that suppresses duplicates for group messages.
     * The state machine is advance the first time it is called for a message,
     * otherwise return the current state.
     *
     * @param group the insteon group of the broadcast message
     * @param groupMessage the type of group message came in (action etc)
     * @param cmd1 cmd1 from the message received
     * @return true if this is message is NOT a duplicate
     */
    public boolean getGroupState(int group, GroupMessage groupMessage, byte cmd1) {
        LegacyGroupMessageStateMachine stateMachine = groupState.get(group);
        if (stateMachine == null) {
            stateMachine = new LegacyGroupMessageStateMachine();
            groupState.put(group, stateMachine);
            logger.trace("{} created group {} state", address, group);
        } else {
            if (lastMsgReceived <= stateMachine.getLastUpdated()) {
                logger.trace("{} using previous group {} state for {}", address, group, groupMessage);
                return stateMachine.getPublish();
            }
        }

        logger.trace("{} updating group {} state to {}", address, group, groupMessage);
        return stateMachine.action(groupMessage, address, group, cmd1);
    }

    @Override
    public String toString() {
        String s = address.toString();
        for (Entry<String, LegacyDeviceFeature> entry : features.entrySet()) {
            s += "|" + entry.getKey() + "->" + entry.getValue().toString();
        }
        return s;
    }

    /**
     * Factory method
     *
     * @param deviceType device type after which to model the device
     * @return newly created device
     */
    public static LegacyDevice makeDevice(LegacyDeviceType deviceType) {
        LegacyDevice device = new LegacyDevice();
        device.instantiateFeatures(deviceType);
        return device;
    }

    /**
     * Queue entry helper class
     *
     * @author Bernd Pfrommer - Initial contribution
     */
    public static class QEntry implements Comparable<QEntry> {
        private LegacyDeviceFeature feature;
        private Msg msg;
        private long expirationTime;

        QEntry(LegacyDeviceFeature feature, Msg msg, long expirationTime) {
            this.feature = feature;
            this.msg = msg;
            this.expirationTime = expirationTime;
        }

        public LegacyDeviceFeature getFeature() {
            return feature;
        }

        public Msg getMsg() {
            return msg;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        @Override
        public int compareTo(QEntry qe) {
            return (int) (expirationTime - qe.expirationTime);
        }
    }
}
