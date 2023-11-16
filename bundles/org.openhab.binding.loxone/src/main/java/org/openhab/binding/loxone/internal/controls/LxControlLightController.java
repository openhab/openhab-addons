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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.loxone.internal.types.LxState;
import org.openhab.binding.loxone.internal.types.LxTags;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

/**
 * A Light Controller type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a light controller is one of following functional blocks:
 * <ul>
 * <li>Lighting Controller
 * <li>Hotel Lighting Controller
 * </ul>
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlLightController extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlLightController(uuid);
        }

        @Override
        String getType() {
            return "lightcontroller";
        }
    }

    /**
     * Number of scenes supported by the Miniserver. Indexing starts with 0 to NUM_OF_SCENES-1.
     */
    private static final int NUM_OF_SCENES = 10;

    /**
     * Current active scene number (0-9)
     */
    private static final String STATE_ACTIVE_SCENE = "activescene";
    /**
     * List of available scenes (public state, so user can monitor scene list updates)
     */
    private static final String STATE_SCENE_LIST = "scenelist";
    /**
     * Command string used to set control's state to ON
     */
    private static final String CMD_ON = "On";
    /**
     * Command string used to set control's state to OFF
     */
    private static final String CMD_OFF = "Off";
    /**
     * Command string used to go to the next scene
     */
    private static final String CMD_NEXT_SCENE = "plus";
    /**
     * Command string used to go to the previous scene
     */
    private static final String CMD_PREVIOUS_SCENE = "minus";
    private static final int SCENE_ALL_ON = 9;

    private List<StateOption> sceneNames = new ArrayList<>();
    private ChannelUID channelId;

    private LxControlLightController(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        tags.addAll(LxTags.SCENE);
        // add only channel, state description will be added later when a control state update message is received
        channelId = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_LIGHT_CTRL),
                defaultChannelLabel, "Light controller", tags, this::handleCommands, this::getChannelState);
    }

    private void handleCommands(Command command) throws IOException {
        if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand == OnOffType.ON) {
                sendAction(CMD_ON);
            } else {
                sendAction(CMD_OFF);
            }
        } else if (command instanceof UpDownType upDownCommand) {
            if (upDownCommand == UpDownType.UP) {
                sendAction(CMD_NEXT_SCENE);
            } else {
                sendAction(CMD_PREVIOUS_SCENE);
            }
        } else if (command instanceof DecimalType decimalCommand) {
            int scene = decimalCommand.intValue();
            if (scene == SCENE_ALL_ON) {
                sendAction(CMD_ON);
            } else if (scene >= 0 && scene < NUM_OF_SCENES) {
                sendAction(Long.toString(scene));
            }
        }
    }

    private DecimalType getChannelState() {
        Double value = getStateDoubleValue(STATE_ACTIVE_SCENE);
        if (value != null && value >= 0 && value < NUM_OF_SCENES) {
            return new DecimalType(value);
        }
        return null;
    }

    /**
     * Get scene names from new state value received from the Miniserver
     */
    @Override
    public void onStateChange(LxState state) {
        if (STATE_SCENE_LIST.equals(state.getName()) && channelId != null) {
            Object value = state.getStateValue();
            if (value instanceof String str) {
                sceneNames.clear();
                String[] scenes = str.split(",");
                for (String line : scenes) {
                    line = line.replace("\"", "");
                    String[] params = line.split("=");
                    if (params.length == 2) {
                        sceneNames.add(new StateOption(params[0], params[1]));
                    }
                }
                addChannelStateDescriptionFragment(channelId,
                        StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.ZERO)
                                .withMaximum(new BigDecimal(NUM_OF_SCENES - 1)).withStep(BigDecimal.ONE)
                                .withReadOnly(false).withOptions(sceneNames).build());
            }
        } else {
            super.onStateChange(state);
        }
    }
}
