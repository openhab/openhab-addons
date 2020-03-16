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
package org.openhab.binding.lcn.internal.common;

import org.openhab.binding.lcn.internal.subhandler.AbstractLcnModuleSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleBinarySensorSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleCodeSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleKeyLockTableSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleLedSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleLogicSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleOutputSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRelaySubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRollershutterOutputSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRollershutterRelaySubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRvarLockSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRvarSetpointSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleS0CounterSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleThresholdSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleVariableSubHandler;

/**
 * Defines the supported channels of an LCN module handler.
 *
 * @author Fabian Wolter - Initial contribution
 */
public enum LcnChannelGroup {
    OUTPUT(4, LcnModuleOutputSubHandler.class),
    ROLLERSHUTTEROUTPUT(1, LcnModuleRollershutterOutputSubHandler.class),
    RELAY(8, LcnModuleRelaySubHandler.class),
    ROLLERSHUTTERRELAY(4, LcnModuleRollershutterRelaySubHandler.class),
    LED(12, LcnModuleLedSubHandler.class),
    LOGIC(4, LcnModuleLogicSubHandler.class),
    BINARYSENSOR(8, LcnModuleBinarySensorSubHandler.class),
    VARIABLE(12, LcnModuleVariableSubHandler.class),
    RVARSETPOINT(2, LcnModuleRvarSetpointSubHandler.class),
    RVARLOCK(2, LcnModuleRvarLockSubHandler.class),
    THRESHOLDREGISTER1(5, LcnModuleThresholdSubHandler.class),
    THRESHOLDREGISTER2(4, LcnModuleThresholdSubHandler.class),
    THRESHOLDREGISTER3(4, LcnModuleThresholdSubHandler.class),
    THRESHOLDREGISTER4(4, LcnModuleThresholdSubHandler.class),
    S0INPUT(4, LcnModuleS0CounterSubHandler.class),
    KEYLOCKTABLEA(8, LcnModuleKeyLockTableSubHandler.class),
    KEYLOCKTABLEB(8, LcnModuleKeyLockTableSubHandler.class),
    KEYLOCKTABLEC(8, LcnModuleKeyLockTableSubHandler.class),
    KEYLOCKTABLED(8, LcnModuleKeyLockTableSubHandler.class),
    CODE(0, LcnModuleCodeSubHandler.class);

    private int count;
    private Class<? extends AbstractLcnModuleSubHandler> subHandlerClass;

    private LcnChannelGroup(int count, Class<? extends AbstractLcnModuleSubHandler> subHandlerClass) {
        this.count = count;
        this.subHandlerClass = subHandlerClass;
    }

    /**
     * Gets the number of Channels within the channel group.
     *
     * @return the Channel count
     */
    public int getCount() {
        return count;
    }

    /**
     * Checks the given Channel id against the max. Channel count in this Channel group.
     *
     * @param number the number to check
     * @return true, if the number is in the range
     */
    public boolean isValidId(int number) {
        return number >= 0 && number < count;
    }

    /**
     * Gets the sub handler class to handle this Channel group.
     *
     * @return the sub handler class
     */
    public Class<? extends AbstractLcnModuleSubHandler> getSubHandlerClass() {
        return subHandlerClass;
    }

    /**
     * Converts a given table ID into the corresponding Channel group.
     *
     * @param tableId to convert
     * @return the channel group
     * @throws LcnException when the ID is out of range
     */
    public static LcnChannelGroup fromTableId(int tableId) throws LcnException {
        switch (tableId) {
            case 0:
                return KEYLOCKTABLEA;
            case 1:
                return KEYLOCKTABLEB;
            case 2:
                return KEYLOCKTABLEC;
            case 3:
                return KEYLOCKTABLED;
            default:
                throw new LcnException("Unknown key table ID: " + tableId);
        }
    }
}
