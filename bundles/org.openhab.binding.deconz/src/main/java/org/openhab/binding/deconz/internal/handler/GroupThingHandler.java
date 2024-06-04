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
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;
import static org.openhab.binding.deconz.internal.Util.constrainToRange;
import static org.openhab.binding.deconz.internal.Util.kelvinToMired;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deconz.internal.DeconzDynamicCommandDescriptionProvider;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.action.GroupActions;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.GroupAction;
import org.openhab.binding.deconz.internal.dto.GroupMessage;
import org.openhab.binding.deconz.internal.dto.GroupState;
import org.openhab.binding.deconz.internal.dto.Scene;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This group thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GroupThingHandler extends DeconzBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Set.of(THING_TYPE_LIGHTGROUP);
    private final Logger logger = LoggerFactory.getLogger(GroupThingHandler.class);
    private final DeconzDynamicCommandDescriptionProvider commandDescriptionProvider;

    private Map<String, String> scenes = Map.of();
    private GroupState groupStateCache = new GroupState();
    private String colorMode = "";

    public GroupThingHandler(Thing thing, Gson gson,
            DeconzDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing, gson, ResourceType.GROUPS);
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public void initialize() {
        ThingConfig thingConfig = getConfigAs(ThingConfig.class);
        colorMode = thingConfig.colormode;

        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();

        GroupAction newGroupAction = new GroupAction();
        switch (channelId) {
            case CHANNEL_ALL_ON, CHANNEL_ANY_ON -> {
                if (command instanceof RefreshType) {
                    valueUpdated(channelUID, groupStateCache);
                    return;
                }
            }
            case CHANNEL_ALERT -> {
                if (command instanceof StringType) {
                    newGroupAction.alert = command.toString();
                } else {
                    return;
                }
            }
            case CHANNEL_COLOR -> {
                if (command instanceof OnOffType) {
                    newGroupAction.on = (command == OnOffType.ON);
                } else if (command instanceof HSBType hsbCommand) {
                    // XY color is the implicit default: Use XY color mode if i) no color mode is set or ii) if the bulb
                    // is in CT mode or iii) already in XY mode. Only if the bulb is in HS mode, use this one.
                    if ("hs".equals(colorMode)) {
                        newGroupAction.hue = (int) (hsbCommand.getHue().doubleValue() * HUE_FACTOR);
                        newGroupAction.sat = Util.fromPercentType(hsbCommand.getSaturation());
                        newGroupAction.bri = Util.fromPercentType(hsbCommand.getBrightness());
                    } else {
                        double[] xy = ColorUtil.hsbToXY(hsbCommand);
                        newGroupAction.xy = new double[] { xy[0], xy[1] };
                        newGroupAction.bri = (int) (xy[2] * BRIGHTNESS_MAX);
                    }
                } else if (command instanceof PercentType percentCommand) {
                    newGroupAction.bri = Util.fromPercentType(percentCommand);
                } else if (command instanceof DecimalType decimalCommand) {
                    newGroupAction.bri = decimalCommand.intValue();
                } else {
                    return;
                }

                // send on/off state together with brightness if not already set or unknown
                Integer newBri = newGroupAction.bri;
                if (newBri != null) {
                    newGroupAction.on = (newBri > 0);
                }
                Double transitiontime = config.transitiontime;
                if (transitiontime != null) {
                    // value is in 1/10 seconds
                    newGroupAction.transitiontime = (int) Math.round(10 * transitiontime);
                }
            }
            case CHANNEL_COLOR_TEMPERATURE -> {
                if (command instanceof DecimalType decimalCommand) {
                    int miredValue = kelvinToMired(decimalCommand.intValue());
                    newGroupAction.ct = constrainToRange(miredValue, ZCL_CT_MIN, ZCL_CT_MAX);
                    newGroupAction.on = true;
                }
            }
            case CHANNEL_SCENE -> {
                if (command instanceof StringType) {
                    getIdFromSceneName(command.toString())
                            .thenAccept(id -> sendCommand(null, command, channelUID, "scenes/" + id + "/recall", null))
                            .exceptionally(e -> {
                                logger.debug("Ignoring command {} for {}, scene is not found in available scenes {}.",
                                        command, channelUID, scenes);
                                return null;
                            });
                }
                return;
            }
            default -> {
                // no supported command
                return;
            }
        }

        Boolean newOn = newGroupAction.on;
        if (newOn != null && !newOn) {
            // if light shall be off, no other commands are allowed, so reset the new light state
            newGroupAction.clear();
            newGroupAction.on = false;
        }

        sendCommand(newGroupAction, command, channelUID, null);
    }

    @Override
    protected void processStateResponse(DeconzBaseMessage stateResponse) {
        scenes = processScenes(stateResponse);
        messageReceived(stateResponse);
    }

    private void valueUpdated(ChannelUID channelUID, GroupState newState) {
        switch (channelUID.getId()) {
            case CHANNEL_ALL_ON -> updateSwitchChannel(channelUID, newState.allOn);
            case CHANNEL_ANY_ON -> updateSwitchChannel(channelUID, newState.anyOn);
        }
    }

    @Override
    public void messageReceived(DeconzBaseMessage message) {
        if (message instanceof GroupMessage groupMessage) {
            logger.trace("{} received {}", thing.getUID(), groupMessage);
            GroupState groupState = groupMessage.state;
            if (groupState != null) {
                updateStatus(ThingStatus.ONLINE);
                thing.getChannels().stream().map(Channel::getUID).forEach(c -> valueUpdated(c, groupState));
                groupStateCache = groupState;
            }
            GroupAction groupAction = groupMessage.action;
            if (groupAction != null) {
                if (colorMode.isEmpty()) {
                    String cmode = groupAction.colormode;
                    if (cmode != null && ("hs".equals(cmode) || "xy".equals(cmode))) {
                        // only set the color mode if it is hs or xy, not ct
                        colorMode = cmode;
                    }
                }
            }
        } else {
            logger.trace("{} received {}", thing.getUID(), message);
            getSceneNameFromId(message.scid).thenAccept(v -> updateState(CHANNEL_SCENE, v));
        }
    }

    private CompletableFuture<String> getIdFromSceneName(String sceneName) {
        CompletableFuture<String> f = new CompletableFuture<>();

        Util.getKeysFromValue(scenes, sceneName).findAny().ifPresentOrElse(f::complete, () -> {
            // we need to check if that is a new scene
            logger.trace("Scene name {} not found in {}, refreshing scene list", sceneName, thing.getUID());
            requestState(stateResponse -> {
                scenes = processScenes(stateResponse);
                Util.getKeysFromValue(scenes, sceneName).findAny().ifPresentOrElse(f::complete,
                        () -> f.completeExceptionally(new IllegalArgumentException("Scene not found")));
            });
        });

        return f;
    }

    private CompletableFuture<State> getSceneNameFromId(String sceneId) {
        CompletableFuture<State> f = new CompletableFuture<>();

        String sceneName = scenes.get(sceneId);
        if (sceneName != null) {
            // we already know that name, exit early
            f.complete(new StringType(sceneName));
        } else {
            // we need to check if that is a new scene
            logger.trace("Scene name for id {} not found in {}, refreshing scene list", sceneId, thing.getUID());
            requestState(stateResponse -> {
                scenes = processScenes(stateResponse);
                String newSceneId = scenes.get(sceneId);
                if (newSceneId != null) {
                    f.complete(new StringType(newSceneId));
                } else {
                    logger.debug("Scene name for id {} not found in {} even after refreshing scene list.", sceneId,
                            thing.getUID());
                    f.complete(UnDefType.UNDEF);
                }
            });
        }

        return f;
    }

    private Map<String, String> processScenes(DeconzBaseMessage stateResponse) {
        if (stateResponse instanceof GroupMessage groupMessage) {
            Map<String, String> scenes = groupMessage.scenes.stream()
                    .collect(Collectors.toMap(scene -> scene.id, scene -> scene.name));
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_SCENE);
            commandDescriptionProvider.setCommandOptions(channelUID,
                    groupMessage.scenes.stream().map(Scene::toCommandOption).collect(Collectors.toList()));
            return scenes;
        }
        return Map.of();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(GroupActions.class);
    }
}
