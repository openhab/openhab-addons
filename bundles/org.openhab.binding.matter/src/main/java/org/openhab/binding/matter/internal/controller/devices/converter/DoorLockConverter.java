/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster.OctetString;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.CredentialStruct;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.CredentialTypeEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.DataOperationTypeEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.OperatingModeEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.UserStatusEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.UserTypeEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameterGroup;
import org.openhab.core.config.core.ConfigDescriptionParameterGroupBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

/**
 * A converter for translating {@link DoorLockCluster} events and attributes to
 * openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DoorLockConverter extends GenericConverter<DoorLockCluster> {

    /**
     * Represents user information retrieved from the lock.
     */
    public static class LockUser {
        public final int userIndex;
        public @Nullable String userName;
        public @Nullable UserTypeEnum userType;
        public @Nullable UserStatusEnum userStatus;
        public List<CredentialStruct> credentials = new ArrayList<>();
        public @Nullable Integer nextUserIndex;
        public @Nullable Integer creatorFabricIndex;

        public LockUser(int userIndex) {
            this.userIndex = userIndex;
        }

        /**
         * Returns true if the user slot is currently in use (occupied in matter terms).
         */
        public boolean isOccupied() {
            return userStatus != null && userStatus != UserStatusEnum.AVAILABLE;
        }

        public boolean hasCredentialOfType(CredentialTypeEnum type) {
            return credentials.stream().anyMatch(c -> c.credentialType == type);
        }

        public boolean isManagedByFabric(int ourFabricIndex) {
            return creatorFabricIndex == null || creatorFabricIndex == ourFabricIndex;
        }
    }

    private static final int ADDITIONAL_USER_SLOTS = 5;

    private final Map<Integer, LockUser> lockUsers = new ConcurrentHashMap<>();
    private final AtomicBoolean fetchingUsers = new AtomicBoolean(false);
    private int numberOfTotalUsersSupported = 0;
    private int minPinCodeLength = 0;
    private int maxPinCodeLength = 255; // Maximum length of a PIN code if not specified is 255
    private int autoRelockTime = 0;
    private boolean pinCredentialSupported = false;
    private boolean userFeatureSupported = false;
    private boolean requirePinForRemoteOperation = false;
    private boolean enableOneTouchLocking = false;
    private OperatingModeEnum operatingMode = OperatingModeEnum.NORMAL;

    public DoorLockConverter(DoorLockCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);

        if (cluster.featureMap != null) {
            pinCredentialSupported = cluster.featureMap.pinCredential;
            userFeatureSupported = cluster.featureMap.user;
        }

        if (cluster.numberOfTotalUsersSupported != null) {
            numberOfTotalUsersSupported = cluster.numberOfTotalUsersSupported;
        }
        if (cluster.minPinCodeLength != null) {
            minPinCodeLength = cluster.minPinCodeLength;
        }
        if (cluster.maxPinCodeLength != null) {
            maxPinCodeLength = cluster.maxPinCodeLength;
        }
        if (cluster.requirePinForRemoteOperation != null) {
            requirePinForRemoteOperation = cluster.requirePinForRemoteOperation;
        }
        if (cluster.autoRelockTime != null) {
            autoRelockTime = cluster.autoRelockTime;
        }
        if (cluster.enableOneTouchLocking != null) {
            enableOneTouchLocking = cluster.enableOneTouchLocking;
        }
        if (cluster.operatingMode != null) {
            operatingMode = cluster.operatingMode;
        }
        updateConfigDescription();
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        Channel stateChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_DOORLOCK_STATE), CoreItemFactory.SWITCH)
                .withType(CHANNEL_DOORLOCK_STATE).build();
        channels.put(stateChannel, null);

        Channel alarmChannel = ChannelBuilder.create(new ChannelUID(channelGroupUID, CHANNEL_ID_DOORLOCK_ALARM), null)
                .withType(CHANNEL_DOORLOCK_ALARM).withKind(ChannelKind.TRIGGER).build();
        List<StateOption> alarmOptions = new ArrayList<StateOption>();
        for (DoorLockCluster.AlarmCodeEnum e : DoorLockCluster.AlarmCodeEnum.values()) {
            alarmOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
        }
        StateDescription stateDescriptionAlarm = StateDescriptionFragmentBuilder.create().withOptions(alarmOptions)
                .build().toStateDescription();
        channels.put(alarmChannel, stateDescriptionAlarm);

        Channel lockOperationErrorChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_DOORLOCK_LOCKOPERATIONERROR), null)
                .withType(CHANNEL_DOORLOCK_LOCKOPERATIONERROR).withKind(ChannelKind.TRIGGER).build();
        List<StateOption> lockOperationErrorOptions = new ArrayList<StateOption>();
        for (DoorLockCluster.OperationErrorEnum e : DoorLockCluster.OperationErrorEnum.values()) {
            lockOperationErrorOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
        }
        StateDescription stateDescriptionLockOperationError = StateDescriptionFragmentBuilder.create()
                .withOptions(lockOperationErrorOptions).build().toStateDescription();
        channels.put(lockOperationErrorChannel, stateDescriptionLockOperationError);

        if (initializingCluster.featureMap.doorPositionSensor) {
            Channel doorStateChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_DOORLOCK_DOORSTATE), CoreItemFactory.CONTACT)
                    .withType(CHANNEL_DOORLOCK_DOORSTATE).build();
            channels.put(doorStateChannel, null);
        }

        return channels;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType onOffType) {
            OctetString pinCode = getPinCodeForRemoteOperation();
            ClusterCommand doorLockCommand = onOffType == OnOffType.ON ? DoorLockCluster.lockDoor(pinCode)
                    : DoorLockCluster.unlockDoor(pinCode);
            handler.sendClusterCommand(endpointNumber, DoorLockCluster.CLUSTER_NAME, doorLockCommand);
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case DoorLockCluster.ATTRIBUTE_LOCK_STATE:
                if (message.value instanceof DoorLockCluster.LockStateEnum lockState) {
                    updateState(CHANNEL_ID_DOORLOCK_STATE,
                            lockState == DoorLockCluster.LockStateEnum.LOCKED ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case DoorLockCluster.ATTRIBUTE_DOOR_STATE:
                if (message.value instanceof DoorLockCluster.DoorStateEnum doorState) {
                    updateState(CHANNEL_ID_DOORLOCK_DOORSTATE,
                            doorState == DoorLockCluster.DoorStateEnum.DOOR_CLOSED ? OpenClosedType.CLOSED
                                    : OpenClosedType.OPEN);
                }
                break;
            case DoorLockCluster.ATTRIBUTE_OPERATING_MODE:
                if (message.value instanceof OperatingModeEnum operatingMode) {
                    this.operatingMode = operatingMode;
                    handler.updateConfiguration(Map.of(MatterBindingConstants.CONFIG_DOORLOCK_OPERATING_MODE,
                            String.valueOf(operatingMode.getValue())));
                }
                break;
            case DoorLockCluster.ATTRIBUTE_AUTO_RELOCK_TIME:
                if (message.value instanceof Number autoRelockTime) {
                    this.autoRelockTime = autoRelockTime.intValue();
                    handler.updateConfiguration(
                            Map.of(MatterBindingConstants.CONFIG_DOORLOCK_AUTO_RELOCK_TIME, this.autoRelockTime));
                }
                break;
            case DoorLockCluster.ATTRIBUTE_ENABLE_ONE_TOUCH_LOCKING:
                if (message.value instanceof Boolean oneTouchLocking) {
                    this.enableOneTouchLocking = oneTouchLocking;
                    handler.updateConfiguration(Map.of(MatterBindingConstants.CONFIG_DOORLOCK_ONE_TOUCH_LOCKING,
                            String.valueOf(oneTouchLocking)));
                }
                break;
            case DoorLockCluster.ATTRIBUTE_REQUIRE_PIN_FOR_REMOTE_OPERATION:
                if (message.value instanceof Boolean requirePin) {
                    requirePinForRemoteOperation = requirePin;
                }
                break;
            default:
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
        switch (message.path.eventName) {
            case "doorLockAlarm":
                if (message.events != null && message.events.length > 0
                        && message.events[0].data instanceof DoorLockCluster.DoorLockAlarm doorLockAlarm) {
                    triggerChannel(CHANNEL_ID_DOORLOCK_ALARM, doorLockAlarm.alarmCode.getValue().toString());
                }
                break;
            case "lockOperationError":
                if (message.events != null && message.events.length > 0
                        && message.events[0].data instanceof DoorLockCluster.LockOperationError lockOperationError) {
                    triggerChannel(CHANNEL_ID_DOORLOCK_LOCKOPERATIONERROR,
                            lockOperationError.operationError.getValue().toString());
                }
                break;
            case "lockUserChange":
                if (userFeatureSupported) {
                    fetchAllUsers();
                }
                break;
        }
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_DOORLOCK_STATE,
                initializingCluster.lockState == DoorLockCluster.LockStateEnum.LOCKED ? OnOffType.ON : OnOffType.OFF);

        if (initializingCluster.featureMap.doorPositionSensor) {
            updateState(CHANNEL_ID_DOORLOCK_DOORSTATE,
                    initializingCluster.doorState == DoorLockCluster.DoorStateEnum.DOOR_CLOSED ? OpenClosedType.CLOSED
                            : OpenClosedType.OPEN);
        }
        Map<String, Object> entries = new HashMap<>();
        if (initializingCluster.operatingMode != null) {
            entries.put(MatterBindingConstants.CONFIG_DOORLOCK_OPERATING_MODE,
                    String.valueOf(initializingCluster.operatingMode.getValue()));
        }
        if (initializingCluster.autoRelockTime != null) {
            entries.put(MatterBindingConstants.CONFIG_DOORLOCK_AUTO_RELOCK_TIME, initializingCluster.autoRelockTime);
        }
        if (initializingCluster.enableOneTouchLocking != null) {
            entries.put(MatterBindingConstants.CONFIG_DOORLOCK_ONE_TOUCH_LOCKING,
                    String.valueOf(initializingCluster.enableOneTouchLocking));
        }
        if (!entries.isEmpty()) {
            handler.updateConfiguration(entries);
        }
        if (userFeatureSupported) {
            fetchAllUsers();
        }
    }

    @Override
    public void handleConfigurationUpdate(Configuration config) {
        processLockManagementConfiguration(config);
        if (userFeatureSupported) {
            int userGroupsToProcess = calculateUserGroupsToShow();
            for (int userIndex = 1; userIndex <= userGroupsToProcess; userIndex++) {
                processUserConfiguration(config, userIndex);
            }
        }
    }

    private @Nullable OctetString getPinCodeForRemoteOperation() {
        if (!requirePinForRemoteOperation) {
            return null;
        }
        Object pinValue = handler.getThing().getConfiguration()
                .get(MatterBindingConstants.CONFIG_DOORLOCK_DEFAULT_LOCK_PIN);
        if (pinValue instanceof String pin && !pin.isEmpty()) {
            return new OctetString(pin, StandardCharsets.UTF_8);
        }
        return null;
    }

    private void processLockManagementConfiguration(Configuration config) {
        Object operatingModeValue = config.get(MatterBindingConstants.CONFIG_DOORLOCK_OPERATING_MODE);
        if (operatingModeValue instanceof Number number) {
            int configuredMode = number.intValue();
            final int currentMode = operatingMode.getValue();
            if (currentMode != configuredMode) {
                logger.debug("Updating operating mode from {} to {}", currentMode, configuredMode);
                handler.writeAttribute(endpointNumber, DoorLockCluster.CLUSTER_NAME,
                        DoorLockCluster.ATTRIBUTE_OPERATING_MODE, String.valueOf(configuredMode)).exceptionally(e -> {
                            logger.warn("Failed to update operating mode: {}", e.getMessage());
                            handler.updateConfiguration(Map.of(MatterBindingConstants.CONFIG_DOORLOCK_OPERATING_MODE,
                                    String.valueOf(Objects.requireNonNullElse(currentMode, 0))));
                            return Void.TYPE.cast(null);
                        });
            }
        }

        Object autoRelockTimeValue = config.get(MatterBindingConstants.CONFIG_DOORLOCK_AUTO_RELOCK_TIME);
        if (autoRelockTimeValue instanceof Number number) {
            int configuredTime = number.intValue();
            final int currentTime = autoRelockTime;
            if (currentTime != configuredTime) {
                logger.debug("Updating auto relock time from {} to {}", currentTime, configuredTime);
                handler.writeAttribute(endpointNumber, DoorLockCluster.CLUSTER_NAME,
                        DoorLockCluster.ATTRIBUTE_AUTO_RELOCK_TIME, String.valueOf(configuredTime)).exceptionally(e -> {
                            logger.warn("Failed to update auto relock time: {}", e.getMessage());
                            handler.updateConfiguration(
                                    Map.of(MatterBindingConstants.CONFIG_DOORLOCK_AUTO_RELOCK_TIME, currentTime));
                            return Void.TYPE.cast(null);
                        });
            }
        }

        Object oneTouchLockingValue = config.get(MatterBindingConstants.CONFIG_DOORLOCK_ONE_TOUCH_LOCKING);
        if (oneTouchLockingValue instanceof Boolean configuredValue) {
            final Boolean currentValue = enableOneTouchLocking;
            if (!currentValue.equals(configuredValue)) {
                logger.debug("Updating one touch locking from {} to {}", currentValue, configuredValue);
                handler.writeAttribute(endpointNumber, DoorLockCluster.CLUSTER_NAME,
                        DoorLockCluster.ATTRIBUTE_ENABLE_ONE_TOUCH_LOCKING, String.valueOf(configuredValue))
                        .exceptionally(e -> {
                            logger.warn("Failed to update one touch locking: {}", e.getMessage());
                            handler.updateConfiguration(Map.of(MatterBindingConstants.CONFIG_DOORLOCK_ONE_TOUCH_LOCKING,
                                    String.valueOf(Objects.requireNonNullElse(currentValue, false))));
                            return Void.TYPE.cast(null);
                        });
            }
        }
    }

    private void updateConfigDescription() {
        List<ConfigDescriptionParameter> params = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        groups.add(
                ConfigDescriptionParameterGroupBuilder.create(MatterBindingConstants.CONFIG_GROUP_DOORLOCK_MANAGEMENT)
                        .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_MANAGEMENT))
                        .withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_MANAGEMENT))
                        .build());

        ConfigDescriptionParameterBuilder builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_DOORLOCK_OPERATING_MODE, Type.INTEGER)
                .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_OPERATING_MODE))
                .withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_OPERATING_MODE))
                .withGroupName(MatterBindingConstants.CONFIG_GROUP_DOORLOCK_MANAGEMENT)
                .withDefault(String.valueOf(OperatingModeEnum.NORMAL.getValue()))
                .withOptions(Arrays.stream(OperatingModeEnum.values())
                        .map(mode -> new ParameterOption(mode.getValue().toString(), mode.getLabel()))
                        .collect(Collectors.toList()));
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_DOORLOCK_AUTO_RELOCK_TIME, Type.INTEGER)
                .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_AUTO_RELOCK_TIME))
                .withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_AUTO_RELOCK_TIME))
                .withGroupName(MatterBindingConstants.CONFIG_GROUP_DOORLOCK_MANAGEMENT).withDefault("10")
                .withMinimum(BigDecimal.ZERO).withUnit("s");
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_DOORLOCK_ONE_TOUCH_LOCKING, Type.BOOLEAN)
                .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_ONE_TOUCH_LOCKING))
                .withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_ONE_TOUCH_LOCKING))
                .withGroupName(MatterBindingConstants.CONFIG_GROUP_DOORLOCK_MANAGEMENT).withDefault("false");
        params.add(builder.build());

        if (pinCredentialSupported) {
            builder = ConfigDescriptionParameterBuilder
                    .create(MatterBindingConstants.CONFIG_DOORLOCK_DEFAULT_LOCK_PIN, Type.TEXT)
                    .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_DEFAULT_LOCK_PIN))
                    .withDescription(
                            handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_DEFAULT_LOCK_PIN,
                                    minPinCodeLength, maxPinCodeLength))
                    .withGroupName(MatterBindingConstants.CONFIG_GROUP_DOORLOCK_MANAGEMENT).withDefault("")
                    .withPattern(buildPinCodePattern(minPinCodeLength, maxPinCodeLength)).withContext("password");
            params.add(builder.build());
        }

        // Add user groups (only if user feature is supported)
        if (userFeatureSupported && numberOfTotalUsersSupported > 0) {
            addUserGroupsToConfig(params, groups);
        }

        handler.addConfigDescription(ConfigDescriptionBuilder.create(handler.getConfigDescriptionURI())
                .withParameters(params).withParameterGroups(groups).build());
    }

    /**
     * Adds user configuration groups based on occupied users plus additional slots for new users. Shows all slots from
     * 1 to (maxOccupiedUserIndex + ADDITIONAL_USER_SLOTS), up to the max supported.
     */
    private void addUserGroupsToConfig(List<ConfigDescriptionParameter> params,
            List<ConfigDescriptionParameterGroup> groups) {
        int userGroupsToShow = calculateUserGroupsToShow();
        int ourFabricIndex = handler.getCurrentFabricIndex();

        for (int userIndex = 1; userIndex <= userGroupsToShow; userIndex++) {
            String groupName = MatterBindingConstants.CONFIG_GROUP_DOORLOCK_USER_PREFIX + userIndex;
            LockUser user = lockUsers.get(userIndex);
            boolean isOccupied = user != null && user.isOccupied();
            boolean isManagedByUs = user == null || user.isManagedByFabric(ourFabricIndex);

            String groupLabel = buildUserGroupLabel(userIndex, user, isManagedByUs);
            String groupDescription = isManagedByUs || !isOccupied
                    ? handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_USER)
                    : handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_EXTERNAL_FABRIC);

            groups.add(ConfigDescriptionParameterGroupBuilder.create(groupName).withLabel(groupLabel)
                    .withDescription(groupDescription).build());

            // User Name
            ConfigDescriptionParameterBuilder builder = ConfigDescriptionParameterBuilder
                    .create(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_NAME, Type.TEXT)
                    .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_USER_NAME))
                    .withDescription(
                            handler.getTranslation(isManagedByUs ? MatterBindingConstants.CONFIG_DESC_DOORLOCK_USER_NAME
                                    : MatterBindingConstants.CONFIG_DESC_DOORLOCK_EXTERNAL_FABRIC))
                    .withGroupName(groupName)
                    .withDefault(Objects.requireNonNullElse(user != null ? user.userName : null, ""))
                    .withReadOnly(!isManagedByUs);
            params.add(builder.build());

            if (isManagedByUs) {
                // User Type
                builder = ConfigDescriptionParameterBuilder
                        .create(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_TYPE, Type.INTEGER)
                        .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_USER_TYPE))
                        .withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_USER_TYPE))
                        .withGroupName(groupName).withDefault(String.valueOf(UserTypeEnum.UNRESTRICTED_USER.getValue()))
                        .withLimitToOptions(true)
                        .withOptions(Arrays.stream(UserTypeEnum.values())
                                .map(type -> new ParameterOption(type.getValue().toString(), type.getLabel()))
                                .collect(Collectors.toList()));
                params.add(builder.build());

                // PIN Credential
                if (pinCredentialSupported) {
                    builder = ConfigDescriptionParameterBuilder
                            .create(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_PIN_CREDENTIAL, Type.TEXT)
                            .withLabel(
                                    handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_PIN_CREDENTIAL,
                                            minPinCodeLength, maxPinCodeLength))
                            .withPattern(buildPinCodePattern(minPinCodeLength, maxPinCodeLength))
                            .withDescription(
                                    buildPinCodeDescription(MatterBindingConstants.CONFIG_DESC_DOORLOCK_PIN_CREDENTIAL))
                            .withGroupName(groupName).withDefault("").withContext("password");
                    params.add(builder.build());
                }
            }

            if (isOccupied) {
                // User Enabled
                boolean isEnabled = user != null && user.userStatus == UserStatusEnum.OCCUPIED_ENABLED;
                builder = ConfigDescriptionParameterBuilder
                        .create(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_ENABLED, Type.BOOLEAN)
                        .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_USER_ENABLED))
                        .withDescription(
                                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_USER_ENABLED))
                        .withGroupName(groupName).withDefault(String.valueOf(isEnabled));
                params.add(builder.build());

                // Delete User
                builder = ConfigDescriptionParameterBuilder
                        .create(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_DELETE_USER, Type.BOOLEAN)
                        .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_DELETE_USER))
                        .withDescription(
                                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_DOORLOCK_DELETE_USER))
                        .withGroupName(groupName).withDefault("false");
                params.add(builder.build());
            }
        }
    }

    private String buildUserGroupLabel(int userIndex, @Nullable LockUser user, boolean isManagedByUs) {
        String baseLabel = handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_USER) + " " + userIndex;
        if (user == null || !user.isOccupied()) {
            return baseLabel;
        }
        String userName = Objects.requireNonNullElse(user.userName, "");
        String userNamePart = userName.isEmpty()
                ? handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_NO_USER_NAME)
                : userName;
        if (!isManagedByUs) {
            return baseLabel + " (" + userNamePart + " - "
                    + handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_DOORLOCK_EXTERNAL_FABRIC) + ")";
        }
        return baseLabel + " (" + userNamePart + ")";
    }

    /**
     * Calculates how many user groups to show in the configuration.
     * Shows all slots from 1 to (maxOccupiedUserIndex + ADDITIONAL_USER_SLOTS) up
     * to at max supported.
     * 
     * @return The number of user groups to display
     */
    private int calculateUserGroupsToShow() {
        int maxOccupiedUserIndex = lockUsers.entrySet().stream().filter(e -> e.getValue().isOccupied())
                .mapToInt(Map.Entry::getKey).max().orElse(0);
        return Math.min(maxOccupiedUserIndex + ADDITIONAL_USER_SLOTS, numberOfTotalUsersSupported);
    }

    /**
     * Fetches all users from the lock starting at slot/index 1.
     */
    private void fetchAllUsers() {
        if (!userFeatureSupported || numberOfTotalUsersSupported == 0) {
            return;
        }
        if (!fetchingUsers.compareAndSet(false, true)) {
            logger.debug("User fetch already in progress, skipping");
            return;
        }
        fetchUserStartingAtIndex(1, new ConcurrentHashMap<>()).thenAccept(users -> {
            lockUsers.clear();
            lockUsers.putAll(users);
            logger.debug("User enumeration complete, found {} users", lockUsers.size());
        }).exceptionally(e -> {
            logger.debug("Error fetching users: {}", e.getMessage());
            return null;
        }).whenComplete((result, error) -> {
            updateConfigDescription();
            updateUserConfiguration();
            fetchingUsers.set(false);
        });
    }

    /**
     * Fetches users starting at a specific index, will skip empty slots and stop
     * when the last user is reached.
     *
     * @param userIndex the user index to start fetching from
     * @param users the map to accumulate users into
     * @return a CompletableFuture containing the map of all fetched users
     */
    private CompletableFuture<Map<Integer, LockUser>> fetchUserStartingAtIndex(int userIndex,
            Map<Integer, LockUser> users) {
        return getUser(userIndex).thenCompose(user -> {
            if (user != null) {
                if (user.isOccupied()) {
                    users.put(userIndex, user);
                }
                Integer nextUserIndex = user.nextUserIndex;
                // Use nextUserIndex from the response to jump to the next occupied user
                if (nextUserIndex != null) {
                    return fetchUserStartingAtIndex(nextUserIndex.intValue(), users);
                }
            }
            // No more users or null response - return accumulated users
            return CompletableFuture.completedFuture(users);
        });
    }

    private CompletableFuture<@Nullable LockUser> getUser(int userIndex) {
        return handler
                .sendClusterCommand(endpointNumber, DoorLockCluster.CLUSTER_NAME, DoorLockCluster.getUser(userIndex))
                .thenApply(result -> {
                    LockUser user = new LockUser(userIndex);
                    var jsonObject = result.getAsJsonObject();

                    if (jsonObject.has("userName") && !jsonObject.get("userName").isJsonNull()) {
                        user.userName = jsonObject.get("userName").getAsString();
                    }
                    if (jsonObject.has("userStatus") && !jsonObject.get("userStatus").isJsonNull()) {
                        int statusValue = jsonObject.get("userStatus").getAsInt();
                        for (UserStatusEnum status : UserStatusEnum.values()) {
                            if (status.getValue() == statusValue) {
                                user.userStatus = status;
                                break;
                            }
                        }
                    }
                    if (jsonObject.has("userType") && !jsonObject.get("userType").isJsonNull()) {
                        int typeValue = jsonObject.get("userType").getAsInt();
                        for (UserTypeEnum type : UserTypeEnum.values()) {
                            if (type.getValue() == typeValue) {
                                user.userType = type;
                                break;
                            }
                        }
                    }
                    if (jsonObject.has("credentials") && !jsonObject.get("credentials").isJsonNull()) {
                        for (var credElement : jsonObject.get("credentials").getAsJsonArray()) {
                            var credObj = credElement.getAsJsonObject();
                            int credTypeValue = credObj.get("credentialType").getAsInt();
                            int credIndex = credObj.get("credentialIndex").getAsInt();
                            for (CredentialTypeEnum credType : CredentialTypeEnum.values()) {
                                if (credType.getValue() == credTypeValue) {
                                    user.credentials.add(new CredentialStruct(credType, credIndex));
                                    break;
                                }
                            }
                        }
                    }
                    // nextUserIndex indicates the next occupied user slot to read from
                    if (jsonObject.has("nextUserIndex") && !jsonObject.get("nextUserIndex").isJsonNull()) {
                        user.nextUserIndex = jsonObject.get("nextUserIndex").getAsInt();
                    }
                    // Users can only be edited by their creator fabric; other fabrics can only
                    // delete/enable/disable
                    if (jsonObject.has("creatorFabricIndex") && !jsonObject.get("creatorFabricIndex").isJsonNull()) {
                        user.creatorFabricIndex = jsonObject.get("creatorFabricIndex").getAsInt();
                    }

                    return user;
                }).exceptionally(e -> {
                    logger.debug("Error getting user {}: {}", userIndex, e.getMessage());
                    return null;
                });
    }

    /**
     * Sets or creates a user on the lock.
     * For ADD operations all fields are sent with values.
     * For MODIFY operations only specified fields are updated, other fields are sent as null per Matter spec.
     *
     * @param userIndex The user index starting at 1
     * @param userName The user name (can be null for changes to foreign fabric users)
     * @param userType The user type (can be null for status-only updates)
     * @param userStatus The user status (can be null to preserve existing status on modify)
     */
    private CompletableFuture<Void> setUser(int userIndex, @Nullable String userName, @Nullable UserTypeEnum userType,
            @Nullable UserStatusEnum userStatus) {
        LockUser existingUser = lockUsers.get(userIndex);
        boolean isModify = existingUser != null && existingUser.isOccupied();
        DataOperationTypeEnum operationType = isModify ? DataOperationTypeEnum.MODIFY : DataOperationTypeEnum.ADD;

        logger.debug("setUser called: userIndex={}, userName='{}', userType={}, userStatus={}, operationType={}",
                userIndex, userName, userType, userStatus, operationType);

        ClusterCommand command;
        if (isModify) {
            command = DoorLockCluster.setUser(operationType, userIndex, userName, null, userStatus, userType, null);
            // Explicitly add null values to args map so they're serialized, normally null
            // values are omitted
            command.args.put("userUniqueId", null);
            if (userStatus == null) {
                command.args.put("userStatus", null);
            }
            if (userType == null) {
                command.args.put("userType", null);
            }
            if (userName == null) {
                command.args.put("userName", null);
            }
            command.args.put("credentialRule", null);
        } else {
            UserStatusEnum status = userStatus != null ? userStatus : UserStatusEnum.OCCUPIED_ENABLED;
            UserTypeEnum type = userType != null ? userType : UserTypeEnum.UNRESTRICTED_USER;
            String name = userName != null ? userName : "User " + userIndex;
            command = DoorLockCluster.setUser(operationType, userIndex, name, 0, status, type,
                    DoorLockCluster.CredentialRuleEnum.SINGLE);
        }

        logger.debug("Sending setUser command: {}", command.args);
        return handler.sendClusterCommand(endpointNumber, DoorLockCluster.CLUSTER_NAME, command)
                .<Void> thenApply(result -> Void.TYPE.cast(null));
    }

    private CompletableFuture<Void> deleteUser(int userIndex) {
        return handler
                .sendClusterCommand(endpointNumber, DoorLockCluster.CLUSTER_NAME, DoorLockCluster.clearUser(userIndex))
                .<Void> thenApply(result -> Void.TYPE.cast(null));
    }

    private CompletableFuture<Void> setPinCredential(int userIndex, String pinCode) {
        LockUser existingUser = lockUsers.get(userIndex);

        // Check if user already has a pin credential
        if (existingUser != null && existingUser.hasCredentialOfType(CredentialTypeEnum.PIN)) {
            // Find the existing pin credential index (which is different from the user
            // index)
            Integer existingCredentialIndex = existingUser.credentials.stream()
                    .filter(c -> c.credentialType == CredentialTypeEnum.PIN).findFirst().map(c -> c.credentialIndex)
                    .orElse(null);

            if (existingCredentialIndex != null) {
                logger.debug("User {} has existing PIN credential at index {}, modifying", userIndex,
                        existingCredentialIndex);
                return setCredentialWithIndex(userIndex, pinCode, existingCredentialIndex,
                        DataOperationTypeEnum.MODIFY);
            }
        }

        logger.debug("Finding available credential slot for user {}", userIndex);
        return findAvailableCredentialSlot(1).thenCompose(availableIndex -> {
            logger.debug("Found available credential slot {} for user {}", availableIndex, userIndex);
            return setCredentialWithIndex(userIndex, pinCode, availableIndex, DataOperationTypeEnum.ADD);
        });
    }

    /**
     * Finds an available credential slot by iterating through credential indices.
     */
    private CompletableFuture<Integer> findAvailableCredentialSlot(int startIndex) {
        CredentialStruct credential = new CredentialStruct(CredentialTypeEnum.PIN, startIndex);
        return handler.sendClusterCommand(endpointNumber, DoorLockCluster.CLUSTER_NAME,
                DoorLockCluster.getCredentialStatus(credential)).thenCompose(result -> {
                    var jsonObject = result.getAsJsonObject();
                    boolean credentialExists = jsonObject.has("credentialExists")
                            && jsonObject.get("credentialExists").getAsBoolean();

                    if (!credentialExists) {
                        logger.debug("Credential slot {} is available", startIndex);
                        return CompletableFuture.completedFuture(startIndex);
                    }

                    // Slot is occupied, check nextCredentialIndex or try next slot
                    Integer nextIndex = null;
                    if (jsonObject.has("nextCredentialIndex") && !jsonObject.get("nextCredentialIndex").isJsonNull()) {
                        nextIndex = jsonObject.get("nextCredentialIndex").getAsInt();
                    }

                    if (nextIndex != null && nextIndex > startIndex) {
                        logger.debug("Credential slot {} occupied, nextCredentialIndex suggests {}", startIndex,
                                nextIndex);
                        return findAvailableCredentialSlot(nextIndex);
                    } else {
                        // Try the next sequential slot
                        logger.debug("Credential slot {} occupied, trying next slot", startIndex);
                        return findAvailableCredentialSlot(startIndex + 1);
                    }
                });
    }

    private CompletableFuture<Void> setCredentialWithIndex(int userIndex, String pinCode, int credentialIndex,
            DataOperationTypeEnum operationType) {
        CredentialStruct credential = new CredentialStruct(CredentialTypeEnum.PIN, credentialIndex);
        BaseCluster.OctetString pinData = new BaseCluster.OctetString(pinCode, StandardCharsets.UTF_8);
        ClusterCommand command = DoorLockCluster.setCredential(operationType, credential, pinData, userIndex, null,
                null);
        // This is a workaround to send null values vs omitting values.
        // TODO: Refactor command construction to use a builder pattern when matter.js 0.16 comes out.
        command.args.put("userStatus", null);
        command.args.put("userType", null);
        logger.debug("Setting credential at index {}: {}", credentialIndex, command.args);
        return handler.sendClusterCommand(endpointNumber, DoorLockCluster.CLUSTER_NAME, command)
                .<Void> thenApply(result -> Void.TYPE.cast(null));
    }

    /**
     * Updates configuration with current user data from the lock.
     */
    private void updateUserConfiguration() {
        Map<String, Object> entries = new HashMap<>();
        int userGroupsToShow = calculateUserGroupsToShow();

        for (int userIndex = 1; userIndex <= userGroupsToShow; userIndex++) {
            String groupName = MatterBindingConstants.CONFIG_GROUP_DOORLOCK_USER_PREFIX + userIndex;
            LockUser user = lockUsers.get(userIndex);
            if (user != null && user.isOccupied()) {
                UserTypeEnum userType = user.userType;
                entries.put(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_NAME,
                        Objects.requireNonNullElse(user.userName, ""));
                entries.put(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_TYPE, String
                        .valueOf(userType != null ? userType.getValue() : UserTypeEnum.UNRESTRICTED_USER.getValue()));
                boolean isEnabled = user.userStatus == UserStatusEnum.OCCUPIED_ENABLED;
                entries.put(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_ENABLED, isEnabled);
            } else {
                entries.put(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_NAME, "");
                entries.put(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_TYPE,
                        String.valueOf(UserTypeEnum.UNRESTRICTED_USER.getValue()));
            }
            entries.put(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_DELETE_USER, false);
        }

        if (!entries.isEmpty()) {
            handler.updateConfiguration(entries);
        }
    }

    /**
     * Processes configuration for a single user.
     */
    private void processUserConfiguration(Configuration config, int userIndex) {
        String groupName = MatterBindingConstants.CONFIG_GROUP_DOORLOCK_USER_PREFIX + userIndex;
        LockUser currentUser = lockUsers.get(userIndex);
        boolean userExistsOnLock = currentUser != null && currentUser.isOccupied();

        Object deleteValue = config.get(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_DELETE_USER);
        if (deleteValue instanceof Boolean delete && delete) {
            logger.debug("Delete flag enabled for user {}, deleting user", userIndex);
            // Reset the delete flag immediately
            Map<String, Object> resetFlag = new HashMap<>();
            resetFlag.put(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_DELETE_USER, false);
            handler.updateConfiguration(resetFlag);

            deleteUser(userIndex).thenRun(() -> {
                logger.debug("User {} deleted, refreshing users from lock", userIndex);
                fetchAllUsers();
            }).exceptionally(e -> {
                logger.warn("Failed to delete user {}: {}", userIndex, e.getMessage());
                return null;
            });
            return; // Don't process other updates if deleting
        }

        int ourFabricIndex = handler.getCurrentFabricIndex();
        Object userEnabledValue = config.get(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_ENABLED);
        if (userExistsOnLock && currentUser != null && userEnabledValue instanceof Boolean enabled) {
            UserStatusEnum currentStatus = currentUser.userStatus;
            UserStatusEnum desiredStatus = enabled ? UserStatusEnum.OCCUPIED_ENABLED : UserStatusEnum.OCCUPIED_DISABLED;

            if (currentStatus != desiredStatus) {
                logger.debug("User {} enabled status changed from {} to {}", userIndex, currentStatus, desiredStatus);
                // For users managed by our fabric, we MUST include userName per the Matter spec
                // For external fabric users, we MUST pass null for userName
                String userName = currentUser.isManagedByFabric(ourFabricIndex) ? currentUser.userName : null;
                setUser(userIndex, userName, null, desiredStatus).thenRun(() -> {
                    logger.debug("User {} status updated, refreshing users from lock", userIndex);
                    fetchAllUsers();
                }).exceptionally(e -> {
                    logger.warn("Failed to update user {} status: {}", userIndex, e.getMessage());
                    fetchAllUsers();
                    return null;
                });
                return; // Don't process other updates when changing status
            }
        }
        if (userExistsOnLock && currentUser != null && !currentUser.isManagedByFabric(ourFabricIndex)) {
            logger.debug("User {} is managed by fabric {}, our fabric is {}. Only status changes allowed.", userIndex,
                    currentUser.creatorFabricIndex, ourFabricIndex);
            return;
        }

        Object userNameValue = config.get(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_NAME);
        Object userTypeValue = config.get(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_USER_TYPE);
        Object pinValue = config.get(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_PIN_CREDENTIAL);

        String configuredUserName = userNameValue instanceof String ? (String) userNameValue : "";
        UserTypeEnum configuredUserType = UserTypeEnum.UNRESTRICTED_USER;
        if (userTypeValue instanceof Number number) {
            int typeInt = number.intValue();
            for (UserTypeEnum type : UserTypeEnum.values()) {
                if (type.getValue() == typeInt) {
                    configuredUserType = type;
                    break;
                }
            }
        }
        String configuredPin = pinValue instanceof String ? (String) pinValue : "";
        // Check if pin is provided - this takes priority as it can create a user
        if (pinCredentialSupported && !configuredPin.isEmpty()) {
            // Clear the pin from config since we can not retrieve it from the lock once set
            handler.updateConfiguration(
                    Map.of(groupName + "_" + MatterBindingConstants.CONFIG_DOORLOCK_PIN_CREDENTIAL, ""));

            // If user doesn't exist, create them first, then set pin
            if (!userExistsOnLock) {
                String userName = configuredUserName.isEmpty() ? "User " + userIndex : configuredUserName;
                final UserTypeEnum finalUserType = configuredUserType;
                setUser(userIndex, userName, finalUserType, null).thenCompose(v -> {
                    logger.debug("User {} created, now setting PIN", userIndex);
                    return setPinCredential(userIndex, configuredPin);
                }).thenRun(() -> {
                    logger.debug("PIN set for user {}, refreshing users from lock", userIndex);
                    fetchAllUsers();
                }).exceptionally(e -> {
                    logger.warn("Failed to create user {} with PIN: {}", userIndex, e.getMessage());
                    fetchAllUsers();
                    return null;
                });
            } else {
                setPinCredential(userIndex, configuredPin).thenRun(() -> {
                    logger.debug("PIN set for user {}, refreshing users from lock", userIndex);
                    fetchAllUsers();
                }).exceptionally(e -> {
                    logger.warn("Failed to set PIN for user {}: {}", userIndex, e.getMessage());
                    return null;
                });
            }
            return;
        } else {
            logger.warn("PIN credentials not supported or No PIN provided for user at index {}, skipping PIN set",
                    userIndex);
        }

        // Check if user name/type changed
        if (!configuredUserName.isEmpty()) {
            String currentUserName = currentUser != null ? currentUser.userName : null;
            boolean nameChanged = !userExistsOnLock || !configuredUserName.equals(currentUserName);
            boolean typeChanged = userExistsOnLock && currentUser != null && currentUser.userType != null
                    && currentUser.userType != configuredUserType;
            logger.debug(
                    "User {} config check: configuredName='{}', currentName='{}', userExists={}, nameChanged={}, typeChanged={}",
                    userIndex, configuredUserName, currentUserName, userExistsOnLock, nameChanged, typeChanged);
            if (nameChanged || typeChanged) {
                final UserTypeEnum finalUserType = configuredUserType;
                logger.debug("Updating user {} with name='{}', type={}", userIndex, configuredUserName, finalUserType);
                setUser(userIndex, configuredUserName, finalUserType, null).thenRun(() -> {
                    logger.debug("User {} updated, refreshing users from lock", userIndex);
                    fetchAllUsers();
                }).exceptionally(e -> {
                    logger.warn("Failed to update user {}: {}", userIndex, e.getMessage());
                    fetchAllUsers();
                    return null;
                });
            }
        }
    }

    private String buildPinCodePattern(int minLength, int maxLength) {
        if (minLength > 0 && maxLength > 0) {
            return "^[0-9]{" + minLength + "," + maxLength + "}$";
        } else if (minLength > 0) {
            return "^[0-9]{" + minLength + ",}$";
        } else if (maxLength > 0) {
            return "^[0-9]{0," + maxLength + "}$";
        } else {
            return "^[0-9]*$";
        }
    }

    private String buildPinCodeDescription(String descriptionKey) {
        return handler.getTranslation(descriptionKey, minPinCodeLength, maxPinCodeLength);
    }
}
