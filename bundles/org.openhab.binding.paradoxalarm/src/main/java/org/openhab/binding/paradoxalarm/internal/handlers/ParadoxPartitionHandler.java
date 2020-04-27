/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.binding.paradoxalarm.internal.model.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxPartitionHandler} Handler that updates states of paradox partitions from the cache.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxPartitionHandler extends EntityBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(ParadoxPartitionHandler.class);

    public ParadoxPartitionHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateEntity() {
        int index = calculateEntityIndex();
        List<Partition> partitions = ParadoxPanel.getInstance().getPartitions();
        Partition partition = partitions.get(index);
        if (partition != null) {
            updateState(PARTITION_LABEL_CHANNEL_UID, new StringType(partition.getLabel()));
            updateState(PARTITION_STATE_CHANNEL_UID, new StringType(partition.getState().getMainState()));
            updateState(PARTITION_ADDITIONAL_STATES_CHANNEL_UID,
                    new StringType("Deprecated field. Use direct channels instead"));
            updateState(PARTITION_READY_TO_ARM_CHANNEL_UID, booleanToSwitchState(partition.getState().isReadyToArm()));
            updateState(PARTITION_IN_EXIT_DELAY_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isInExitDelay()));
            updateState(PARTITION_IN_ENTRY_DELAY_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isInEntryDelay()));
            updateState(PARTITION_IN_TROUBLE_CHANNEL_UID, booleanToSwitchState(partition.getState().isInTrouble()));
            updateState(PARTITION_ALARM_IN_MEMORY_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isHasAlarmInMemory()));
            updateState(PARTITION_ZONE_BYPASS_CHANNEL_UID, booleanToSwitchState(partition.getState().isInZoneBypass()));
            updateState(PARTITION_ZONE_IN_TAMPER_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isHasZoneInTamperTrouble()));
            updateState(PARTITION_ZONE_IN_LOW_BATTERY_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isHasZoneInLowBatteryTrouble()));
            updateState(PARTITION_ZONE_IN_FIRE_LOOP_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isHasZoneInFireLoopTrouble()));
            updateState(PARTITION_ZONE_IN_SUPERVISION_TROUBLE_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isHasZoneInSupervisionTrouble()));
            updateState(PARTITION_STAY_INSTANT_READY_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isStayInstantReady()));
            updateState(PARTITION_FORCE_READY_CHANNEL_UID, booleanToSwitchState(partition.getState().isForceReady()));
            updateState(PARTITION_BYPASS_READY_CHANNEL_UID, booleanToSwitchState(partition.getState().isBypassReady()));
            updateState(PARTITION_INHIBIT_READY_CHANNEL_UID,
                    booleanToSwitchState(partition.getState().isInhibitReady()));
            updateState(PARTITION_ALL_ZONES_CLOSED_CHANNEL_UID,
                    booleanToContactState(partition.getState().isAreAllZoneclosed()));
        }
    }

    private OpenClosedType booleanToContactState(boolean value) {
        return value ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    private OnOffType booleanToSwitchState(boolean value) {
        return value ? OnOffType.ON : OnOffType.OFF;
    }
}
