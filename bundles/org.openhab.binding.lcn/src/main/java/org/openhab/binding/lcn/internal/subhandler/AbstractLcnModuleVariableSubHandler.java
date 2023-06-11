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
package org.openhab.binding.lcn.internal.subhandler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for LCN module Thing sub handlers processing variables.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLcnModuleVariableSubHandler extends AbstractLcnModuleSubHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractLcnModuleVariableSubHandler.class);

    public AbstractLcnModuleVariableSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        requestVariable(info, channelGroup, number);
    }

    /**
     * Requests the current state of the given Channel.
     *
     * @param info the modules ModInfo cache
     * @param channelGroup the Channel group
     * @param number the Channel's number within the Channel group
     */
    protected void requestVariable(ModInfo info, LcnChannelGroup channelGroup, int number) {
        try {
            Variable var = getVariable(channelGroup, number);
            info.refreshVariable(var);
        } catch (IllegalArgumentException e) {
            logger.warn("Could not parse variable name: {}{}", channelGroup, (number + 1));
        }
    }

    /**
     * Gets a Variable from the given parameters.
     *
     * @param channelGroup the Channel group the Variable is in
     * @param number the number of the Variable's Channel
     * @return the Variable
     * @throws IllegalArgumentException when the Channel group and number do not exist
     */
    protected Variable getVariable(LcnChannelGroup channelGroup, int number) throws IllegalArgumentException {
        return Variable.valueOf(channelGroup.name() + (number + 1));
    }

    /**
     * Calculates the relative change between the current and the demanded value of a Variable.
     *
     * @param command the requested value
     * @param variable the Variable type
     * @return the difference
     * @throws LcnException when the difference is too big
     */
    protected int getRelativeChange(DecimalType command, Variable variable) throws LcnException {
        // LCN doesn't support setting thresholds or variables with absolute values. So, calculate the relative change.
        int relativeVariableChange = (int) (command.longValue() - info.getVariableValue(variable));

        int result;
        if (relativeVariableChange > 0) {
            result = Math.min(relativeVariableChange, getMaxAbsChange(variable));
        } else {
            result = Math.max(relativeVariableChange, -getMaxAbsChange(variable));
        }
        if (result != relativeVariableChange) {
            logger.warn("Relative change of {} too big, limiting: {}", variable, relativeVariableChange);
        }
        return result;
    }

    private int getMaxAbsChange(Variable variable) {
        switch (variable) {
            case RVARSETPOINT1:
            case RVARSETPOINT2:
            case THRESHOLDREGISTER11:
            case THRESHOLDREGISTER12:
            case THRESHOLDREGISTER13:
            case THRESHOLDREGISTER14:
            case THRESHOLDREGISTER15:
            case THRESHOLDREGISTER21:
            case THRESHOLDREGISTER22:
            case THRESHOLDREGISTER23:
            case THRESHOLDREGISTER24:
            case THRESHOLDREGISTER31:
            case THRESHOLDREGISTER32:
            case THRESHOLDREGISTER33:
            case THRESHOLDREGISTER34:
            case THRESHOLDREGISTER41:
            case THRESHOLDREGISTER42:
            case THRESHOLDREGISTER43:
            case THRESHOLDREGISTER44:
                return 1000;
            case VARIABLE1:
            case VARIABLE2:
            case VARIABLE3:
            case VARIABLE4:
            case VARIABLE5:
            case VARIABLE6:
            case VARIABLE7:
            case VARIABLE8:
            case VARIABLE9:
            case VARIABLE10:
            case VARIABLE11:
            case VARIABLE12:
                return 4000;
            case UNKNOWN:
            case S0INPUT1:
            case S0INPUT2:
            case S0INPUT3:
            case S0INPUT4:
            default:
                return 0;
        }
    }
}
