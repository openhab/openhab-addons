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
package org.openhab.binding.lcn.internal.subhandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnDefs.RelayStateModifier;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.common.VariableValue;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for LCN module Thing sub handlers.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLcnModuleSubHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractLcnModuleSubHandler.class);
    protected LcnModuleHandler handler;
    protected ModInfo info;

    public AbstractLcnModuleSubHandler(LcnModuleHandler handler, ModInfo info) {
        this.handler = handler;
        this.info = info;
    }

    /**
     * Gets the Patterns, the sub handler is capable to process.
     *
     * @return the Patterns
     */
    public abstract Collection<Pattern> getPckStatusMessagePatterns();

    /**
     * Processes the payload of a pre-matched PCK message.
     *
     * @param matcher the pre-matched matcher.
     * @throws LcnException when the message cannot be processed
     */
    public abstract void handleStatusMessage(Matcher matcher) throws LcnException;

    /**
     * Processes a refresh request from openHAB.
     *
     * @param channelGroup the Channel group that shall be refreshed
     * @param number the Channel number within the Channel group
     */
    public abstract void handleRefresh(LcnChannelGroup channelGroup, int number);

    /**
     * Processes a refresh request from openHAB.
     *
     * @param groupId the Channel ID that shall be refreshed
     */
    public void handleRefresh(String groupId) {
        // can be overwritten by subclasses.
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param idWithoutGroup the Channel's name within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, String idWithoutGroup)
            throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandDecimal(DecimalType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandDimmerOutput(DimmerOutputCommand command, int number) throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandString(StringType command, int number) throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandUpDown(UpDownType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param channelGroup the addressed Channel group
     * @param number the Channel's number within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandStopMove(StopMoveType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        unsupportedCommand(command);
    }

    /**
     * Handles a Command from openHAB.
     *
     * @param command the command to handle
     * @param groupId the Channel's name within the Channel group
     * @throws LcnException when the command could not processed
     */
    public void handleCommandHsb(HSBType command, String groupId) throws LcnException {
        unsupportedCommand(command);
    }

    private void unsupportedCommand(Command command) {
        logger.warn("Unsupported command: {}", command.getClass().getSimpleName());
    }

    /**
     * Tries to parses the given PCK message. Fails silently to let another sub handler give the chance to process the
     * message.
     *
     * @param pck the message to process
     * @return true, if the message could be processed successfully
     */
    public boolean tryParse(String pck) {
        Optional<Matcher> firstSuccessfulMatcher = getPckStatusMessagePatterns().stream().map(p -> p.matcher(pck))
                .filter(m -> m.matches()).filter(m -> handler.isMyAddress(m.group("segId"), m.group("modId")))
                .findAny();

        firstSuccessfulMatcher.ifPresent(matcher -> {
            try {
                handleStatusMessage(matcher);
            } catch (LcnException e) {
                logger.warn("Parse error: {}", e.getMessage());
            }
        });

        return firstSuccessfulMatcher.isPresent();
    }

    /**
     * Creates a RelayStateModifier array with all elements set to NOCHANGE.
     *
     * @return the created array
     */
    protected RelayStateModifier[] createRelayStateModifierArray() {
        RelayStateModifier[] ret = new LcnDefs.RelayStateModifier[LcnChannelGroup.RELAY.getCount()];
        Arrays.fill(ret, LcnDefs.RelayStateModifier.NOCHANGE);
        return ret;
    }

    /**
     * Updates the state of the LCN module.
     *
     * @param type the channel type which shall be updated
     * @param number the Channel's number within the channel type, zero-based
     * @param state the new state
     */
    protected void fireUpdate(LcnChannelGroup type, int number, State state) {
        handler.updateChannel(type, (number + 1) + "", state);
    }

    /**
     * Fires the current state of a Variable to openHAB. Resets running value request logic.
     *
     * @param matcher the pre-matched matcher
     * @param channelId the Channel's ID to update
     * @param variable the Variable to update
     * @return the new variable's value
     */
    protected VariableValue fireUpdateAndReset(Matcher matcher, String channelId, Variable variable) {
        VariableValue value = new VariableValue(Long.parseLong(matcher.group("value" + channelId)));

        info.updateVariableValue(variable, value);
        info.onVariableResponseReceived(variable);

        fireUpdate(variable.getChannelType(), variable.getThresholdNumber().orElse(variable.getNumber()),
                value.getState(variable));
        return value;
    }
}
