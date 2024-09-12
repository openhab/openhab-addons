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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openhab.binding.loxone.internal.types.LxState;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * A Light Controller V2 type of control on Loxone Miniserver.
 * <p>
 * This control has been introduced in Loxone Config 9 in 2017 and it makes the {@link LxControlLightController}
 * obsolete. Both controls will exist for some time together.
 * <p>
 * Light controller V2 can have N outputs named AQ1...AQN that can function as Switch, Dimmer, RGB, Lumitech or Smart
 * Actuator functional blocks. Individual controls will be created for these outputs so they can be operated directly
 * and independently from the controller.
 * <p>
 * Controller can also have M moods configured. Each mood defines own subset of outputs and their settings, which will
 * be engaged when the mood is active. A dedicated switch control object will be created for each mood.
 * This effectively will allow for mixing various moods by individually enabling/disabling them.
 * <p>
 * It seems there is no imposed limitation for the number of outputs and moods.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlLightControllerV2 extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlLightControllerV2(uuid);
        }

        @Override
        String getType() {
            return "lightcontrollerv2";
        }
    }

    /**
     * State with list of active moods
     */
    private static final String STATE_ACTIVE_MOODS_LIST = "activemoods";
    /**
     * State with list of available moods
     */
    private static final String STATE_MOODS_LIST = "moodlist";

    /**
     * Command string used to set a given mood
     */
    private static final String CMD_CHANGE_TO_MOOD = "changeTo";
    /**
     * Command string used to change to the next mood
     */
    private static final String CMD_NEXT_MOOD = "plus";
    /**
     * Command string used to change to the previous mood
     */
    private static final String CMD_PREVIOUS_MOOD = "minus";
    /**
     * Command string used to add mood to the active moods (mix it in)
     */
    private static final String CMD_ADD_MOOD = "addMood";
    /**
     * Command string used to remove mood from the active moods (mix it out)
     */
    private static final String CMD_REMOVE_MOOD = "removeMood";

    private final transient Logger logger = LoggerFactory.getLogger(LxControlLightControllerV2.class);

    // Following commands are not supported:
    // moveFavoriteMood, moveAdditionalMood, moveMood, addToFavoriteMood, removeFromFavoriteMood, learn, delete

    private Map<Integer, LxControlMood> moodList = new HashMap<>();
    private List<Integer> activeMoods = new ArrayList<>();
    private ChannelUID channelId;

    private LxControlLightControllerV2(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        tags.add("Scene");
        // add only channel, state description will be added later when a control state update message is received
        channelId = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_LIGHT_CTRL),
                defaultChannelLabel, "Light controller V2", tags, this::handleCommands, this::getChannelState);
    }

    private void handleCommands(Command command) throws IOException {
        if (command instanceof UpDownType upDownCommand) {
            if (upDownCommand == UpDownType.UP) {
                sendAction(CMD_NEXT_MOOD);
            } else {
                sendAction(CMD_PREVIOUS_MOOD);
            }
        } else if (command instanceof DecimalType decimalCommand) {
            int moodId = decimalCommand.intValue();
            if (isMoodOk(moodId)) {
                sendAction(CMD_CHANGE_TO_MOOD + "/" + moodId);
            }
        }
    }

    private State getChannelState() {
        // update the single mood channel state
        if (activeMoods.size() == 1) {
            Integer id = activeMoods.get(0);
            if (isMoodOk(id)) {
                return new DecimalType(id);
            }
        }
        return UnDefType.UNDEF;
    }

    /**
     * Get configured and active moods from a new state value received from the Miniserver
     *
     * @param state state update from the Miniserver
     */
    @Override
    public void onStateChange(LxState state) {
        String stateName = state.getName();
        Object value = state.getStateValue();
        try {
            if (STATE_MOODS_LIST.equals(stateName) && value instanceof String string) {
                onMoodsListChange(string);
            } else if (STATE_ACTIVE_MOODS_LIST.equals(stateName) && value instanceof String string) {
                // this state can be received before list of moods, but it contains a valid list of IDs
                Integer[] array = getGson().fromJson(string, Integer[].class);
                activeMoods = Arrays.asList(array).stream().filter(id -> isMoodOk(id)).collect(Collectors.toList());
                // update all moods states - this will force update of channels too
                moodList.values().forEach(mood -> mood.onStateChange(null));
                // finally we update controller's state based on the active moods list
                super.onStateChange(state);
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Error parsing state {}: {}", stateName, e.getMessage());
        }
    }

    /**
     * Mix a mood into currently active moods.
     *
     * @param moodId ID of the mood to add
     * @throws IOException when something went wrong with communication
     */
    void addMood(Integer moodId) throws IOException {
        if (isMoodOk(moodId)) {
            sendAction(CMD_ADD_MOOD + "/" + moodId);
        }
    }

    /**
     * Check if mood is currently active.
     *
     * @param moodId mood ID to check
     * @return true if mood is currently active
     */
    boolean isMoodActive(Integer moodId) {
        return activeMoods.contains(moodId);
    }

    /**
     * Check if mood ID is within allowed range
     *
     * @param moodId mood ID to check
     * @return true if mood ID is within allowed range or range is not configured
     */
    boolean isMoodOk(Integer moodId) {
        return moodId != null && moodList.containsKey(moodId);
    }

    /**
     * Mix a mood out of currently active moods.
     *
     * @param moodId ID of the mood to remove
     * @throws IOException when something went wrong with communication
     */
    void removeMood(Integer moodId) throws IOException {
        if (isMoodOk(moodId)) {
            sendAction(CMD_REMOVE_MOOD + "/" + moodId);
        }
    }

    /**
     * Handles a change in the list of configured moods
     *
     * @param text json structure with new moods
     * @throws JsonSyntaxException error parsing json structure
     */
    private void onMoodsListChange(String text) throws JsonSyntaxException {
        LxControlMood[] array = getGson().fromJson(text, LxControlMood[].class);
        Map<Integer, LxControlMood> newMoodList = new HashMap<>();
        Integer minMoodId = null;
        Integer maxMoodId = null;
        for (LxControlMood mood : array) {
            Integer id = mood.getId();
            if (id != null && mood.getName() != null) {
                logger.debug("Adding mood (id={}, name={})", id, mood.getName());
                // mood-UUID = <controller-UUID>-M<mood-ID>
                LxUuid moodUuid = new LxUuid(getUuid().toString() + "-M" + id);
                mood.initialize(getConfig(), this, moodUuid);
                newMoodList.put(id, mood);
                if (minMoodId == null || minMoodId > id) {
                    minMoodId = id;
                }
                if (maxMoodId == null || maxMoodId < id) {
                    maxMoodId = id;
                }
            }
        }

        if (channelId != null && minMoodId != null && maxMoodId != null) {
            // convert all moods to options list for state description
            List<StateOption> optionsList = newMoodList.values().stream()
                    .map(mood -> new StateOption(mood.getId().toString(), mood.getName())).collect(Collectors.toList());
            addChannelStateDescriptionFragment(channelId,
                    StateDescriptionFragmentBuilder.create().withMinimum(new BigDecimal(minMoodId))
                            .withMaximum(new BigDecimal(maxMoodId)).withStep(BigDecimal.ONE).withReadOnly(false)
                            .withOptions(optionsList).build());
        }

        moodList.values().forEach(m -> removeControl(m));
        newMoodList.values().forEach(m -> addControl(m));
        moodList = newMoodList;
    }
}
