/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

/**
 * A radio-button type of control on Loxone Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlRadio extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlRadio(uuid);
        }

        @Override
        String getType() {
            return "radio";
        }
    }

    /**
     * Number of outputs a radio controller may have
     */
    private static final int MAX_RADIO_OUTPUTS = 16;

    /**
     * Radio-button has one state that is a number representing current active output
     */
    private static final String STATE_ACTIVE_OUTPUT = "activeoutput";

    /**
     * Command string used to set radio button to all outputs off
     */
    private static final String CMD_RESET = "reset";

    private Map<String, String> outputsMap;

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        // add both channel and state description (all needed configuration is available)
        ChannelUID cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RADIO_BUTTON),
                defaultChannelLabel, "Radio button", tags, this::handleCommands, this::getChannelState);

        if (details != null) {
            List<StateOption> outputs = new ArrayList<>();
            if (details.outputs != null) {
                outputsMap = details.outputs;
                outputs = details.outputs.entrySet().stream().map(e -> new StateOption(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());
            }
            if (details.allOff != null && !details.allOff.isEmpty()) {
                outputs.add(new StateOption("0", details.allOff));
                outputsMap.put("0", details.allOff);
            }
            addChannelStateDescriptionFragment(cid,
                    StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.ZERO)
                            .withMaximum(new BigDecimal(MAX_RADIO_OUTPUTS)).withStep(BigDecimal.ONE).withReadOnly(false)
                            .withOptions(outputs).build());
        }
    }

    private LxControlRadio(LxUuid uuid) {
        super(uuid);
    }

    private void handleCommands(Command command) throws IOException {
        if (((command instanceof OnOffType onOffCommand && onOffCommand == OnOffType.OFF)
                || DecimalType.ZERO.equals(command)) && outputsMap.containsKey("0")) {
            sendAction(CMD_RESET);
        } else if (command instanceof DecimalType output) {
            if (outputsMap.containsKey(output.toString())) {
                sendAction(String.valueOf(output.intValue()));
            }
        }
    }

    private DecimalType getChannelState() {
        Double output = getStateDoubleValue(STATE_ACTIVE_OUTPUT);
        if (output != null && output % 1 == 0 && outputsMap.containsKey(String.valueOf(output.intValue()))) {
            return new DecimalType(output);
        }
        return null;
    }
}
