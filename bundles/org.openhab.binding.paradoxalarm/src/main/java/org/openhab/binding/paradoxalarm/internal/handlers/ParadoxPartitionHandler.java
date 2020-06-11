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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
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
                    new StringType(partition.getState().getAdditionalState()));
        }
    }
}
