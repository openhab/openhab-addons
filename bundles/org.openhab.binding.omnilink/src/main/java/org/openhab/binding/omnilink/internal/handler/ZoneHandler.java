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
package org.openhab.binding.omnilink.internal.handler;

import static com.digitaldan.jomnilinkII.MessageTypes.properties.AuxSensorProperties.SENSOR_TYPE_PROGRAMMABLE_ENERGY_SAVER_MODULE;
import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.SecurityCodeValidation;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.ZoneProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedZoneStatus;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link ZoneHandler} defines some methods that are used to
 * interface with an OmniLink Zone. This by extension also defines the
 * OmniPro Zone thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 */
@NonNullByDefault
public class ZoneHandler extends AbstractOmnilinkStatusHandler<ExtendedZoneStatus> {
    private final Logger logger = LoggerFactory.getLogger(ZoneHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public ZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateZoneProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Zone!");
        }
    }

    private void updateZoneProperties(OmnilinkBridgeHandler bridgeHandler) {
        final List<AreaProperties> areas = super.getAreaProperties();
        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = super.bitFilterForArea(areaProperties);

                ObjectPropertyRequest<ZoneProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(bridgeHandler, ObjectPropertyRequests.ZONE, getThingNumber(), 0).selectNamed()
                        .areaFilter(areaFilter).build();

                for (ZoneProperties zoneProperties : objectPropertyRequest) {
                    if (zoneProperties.getZoneType() <= SENSOR_TYPE_PROGRAMMABLE_ENERGY_SAVER_MODULE) {
                        Map<String, String> properties = editProperties();
                        properties.put(THING_PROPERTIES_NAME, zoneProperties.getName());
                        properties.put(THING_PROPERTIES_AREA, Integer.toString(areaProperties.getNumber()));
                        updateProperties(properties);
                    }
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);
        int mode;

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        if (!(command instanceof StringType)) {
            logger.debug("Invalid command: {}, must be StringType", command);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ZONE_BYPASS:
                mode = CommandMessage.CMD_SECURITY_BYPASS_ZONE;
                break;
            case CHANNEL_ZONE_RESTORE:
                mode = CommandMessage.CMD_SECURITY_RESTORE_ZONE;
                break;
            default:
                mode = -1;
        }
        logger.debug("mode {} on zone {} with code {}", mode, thingID, command.toFullString());
        char[] code = command.toFullString().toCharArray();
        if (code.length != 4) {
            logger.warn("Invalid code length, code must be 4 digits");
        } else {
            try {
                final OmnilinkBridgeHandler bridge = getOmnilinkBridgeHandler();
                if (bridge != null) {
                    int areaNumber = getAreaNumber();
                    if (areaNumber == -1) {
                        logger.warn("Could not identify area, canceling bypass/restore command!");
                    } else {
                        SecurityCodeValidation codeValidation = bridge.reqSecurityCodeValidation(getAreaNumber(),
                                Character.getNumericValue(code[0]), Character.getNumericValue(code[1]),
                                Character.getNumericValue(code[2]), Character.getNumericValue(code[3]));
                        /*
                         * 0 Invalid code
                         * 1 Master
                         * 2 Manager
                         * 3 User
                         */
                        logger.debug("User code number: {} level: {}", codeValidation.getCodeNumber(),
                                codeValidation.getAuthorityLevel());
                        /*
                         * Valid user code number are 1-99, 251 is duress code, 0 means code does not exist
                         */
                        if ((codeValidation.getCodeNumber() > 0 && codeValidation.getCodeNumber() <= 99)
                                && codeValidation.getAuthorityLevel() > 0) {
                            sendOmnilinkCommand(mode, codeValidation.getCodeNumber(), thingID);
                        } else {
                            logger.warn("System reported an invalid code!");
                        }
                    }
                } else {
                    logger.debug("Received null bridge while sending zone command!");
                }
            } catch (OmniInvalidResponseException e) {
                logger.debug("Zone command failed: {}", e.getMessage());
            } catch (OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.debug("Could not send zone command: {}", e.getMessage());
            }
        }
        // This is a send only channel, so don't store the user code
        updateState(channelUID, UnDefType.UNDEF);
    }

    @Override
    protected void updateChannels(ExtendedZoneStatus zoneStatus) {
        // 0 Secure. 1 Not ready, 3 Trouble
        int current = ((zoneStatus.getStatus() >> 0) & 0x03);
        // 0 Secure, 1 Tripped, 2 Reset, but previously tripped
        int latched = ((zoneStatus.getStatus() >> 2) & 0x03);
        // 0 Disarmed, 1 Armed, 2 Bypass user, 3 Bypass system
        int arming = ((zoneStatus.getStatus() >> 4) & 0x03);
        State contactState = Integer.valueOf(current).equals(0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        logger.debug("handling Zone Status change to state: {}, current: {}, latched: {}, arming: {}", contactState,
                current, latched, arming);
        updateState(CHANNEL_ZONE_CONTACT, contactState);
        updateState(CHANNEL_ZONE_CURRENT_CONDITION, new DecimalType(current));
        updateState(CHANNEL_ZONE_LATCHED_ALARM_STATUS, new DecimalType(latched));
        updateState(CHANNEL_ZONE_ARMING_STATUS, new DecimalType(arming));
    }

    @Override
    protected Optional<ExtendedZoneStatus> retrieveStatus() {
        try {
            final OmnilinkBridgeHandler bridge = getOmnilinkBridgeHandler();
            if (bridge != null) {
                ObjectStatus objStatus = bridge.requestObjectStatus(Message.OBJ_TYPE_ZONE, thingID, thingID, true);
                return Optional.of((ExtendedZoneStatus) objStatus.getStatuses()[0]);
            } else {
                logger.debug("Received null bridge while updating Zone status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Zone status: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
