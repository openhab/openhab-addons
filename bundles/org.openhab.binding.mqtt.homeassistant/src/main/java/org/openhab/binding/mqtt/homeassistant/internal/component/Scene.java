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
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT scene, following the https://www.home-assistant.io/integrations/scene.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Scene extends AbstractComponent<Scene.ChannelConfiguration> {
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
            super();
        }

        @Override
        public CommandDescriptionBuilder createCommandDescription() {
            CommandDescriptionBuilder builder = super.createCommandDescription();
            objectIdToScene.forEach((k, v) -> builder.withCommandOption(new CommandOption(k, v.getName())));
            return builder;
        }
    }

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Scene");
        }

        @SerializedName("command_topic")
        protected String commandTopic = "";

        @SerializedName("payload_on")
        protected String payloadOn = "ON";
    }

    // Keeps track of discrete command topics, and one SceneValue that uses that topic
    private final Map<String, ChannelState> topicsToChannelStates = new HashMap<>();
    private final Map<String, ChannelConfiguration> objectIdToScene = new TreeMap<>();
    private final Map<String, ChannelConfiguration> labelToScene = new HashMap<>();

    private final SceneValue value = new SceneValue();
    private ComponentChannel channel;

    public Scene(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        if (channelConfiguration.commandTopic.isEmpty()) {
            throw new ConfigurationException("command_topic is required");
        }

        // Name the channel with a constant, not the component ID
        // So that we only end up with a single channel for all scenes
        componentId = SCENE_CHANNEL_ID;
        groupId = null;

        channel = buildChannel(SCENE_CHANNEL_ID, ComponentChannelType.STRING, value, getName(),
                componentConfiguration.getUpdateListener())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .commandFilter(this::handleCommand).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        topicsToChannelStates.put(channelConfiguration.commandTopic, channel.getState());
        addScene(this);
    }

    ComponentChannel getChannel() {
        return channel;
    }

    private void addScene(Scene scene) {
        ChannelConfiguration channelConfiguration = scene.getChannelConfiguration();
        objectIdToScene.put(scene.getHaID().objectID, channelConfiguration);
        labelToScene.put(channelConfiguration.getName(), channelConfiguration);

        if (!topicsToChannelStates.containsKey(channelConfiguration.commandTopic)) {
            hiddenChannels.add(scene.getChannel());
            topicsToChannelStates.put(channelConfiguration.commandTopic, scene.getChannel().getState());
        }
    }

    private boolean handleCommand(Command command) {
        // This command has already been processed by the rest of this method,
        // so just return immediately.
        if (command instanceof SceneCommand) {
            return true;
        }

        String valueStr = command.toString();
        ChannelConfiguration sceneConfig = objectIdToScene.get(valueStr);
        if (sceneConfig == null) {
            sceneConfig = labelToScene.get(command.toString());
        }
        if (sceneConfig == null) {
            throw new IllegalArgumentException("Value " + valueStr + " not within range");
        }

        ChannelState state = Objects.requireNonNull(topicsToChannelStates.get(sceneConfig.commandTopic));
        // This will end up calling this same method, so be sure no further processing is done
        state.publishValue(new SceneCommand(sceneConfig.payloadOn));

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
        Configuration newConfiguration = mergeChannelConfiguration(channel, newScene);

        addScene(newScene);

        // Recreate the channel so that the configuration will have all the scenes
        stop();
        channel = buildChannel(SCENE_CHANNEL_ID, ComponentChannelType.STRING, value, "Scene",
                componentConfiguration.getUpdateListener())
                .withConfiguration(newConfiguration)
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .commandFilter(this::handleCommand).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        // New ChannelState created; need to make sure we're referencing the correct one
        topicsToChannelStates.put(channelConfiguration.commandTopic, channel.getState());
        return true;
    }
}
