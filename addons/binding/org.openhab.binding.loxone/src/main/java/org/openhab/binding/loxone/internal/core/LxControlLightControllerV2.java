/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

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
public class LxControlLightControllerV2 extends LxControlAbstractController implements LxControlStateListener {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlLightControllerV2(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to light controller v2 controls
     */
    private static final String TYPE_NAME = "lightcontrollerv2";

    /**
     * State with list of active moods
     */
    public static final String STATE_ACTIVE_MOODS_LIST = "activemoods";
    /**
     * State with list of available moods
     */
    public static final String STATE_MOODS_LIST = "moodlist";

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

    // Following commands are not supported:
    // moveFavoriteMood, moveAdditionalMood, moveMood, addToFavoriteMood, removeFromFavoriteMood, learn, delete

    private Map<LxUuid, LxControlMood> moodList = new HashMap<>();
    private List<Integer> activeMoods = new ArrayList<>();
    private Integer minMoodId;
    private Integer maxMoodId;

    /**
     * Create lighting controller v2 object.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            controller's UUID
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            room to which controller belongs
     * @param category
     *            category to which controller belongs
     */
    LxControlLightControllerV2(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(client, uuid, json, room, category);
        // sub-controls of this control have been created when update() method was called by the super class constructor
        addStateListener(STATE_MOODS_LIST, this);
        addStateListener(STATE_ACTIVE_MOODS_LIST, this);
    }

    /**
     * Set a mood, deactivate other moods
     *
     * @param moodId
     *            ID of the mood to set
     * @throws IOException
     *             when something went wrong with communication
     */
    public void setMood(Integer moodId) throws IOException {
        if (isMoodOk(moodId)) {
            socketClient.sendAction(uuid, CMD_CHANGE_TO_MOOD + "/" + moodId);
        }
    }

    /**
     * Select next mood.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void nextMood() throws IOException {
        socketClient.sendAction(uuid, CMD_NEXT_MOOD);
    }

    /**
     * Select previous mood.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void previousMood() throws IOException {
        socketClient.sendAction(uuid, CMD_PREVIOUS_MOOD);
    }

    /**
     * Mix a mood into currently active moods.
     *
     * @param moodId
     *            ID of the mood to add
     * @throws IOException
     *             when something went wrong with communication
     */
    public void addMood(Integer moodId) throws IOException {
        if (isMoodOk(moodId)) {
            socketClient.sendAction(uuid, CMD_ADD_MOOD + "/" + moodId);
        }
    }

    /**
     * Mix a mood out of currently active moods.
     *
     * @param moodId
     *            ID of the mood to remove
     * @throws IOException
     *             when something went wrong with communication
     */
    public void removeMood(Integer moodId) throws IOException {
        if (isMoodOk(moodId)) {
            socketClient.sendAction(uuid, CMD_REMOVE_MOOD + "/" + moodId);
        }
    }

    /**
     * Get IDs of currently active moods.
     *
     * @return
     *         list of IDs of active moods or null if active moods list is not available
     */
    public List<Integer> getActiveMoods() {
        return activeMoods;
    }

    /**
     * Get all configured moods.
     *
     * @return
     *         Map with mood ID as key and mood control object as value or null if moods are not configured
     */
    public Map<LxUuid, LxControlMood> getMoods() {
        return moodList;
    }

    /**
     * Get minimum value a mood ID can have for the current list of moods.
     *
     * @return
     *         minimum value of a mood ID for the current list of moods or null if not defined
     */
    public Integer getMinMoodId() {
        return minMoodId;
    }

    /**
     * Get maximum value a mood ID can have for the current list of moods.
     *
     * @return
     *         maximum value of a mood ID for the current list of moods or null if not defined
     */
    public Integer getMaxMoodId() {
        return maxMoodId;
    }

    /**
     * Get configured and active moods from a new state value received from the Miniserver
     *
     * @param state
     *            state update from the Miniserver
     */
    @Override
    public void onStateChange(LxControlState state) {
        String stateName = state.getName();
        String text = state.getTextValue();
        logger.debug("Received state {} update to {}", stateName, text);
        try {
            if (STATE_MOODS_LIST.equals(stateName)) {
                LxJsonMood[] array = socketClient.getGson().fromJson(text, LxJsonMood[].class);
                moodList.clear();
                minMoodId = null;
                maxMoodId = null;
                LxJsonControl json = new LxJsonApp3().new LxJsonControl();
                for (LxJsonMood mood : array) {
                    if (mood.id != null && mood.name != null) {
                        logger.debug("Adding mood {} (name={}, isUsed={}, t5={}, static={}", mood.id, mood.name,
                                mood.isUsed, mood.isT5Controlled, mood.isStatic);
                        json.name = mood.name;
                        // mood-UUID = <controller-UUID>-M<mood-ID>
                        LxUuid moodUuid = new LxUuid(getUuid().toString() + "-M" + mood.id);
                        LxControlMood control = new LxControlMood(socketClient, moodUuid, json, getRoom(),
                                getCategory(), mood.id, mood.isStatic, this);
                        moodList.put(moodUuid, control);
                        if (minMoodId == null || minMoodId > mood.id) {
                            minMoodId = mood.id;
                        }
                        if (maxMoodId == null || maxMoodId < mood.id) {
                            maxMoodId = mood.id;
                        }
                    }
                }
            } else if (STATE_ACTIVE_MOODS_LIST.equals(stateName)) {
                // this state can be received before list of moods, but it contains a valid list of IDs
                Integer[] array = socketClient.getGson().fromJson(text, Integer[].class);
                activeMoods = Arrays.asList(array);
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Error parsing state {}: {}", stateName, e.getMessage());
        }
    }

    /**
     * Check if mood ID is within allowed range
     *
     * @param moodId
     *            mood ID to check
     * @return
     *         true if mood ID is within allowed range or range is not configured
     */
    private boolean isMoodOk(Integer moodId) {
        if ((minMoodId != null && minMoodId > moodId) || (maxMoodId != null && maxMoodId < moodId)) {
            return false;
        }
        return true;
    }

    /**
     * Check if mood is currently active.
     *
     * @param moodId
     *            mood ID to check
     * @return
     *         true if mood is currently active
     */
    boolean isMoodActive(Integer moodId) {
        return activeMoods.contains(moodId);
    }
}
