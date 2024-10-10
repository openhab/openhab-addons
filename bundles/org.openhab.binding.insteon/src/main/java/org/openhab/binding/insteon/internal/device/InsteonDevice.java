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

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature.QueryStatus;
import org.openhab.binding.insteon.internal.device.database.LinkDB;
import org.openhab.binding.insteon.internal.device.database.LinkDBChange;
import org.openhab.binding.insteon.internal.device.database.LinkDBRecord;
import org.openhab.binding.insteon.internal.device.database.ModemDB;
import org.openhab.binding.insteon.internal.device.database.ModemDBChange;
import org.openhab.binding.insteon.internal.device.database.ModemDBEntry;
import org.openhab.binding.insteon.internal.device.database.ModemDBRecord;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.DeviceTypeRenamer;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.KeypadButtonToggleMode;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.GroupMessageStateMachine;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.BinaryUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link InsteonDevice} represents an Insteon device
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonDevice extends BaseDevice<InsteonAddress, InsteonDeviceHandler> {
    private static final int BCAST_STATE_TIMEOUT = 2000; // in milliseconds
    private static final int DEFAULT_HEARTBEAT_TIMEOUT = 1440; // in minutes
    private static final int FAILED_MSG_COUNT_THRESHOLD = 5;

    private InsteonEngine engine = InsteonEngine.UNKNOWN;
    private LinkDB linkDB;
    private Map<String, DefaultLink> defaultLinks = new LinkedHashMap<>();
    private List<Msg> storedMessages = new LinkedList<>();
    private Queue<DeviceRequest> deferredQueue = new PriorityQueue<>();
    private Map<Msg, DeviceRequest> deferredQueueHash = new HashMap<>();
    private Map<Byte, Long> lastBroadcastReceived = new HashMap<>();
    private Map<Integer, GroupMessageStateMachine> groupState = new HashMap<>();
    private volatile int failedMsgCount = 0;
    private volatile long lastMsgReceived = 0L;

    public InsteonDevice() {
        super(InsteonAddress.UNKNOWN);
        this.linkDB = new LinkDB(this);
    }

    public InsteonEngine getInsteonEngine() {
        return engine;
    }

    public LinkDB getLinkDB() {
        return linkDB;
    }

    public @Nullable DefaultLink getDefaultLink(String name) {
        synchronized (defaultLinks) {
            return defaultLinks.get(name);
        }
    }

    public List<DefaultLink> getDefaultLinks() {
        synchronized (defaultLinks) {
            return defaultLinks.values().stream().toList();
        }
    }

    public List<Msg> getStoredMessages() {
        synchronized (storedMessages) {
            return storedMessages;
        }
    }

    public List<DeviceFeature> getControllerFeatures() {
        return getFeatures().stream().filter(DeviceFeature::isControllerFeature).toList();
    }

    public List<DeviceFeature> getResponderFeatures() {
        return getFeatures().stream().filter(DeviceFeature::isResponderFeature).toList();
    }

    public List<DeviceFeature> getControllerOrResponderFeatures() {
        return getFeatures().stream().filter(DeviceFeature::isControllerOrResponderFeature).toList();
    }

    public List<DeviceFeature> getFeatures(String type) {
        return getFeatures().stream().filter(feature -> feature.getType().equals(type)).toList();
    }

    public @Nullable DeviceFeature getFeature(String type, int group) {
        return getFeatures().stream().filter(feature -> feature.getType().equals(type) && feature.getGroup() == group)
                .findFirst().orElse(null);
    }

    public double getLastMsgValueAsDouble(String type, int group, double defaultValue) {
        return Optional.ofNullable(getFeature(type, group)).map(DeviceFeature::getLastMsgValue).map(Double::doubleValue)
                .orElse(defaultValue);
    }

    public int getLastMsgValueAsInteger(String type, int group, int defaultValue) {
        return Optional.ofNullable(getFeature(type, group)).map(DeviceFeature::getLastMsgValue).map(Double::intValue)
                .orElse(defaultValue);
    }

    public @Nullable State getFeatureState(String type, int group) {
        return Optional.ofNullable(getFeature(type, group)).map(DeviceFeature::getState).orElse(null);
    }

    public boolean isResponding() {
        return failedMsgCount < FAILED_MSG_COUNT_THRESHOLD;
    }

    public boolean isBatteryPowered() {
        return getFlag("batteryPowered", false);
    }

    public boolean isDeviceSyncEnabled() {
        return getFlag("deviceSyncEnabled", false);
    }

    public boolean hasModemDBEntry() {
        return getFlag("modemDBEntry", false);
    }

    public void setInsteonEngine(InsteonEngine engine) {
        logger.trace("setting insteon engine for {} to {}", address, engine);
        this.engine = engine;
        // notify properties changed
        propertiesChanged(false);
    }

    public void setHasModemDBEntry(boolean value) {
        setFlag("modemDBEntry", value);
        // notify status changed
        statusChanged();
    }

    public void setIsDeviceSyncEnabled(boolean value) {
        setFlag("deviceSyncEnabled", value);
    }

    /**
     * Returns this device heartbeat timeout
     *
     * @return heartbeat timeout in minutes
     */
    public int getHeartbeatTimeout() {
        DeviceFeature feature = getFeature(FEATURE_HEARTBEAT_INTERVAL);
        if (feature != null) {
            if (feature.getState() instanceof QuantityType<?> interval) {
                return Objects.requireNonNullElse(interval.toInvertibleUnit(Units.MINUTE), interval).intValue();
            }
            return 0;
        }
        return DEFAULT_HEARTBEAT_TIMEOUT;
    }

    /**
     * Returns if this device has heartbeat
     *
     * @return true if has heartbeat feature and heartbeat on/off feature state on when available, otherise false
     */
    public boolean hasHeartbeat() {
        return hasFeature(FEATURE_HEARTBEAT) && (!hasFeature(FEATURE_HEARTBEAT_ON_OFF)
                || OnOffType.ON.equals(getFeatureState(FEATURE_HEARTBEAT_ON_OFF)));
    }

    /**
     * Returns if this device is awake
     *
     * @return true if device not battery powered or within awake time
     */
    public boolean isAwake() {
        if (isBatteryPowered()) {
            // define awake time based on the stay awake feature state (ON => 4 minutes; OFF => 3 seconds)
            State state = getFeatureState(FEATURE_STAY_AWAKE);
            int awakeTime = OnOffType.ON.equals(state) ? 240000 : 3000; // in msec
            return System.currentTimeMillis() - lastMsgReceived <= awakeTime;
        }
        return true;
    }

    /**
     * Returns if an incoming message is duplicate
     *
     * @param msg the message received
     * @return true if group or broadcast message is duplicate
     */
    public boolean isDuplicateMsg(Msg msg) {
        try {
            if (msg.isAllLinkBroadcastOrCleanup()) {
                synchronized (groupState) {
                    int group = msg.getGroup();
                    GroupMessageStateMachine stateMachine = groupState.computeIfAbsent(group,
                            k -> new GroupMessageStateMachine());
                    return stateMachine != null && stateMachine.isDuplicate(msg);
                }
            } else if (msg.isBroadcast()) {
                synchronized (lastBroadcastReceived) {
                    byte cmd1 = msg.getByte("command1");
                    long timestamp = msg.getTimestamp();
                    Long lastTimestamp = lastBroadcastReceived.put(cmd1, timestamp);
                    return lastTimestamp != null && Math.abs(timestamp - lastTimestamp) <= BCAST_STATE_TIMEOUT;
                }
            }
        } catch (FieldException e) {
            logger.warn("error parsing msg: {}", msg, e);
        }
        return false;
    }

    /**
     * Returns if device is pollable
     *
     * @return true if parent pollable and not battery powered
     */
    @Override
    public boolean isPollable() {
        return super.isPollable() && !isBatteryPowered();
    }

    /**
     * Polls this device
     *
     * @param delay scheduling delay (in milliseconds)
     */
    @Override
    public void doPoll(long delay) {
        // process deferred queue
        processDeferredQueue(delay);
        // poll insteon engine if unknown or its feature never queried
        DeviceFeature engineFeature = getFeature(FEATURE_INSTEON_ENGINE);
        if (engineFeature != null
                && (engine == InsteonEngine.UNKNOWN || engineFeature.getQueryStatus() == QueryStatus.NEVER_QUERIED)) {
            engineFeature.doPoll(delay);
            return; // insteon engine needs to be known before enqueueing more messages
        }
        // load this device link db if not complete or should be reloaded
        if (!linkDB.isComplete() || linkDB.shouldReload()) {
            linkDB.load(delay);
            return; // link db needs to be complete before enqueueing more messages
        }
        // update this device link db if needed
        if (linkDB.shouldUpdate()) {
            linkDB.update(delay);
        }

        super.doPoll(delay);
    }

    /**
     * Schedules polling for this device
     *
     * @param delay scheduling delay (in milliseconds)
     * @param featureFilter feature filter to apply
     * @return delay spacing
     */
    @Override
    protected long schedulePoll(long delay, Predicate<DeviceFeature> featureFilter) {
        long spacing = super.schedulePoll(delay, featureFilter);
        // ping non-battery powered device if no other feature scheduled poll
        if (!isBatteryPowered() && spacing == 0) {
            Msg msg = pollFeature(FEATURE_PING, delay);
            if (msg != null) {
                spacing += msg.getQuietTime();
            }
        }
        return spacing;
    }

    /**
     * Polls all responder features for this device
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollResponders(long delay) {
        schedulePoll(delay, DeviceFeature::hasResponderFeatures);
    }

    /**
     * Polls responder features for a controller address and group
     *
     * @param address the controller address
     * @param group the controller group
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollResponders(InsteonAddress address, int group, long delay) {
        // poll all responder features if link db not complete
        if (!linkDB.isComplete()) {
            getResponderFeatures().forEach(feature -> feature.triggerPoll(delay));
            return;
        }
        // poll responder features matching record component id (data 3)
        linkDB.getResponderRecords(address, group)
                .forEach(record -> getResponderFeatures().stream()
                        .filter(feature -> feature.getComponentId() == record.getComponentId()).findFirst()
                        .ifPresent(feature -> feature.triggerPoll(delay)));
    }

    /**
     * Polls related devices to a controller group
     *
     * @param group the controller group
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedDevices(int group, long delay) {
        InsteonModem modem = getModem();
        if (modem != null) {
            linkDB.getRelatedDevices(group).stream().map(modem::getInsteonDevice).filter(Objects::nonNull)
                    .map(Objects::requireNonNull).forEach(device -> {
                        logger.debug("polling related device {} to controller {} group {}", device.getAddress(),
                                address, group);
                        device.pollResponders(address, group, delay);
                    });
        }
    }

    /**
     * Adjusts responder features for a controller address and group
     *
     * @param address the controller address
     * @param group the controller group
     * @param onLevel the controller channel config
     * @param cmd the cmd to adjust to
     */
    public void adjustResponders(InsteonAddress address, int group, InsteonChannelConfiguration config, Command cmd) {
        // handle command for responder feature with group matching record component id (data 3)
        linkDB.getResponderRecords(address, group)
                .forEach(record -> getResponderFeatures().stream()
                        .filter(feature -> feature.getComponentId() == record.getComponentId()).findFirst()
                        .ifPresent(feature -> {
                            InsteonChannelConfiguration adjustConfig = InsteonChannelConfiguration.copyOf(config,
                                    record.getOnLevel(), record.getRampRate());
                            feature.handleCommand(adjustConfig, cmd);
                        }));
    }

    /**
     * Adjusts related devices to a controller group
     *
     * @param group the controller group
     * @param config the controller channel config
     * @param cmd the cmd to adjust to
     */
    public void adjustRelatedDevices(int group, InsteonChannelConfiguration config, Command cmd) {
        InsteonModem modem = getModem();
        if (modem != null) {
            linkDB.getRelatedDevices(group).stream().map(modem::getInsteonDevice).filter(Objects::nonNull)
                    .map(Objects::requireNonNull).forEach(device -> {
                        logger.debug("adjusting related device {} to controller {} group {}", device.getAddress(),
                                address, group);
                        device.adjustResponders(address, group, config, cmd);
                    });
        }
    }

    /**
     * Returns broadcast group for a controller feature
     *
     * @param feature the device feature
     * @return the brodcast group if found, otherwise -1
     */
    public int getBroadcastGroup(DeviceFeature feature) {
        InsteonModem modem = getModem();
        if (modem != null) {
            List<InsteonAddress> relatedDevices = linkDB.getRelatedDevices(feature.getGroup());
            // return broadcast group with matching link and modem db related devices
            return linkDB.getBroadcastGroups(feature.getComponentId()).stream()
                    .filter(group -> modem.getDB().getRelatedDevices(group).stream()
                            .allMatch(address -> getAddress().equals(address) || relatedDevices.contains(address)))
                    .findFirst().orElse(-1);
        }
        return -1;
    }

    /**
     * Replays a list of messages
     */
    public void replayMessages(List<Msg> messages) {
        for (Msg msg : messages) {
            logger.trace("replaying msg: {}", msg);
            msg.setIsReplayed(true);
            handleMessage(msg);
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
        // update last msg received if not failure report and more recent msg timestamp
        if (!msg.isFailureReport() && msg.getTimestamp() > lastMsgReceived) {
            lastMsgReceived = msg.getTimestamp();
        }
        // store message if no feature defined
        if (!hasFeatures()) {
            logger.debug("storing message for unknown device {}", address);

            synchronized (storedMessages) {
                storedMessages.add(msg);
            }
            return;
        }
        // store current responding state
        boolean isPrevResponding = isResponding();
        // handle message depending if failure report or not
        if (msg.isFailureReport()) {
            getFeatures().stream().filter(feature -> feature.isMyDirectAckOrNack(msg)).findFirst()
                    .ifPresent(feature -> {
                        logger.debug("got a failure report reply of direct for {}", feature.getName());
                        // increase failed message counter
                        failedMsgCount++;
                        // mark feature queried as processed and never queried
                        setFeatureQueried(null);
                        feature.setQueryMessage(null);
                        feature.setQueryStatus(QueryStatus.NEVER_QUERIED);
                        // poll feature again if device is responding
                        if (isResponding()) {
                            feature.doPoll(0L);
                        }
                    });
        } else {
            // update non-status features
            getFeatures().stream().filter(feature -> !feature.isStatusFeature() && feature.handleMessage(msg))
                    .findFirst().ifPresent(feature -> {
                        logger.trace("handled reply of direct for {}", feature.getName());
                        // reset failed message counter
                        failedMsgCount = 0;
                        // mark feature queried as processed and answered
                        setFeatureQueried(null);
                        feature.setQueryMessage(null);
                        feature.setQueryStatus(QueryStatus.QUERY_ANSWERED);
                    });
            // update all status features (e.g. device last update time)
            getFeatures().stream().filter(DeviceFeature::isStatusFeature)
                    .forEach(feature -> feature.handleMessage(msg));
        }
        // poll battery powered device while awake if non-duplicate all link or broadcast message
        if ((msg.isAllLinkBroadcastOrCleanup() || msg.isBroadcast()) && isBatteryPowered() && isAwake()
                && !isDuplicateMsg(msg)) {
            // add poll delay for non-replayed all link broadcast allowing cleanup msg to be be processed beforehand
            long delay = msg.isAllLinkBroadcast() && !msg.isAllLinkSuccessReport() && !msg.isReplayed() ? 1500L : 0L;
            doPoll(delay);
        }
        // notify if responding state changed
        if (isPrevResponding != isResponding()) {
            statusChanged();
        }
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
        if (isAwake()) {
            addDeviceRequest(msg, feature, delay);
        } else {
            addDeferredRequest(msg, feature);
        }
        // mark feature query status as scheduled for non-broadcast request message
        if (!msg.isAllLinkBroadcast()) {
            feature.setQueryStatus(QueryStatus.QUERY_SCHEDULED);
        }
    }

    /**
     * Processes deferred queue
     *
     * @param delay time (in milliseconds) to delay before sending message
     */
    private void processDeferredQueue(long delay) {
        synchronized (deferredQueue) {
            while (!deferredQueue.isEmpty()) {
                DeviceRequest request = deferredQueue.poll();
                if (request != null) {
                    Msg msg = request.getMessage();
                    DeviceFeature feature = request.getFeature();
                    deferredQueueHash.remove(msg);
                    request.setExpirationTime(delay);
                    logger.trace("enqueuing deferred request for {}", feature.getName());
                    addDeviceRequest(msg, feature, delay);
                }
            }
        }
    }

    /**
     * Adds deferred request
     *
     * @param request device request to add
     */
    private void addDeferredRequest(Msg msg, DeviceFeature feature) {
        logger.trace("deferring request for sleeping device {}", address);

        synchronized (deferredQueue) {
            DeviceRequest request = new DeviceRequest(feature, msg, 0L);
            DeviceRequest prevRequest = deferredQueueHash.get(msg);
            if (prevRequest != null) {
                logger.trace("overwriting existing deferred request for {}: {}", feature.getName(), msg);
                deferredQueue.remove(prevRequest);
                deferredQueueHash.remove(msg);
            }
            deferredQueue.add(request);
            deferredQueueHash.put(msg, request);
        }
    }

    /**
     * Clears request queue
     */
    @Override
    protected void clearRequestQueue() {
        super.clearRequestQueue();

        synchronized (deferredQueue) {
            deferredQueue.clear();
            deferredQueueHash.clear();
        }
    }

    /**
     * Updates product data for this device
     *
     * @param newData the new product data to use
     */
    public void updateProductData(ProductData newData) {
        ProductData productData = getProductData();
        if (productData == null) {
            setProductData(newData);
            propertiesChanged(true);
        } else {
            logger.trace("updating product data for {} to {}", address, newData);
            if (productData.update(newData)) {
                propertiesChanged(true);
            } else {
                propertiesChanged(false);
                resetFeaturesQueryStatus();
            }
        }
    }

    /**
     * Updates this device type
     *
     * @param renamer the device type renamer
     */
    public void updateType(DeviceTypeRenamer renamer) {
        Optional.ofNullable(getType()).map(DeviceType::getName).map(renamer::getNewDeviceType)
                .map(name -> DeviceTypeRegistry.getInstance().getDeviceType(name)).ifPresent(this::updateType);
    }

    /**
     * Updates this device type
     *
     * @param newType the new device type to use
     */
    public void updateType(DeviceType newType) {
        ProductData productData = getProductData();
        DeviceType currentType = getType();
        if (productData != null && !newType.equals(currentType)) {
            logger.trace("updating device type from {} to {} for {}",
                    currentType != null ? currentType.getName() : "undefined", newType.getName(), address);
            productData.setDeviceType(newType);
            propertiesChanged(true);
        }
    }

    /**
     * Updates the default links
     */
    public void updateDefaultLinks() {
        InsteonModem modem = getModem();
        ProductData productData = getProductData();
        DeviceType deviceType = getType();
        State linkFFGroup = getFeatureState(FEATURE_LINK_FF_GROUP);
        State twoGroups = getFeatureState(FEATURE_TWO_GROUPS);
        if (modem == null || productData == null || deviceType == null || linkFFGroup == UnDefType.NULL
                || twoGroups == UnDefType.NULL || InsteonAddress.UNKNOWN.equals(modem.getAddress())) {
            return;
        }
        // clear default links
        synchronized (defaultLinks) {
            defaultLinks.clear();
        }
        // iterate over device type default links
        deviceType.getDefaultLinks().forEach((name, link) -> {
            // skip default link if 2Groups feature is off and its group is 2
            if (OnOffType.OFF.equals(twoGroups) && link.getGroup() == 2) {
                return;
            }
            // create link db record based on FFGroup feature state
            LinkDBRecord linkDBRecord = LinkDBRecord.create(0, modem.getAddress(),
                    OnOffType.ON.equals(linkFFGroup) ? 0xFF : link.getGroup(), link.isController(), link.getData());
            // create modem db record
            ModemDBRecord modemDBRecord = ModemDBRecord.create(address, link.getGroup(), !link.isController(),
                    !link.isController() ? productData.getRecordData() : new byte[3]);
            // create default link commands
            List<Msg> commands = link.getCommands().stream().map(command -> command.getMessage(this))
                    .filter(Objects::nonNull).map(Objects::requireNonNull).toList();
            // add default link
            addDefaultLink(new DefaultLink(name, linkDBRecord, modemDBRecord, commands));
        });
    }

    /**
     * Adds a default link for this device
     *
     * @param link the default link to add
     */
    private void addDefaultLink(DefaultLink link) {
        logger.trace("adding default link {} for {}", link.getName(), address);

        synchronized (defaultLinks) {
            defaultLinks.put(link.getName(), link);
        }
    }

    /**
     * Returns a map of missing device links for this device
     *
     * @return map of missing link db records based on default links
     */
    public Map<String, LinkDBChange> getMissingDeviceLinks() {
        Map<String, LinkDBChange> links = new LinkedHashMap<>();
        if (linkDB.isComplete() && hasModemDBEntry()) {
            for (DefaultLink link : getDefaultLinks()) {
                LinkDBRecord record = link.getLinkDBRecord();
                if ((record.getComponentId() > 0 && !linkDB.hasComponentIdRecord(record.getComponentId(), true))
                        || !linkDB.hasGroupRecord(record.getGroup(), true)) {
                    links.put(link.getName(), LinkDBChange.forAdd(record));
                }
            }
        }
        return links;
    }

    /**
     * Returns a map of missing modem links for this device
     *
     * @return map of missing modem db records based on default links
     */
    public Map<String, ModemDBChange> getMissingModemLinks() {
        Map<String, ModemDBChange> links = new LinkedHashMap<>();
        InsteonModem modem = getModem();
        if (modem != null && modem.getDB().isComplete() && hasModemDBEntry()) {
            for (DefaultLink link : getDefaultLinks()) {
                ModemDBRecord record = link.getModemDBRecord();
                if (!modem.getDB().hasRecord(record.getAddress(), record.getGroup(), record.isController())) {
                    links.put(link.getName(), ModemDBChange.forAdd(record));
                }
            }
        }
        return links;
    }

    /**
     * Returns a set of missing links for this device
     *
     * @return a set of missing link names
     */
    public Set<String> getMissingLinks() {
        return Stream.of(getMissingDeviceLinks().keySet(), getMissingModemLinks().keySet()).flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Logs missing links for this device
     */
    public void logMissingLinks() {
        Set<String> links = getMissingLinks();
        if (!links.isEmpty()) {
            logger.warn(
                    "device {} has missing default links {}, "
                            + "run 'insteon device addMissingLinks' command via openhab console to fix.",
                    address, links);
        }
    }

    /**
     * Adds missing links to link db for this device
     */
    public void addMissingDeviceLinks() {
        if (getDefaultLinks().isEmpty()) {
            return;
        }
        List<LinkDBChange> changes = getMissingDeviceLinks().values().stream().distinct().toList();
        if (changes.isEmpty()) {
            logger.debug("no missing default links from link db to add for {}", address);
        } else {
            logger.trace("adding missing default links to link db for {}", address);
            linkDB.clearChanges();
            changes.forEach(linkDB::addChange);
            linkDB.update();
        }

        InsteonModem modem = getModem();
        if (modem != null) {
            getMissingDeviceLinks().keySet().stream().map(this::getDefaultLink).filter(Objects::nonNull)
                    .map(Objects::requireNonNull).flatMap(link -> link.getCommands().stream()).forEach(msg -> {
                        try {
                            modem.writeMessage(msg);
                        } catch (IOException e) {
                            logger.warn("message write failed for msg: {}", msg, e);
                        }
                    });
        }
    }

    /**
     * Adds missing links to modem db for this device
     */
    public void addMissingModemLinks() {
        InsteonModem modem = getModem();
        if (modem == null || getDefaultLinks().isEmpty()) {
            return;
        }
        List<ModemDBChange> changes = getMissingModemLinks().values().stream().distinct().toList();
        if (changes.isEmpty()) {
            logger.debug("no missing default links from modem db to add for {}", address);
        } else {
            logger.trace("adding missing default links to modem db for {}", address);
            ModemDB modemDB = modem.getDB();
            modemDB.clearChanges();
            changes.forEach(modemDB::addChange);
            modemDB.update();
        }
    }

    /**
     * Sets a keypad button radio group
     *
     * @param buttons list of button groups to set
     */
    public void setButtonRadioGroup(List<Integer> buttons) {
        // set each radio button to turn off each others when turned on if should set
        for (int buttonGroup : buttons) {
            DeviceFeature onMaskFeature = getFeature(FEATURE_TYPE_KEYPAD_BUTTON_ON_MASK, buttonGroup);
            DeviceFeature offMaskFeature = getFeature(FEATURE_TYPE_KEYPAD_BUTTON_OFF_MASK, buttonGroup);

            if (onMaskFeature != null && offMaskFeature != null) {
                int onMask = onMaskFeature.getLastMsgValueAsInteger(0);
                int offMask = offMaskFeature.getLastMsgValueAsInteger(0);

                for (int group : buttons) {
                    int bit = group - 1;
                    onMask = BinaryUtils.clearBit(onMask, bit);
                    offMask = BinaryUtils.updateBit(offMask, bit, buttonGroup != group);
                }
                onMaskFeature.handleCommand(new DecimalType(onMask));
                offMaskFeature.handleCommand(new DecimalType(offMask));
            }
        }
    }

    /**
     * Clears a keypad button radion group
     *
     * @param buttons list of button groups to clear
     */
    public void clearButtonRadioGroup(List<Integer> buttons) {
        List<Integer> allButtons = getFeatures(FEATURE_TYPE_KEYPAD_BUTTON).stream().map(DeviceFeature::getGroup)
                .toList();
        // clear each radio button and decouple from others
        for (int buttonGroup : allButtons) {
            DeviceFeature onMaskFeature = getFeature(FEATURE_TYPE_KEYPAD_BUTTON_ON_MASK, buttonGroup);
            DeviceFeature offMaskFeature = getFeature(FEATURE_TYPE_KEYPAD_BUTTON_OFF_MASK, buttonGroup);

            if (onMaskFeature != null && offMaskFeature != null) {
                int onMask = onMaskFeature.getLastMsgValueAsInteger(0);
                int offMask = offMaskFeature.getLastMsgValueAsInteger(0);

                for (int group : buttons.contains(buttonGroup) ? allButtons : buttons) {
                    int bit = group - 1;
                    onMask = BinaryUtils.clearBit(onMask, bit);
                    offMask = BinaryUtils.clearBit(offMask, bit);
                }
                onMaskFeature.handleCommand(new DecimalType(onMask));
                offMaskFeature.handleCommand(new DecimalType(offMask));
            }
        }
    }

    /**
     * Sets keypad button toggle mode
     *
     * @param buttons list of button groups to use
     * @param mode toggle mode to set
     */
    public void setButtonToggleMode(List<Integer> buttons, KeypadButtonToggleMode mode) {
        // use the first button group if available to set toggle mode
        int buttonGroup = !buttons.isEmpty() ? buttons.get(0) : -1;
        DeviceFeature toggleModeFeature = getFeature(FEATURE_TYPE_KEYPAD_BUTTON_TOGGLE_MODE, buttonGroup);

        if (toggleModeFeature != null) {
            int nonToggleMask = toggleModeFeature.getLastMsgValueAsInteger(0) >> 8;
            int alwaysOnOffMask = toggleModeFeature.getLastMsgValueAsInteger(0) & 0xFF;

            for (int group : buttons) {
                int bit = group - 1;
                nonToggleMask = BinaryUtils.updateBit(nonToggleMask, bit, mode != KeypadButtonToggleMode.TOGGLE);
                alwaysOnOffMask = BinaryUtils.updateBit(alwaysOnOffMask, bit, mode == KeypadButtonToggleMode.ALWAYS_ON);
            }
            toggleModeFeature.handleCommand(new DecimalType(nonToggleMask << 8 | alwaysOnOffMask));
        }
    }

    /**
     * Initializes this device
     */
    public void initialize() {
        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete()) {
            return;
        }

        ModemDBEntry dbe = modem.getDB().getEntry(address);
        if (dbe == null) {
            logger.warn("device {} not found in the modem database. Did you forget to link?", address);
            setHasModemDBEntry(false);
            stopPolling();
            return;
        }

        ProductData productData = dbe.getProductData();
        if (productData != null) {
            updateProductData(productData);
        }

        if (!hasModemDBEntry()) {
            logger.debug("device {} found in the modem database.", address);
            setHasModemDBEntry(true);
        }

        if (isPollable()) {
            startPolling();
        }

        updateDefaultLinks();
    }

    /**
     * Refreshes this device
     */
    @Override
    public void refresh() {
        initialize();

        super.refresh();
    }

    /**
     * Resets heartbeat monitor
     */
    public void resetHeartbeatMonitor() {
        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.resetHeartbeatMonitor();
        }
    }

    /**
     * Notifies that the link db has been updated for this device
     */
    public void linkDBUpdated() {
        logger.trace("link db for {} has been updated", address);

        if (linkDB.isComplete()) {
            if (isBatteryPowered() && isAwake() || getStatus() == DeviceStatus.POLLING) {
                // poll database delta feature
                pollFeature(FEATURE_DATABASE_DELTA, 0L);
                // poll remaining features for this device
                doPoll(0L);
            }
            // log missing links
            logMissingLinks();
        }
        // notify device handler if defined
        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.deviceLinkDBUpdated(this);
        }
    }

    /**
     * Notifies that the properties have changed for this device
     *
     * @param reset if the device should be reset
     */
    public void propertiesChanged(boolean reset) {
        logger.trace("properties for {} has changed", address);

        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            if (reset) {
                handler.reset(this);
            } else {
                handler.updateProperties(this);
            }
        }
    }

    /**
     * Notifies that the status has changed for this device
     */
    public void statusChanged() {
        logger.trace("status for {} has changed", address);

        InsteonDeviceHandler handler = getHandler();
        if (handler != null) {
            handler.updateStatus();
        }
    }

    /**
     * Factory method for creating a InsteonDevice from a device address, modem and cache
     *
     * @param address the device address
     * @param modem the device modem
     * @param productData the device product data
     * @return the newly created InsteonDevice
     */
    public static InsteonDevice makeDevice(InsteonAddress address, @Nullable InsteonModem modem,
            @Nullable ProductData productData) {
        InsteonDevice device = new InsteonDevice();
        device.setAddress(address);
        device.setModem(modem);

        if (productData != null) {
            DeviceType deviceType = productData.getDeviceType();
            if (deviceType != null) {
                device.instantiateFeatures(deviceType);
                device.setFlags(deviceType.getFlags());
            }
            int location = productData.getFirstRecordLocation();
            if (location != LinkDBRecord.LOCATION_ZERO) {
                device.getLinkDB().setFirstRecordLocation(location);
            }
            device.setProductData(productData);
        }

        return device;
    }
}
