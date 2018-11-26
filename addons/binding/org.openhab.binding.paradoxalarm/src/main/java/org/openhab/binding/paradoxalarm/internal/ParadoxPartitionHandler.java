/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal;

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
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxPartitionHandler extends EntityBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(ParadoxPartitionHandler.class);

    public ParadoxPartitionHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateEntity() {
        List<Partition> partitions = ParadoxPanel.getInstance().getPartitions();
        int index = calculateEntityIndex();
        Partition partition = partitions.get(index);
        if (partition != null) {
            updateState("label", new StringType(partition.getLabel()));
            updateState("state", new StringType(partition.getState().getMainState()));
            updateState("additionalState", new StringType(partition.getState().getAdditionalState()));
        }
    }
}
