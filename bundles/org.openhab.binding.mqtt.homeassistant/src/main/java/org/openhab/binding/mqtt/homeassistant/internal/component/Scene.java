/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;

/**
 * A MQTT scene, following the https://www.home-assistant.io/integrations/scene.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Scene extends AbstractComponent<Scene.Configuration> {
    public static final String SCENE_CHANNEL_ID = "scene";

    // A command that has already been processed and routed to the correct Value,
    // and should be immediately published. This will be the payloadOn value from
    // the configuration
    private static class SceneCommand extends StringType {
        SceneCommand(String value) {
            super(value);
        }
    }

    // A value that can provide a proper CommandDescription with values and labels
    class SceneValue extends TextValue {
        SceneValue() {
        }

        @Override
        public CommandDescriptionBuilder createCommandDescription() {
            CommandDescriptionBuilder builder = super.createCommandDescription();
            objectIdToScene.forEach((k, v) -> builder.withCommandOption(new CommandOption(k, v.getName())));
            return builder;
        }
    }

    public static class Configuration extends EntityConfiguration {
        private final String commandTopic;

        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Scene");
            commandTopic = getString("command_topic");
            // Oddly, Home Assistant doesn't provide a default for this
            if (config.get("payload_on") == null) {
                config.put("payload_on", "ON");
            }
        }

        String getCommandTopic() {
            return commandTopic;
        }

        String getPayloadOn() {
            return getString("payload_on");
        }

        boolean isRetain() {
            return getBoolean("retain");
        }
    }

    // Keeps track of discrete command topics, and one SceneValue that uses that topic
    private final Map<String, ChannelState> topicsToChannelStates = new HashMap<>();
    private final Map<String, Configuration> objectIdToScene = new TreeMap<>();
    private final Map<String, Configuration> labelToScene = new HashMap<>();

    private final SceneValue value = new SceneValue();
    private ComponentChannel channel;

    public Scene(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        if (config.commandTopic.isEmpty()) {
            throw new ConfigurationException("command_topic is required");
        }

        // Name the channel with a constant, not the component ID
        // So that we only end up with a single channel for all scenes
        componentId = SCENE_CHANNEL_ID;
        groupId = null;

        channel = buildChannel(SCENE_CHANNEL_ID, ComponentChannelType.STRING, value, "Scene",
                componentContext.getUpdateListener())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos())
                .commandFilter(this::handleCommand).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        topicsToChannelStates.put(config.getCommandTopic(), channel.getState());
        addScene(this);
    }

    ComponentChannel getChannel() {
        return channel;
    }

    private void addScene(Scene scene) {
        Configuration config = scene.getConfig();
        objectIdToScene.put(scene.getHaID().objectID, config);
        labelToScene.put(config.getName(), config);

        if (!topicsToChannelStates.containsKey(config.commandTopic)) {
            hiddenChannels.add(scene.getChannel());
            topicsToChannelStates.put(config.commandTopic, scene.getChannel().getState());
        }
    }

    private boolean handleCommand(Command command) {
        // This command has already been processed by the rest of this method,
        // so just return immediately.
        if (command instanceof SceneCommand) {
            return true;
        }

        String valueStr = command.toString();
        Configuration sceneConfig = objectIdToScene.get(valueStr);
        if (sceneConfig == null) {
            sceneConfig = labelToScene.get(command.toString());
        }
        if (sceneConfig == null) {
            throw new IllegalArgumentException("Value " + valueStr + " not within range");
        }

        ChannelState state = Objects.requireNonNull(topicsToChannelStates.get(sceneConfig.commandTopic));
        // This will end up calling this same method, so be sure no further processing is done
        state.publishValue(new SceneCommand(sceneConfig.getPayloadOn()));

        return false;
    }

    @Override
    public String getName() {
        return "Scene";
    }

    @Override
    public boolean mergeable(AbstractComponent<?> other) {
        return other instanceof Scene;
    }

    @Override
    public boolean merge(AbstractComponent<?> other) {
        Scene newScene = (Scene) other;
        org.openhab.core.config.core.Configuration newConfiguration = mergeChannelConfiguration(channel, newScene);

        addScene(newScene);

        // Recreate the channel so that the configuration will have all the scenes
        stop();
        channel = buildChannel(SCENE_CHANNEL_ID, ComponentChannelType.STRING, value, "Scene",
                componentContext.getUpdateListener()).withConfiguration(newConfiguration)
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos())
                .commandFilter(this::handleCommand).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        // New ChannelState created; need to make sure we're referencing the correct one
        topicsToChannelStates.put(config.getCommandTopic(), channel.getState());
        return true;
    }
}
