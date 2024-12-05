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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceFeature.QueryStatus;
import org.openhab.binding.insteon.internal.device.DeviceType.FeatureEntry;
import org.openhab.binding.insteon.internal.handler.InsteonThingHandler;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseDevice} represents a base device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class BaseDevice<@NonNull T extends DeviceAddress, @NonNull S extends InsteonThingHandler>
        implements Device {
    private static final int DIRECT_ACK_TIMEOUT = 6000; // in milliseconds
    private static final int REQUEST_QUEUE_TIMEOUT = 30000; // in milliseconds

    protected static enum DeviceStatus {
        INITIALIZED,
        POLLING,
        STOPPED
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected T address;
    private @Nullable S handler;
    private @Nullable InsteonModem modem;
    private @Nullable ProductData productData;
    private DeviceStatus status = DeviceStatus.INITIALIZED;
    private Map<String, DeviceFeature> features = new LinkedHashMap<>();
    private Map<String, Boolean> flags = new HashMap<>();
    private Queue<DeviceRequest> requestQueue = new PriorityQueue<>();
    private Map<Msg, DeviceRequest> requestQueueHash = new HashMap<>();
    private @Nullable DeviceFeature featureQueried;
    private long pollInterval = -1L; // in milliseconds
    private volatile long lastRequestQueued = 0L;
    private volatile long lastRequestSent = 0L;

    public BaseDevice(T address) {
        this.address = address;
    }

    @Override
    public T getAddress() {
        return address;
    }

    public @Nullable S getHandler() {
        return handler;
    }

    public @Nullable InsteonModem getModem() {
        return modem;
    }

    @Override
    public @Nullable ProductData getProductData() {
        return productData;
    }

    @Override
    public @Nullable DeviceType getType() {
        return Optional.ofNullable(productData).map(ProductData::getDeviceType).orElse(null);
    }

    protected DeviceStatus getStatus() {
        return status;
    }

    @Override
    public List<DeviceFeature> getFeatures() {
        synchronized (features) {
            return features.values().stream().toList();
        }
    }

    @Override
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
        return Optional.ofNullable(getFeature(name)).map(DeviceFeature::getLastMsgValue).map(Double::doubleValue)
                .orElse(defaultValue);
    }

    public int getLastMsgValueAsInteger(String name, int defaultValue) {
        return Optional.ofNullable(getFeature(name)).map(DeviceFeature::getLastMsgValue).map(Double::intValue)
                .orElse(defaultValue);
    }

    public @Nullable State getFeatureState(String name) {
        return Optional.ofNullable(getFeature(name)).map(DeviceFeature::getState).orElse(null);
    }

    public boolean getFlag(String key, boolean def) {
        synchronized (flags) {
            return flags.getOrDefault(key, def);
        }
    }

    public @Nullable DeviceFeature getFeatureQueried() {
        synchronized (requestQueue) {
            return featureQueried;
        }
    }

    public void setModem(@Nullable InsteonModem modem) {
        this.modem = modem;
    }

    public void setAddress(T address) {
        this.address = address;
    }

    public void setHandler(S handler) {
        this.handler = handler;
    }

    public void setProductData(ProductData productData) {
        logger.trace("setting product data for {} to {}", address, productData);
        this.productData = productData;
    }

    protected void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public void setFlag(String key, boolean value) {
        logger.trace("setting {} flag for {} to {}", key, address, value);
        synchronized (flags) {
            flags.put(key, value);
        }
    }

    public void setFlags(Map<String, Boolean> flags) {
        flags.forEach(this::setFlag);
    }

    public void setFeatureQueried(@Nullable DeviceFeature featureQueried) {
        synchronized (requestQueue) {
            this.featureQueried = featureQueried;
        }
    }

    public void setPollInterval(long pollInterval) {
        if (pollInterval > 0) {
            logger.trace("setting poll interval for {} to {}", address, pollInterval);
            this.pollInterval = pollInterval;
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InsteonDevice other = (InsteonDevice) obj;
        return address.equals(other.address);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + address.hashCode();
        return result;
    }

    /**
     * Returns if device is pollable
     *
     * @return true if has a pollable feature
     */
    public boolean isPollable() {
        return getFeatures().stream().anyMatch(DeviceFeature::isPollable);
    }

    /**
     * Starts polling this device
     */
    public void startPolling() {
        InsteonModem modem = getModem();
        // start polling if currently disabled
        if (modem != null && getStatus() != DeviceStatus.POLLING) {
            getFeatures().forEach(DeviceFeature::initializeQueryStatus);
            int ndbes = modem.getDB().getEntries().size();
            modem.getPollManager().startPolling(this, pollInterval, ndbes);
            setStatus(DeviceStatus.POLLING);
        }
    }

    /**
     * Stops polling this device
     */
    public void stopPolling() {
        InsteonModem modem = getModem();
        // stop polling if currently enabled
        if (modem != null && getStatus() == DeviceStatus.POLLING) {
            modem.getPollManager().stopPolling(this);
            clearRequestQueue();
            setStatus(DeviceStatus.STOPPED);
        }
    }

    /**
     * Polls this device
     *
     * @param delay scheduling delay (in milliseconds)
     */
    @Override
    public void doPoll(long delay) {
        schedulePoll(delay, feature -> true);
    }

    /**
     * Polls a specific feature for this device
     *
     * @param name name of the feature to poll
     * @param delay scheduling delay (in milliseconds)
     * @return poll message
     */
    public @Nullable Msg pollFeature(String name, long delay) {
        return Optional.ofNullable(getFeature(name)).map(feature -> feature.doPoll(delay)).orElse(null);
    }

    /**
     * Schedules polling for this device
     *
     * @param delay scheduling delay (in milliseconds)
     * @param featureFilter feature filter to apply
     * @return delay spacing
     */
    protected long schedulePoll(long delay, Predicate<DeviceFeature> featureFilter) {
        long spacing = 0;
        for (DeviceFeature feature : getFeatures()) {
            // skip if is event feature or feature filter doesn't match
            if (feature.isEventFeature() || !featureFilter.test(feature)) {
                continue;
            }
            // poll feature with listeners or never queried before
            if (feature.hasListeners() || feature.getQueryStatus() == QueryStatus.NEVER_QUERIED) {
                Msg msg = feature.doPoll(delay + spacing);
                if (msg != null) {
                    spacing += msg.getQuietTime();
                }
            }
        }
        return spacing;
    }

    /**
     * Clears request queue
     */
    protected void clearRequestQueue() {
        logger.trace("clearing request queue for {}", address);

        synchronized (requestQueue) {
            requestQueue.clear();
            requestQueueHash.clear();
        }
    }

    /**
     * Instantiates features for this device based on a device type
     *
     * @param deviceType device type to instantiate features from
     */
    protected void instantiateFeatures(DeviceType deviceType) {
        for (FeatureEntry featureEntry : deviceType.getFeatures()) {
            DeviceFeature feature = DeviceFeature.makeDeviceFeature(this, featureEntry.getName(),
                    featureEntry.getType(), featureEntry.getParameters());
            if (feature == null) {
                logger.warn("device type {} references unknown feature type {}", deviceType.getName(),
                        featureEntry.getType());
            } else {
                addFeature(feature);
            }
        }
        for (FeatureEntry featureEntry : deviceType.getFeatureGroups()) {
            DeviceFeature feature = getFeature(featureEntry.getName());
            if (feature == null) {
                logger.warn("device type {} references unknown feature group {}", deviceType.getName(),
                        featureEntry.getName());
            } else {
                connectFeatures(feature, featureEntry.getConnectedFeatures());
            }
        }
    }

    /**
     * Adds feature to this device
     *
     * @param feature device feature to add
     */
    private void addFeature(DeviceFeature feature) {
        synchronized (features) {
            features.put(feature.getName(), feature);
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
                logger.warn("group feature {} references unknown feature {}", groupFeature.getName(), name);
            } else {
                logger.trace("{} connected feature: {}", groupFeature.getName(), feature.getName());
                feature.addParameters(groupFeature.getParameters());
                feature.setGroupFeature(groupFeature);
                feature.setPollHandler(null);
                groupFeature.addConnectedFeature(feature);
            }
        }
    }

    /**
     * Resets features query status for this device
     */
    public void resetFeaturesQueryStatus() {
        if (getStatus() == DeviceStatus.POLLING) {
            logger.trace("resetting device features query status for {}", address);

            DeviceFeature featureQueried = getFeatureQueried();
            getFeatures().stream().filter(feature -> !feature.equals(featureQueried))
                    .forEach(DeviceFeature::initializeQueryStatus);
        }
    }

    /**
     * Handles incoming message for this device by forwarding
     * it to all features that this device supports
     *
     * @param msg the incoming message
     */
    @Override
    public void handleMessage(Msg msg) {
        getFeatures().stream().filter(feature -> feature.handleMessage(msg)).findFirst().ifPresent(feature -> {
            logger.trace("handled reply of direct for {}", feature.getName());
            // mark feature queried as processed and answered
            setFeatureQueried(null);
            feature.setQueryMessage(null);
            feature.setQueryStatus(QueryStatus.QUERY_ANSWERED);
        });
    }

    /**
     * Sends a message after a delay to this device
     *
     * @param msg the message to be sent
     * @param feature device feature associated to the message
     * @param delay time (in milliseconds) to delay before sending message
     */
    @Override
    public void sendMessage(Msg msg, DeviceFeature feature, long delay) {
        addDeviceRequest(msg, feature, delay);
    }

    /**
     * Adds a request for this device
     *
     * @param msg message to be sent
     * @param feature device feature that sent this message
     * @param delay time (in milliseconds) to delay before sending message
     */
    protected void addDeviceRequest(Msg msg, DeviceFeature feature, long delay) {
        logger.trace("enqueuing request with delay {} msec", delay);

        synchronized (requestQueue) {
            DeviceRequest request = new DeviceRequest(feature, msg, delay);
            DeviceRequest prevRequest = requestQueueHash.get(msg);
            if (prevRequest != null) {
                logger.trace("overwriting existing request for {}: {}", feature.getName(), msg);
                requestQueue.remove(prevRequest);
                requestQueueHash.remove(msg);
            }
            requestQueue.add(request);
            requestQueueHash.put(msg, request);
        }
        InsteonModem modem = getModem();
        if (modem != null) {
            modem.getRequestManager().addQueue(this, delay);
        }
    }

    /**
     * Handles next request for this device
     *
     * @return wait time (in milliseconds) before processing the subsequent request
     */
    @Override
    public long handleNextRequest() {
        long now = System.currentTimeMillis();
        // wait for feature queried to complete
        long waitTime = checkFeatureQueried(now);
        if (waitTime > 0) {
            return waitTime;
        }

        synchronized (requestQueue) {
            // take the next request off the queue
            DeviceRequest request = requestQueue.poll();
            if (request == null) {
                return 0L;
            }
            // get requested feature and message
            DeviceFeature feature = request.getFeature();
            Msg msg = request.getMessage();
            // remove request from queue hash
            requestQueueHash.remove(msg);
            // set last request queued time
            lastRequestQueued = now;
            // set feature queried for non-broadcast request message
            if (!msg.isAllLinkBroadcast()) {
                logger.trace("request taken off direct for {}: {}", feature.getName(), msg);
                // mark requested feature query status as queued
                feature.setQueryStatus(QueryStatus.QUERY_QUEUED);
                // store requested feature query message
                feature.setQueryMessage(msg);
                // set feature queried
                setFeatureQueried(feature);
            } else {
                logger.trace("request taken off bcast for {}: {}", feature.getName(), msg);
            }
            // write message
            InsteonModem modem = getModem();
            if (modem != null) {
                try {
                    modem.writeMessage(msg);
                } catch (IOException e) {
                    logger.warn("message write failed for msg: {}", msg, e);
                }
            }
            // determine the wait time for the next request
            long quietTime = msg.getQuietTime();
            long nextExpTime = Optional.ofNullable(requestQueue.peek()).map(DeviceRequest::getExpirationTime)
                    .orElse(0L);
            long nextTime = Math.max(now + quietTime, nextExpTime);
            logger.trace("next request queue processed in {} msec, quiettime {} msec", nextTime - now, quietTime);
            return nextTime;
        }
    }

    /**
     * Checks feature queried status
     *
     * @param now the current time
     * @return wait time if necessary otherwise 0
     */
    private long checkFeatureQueried(long now) {
        DeviceFeature feature = getFeatureQueried();
        if (feature != null) {
            QueryStatus queryStatus = feature.getQueryStatus();
            switch (queryStatus) {
                case QUERY_QUEUED:
                    // wait for feature queried request to be sent
                    long maxQueueTime = lastRequestQueued + REQUEST_QUEUE_TIMEOUT;
                    if (maxQueueTime > now) {
                        logger.trace("still waiting for {} query to be sent to {} for another {} msec",
                                feature.getName(), address, maxQueueTime - now);
                        return now + 1000L; // retry in 1000 ms
                    }
                    logger.debug("gave up waiting for {} query to be sent to {}", feature.getName(), address);
                    // reset feature queried as never queried
                    feature.setQueryMessage(null);
                    feature.setQueryStatus(QueryStatus.NEVER_QUERIED);
                    break;
                case QUERY_SENT:
                case QUERY_ACKED:
                    // wait for the feature queried to be answered
                    long maxAckTime = lastRequestSent + DIRECT_ACK_TIMEOUT;
                    if (maxAckTime > now) {
                        logger.trace("still waiting for {} query reply from {} for another {} msec", feature.getName(),
                                address, maxAckTime - now);
                        return now + 500L; // retry in 500 ms
                    }
                    logger.debug("gave up waiting for {} query reply from {}", feature.getName(), address);
                    // reset feature queried as never queried
                    feature.setQueryMessage(null);
                    feature.setQueryStatus(QueryStatus.NEVER_QUERIED);
                    break;
                default:
                    logger.debug("unexpected feature {} query status {} for {}", feature.getName(), queryStatus,
                            address);
            }
            // reset feature queried otheriwse
            setFeatureQueried(null);
        }
        return 0L;
    }

    /**
     * Notifies that a message request was replied for this device
     *
     * @param msg the message received
     */
    @Override
    public void requestReplied(Msg msg) {
        DeviceFeature feature = getFeatureQueried();
        if (feature != null && feature.isMyReply(msg)) {
            if (msg.isReplyAck()) {
                // mark feature queried as acked
                feature.setQueryStatus(QueryStatus.QUERY_ACKED);
            } else {
                logger.debug("got a reply nack msg: {}", msg);
                // mark feature queried as processed and answered
                setFeatureQueried(null);
                feature.setQueryMessage(null);
                feature.setQueryStatus(QueryStatus.QUERY_ANSWERED);
            }
        }
    }

    /**
     * Notifies that a message request was sent to this device
     *
     * @param msg the message sent
     * @param time the time the request was sent
     */
    @Override
    public void requestSent(Msg msg, long time) {
        DeviceFeature feature = getFeatureQueried();
        if (feature != null && msg.equals(feature.getQueryMessage())) {
            // mark feature queried as pending
            feature.setQueryStatus(QueryStatus.QUERY_SENT);
            // set last request sent time
            lastRequestSent = time;
        }
    }

    /**
     * Refreshes this device
     */
    @Override
    public void refresh() {
        logger.trace("refreshing device {}", address);
        @Nullable
        S handler = getHandler();
        if (handler != null) {
            handler.refresh();
        }
    }

    /**
     * Class that represents a device request
     */
    protected static class DeviceRequest implements Comparable<DeviceRequest> {
        private DeviceFeature feature;
        private Msg msg;
        private long expirationTime;

        public DeviceRequest(DeviceFeature feature, Msg msg, long delay) {
            this.feature = feature;
            this.msg = msg;
            setExpirationTime(delay);
        }

        public DeviceFeature getFeature() {
            return feature;
        }

        public Msg getMessage() {
            return msg;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long delay) {
            this.expirationTime = System.currentTimeMillis() + delay;
        }

        @Override
        public int compareTo(DeviceRequest other) {
            return (int) (expirationTime - other.expirationTime);
        }
    }
}
