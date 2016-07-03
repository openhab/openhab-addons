/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.event;

import java.util.List;

import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * ZWave association group received event.
 * Send from the association members to the binding
 * Note that multiple events can be required to build up the full list.
 *
 * @author Chris Jackson
 */
public class ZWaveAssociationEvent extends ZWaveCommandClassValueEvent {

    private ZWaveAssociationGroup group;

    /**
     * Constructor. Creates a new instance of the ZWaveAssociationEvent
     * class.
     *
     * @param nodeId the nodeId of the event. Must be set to the controller node.
     */
    public ZWaveAssociationEvent(int nodeId, ZWaveAssociationGroup group) {
        super(nodeId, 0, CommandClass.ASSOCIATION, 0);

        this.group = group;
    }

    public int getGroupId() {
        return group.getIndex();
    }

    public List<ZWaveAssociation> getGroupMembers() {
        return group.getAssociations();
    }
}
