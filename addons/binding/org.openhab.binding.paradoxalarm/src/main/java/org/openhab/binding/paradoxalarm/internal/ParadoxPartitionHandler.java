/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;

/**
 * The {@link ParadoxPartitionHandler} Handler that updates states of paradox partitions from the cache.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxPartitionHandler extends BaseThingHandler {

    public ParadoxPartitionHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ParadoxPanel panel = ParadoxPanel.getInstance();

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }
}
