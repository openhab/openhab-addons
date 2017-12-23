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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;

import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.exception.KNXFormatException;

@NonNullByDefault
class TypeDimmer extends KNXChannelType {

    TypeDimmer() {
        super(CHANNEL_DIMMER);
    }

    @Override
    public @Nullable CommandSpec getCommandSpec(Configuration configuration, Command command)
            throws KNXFormatException {
        ChannelConfiguration confSwitch = parse((String) configuration.get(SWITCH_GA));
        ChannelConfiguration confPosition = parse((String) configuration.get(POSITION_GA));

        if (command instanceof OnOffType) {
            if (confSwitch != null) {
                return new CommandSpec(confSwitch, getDefaultDPT(SWITCH_GA), command);
            } else if (confPosition != null) {
                return new CommandSpec(confPosition, getDefaultDPT(POSITION_GA),
                        (PercentType) ((OnOffType) command).as(PercentType.class));
            }
        }

        if (command instanceof PercentType) {
            if (confPosition != null) {
                return new CommandSpec(confPosition, getDefaultDPT(POSITION_GA), command);
            } else if (confSwitch != null) {
                return new CommandSpec(confSwitch, getDefaultDPT(SWITCH_GA),
                        (OnOffType) ((PercentType) command).as(OnOffType.class));
            }
        }

        if (command instanceof UpDownType) {
            return new CommandSpec(parse((String) configuration.get(INCREASE_DECREASE_GA)),
                    getDefaultDPT(INCREASE_DECREASE_GA), command);
        }

        return null;
    }

    @Override
    protected Set<String> getAllGAKeys() {
        return Stream.of(SWITCH_GA, POSITION_GA, INCREASE_DECREASE_GA).collect(toSet());
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        if (Objects.equals(gaConfigKey, INCREASE_DECREASE_GA)) {
            return DPTXlator3BitControlled.DPT_CONTROL_DIMMING.getID();
        }
        if (Objects.equals(gaConfigKey, SWITCH_GA)) {
            return DPTXlatorBoolean.DPT_SWITCH.getID();
        }
        if (Objects.equals(gaConfigKey, POSITION_GA)) {
            return DPTXlator8BitUnsigned.DPT_SCALING.getID();
        }
        throw new IllegalArgumentException("GA configuration '" + gaConfigKey + "' is not supported");
    }
}
