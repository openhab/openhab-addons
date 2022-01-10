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

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnDefs.RelayStateModifier;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.binding.lcn.internal.common.VariableValue;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for LCN module Thing sub handlers.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLcnModuleSubHandler implements ILcnModuleSubHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractLcnModuleSubHandler.class);
    protected final LcnModuleHandler handler;
    protected final ModInfo info;

    public AbstractLcnModuleSubHandler(LcnModuleHandler handler, ModInfo info) {
        this.handler = handler;
        this.info = info;
    }

    @Override
    public void handleRefresh(String groupId) {
        // can be overwritten by subclasses.
    }

    @Override
    public void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, String idWithoutGroup)
            throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandDecimal(DecimalType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandDimmerOutput(DimmerOutputCommand command, int number) throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandString(StringType command, int number) throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandUpDown(UpDownType command, LcnChannelGroup channelGroup, int number, boolean invertUpDown)
            throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandStopMove(StopMoveType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        unsupportedCommand(command);
    }

    @Override
    public void handleCommandHsb(HSBType command, String groupId) throws LcnException {
        unsupportedCommand(command);
    }

    private void unsupportedCommand(Command command) {
        logger.warn("Unsupported command: {}: {}", getClass().getSimpleName(), command.getClass().getSimpleName());
    }

    /**
     * Tries to parses the given PCK message. Fails silently to let another sub handler give the chance to process the
     * message.
     *
     * @param pck the message to process
     * @return true, if the message could be processed successfully
     */
    public void tryParse(String pck) {
        Optional<Matcher> firstSuccessfulMatcher = getPckStatusMessagePatterns().stream().map(p -> p.matcher(pck))
                .filter(Matcher::matches).filter(m -> handler.isMyAddress(m.group("segId"), m.group("modId")))
                .findAny();

        firstSuccessfulMatcher.ifPresent(matcher -> {
            try {
                handleStatusMessage(matcher);
            } catch (LcnException e) {
                logger.warn("Parse error: {}", e.getMessage());
            }
        });
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
