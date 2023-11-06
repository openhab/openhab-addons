/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;

/**
 * Interface for LCN module Thing sub handlers processing variables.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public interface ILcnModuleSubHandler {
    /**
     * Gets the Patterns, the sub handler is capable to process.
     *
     * @return the Patterns
     */
    Collection<Pattern> getPckStatusMessagePatterns();

    /**
     * Processes the payload of a pre-matched PCK message.
     *
     * @param matcher the pre-matched matcher.
     * @throws LcnException when the message cannot be processed
     */
    void handleStatusMessage(Matcher matcher) throws LcnException;

    /**
     * Processes a refresh request from openHAB.
     *
     * @param channelGroup the Channel group that shall be refreshed
     * @param number the Channel number within the Channel group
     */
    void handleRefresh(LcnChannelGroup channelGroup, int number);

    /**
     * Processes a refresh request from openHAB.
     *
     * @param groupId the Channel ID that shall be refreshed
     */
    void handleRefresh(String groupId);

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, int number) throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param idWithoutGroup the Channel's name within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, String idWithoutGroup)
            throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandDecimal(DecimalType command, LcnChannelGroup channelGroup, int number) throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandDimmerOutput(DimmerOutputCommand command, int number) throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandString(StringType command, int number) throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @param invertUpDown true, if Up/Down is inverted
     * @throws LcnException when the command could not processed
     */
    void handleCommandUpDown(UpDownType command, LcnChannelGroup channelGroup, int number, boolean invertUpDown)
            throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandStopMove(StopMoveType command, LcnChannelGroup channelGroup, int number) throws LcnException;

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param groupId the Channel's name within the Channel group
     * @throws LcnException when the command could not processed
     */
    void handleCommandHsb(HSBType command, String groupId) throws LcnException;
}
