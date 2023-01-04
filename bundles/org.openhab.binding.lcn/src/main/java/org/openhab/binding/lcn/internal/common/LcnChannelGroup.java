/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.binding.lcn.internal.subhandler.AbstractLcnModuleSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleBinarySensorSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleCodeSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleHostCommandSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleKeyLockTableSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleLedSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleLogicSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleOperatingHoursCounterSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleOutputSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRelaySubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRollershutterOutputSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRollershutterRelayPositionSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRollershutterRelaySlatAngleSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRvarLockSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRvarModeSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleRvarSetpointSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleS0CounterSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleThresholdSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleVariableSubHandler;

/**
 * Defines the supported channels of an LCN module handler.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public enum LcnChannelGroup {
    OUTPUT(4, LcnModuleOutputSubHandler::new),
    ROLLERSHUTTEROUTPUT(1, LcnModuleRollershutterOutputSubHandler::new),
    RELAY(8, LcnModuleRelaySubHandler::new),
    ROLLERSHUTTERRELAY(4, LcnModuleRollershutterRelayPositionSubHandler::new),
    ROLLERSHUTTERRELAYSLAT(4, LcnModuleRollershutterRelaySlatAngleSubHandler::new),
    LED(12, LcnModuleLedSubHandler::new),
    LOGIC(4, LcnModuleLogicSubHandler::new),
    BINARYSENSOR(8, LcnModuleBinarySensorSubHandler::new),
    VARIABLE(12, LcnModuleVariableSubHandler::new),
    RVARSETPOINT(2, LcnModuleRvarSetpointSubHandler::new),
    RVARMODE(2, LcnModuleRvarModeSubHandler::new),
    RVARLOCK(2, LcnModuleRvarLockSubHandler::new),
    THRESHOLDREGISTER1(5, LcnModuleThresholdSubHandler::new),
    THRESHOLDREGISTER2(4, LcnModuleThresholdSubHandler::new),
    THRESHOLDREGISTER3(4, LcnModuleThresholdSubHandler::new),
    THRESHOLDREGISTER4(4, LcnModuleThresholdSubHandler::new),
    S0INPUT(4, LcnModuleS0CounterSubHandler::new),
    KEYLOCKTABLEA(8, LcnModuleKeyLockTableSubHandler::new),
    KEYLOCKTABLEB(8, LcnModuleKeyLockTableSubHandler::new),
    KEYLOCKTABLEC(8, LcnModuleKeyLockTableSubHandler::new),
    KEYLOCKTABLED(8, LcnModuleKeyLockTableSubHandler::new),
    CODE(0, LcnModuleCodeSubHandler::new),
    OPERATINGHOURS(0, LcnModuleOperatingHoursCounterSubHandler::new),
    HOSTCOMMAND(0, LcnModuleHostCommandSubHandler::new);

    private int count;
    private BiFunction<LcnModuleHandler, ModInfo, ? extends AbstractLcnModuleSubHandler> handlerFactory;

    private LcnChannelGroup(int count,
            BiFunction<LcnModuleHandler, ModInfo, ? extends AbstractLcnModuleSubHandler> handlerFactory) {
        this.count = count;
        this.handlerFactory = handlerFactory;
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
    public AbstractLcnModuleSubHandler createSubHandler(LcnModuleHandler handler, ModInfo info) {
        return handlerFactory.apply(handler, info);
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
