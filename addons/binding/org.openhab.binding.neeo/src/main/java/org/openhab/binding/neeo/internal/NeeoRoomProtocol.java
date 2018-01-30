/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.NeeoUtil;
import org.openhab.binding.neeo.internal.models.ExecuteResult;
import org.openhab.binding.neeo.internal.models.ExecuteStep;
import org.openhab.binding.neeo.internal.models.NeeoAction;
import org.openhab.binding.neeo.internal.models.NeeoRecipe;
import org.openhab.binding.neeo.internal.models.NeeoRecipes;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.models.NeeoScenario;
import org.openhab.binding.neeo.internal.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This protocol class for a Neeo Room
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoRoomProtocol {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoRoomProtocol.class);

    /** The {@link NeeoHandlerCallback} */
    private final NeeoHandlerCallback callback;

    /** The {@link NeeoRoom} */
    private final NeeoRoom neeoRoom;

    /** The currently active scenarios */
    private final AtomicReference<String[]> activeScenarios = new AtomicReference<>(null);

    /** The currently active step */
    private final AtomicReference<String> currentStep = new AtomicReference<>(null);

    /** Gson to serialize active scenarios */
    private final Gson gson = NeeoUtil.getGson();

    /**
     * Instantiates a new neeo room protocol.
     *
     * @param callback the non-null callback
     * @param roomKey the non-empty room key
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NeeoRoomProtocol(NeeoHandlerCallback callback, String roomKey) throws IOException {
        Objects.requireNonNull(callback, "callback cannot be null");
        Objects.requireNonNull(roomKey, "roomKey cannot be empty");

        this.callback = callback;

        final NeeoBrainApi api = callback.getApi();
        if (api == null) {
            throw new IllegalArgumentException("NeeoBrainApi cannot be null");
        }

        neeoRoom = api.getRoom(roomKey);

        if (neeoRoom == null) {
            throw new IllegalArgumentException("room (" + roomKey + ") was not found in the NEEO Brain");
        }
    }

    /**
     * Returns the callback being used
     *
     * @return the non-null callback
     */
    public NeeoHandlerCallback getCallback() {
        return callback;
    }

    /**
     * Processes the action if it applies to this room
     *
     * @param action a non-null action to process
     */
    public void processAction(NeeoAction action) {
        final NeeoRecipes recipes = neeoRoom.getRecipes();
        if (recipes == null) {
            return;
        }

        final boolean launch = StringUtils.equalsIgnoreCase(NeeoRecipe.LAUNCH, action.getAction());
        final boolean poweroff = StringUtils.equalsIgnoreCase(NeeoRecipe.POWEROFF, action.getAction());

        // Can't be both true but if both false - it's neither one
        if (launch == poweroff) {
            return;
        }

        final NeeoRecipe recipe = recipes.getRecipeByName(action.getRecipe());
        if (recipe != null) {
            processScenarioChange(recipe.getScenarioKey(), launch);
        } else {
            logger.debug("Could not find a recipe named '{}' for the action {}", action.getRecipe(), action);
        }
    }

    /**
     * Processes a change to the scenario (whether it's been launched or not)
     *
     * @param scenarioKey a non-null, non-empty scenario key
     * @param launch true if the scenario was launched, false otherwise
     */
    private void processScenarioChange(String scenarioKey, boolean launch) {
        NeeoUtil.requireNotEmpty(scenarioKey, "scenarioKey cannot be empty");

        final String[] activeScenarios = this.activeScenarios.get();

        final int idx = ArrayUtils.indexOf(activeScenarios, scenarioKey);

        // already set that way
        if ((idx < 0 && !launch) || (idx >= 0 && launch)) {
            return;
        }

        final String[] newScenarios = idx >= 0 ? (String[]) ArrayUtils.remove(activeScenarios, idx)
                : (String[]) ArrayUtils.add(activeScenarios, scenarioKey);

        this.activeScenarios.set(newScenarios);
        triggerActiveScenarios();

        refreshScenarioStatus(scenarioKey);
    }

    /**
     * Sends the channel trigger for active scenarios
     */
    private void triggerActiveScenarios() {
        final String[] newScenarios = this.activeScenarios.get();

        final List<String> scenarioNames = new ArrayList<>();
        if (newScenarios != null) {
            for (String key : newScenarios) {
                final NeeoScenario scenario = neeoRoom.getScenarios().getScenario(key);
                if (scenario == null) {
                    scenarioNames.add(key);
                } else {
                    scenarioNames.add(scenario.getName());
                }
            }
        }
        callback.triggerEvent(UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_STATEID,
                NeeoConstants.ROOM_CHANNEL_ACTIVESCENARIOS), gson.toJson(scenarioNames));
    }

    /**
     * Refresh state of the room - currently only refreshes the active scenarios via {@link #refreshActiveScenarios()}
     */
    public void refreshState() {
        refreshActiveScenarios();
    }

    /**
     * Refresh the recipe name
     *
     * @param recipeKey the non-empty recipe key
     */
    public void refreshRecipeName(String recipeKey) {
        NeeoUtil.requireNotEmpty(recipeKey, "recipeKey cannot be empty");

        final NeeoRecipe recipe = neeoRoom.getRecipes().getRecipe(recipeKey);
        if (recipe != null) {
            callback.stateChanged(UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_RECIPEID,
                    NeeoConstants.ROOM_CHANNEL_NAME, recipeKey), new StringType(recipe.getName()));
        }
    }

    /**
     * Refresh the recipe type
     *
     * @param recipeKey the non-empty recipe key
     */
    public void refreshRecipeType(String recipeKey) {
        NeeoUtil.requireNotEmpty(recipeKey, "recipeKey cannot be empty");

        final NeeoRecipe recipe = neeoRoom.getRecipes().getRecipe(recipeKey);
        if (recipe != null) {
            callback.stateChanged(UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_RECIPEID,
                    NeeoConstants.ROOM_CHANNEL_TYPE, recipeKey), new StringType(recipe.getType()));
        }
    }

    /**
     * Refresh whether the recipe is enabled
     *
     * @param recipeKey the non-null recipe key
     */
    public void refreshRecipeEnabled(String recipeKey) {
        NeeoUtil.requireNotEmpty(recipeKey, "recipeKey cannot be empty");

        final NeeoRecipe recipe = neeoRoom.getRecipes().getRecipe(recipeKey);
        if (recipe != null) {
            callback.stateChanged(
                    UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_RECIPEID,
                            NeeoConstants.ROOM_CHANNEL_ENABLED, recipeKey),
                    recipe.isEnabled() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    /**
     * Refresh the recipe status.
     *
     * @param recipeKey the non-null recipe key
     */
    public void refreshRecipeStatus(String recipeKey) {
        NeeoUtil.requireNotEmpty(recipeKey, "recipeKey cannot be empty");

        final NeeoRecipe recipe = neeoRoom.getRecipes().getRecipe(recipeKey);
        if (recipe != null) {
            callback.stateChanged(UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_RECIPEID,
                    NeeoConstants.ROOM_CHANNEL_STATUS, recipeKey), OnOffType.OFF);
        }
    }

    /**
     * Refresh the scenario name.
     *
     * @param scenarioKey the non-null scenario key
     */
    public void refreshScenarioName(String scenarioKey) {
        NeeoUtil.requireNotEmpty(scenarioKey, "scenarioKey cannot be empty");

        final NeeoScenario scenario = neeoRoom.getScenarios().getScenario(scenarioKey);
        if (scenario != null) {
            callback.stateChanged(UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_SCENARIOID,
                    NeeoConstants.ROOM_CHANNEL_NAME, scenarioKey), new StringType(scenario.getName()));
        }
    }

    /**
     * Refresh whether the scenario is configured.
     *
     * @param scenarioKey the non-null scenario key
     */
    public void refreshScenarioConfigured(String scenarioKey) {
        NeeoUtil.requireNotEmpty(scenarioKey, "scenarioKey cannot be empty");

        final NeeoScenario scenario = neeoRoom.getScenarios().getScenario(scenarioKey);
        if (scenario != null) {
            callback.stateChanged(
                    UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_SCENARIOID,
                            NeeoConstants.ROOM_CHANNEL_ENABLED, scenarioKey),
                    scenario.isConfigured() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    /**
     * Refresh the scenario status.
     *
     * @param scenarioKey the non-null scenario key
     */
    public void refreshScenarioStatus(String scenarioKey) {
        NeeoUtil.requireNotEmpty(scenarioKey, "scenarioKey cannot be empty");

        final NeeoScenario scenario = neeoRoom.getScenarios().getScenario(scenarioKey);
        if (scenario != null) {
            final String[] active = activeScenarios.get();
            final boolean isActive = ArrayUtils.contains(active, scenarioKey);
            callback.stateChanged(UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_SCENARIOID,
                    NeeoConstants.ROOM_CHANNEL_STATUS, scenarioKey), isActive ? OnOffType.ON : OnOffType.OFF);
        }
    }

    /**
     * Refresh active scenarios
     */
    private void refreshActiveScenarios() {
        final NeeoBrainApi api = callback.getApi();
        if (api == null) {
            logger.debug("API is null [likely bridge is offline]");
        } else {
            try {
                final String[] activeScenarios = api.getActiveScenarios();
                final String[] oldScenarios = this.activeScenarios.getAndSet(activeScenarios);

                if (!ArrayUtils.isEquals(activeScenarios, oldScenarios)) {
                    if (activeScenarios != null) {
                        triggerActiveScenarios();

                        for (String scenario : activeScenarios) {
                            refreshScenarioStatus(scenario);
                        }
                    }

                    if (oldScenarios != null) {
                        for (String oldScenario : oldScenarios) {
                            if (!ArrayUtils.contains(activeScenarios, oldScenario)) {
                                refreshScenarioStatus(oldScenario);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.debug("Exception requesting active scenarios: {}", e.getMessage(), e);
                // callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Refresh the current step
     */
    private void refreshCurrentStep() {
        final String step = currentStep.get();
        callback.triggerEvent(UidUtils.createChannelId(NeeoConstants.ROOM_CHANNEL_GROUP_STATEID,
                NeeoConstants.ROOM_CHANNEL_CURRENTSTEP), step);
    }

    /**
     * Starts the given recipe key
     *
     * @param recipeKey the non-null recipe key
     */
    public void startRecipe(String recipeKey) {
        NeeoUtil.requireNotEmpty(recipeKey, "recipeKey cannot be empty");

        final NeeoBrainApi api = callback.getApi();
        if (api == null) {
            logger.debug("API is null [likely bridge is offline] - cannot start recipe: {}", recipeKey);
        } else {
            final NeeoRecipe recipe = neeoRoom.getRecipes().getRecipe(recipeKey);

            if (recipe != null) {
                if (recipe.isEnabled()) {
                    final boolean isLaunch = StringUtils.equalsIgnoreCase(NeeoRecipe.LAUNCH, recipe.getType());

                    try {
                        if (isLaunch) {
                            handleExecuteResult(recipe.getScenarioKey(), recipeKey, true,
                                    api.executeRecipe(neeoRoom.getKey(), recipeKey));
                        } else {
                            handleExecuteResult(recipe.getScenarioKey(), recipeKey, false,
                                    api.stopScenario(neeoRoom.getKey(), recipe.getScenarioKey()));
                        }
                    } catch (IOException e) {
                        logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                } else {
                    logger.debug("recipe for key {} was not enabled, cannot start or stop", recipeKey);
                }
            } else {
                logger.debug("recipe key {} was not found", recipeKey);
            }
        }
    }

    /**
     * Sets the scenario status.
     *
     * @param scenarioKey the non-null scenario key
     * @param start whether to start (true) or stop (false) the scenario
     */
    public void setScenarioStatus(String scenarioKey, boolean start) {
        NeeoUtil.requireNotEmpty(scenarioKey, "scenarioKey cannot be empty");

        final NeeoRecipe recipe = neeoRoom.getRecipes().getRecipeByScenarioKey(scenarioKey,
                start ? NeeoRecipe.LAUNCH : NeeoRecipe.POWEROFF);

        if (recipe != null) {
            if (recipe.isEnabled()) {
                startRecipe(recipe.getKey());
            } else {
                logger.debug("Recipe ({}) found for scenario {} but was not enabled", recipe.getKey(), scenarioKey);
            }
        } else {
            logger.debug("No recipe found for scenario {} to start ({})", scenarioKey, start);
        }
    }

    /**
     * Handle the {@link ExecuteResult} from a call
     *
     * @param scenarioKey the non-null scenario key being changed
     * @param recipeKey the non-null recipe key being used
     * @param launch whether the recipe launches the scenario (true) or not (false)
     * @param result the non-null result (null will do nothing)
     */
    private void handleExecuteResult(String scenarioKey, String recipeKey, boolean launch, ExecuteResult result) {
        Objects.requireNonNull(result, "result cannot be empty");
        NeeoUtil.requireNotEmpty(recipeKey, "recipeKey cannot be empty");

        int nextStep = 0;
        callback.scheduleTask(() -> {
            processScenarioChange(scenarioKey, launch);
        }, 1);

        for (final ExecuteStep step : result.getSteps()) {
            callback.scheduleTask(() -> {
                currentStep.set(step.getText());
                refreshCurrentStep();
            }, nextStep);
            nextStep += step.getDuration();
        }

        callback.scheduleTask(() -> {
            currentStep.set(null);
            refreshCurrentStep();
            refreshRecipeStatus(recipeKey);
        }, nextStep);
    }
}
