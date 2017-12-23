/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;

import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.exception.KNXFormatException;

@NonNullByDefault
class TypeRollershutter extends KNXChannelType {

    TypeRollershutter() {
        super(CHANNEL_ROLLERSHUTTER);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        if (Objects.equals(gaConfigKey, UP_DOWN_GA)) {
            return DPTXlatorBoolean.DPT_UPDOWN.getID();
        }
        if (Objects.equals(gaConfigKey, STOP_MOVE_GA)) {
            return DPTXlatorBoolean.DPT_START.getID();
        }
        if (Objects.equals(gaConfigKey, POSITION_GA)) {
            return DPTXlator8BitUnsigned.DPT_SCALING.getID();
        }
        throw new IllegalArgumentException("GA configuration '" + gaConfigKey + "' is not supported");
    }

    @Override
    public @Nullable CommandSpec getCommandSpec(Configuration configuration, Command command)
            throws KNXFormatException {
        ChannelConfiguration confUpDown = parse((String) configuration.get(UP_DOWN_GA));
        ChannelConfiguration confPosition = parse((String) configuration.get(POSITION_GA));

        if (command instanceof UpDownType) {
            if (confUpDown != null) {
                return new CommandSpec(confUpDown, getDefaultDPT(UP_DOWN_GA), command);
            } else if (confPosition != null) {
                return new CommandSpec(confPosition, getDefaultDPT(POSITION_GA),
                        (PercentType) ((UpDownType) command).as(PercentType.class));
            }
        }

        if (command instanceof PercentType) {
            if (confPosition != null) {
                return new CommandSpec(confPosition, getDefaultDPT(POSITION_GA), command);
            } else if (confUpDown != null) {
                return new CommandSpec(confUpDown, getDefaultDPT(UP_DOWN_GA),
                        (UpDownType) ((PercentType) command).as(UpDownType.class));
            }
        }

        if (command instanceof StopMoveType) {
            return new CommandSpec(parse((String) configuration.get(STOP_MOVE_GA)), getDefaultDPT(STOP_MOVE_GA),
                    command);
        }

        return null;
    }

    @Override
    protected Set<String> getAllGAKeys() {
        return Stream.of(UP_DOWN_GA, STOP_MOVE_GA, POSITION_GA).collect(toSet());
    }

}
