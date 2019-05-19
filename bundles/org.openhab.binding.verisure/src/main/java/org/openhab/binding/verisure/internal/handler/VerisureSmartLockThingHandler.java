/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.model.VerisureSmartLockJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartLockJSON.DoorLockVolumeSettings;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

//import com.google.common.collect.Sets;

/**
 * Handler for the Smart Lock Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLockThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_SMARTLOCK);
    }

    public VerisureSmartLockThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (channelUID.getId().equals(CHANNEL_SET_SMARTLOCK_STATUS)) {
            handleSmartLockState(command);
            scheduleImmediateRefresh();
        } else if (channelUID.getId().equals(CHANNEL_SET_AUTO_RELOCK)) {
            handleAutoRelock(command);
            scheduleImmediateRefresh();
        } else if (channelUID.getId().equals(CHANNEL_SET_SMARTLOCK_VOLUME)) {
            handleSmartLockVolume(command);
            scheduleImmediateRefresh();
        } else if (channelUID.getId().equals(CHANNEL_SET_SMARTLOCK_VOICE_LEVEL)) {
            handleSmartLockVoiceLevel(command);
            scheduleImmediateRefresh();
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    private void handleSmartLockState(Command command) {
        if (session != null && config.deviceId != null) {
            VerisureSmartLockJSON smartLock = (VerisureSmartLockJSON) session
                    .getVerisureThing(config.deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
			if (smartLock != null) {
				BigDecimal pinCode = session.getPinCode();
				BigDecimal installationId = smartLock.getSiteId();
				String deviceId = config.deviceId;
				if (deviceId != null && pinCode != null && installationId != null) {
					StringBuilder sb = new StringBuilder(deviceId);
					sb.insert(4, " ");
					String url = START_GRAPHQL;
					String queryQLSmartLockSetState;

					if (command == OnOffType.OFF) {
						queryQLSmartLockSetState = "[{\"operationName\":\"DoorUnlock\",\"variables\":{\"giid\":\"" + installationId + "\",\"deviceLabel\":\"" + deviceId + "\",\"input\":{\"code\":\"" + pinCode + "\"}},\"query\":\"mutation DoorUnlock($giid: String!, $deviceLabel: String!, $input: LockDoorInput!) {\\n  DoorUnlock(giid: $giid, deviceLabel: $deviceLabel, input: $input)\\n}\\n\"}]";
						logger.debug("Trying to set SmartLock state to unlocked with URL {} and data {}", url,
								queryQLSmartLockSetState);
					} else if (command == OnOffType.ON) {
						queryQLSmartLockSetState = "[{\"operationName\":\"DoorLock\",\"variables\":{\"giid\":\"" + installationId + "\",\"deviceLabel\":\"" + deviceId + "\",\"input\":{\"code\":\"" + pinCode + "\"}},\"query\":\"mutation DoorLock($giid: String!, $deviceLabel: String!, $input: LockDoorInput!) {\\n  DoorLock(giid: $giid, deviceLabel: $deviceLabel, input: $input)\\n}\\n\"}]";
						logger.debug("Trying to set SmartLock state to locked with URL {} and data {}", url,
								queryQLSmartLockSetState);
					} else {
						logger.debug("Unknown command! {}", command);
						return;
					}
					session.sendCommand2(START_GRAPHQL, queryQLSmartLockSetState);
					ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
					updateState(cuid, new StringType("pending"));
				}  else if (pinCode == null) {
                    logger.warn("PIN code is not configured! It is andatory to control SmartLock!");
                }
			}
		}      
    }

    private void handleAutoRelock(Command command) {
        if (session != null && config.deviceId != null) {
            VerisureSmartLockJSON smartLock = (VerisureSmartLockJSON) session
                    .getVerisureThing(config.deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
            if (smartLock != null) {
            	BigDecimal installationId = smartLock.getSiteId();
                String deviceId = config.deviceId;
                if (installationId != null && deviceId != null) {
                	String csrf = session.getCsrfToken(installationId);
                	StringBuilder sb = new StringBuilder(deviceId);
					sb.insert(4, "+");
                    String data;
                    String url = SMARTLOCK_AUTORELOCK_COMMAND;
                    if (command == OnOffType.ON) {
                    	data = "enabledDoorLocks=" + sb.toString() + "&doorLockDevices%5B0%5D.autoRelockEnabled=true&_doorLockDevices%5B0%5D.autoRelockEnabled=on&_csrf=" + csrf;
                        logger.debug("Trying to set Auto Relock ON with URL {} and data {}", url, data);
                        session.sendCommand2(url, data);
                        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_AUTO_RELOCK_ENABLED);
                        updateState(cuid, new StringType("true"));
                        smartLock.setAutoRelockEnabled(true);
                    } else if (command == OnOffType.OFF) {
                    	data = "enabledDoorLocks=&doorLockDevices%5B0%5D.autoRelockEnabled=true&_doorLockDevices%5B0%5D.autoRelockEnabled=on&_csrf=" + csrf;
                        logger.debug("Trying to set Auto Relock OFF with URL {} and data {}", url, data);
                        session.sendCommand2(url, data);
                        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_AUTO_RELOCK_ENABLED);
                        updateState(cuid, new StringType("false"));
                        smartLock.setAutoRelockEnabled(false);
                    } else {
                        logger.debug("Unknown command! {}", command);
                    }
                }
            }
        }
    }

    private void handleSmartLockVolume(Command command) {
        if (session != null && config.deviceId != null) {
            VerisureSmartLockJSON smartLock = (VerisureSmartLockJSON) session
                    .getVerisureThing(config.deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
            if (smartLock != null) {
                DoorLockVolumeSettings settings = smartLock.getDoorLockVolumeSettings();
                if (settings != null) {
                    List<String> volumeSettings = settings.getAvailableVolumes();
                    if (volumeSettings != null) {
                        Boolean isVolumeSettingAllowed = Boolean.FALSE;
                        for (String volume : volumeSettings) {
                            if (volume.equals(command.toString())) {
                                isVolumeSettingAllowed = Boolean.TRUE;
                                break;
                            }
                        }
                        BigDecimal installationId = smartLock.getSiteId();
                        String csrf = session.getCsrfToken(installationId);
                        if (isVolumeSettingAllowed && installationId != null && csrf != null) {
                            String url = SMARTLOCK_VOLUME_COMMAND;
							String data = "keypad.volume=MEDIUM&keypad.beepOnKeypress=true&_keypad.beepOnKeypress=on&siren.volume=MEDIUM&voiceDevice.volume=MEDIUM&doorLock.volume="
									+ command.toString() + "&doorLock.voiceLevel="
									+ smartLock.getDoorLockVolumeSettings().getVoiceLevel()
									+ "&_devices%5B0%5D.on=on&devices%5B1%5D.on=true&_devices%5B1%5D.on=on&devices%5B2%5D.on=true&_devices%5B2%5D.on=on&_devices%5B3%5D.on=on&_keypad.keypadsPlayChime=on&_siren.sirensPlayChime=on&_csrf="
									+ csrf;
                            logger.debug("Trying to set SmartLock volume with URL {} and data {}", url, data);
                            session.sendCommand2(url, data);
                            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTLOCK_VOLUME);
                            updateState(cuid, new StringType(command.toString()));
                            settings.setVolume(command.toString());
                        } else {
                            logger.debug("Unknown command! {}", command);
                        }
                    }
                }
            }
        }
    }

	private void handleSmartLockVoiceLevel(Command command) {
		if (session != null && config.deviceId != null) {
			VerisureSmartLockJSON smartLock = (VerisureSmartLockJSON) session
					.getVerisureThing(config.deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
			if (smartLock != null) {
				DoorLockVolumeSettings settings = smartLock.getDoorLockVolumeSettings();
				if (settings != null) {
					List<String> volumeSettings = settings.getAvailableVolumes();
					if (volumeSettings != null) {
						Boolean isVolumeSettingAllowed = Boolean.FALSE;
						for (String volume : volumeSettings) {
							if (volume.equals(command.toString())) {
								isVolumeSettingAllowed = Boolean.TRUE;
								break;
							}
						}
						BigDecimal installationId = smartLock.getSiteId();
						String csrf = session.getCsrfToken(installationId);
						if (isVolumeSettingAllowed && installationId != null && csrf != null) {
							String url = SMARTLOCK_VOLUME_COMMAND;
							String data = "keypad.volume=MEDIUM&keypad.beepOnKeypress=true&_keypad.beepOnKeypress=on&siren.volume=MEDIUM&voiceDevice.volume=MEDIUM&doorLock.volume="
									+ smartLock.getDoorLockVolumeSettings().getVolume() + "&doorLock.voiceLevel="
									+ command.toString()
									+ "&_devices%5B0%5D.on=on&devices%5B1%5D.on=true&_devices%5B1%5D.on=on&devices%5B2%5D.on=true&_devices%5B2%5D.on=on&_devices%5B3%5D.on=on&_keypad.keypadsPlayChime=on&_siren.sirensPlayChime=on&_csrf="
									+ csrf;
							logger.debug("Trying to set SmartLock voice level with URL {} and data {}", url, data);
							session.sendCommand2(url, data);
							ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTLOCK_VOICE_LEVEL);
							updateState(cuid, new StringType(command.toString()));
							settings.setVoiceLevel(command.toString());
						} else {
							logger.debug("Unknown command! {}", command);
						}
					}
				}
			}
		}
	}

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_SMARTLOCK)) {
            VerisureSmartLockJSON obj = (VerisureSmartLockJSON) thing;
            if (obj != null) {
                updateSmartLockState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateSmartLockState(VerisureSmartLockJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
        String smartLockStatus = status.getStatus();
        if (smartLockStatus != null) {
            updateState(cuid, new StringType(smartLockStatus));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SET_SMARTLOCK_STATUS);
            if ("locked".equals(smartLockStatus)) {
                updateState(cuid, OnOffType.ON);
            } else if ("unlocked".equals(smartLockStatus)) {
                updateState(cuid, OnOffType.OFF);
            } else if ("pending".equals(smartLockStatus)) {
                // Schedule another refresh.
                this.scheduleImmediateRefresh();
            }
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_NUMERIC_STATUS);
            DecimalType val = new DecimalType(0);
            if (smartLockStatus.equals("locked")) {
                val = new DecimalType(1);
            } else {
                val = new DecimalType(0);
            }
            updateState(cuid, val);
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTLOCK_STATUS);
            updateState(cuid, new StringType(status.getLabel()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_CHANGED_BY_USER);
            updateState(cuid, new StringType(status.getName()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_LASTUPDATE);
            updateState(cuid, new StringType(status.getDate()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
            updateState(cuid, new StringType(status.getLocation()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SET_AUTO_RELOCK);
            Boolean autoRelock = status.getAutoRelockEnabled();
            if (autoRelock != null && autoRelock) {
                updateState(cuid, OnOffType.ON);
            } else {
                updateState(cuid, OnOffType.OFF);
            }
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_AUTO_RELOCK_ENABLED);
            Boolean autoRelockEnabled = status.getAutoRelockEnabled();
            if (autoRelockEnabled != null) {
                updateState(cuid, new StringType(autoRelockEnabled.toString()));
            }
            DoorLockVolumeSettings dlvs = status.getDoorLockVolumeSettings();
            if (dlvs != null) {
                cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTLOCK_VOLUME);
                StringType volume = new StringType(dlvs.getVolume());
                updateState(cuid, volume);

                cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTLOCK_VOICE_LEVEL);
                StringType voiceLevel = new StringType(dlvs.getVoiceLevel());
                updateState(cuid, voiceLevel);
            }
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_ID);
            BigDecimal siteId = status.getSiteId();
            if (siteId != null) {
                updateState(cuid, new DecimalType(siteId.longValue()));
            }
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_NAME);
            StringType instName = new StringType(status.getSiteName());
            updateState(cuid, instName);
        }
    }
}
