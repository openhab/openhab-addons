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
import org.openhab.binding.paradoxalarm.internal.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxZoneHandler} Handler that updates states of paradox zones from the cache.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxZoneHandler extends EntityBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(ParadoxPartitionHandler.class);

    public ParadoxZoneHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateEntity() {
        List<Zone> zones = ParadoxPanel.getInstance().getZones();
        int index = calculateEntityIndex();
        Zone zone = zones.get(index);
        if (zone != null) {
            updateState("label", new StringType(zone.getLabel()));
            updateState("isOpened", OpenClosedType.from(zone.getZoneState().isOpened()));
            updateState("isTampered", OpenClosedType.from(zone.getZoneState().isTampered()));
            updateState("hasLowBattery", OpenClosedType.from(zone.getZoneState().hasLowBattery()));
        }
    }
}
