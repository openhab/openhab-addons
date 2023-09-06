/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.intrusion;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ACTIVE_CONFIGURATION_PROFILE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ALARM_STATE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ARMING_STATE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ARM_ACTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_DISARM_ACTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_MUTE_ACTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SYSTEM_AVAILABILITY;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.intrusion.IntrusionDetectionControlStateService;
import org.openhab.binding.boschshc.internal.services.intrusion.IntrusionDetectionSystemStateService;
import org.openhab.binding.boschshc.internal.services.intrusion.SurveillanceAlarmService;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.ArmActionService;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.dto.ArmActionRequest;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.disarm.DisarmActionService;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.mute.MuteActionService;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionControlState;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionSystemState;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.SurveillanceAlarmState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for the intrusion detection alarm system.
 * <p>
 * It supports
 * <ul>
 * <li>Obtaining the current intrusion detection system state</li>
 * <li>Receiving updates related to the detection control state</li>
 * <li>Receiving updates related to surveillance alarm events</li>
 * <li>Arming the system</li>
 * <li>Disarming the system</li>
 * <li>Muting the alarm</li>
 * </ul>
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class IntrusionDetectionHandler extends BoschSHCHandler {

    private IntrusionDetectionSystemStateService intrusionDetectionSystemStateService;
    private IntrusionDetectionControlStateService intrusionDetectionControlStateService;
    private SurveillanceAlarmService surveillanceAlarmService;
    private ArmActionService armActionService;
    private DisarmActionService disarmActionService;
    private MuteActionService muteActionService;

    public IntrusionDetectionHandler(Thing thing) {
        super(thing);
        this.intrusionDetectionSystemStateService = new IntrusionDetectionSystemStateService();
        this.intrusionDetectionControlStateService = new IntrusionDetectionControlStateService();
        this.surveillanceAlarmService = new SurveillanceAlarmService();
        this.armActionService = new ArmActionService();
        this.disarmActionService = new DisarmActionService();
        this.muteActionService = new MuteActionService();
    }

    @Override
    public @Nullable String getBoschID() {
        return BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION;
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(intrusionDetectionSystemStateService, this::updateChannels,
                List.of(CHANNEL_SYSTEM_AVAILABILITY, CHANNEL_ARMING_STATE, CHANNEL_ALARM_STATE,
                        CHANNEL_ACTIVE_CONFIGURATION_PROFILE),
                true);
        this.registerService(intrusionDetectionControlStateService, this::updateChannels,
                List.of(CHANNEL_ARMING_STATE));
        this.registerService(surveillanceAlarmService, this::updateChannels, List.of(CHANNEL_ALARM_STATE));
        this.registerStatelessService(armActionService);
        this.registerStatelessService(disarmActionService);
        this.registerStatelessService(muteActionService);
    }

    private void updateChannels(IntrusionDetectionSystemState systemState) {
        super.updateState(CHANNEL_SYSTEM_AVAILABILITY, OnOffType.from(systemState.systemAvailability.available));
        super.updateState(CHANNEL_ARMING_STATE, new StringType(systemState.armingState.state.toString()));
        super.updateState(CHANNEL_ALARM_STATE, new StringType(systemState.alarmState.value.toString()));
        super.updateState(CHANNEL_ACTIVE_CONFIGURATION_PROFILE,
                new StringType(systemState.activeConfigurationProfile.profileId));
    }

    private void updateChannels(IntrusionDetectionControlState controlState) {
        super.updateState(CHANNEL_ARMING_STATE, new StringType(controlState.value.toString()));
    }

    private void updateChannels(SurveillanceAlarmState surveillanceAlarmState) {
        super.updateState(CHANNEL_ALARM_STATE, new StringType(surveillanceAlarmState.value.toString()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_ARM_ACTION:
                if (command instanceof StringType) {
                    armIntrusionDetectionSystem((StringType) command);
                }
                break;
            case CHANNEL_DISARM_ACTION:
                if (command instanceof OnOffType) {
                    disarmIntrusionDetectionSystem((OnOffType) command);
                }
                break;
            case CHANNEL_MUTE_ACTION:
                if (command instanceof OnOffType) {
                    muteIntrusionDetectionSystem((OnOffType) command);
                }
                break;
        }
    }

    private void armIntrusionDetectionSystem(StringType profileIdCommand) {
        ArmActionRequest armActionRequest = new ArmActionRequest();
        armActionRequest.profileId = profileIdCommand.toFullString();
        postAction(armActionService, armActionRequest);
    }

    private void disarmIntrusionDetectionSystem(OnOffType command) {
        if (command == OnOffType.ON) {
            postAction(disarmActionService);
        }
    }

    private void muteIntrusionDetectionSystem(OnOffType command) {
        if (command == OnOffType.ON) {
            postAction(muteActionService);
        }
    }
}
