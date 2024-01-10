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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLockDTO;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLockDTO.DoorLockVolumeSettings;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLocksDTO;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLocksDTO.Doorlock;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Handler for the Smart Lock Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLockThingHandler extends VerisureThingHandler<VerisureSmartLocksDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SMARTLOCK);

    private static final int REFRESH_DELAY_SECONDS = 10;

    public VerisureSmartLockThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
            return;
        } else if (channelUID.getId().equals(CHANNEL_SMARTLOCK_STATUS)) {
            handleSmartLockState(command);
        } else if (channelUID.getId().equals(CHANNEL_AUTO_RELOCK)) {
            handleAutoRelock(command);
        } else if (channelUID.getId().equals(CHANNEL_SMARTLOCK_VOLUME)) {
            handleSmartLockVolumeAndVoiceLevel(command, true);
        } else if (channelUID.getId().equals(CHANNEL_SMARTLOCK_VOICE_LEVEL)) {
            handleSmartLockVolumeAndVoiceLevel(command, false);
        } else {
            logger.warn("Unknown command! {}", command);
            return;
        }
        scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
    }

    private void handleSmartLockState(Command command) {
        String deviceId = config.getDeviceId();
        VerisureSession session = getSession();
        if (session != null) {
            VerisureSmartLocksDTO smartLock = session.getVerisureThing(deviceId, getVerisureThingClass());
            if (smartLock != null) {
                BigDecimal installationId = smartLock.getSiteId();
                String pinCode = session.getPinCode(installationId);
                if (pinCode != null) {
                    String url = START_GRAPHQL;
                    String operation;
                    if (command == OnOffType.OFF) {
                        operation = "DoorUnlock";
                    } else if (command == OnOffType.ON) {
                        operation = "DoorLock";
                    } else {
                        logger.debug("Unknown command! {}", command);
                        return;
                    }

                    ArrayList<SmartLockDTO> list = new ArrayList<>();
                    SmartLockDTO smartLockJSON = new SmartLockDTO();
                    VariablesDTO variables = new VariablesDTO();
                    InputDTO input = new InputDTO();

                    variables.setDeviceLabel(deviceId);
                    variables.setGiid(installationId.toString());
                    input.setCode(pinCode);
                    variables.setInput(input);
                    smartLockJSON.setOperationName(operation);
                    smartLockJSON.setVariables(variables);
                    String query = "mutation " + operation
                            + "($giid: String!, $deviceLabel: String!, $input: LockDoorInput!) {\n  " + operation
                            + "(giid: $giid, deviceLabel: $deviceLabel, input: $input)\n}\n";
                    smartLockJSON.setQuery(query);
                    list.add(smartLockJSON);

                    String queryQLSmartLockSetState = gson.toJson(list);
                    logger.debug("Trying to set SmartLock state to {} with URL {} and data {}", operation, url,
                            queryQLSmartLockSetState);

                    int httpResultCode = session.sendCommand(url, queryQLSmartLockSetState, installationId);
                    if (httpResultCode == HttpStatus.OK_200) {
                        logger.debug("SmartLock status successfully changed!");
                    } else {
                        logger.warn("Failed to send command, HTTP result code {}", httpResultCode);
                    }
                } else {
                    logger.warn("PIN code is not configured! It is mandatory to control SmartLock!");
                }
            }
        }
    }

    private void handeAutoRelockResult(String url, String data, BigDecimal installationId, Command command) {
        logger.debug("Trying to set Auto Relock {} with URL {} and data {}", command.toString(), url, data);
        VerisureSession session = getSession();
        if (session != null) {
            int httpResultCode = session.sendCommand(url, data, installationId);
            if (httpResultCode == HttpStatus.OK_200) {
                logger.debug("AutoRelock successfully changed to {}", command.toString());
            } else {
                logger.warn("Failed to send command, HTTP result code {}", httpResultCode);
            }
        }
    }

    private void handleAutoRelock(Command command) {
        String deviceId = config.getDeviceId();
        VerisureSession session = getSession();
        if (session != null) {
            VerisureSmartLocksDTO smartLock = session.getVerisureThing(deviceId, getVerisureThingClass());
            if (smartLock != null) {
                BigDecimal installationId = smartLock.getSiteId();
                try {
                    String csrf = session.getCsrfToken(installationId);
                    StringBuilder sb = new StringBuilder(deviceId);
                    sb.insert(4, "+");
                    String data;
                    String url = SMARTLOCK_AUTORELOCK_COMMAND;
                    if (command == OnOffType.ON) {
                        data = "enabledDoorLocks=" + sb.toString()
                                + "&doorLockDevices%5B0%5D.autoRelockEnabled=true&_doorLockDevices%5B0%5D.autoRelockEnabled=on&_csrf="
                                + csrf;
                        handeAutoRelockResult(url, data, installationId, command);
                    } else if (command == OnOffType.OFF) {
                        data = "enabledDoorLocks=&doorLockDevices%5B0%5D.autoRelockEnabled=true&_doorLockDevices%5B0%5D.autoRelockEnabled=on&_csrf="
                                + csrf;
                        handeAutoRelockResult(url, data, installationId, command);
                    } else {
                        logger.warn("Unknown command! {}", command);
                    }
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    logger.debug("Failed to handle auto-relock {}", e.getMessage());
                }
            }
        }
    }

    private boolean isSettingAllowed(List<String> allowedSettings, String setting) {
        return allowedSettings.contains(setting);
    }

    private void handleSmartLockVolumeAndVoiceLevel(Command command, boolean setVolume) {
        String deviceId = config.getDeviceId();
        VerisureSession session = getSession();
        if (session != null) {
            VerisureSmartLocksDTO smartLocks = session.getVerisureThing(deviceId, getVerisureThingClass());
            if (smartLocks != null) {
                VerisureSmartLockDTO smartLock = smartLocks.getSmartLockJSON();
                if (smartLock != null) {
                    DoorLockVolumeSettings volumeSettings = smartLock.getDoorLockVolumeSettings();
                    String volume;
                    String voiceLevel;
                    if (setVolume) {
                        List<String> availableVolumes = volumeSettings.getAvailableVolumes();
                        if (isSettingAllowed(availableVolumes, command.toString())) {
                            volume = command.toString();
                            voiceLevel = volumeSettings.getVoiceLevel();
                        } else {
                            logger.warn("Failed to change volume, setting not allowed {}", command.toString());
                            return;
                        }
                    } else {
                        List<String> availableVoiceLevels = volumeSettings.getAvailableVoiceLevels();
                        if (isSettingAllowed(availableVoiceLevels, command.toString())) {
                            volume = volumeSettings.getVolume();
                            voiceLevel = command.toString();
                        } else {
                            logger.warn("Failed to change voice level, setting not allowed {}", command.toString());
                            return;
                        }
                        BigDecimal installationId = smartLocks.getSiteId();
                        try {
                            String csrf = session.getCsrfToken(installationId);
                            String url = SMARTLOCK_VOLUME_COMMAND;
                            String data = "keypad.volume=MEDIUM&keypad.beepOnKeypress=true&_keypad.beepOnKeypress=on&siren.volume=MEDIUM&voiceDevice.volume=MEDIUM&doorLock.volume="
                                    + volume + "&doorLock.voiceLevel=" + voiceLevel
                                    + "&_devices%5B0%5D.on=on&devices%5B1%5D.on=true&_devices%5B1%5D.on=on&devices%5B2%5D.on=true&_devices%5B2%5D.on=on&_devices%5B3%5D.on=on&_keypad.keypadsPlayChime=on&_siren.sirensPlayChime=on&_csrf="
                                    + csrf;
                            logger.debug("Trying to set SmartLock volume with URL {} and data {}", url, data);
                            int httpResultCode = session.sendCommand(url, data, installationId);
                            if (httpResultCode == HttpStatus.OK_200) {
                                logger.debug("SmartLock volume successfully changed!");
                            } else {
                                logger.warn("Failed to send command, HTTP result code {}", httpResultCode);
                            }
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            logger.warn("Failed to get CSRF token {}", e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public Class<VerisureSmartLocksDTO> getVerisureThingClass() {
        return VerisureSmartLocksDTO.class;
    }

    @Override
    public synchronized void update(VerisureSmartLocksDTO thing) {
        updateSmartLockState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateSmartLockState(VerisureSmartLocksDTO smartLocksJSON) {
        List<Doorlock> doorLockList = smartLocksJSON.getData().getInstallation().getDoorlocks();
        if (!doorLockList.isEmpty()) {
            Doorlock doorlock = doorLockList.get(0);
            String smartLockStatus = doorlock.getCurrentLockState();
            VerisureSmartLockDTO smartLockJSON = smartLocksJSON.getSmartLockJSON();
            if (smartLockStatus != null) {
                getThing().getChannels().stream().map(Channel::getUID)
                        .filter(channelUID -> isLinked(channelUID) && !"timestamp".equals(channelUID.getId()))
                        .forEach(channelUID -> {
                            State state = getValue(channelUID.getId(), doorlock, smartLockStatus, smartLockJSON);
                            updateState(channelUID, state);
                        });
                updateTimeStamp(doorlock.getEventTime());
                updateInstallationChannels(smartLocksJSON);
            } else {
                logger.debug("Smart lock status {} or smartLockJSON {} is null!", smartLockStatus, smartLockJSON);
            }
        } else {
            logger.debug("DoorLock list is empty!");
        }
    }

    public State getValue(String channelId, Doorlock doorlock, String smartLockStatus,
            @Nullable VerisureSmartLockDTO smartLockJSON) {
        switch (channelId) {
            case CHANNEL_SMARTLOCK_STATUS:
                if ("LOCKED".equals(smartLockStatus)) {
                    return OnOffType.ON;
                } else if ("UNLOCKED".equals(smartLockStatus)) {
                    return OnOffType.OFF;
                } else if ("PENDING".equals(smartLockStatus)) {
                    // Schedule another refresh.
                    logger.debug("Issuing another immediate refresh since status is still PENDING ...");
                    this.scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
                }
                break;
            case CHANNEL_CHANGED_BY_USER:
                String user = doorlock.getUserString();
                return user != null ? new StringType(user) : UnDefType.NULL;
            case CHANNEL_CHANGED_VIA:
                String method = doorlock.getMethod();
                return method != null ? new StringType(method) : UnDefType.NULL;
            case CHANNEL_MOTOR_JAM:
                return OnOffType.from(doorlock.isMotorJam());
            case CHANNEL_LOCATION:
                String location = doorlock.getDevice().getArea();
                return location != null ? new StringType(location) : UnDefType.NULL;
            case CHANNEL_AUTO_RELOCK:
                if (smartLockJSON != null) {
                    return OnOffType.from(smartLockJSON.getAutoRelockEnabled());
                } else {
                    return UnDefType.NULL;
                }
            case CHANNEL_SMARTLOCK_VOLUME:
                if (smartLockJSON != null) {
                    return new StringType(smartLockJSON.getDoorLockVolumeSettings().getVolume());
                } else {
                    return UnDefType.NULL;
                }
            case CHANNEL_SMARTLOCK_VOICE_LEVEL:
                if (smartLockJSON != null) {
                    return new StringType(smartLockJSON.getDoorLockVolumeSettings().getVoiceLevel());
                } else {
                    return UnDefType.NULL;
                }
        }
        return UnDefType.UNDEF;
    }

    private static class SmartLockDTO {

        @SuppressWarnings("unused")
        private @Nullable String operationName;
        @SuppressWarnings("unused")
        private VariablesDTO variables = new VariablesDTO();
        @SuppressWarnings("unused")
        private @Nullable String query;

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public void setVariables(VariablesDTO variables) {
            this.variables = variables;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    private static class VariablesDTO {

        @SuppressWarnings("unused")
        private @Nullable String giid;
        @SuppressWarnings("unused")
        private @Nullable String deviceLabel;
        @SuppressWarnings("unused")
        private InputDTO input = new InputDTO();

        public void setGiid(String giid) {
            this.giid = giid;
        }

        public void setDeviceLabel(String deviceLabel) {
            this.deviceLabel = deviceLabel;
        }

        public void setInput(InputDTO input) {
            this.input = input;
        }
    }

    private static class InputDTO {

        @SuppressWarnings("unused")
        private @Nullable String code;

        public void setCode(String code) {
            this.code = code;
        }
    }

    @Override
    public void updateTriggerChannel(String event) {
        logger.debug("SmartLockThingHandler trigger event {}", event);
        triggerChannel(CHANNEL_SMARTLOCK_TRIGGER_CHANNEL, event);
    }
}
