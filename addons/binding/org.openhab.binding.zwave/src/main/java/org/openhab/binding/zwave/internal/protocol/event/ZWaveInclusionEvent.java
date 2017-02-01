/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * This event signals a node being included or excluded into the network.
 *
 * @author Chris Jackson
 */
public class ZWaveInclusionEvent extends ZWaveEvent {
    private Type type;
    private final Date includedAt;
    List<CommandClass> commandClasses = new ArrayList<CommandClass>();
    Basic basic = Basic.NOT_KNOWN;
    Generic generic = Generic.NOT_KNOWN;
    Specific specific = Specific.NOT_USED;

    /**
     * Constructor. Creates a new instance of the ZWaveInclusionEvent
     * class.
     *
     * @param nodeId the nodeId of the event.
     */
    public ZWaveInclusionEvent(Type type) {
        super(255);
        this.includedAt = new Date();
        this.type = type;
    }

    public ZWaveInclusionEvent(Type type, int nodeId) {
        super(nodeId);
        this.includedAt = new Date();
        this.type = type;
    }

    public ZWaveInclusionEvent(Type type, int nodeId, Basic basic, Generic generic, Specific specific,
            List<CommandClass> commandClasses) {
        super(nodeId);
        this.includedAt = new Date();
        this.type = type;
        this.basic = basic;
        this.generic = generic;
        this.specific = specific;
        this.commandClasses.addAll(commandClasses);
    }

    public Date getIncludedAt() {
        return includedAt;
    }

    public Type getEvent() {
        return type;
    }

    public Basic getBasic() {
        return basic;
    }

    public Generic getGeneric() {
        return generic;
    }

    public Specific getSpecific() {
        return specific;
    }

    public List<CommandClass> getCommandClasses() {
        return commandClasses;
    }

    public enum Type {
        IncludeStart,
        IncludeSlaveFound,
        IncludeControllerFound,
        IncludeFail,
        IncludeDone,
        ExcludeStart,
        ExcludeSlaveFound,
        ExcludeControllerFound,
        ExcludeFail,
        ExcludeDone,
    }
}
