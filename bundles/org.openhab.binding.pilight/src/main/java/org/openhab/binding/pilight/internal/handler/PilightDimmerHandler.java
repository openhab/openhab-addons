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
package org.openhab.binding.pilight.internal.handler;

import static org.openhab.binding.pilight.internal.PilightBindingConstants.CHANNEL_DIMLEVEL;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.Code;
import org.openhab.binding.pilight.internal.dto.Device;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.binding.pilight.internal.dto.Values;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public class PilightDimmerHandler extends PilightBaseHandler {

    private static final int MAX_DIM_LEVEL_DEFAULT = 15;
    private static final BigDecimal BIG_DECIMAL_100 = new BigDecimal(100);

    private final Logger logger = LoggerFactory.getLogger(PilightDimmerHandler.class);

    private int maxDimLevel = MAX_DIM_LEVEL_DEFAULT;

    public PilightDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateFromStatus(Status status) {
        BigDecimal dimLevel = BigDecimal.ZERO;
        String dimLevelAsString = status.getValues().get("dimlevel");

        if (dimLevelAsString != null) {
            dimLevel = getPercentageFromDimLevel(dimLevelAsString);
        } else {
            // Dimmer items can can also be switched on or off in pilight.
            // When this happens, the dimmer value is not reported. At least we know it's on or off.
            String stateAsString = status.getValues().get("state");
            if (stateAsString != null) {
                State state = OnOffType.valueOf(stateAsString.toUpperCase());
                dimLevel = state.equals(OnOffType.ON) ? BIG_DECIMAL_100 : BigDecimal.ZERO;
            }
        }

        State state = new PercentType(dimLevel);
        updateState(CHANNEL_DIMLEVEL, state);
    }

    @Override
    protected void updateFromConfigDevice(Device device) {
        Integer max = device.getDimlevelMaximum();

        if (max != null) {
            maxDimLevel = max;
        }
    }

    @Override
    protected @Nullable Action createUpdateCommand(ChannelUID unused, Command command) {
        Code code = new Code();
        code.setDevice(getName());

        if (command instanceof OnOffType) {
            code.setState(command.equals(OnOffType.ON) ? Code.STATE_ON : Code.STATE_OFF);
        } else if (command instanceof PercentType percentCommand) {
            setDimmerValue(percentCommand, code);
        } else {
            logger.warn("Only OnOffType and PercentType are supported by a dimmer.");
            return null;
        }

        Action action = new Action(Action.ACTION_CONTROL);
        action.setCode(code);
        return action;
    }

    private BigDecimal getPercentageFromDimLevel(String string) {
        return new BigDecimal(string).setScale(2).divide(new BigDecimal(maxDimLevel), RoundingMode.HALF_UP)
                .multiply(BIG_DECIMAL_100);
    }

    private void setDimmerValue(PercentType percent, Code code) {
        if (PercentType.ZERO.equals(percent)) {
            // pilight is not responding to commands that set both the dimlevel to 0 and state to off.
            // So, we're only updating the state for now
            code.setState(Code.STATE_OFF);
        } else {
            BigDecimal dimlevel = percent.toBigDecimal().setScale(2).divide(BIG_DECIMAL_100, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(maxDimLevel)).setScale(0, RoundingMode.HALF_UP);

            Values values = new Values();
            values.setDimlevel(dimlevel.intValue());
            code.setValues(values);
            code.setState(dimlevel.compareTo(BigDecimal.ZERO) == 1 ? Code.STATE_ON : Code.STATE_OFF);
        }
    }
}
