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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.ROConfiguration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * A MQTT Update component, following the https://www.home-assistant.io/integrations/update.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Update extends AbstractComponent<Update.Configuration> implements ChannelStateUpdateListener {
    public static final String UPDATE_CHANNEL_ID = "update";
    public static final String LATEST_VERSION_CHANNEL_ID = "latest-version";

    public static class Configuration extends EntityConfiguration implements ROConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Update");
        }

        @Nullable
        String getCommandTopic() {
            return getOptionalString("command_topic");
        }

        @Nullable
        Value getLatestVersionTemplate() {
            return getOptionalValue("latest_version_template");
        }

        @Nullable
        String getLatestVersionTopic() {
            return getOptionalString("latest_version_topic");
        }

        @Nullable
        String getPayloadInstall() {
            return getOptionalString("payload_install");
        }

        @Nullable
        String getReleaseSummary() {
            return getOptionalString("release_summary");
        }

        @Nullable
        String getReleaseUrl() {
            return getOptionalString("release_url");
        }

        boolean isRetain() {
            return getBoolean("retain");
        }

        @Nullable
        String getTitle() {
            return getOptionalString("title");
        }
    }

    /**
     * Describes the state payload if it's JSON
     */
    public static class ReleaseState {
        // these are designed to fit in with the default property of firmwareVersion
        public static final String PROPERTY_LATEST_VERSION = "latestFirmwareVersion";
        public static final String PROPERTY_TITLE = "firmwareTitle";
        public static final String PROPERTY_RELEASE_SUMMARY = "firmwareSummary";
        public static final String PROPERTY_RELEASE_URL = "firmwareURL";

        @Nullable
        String installedVersion;
        @Nullable
        String latestVersion;
        @Nullable
        String title;
        @Nullable
        String releaseSummary;
        @Nullable
        String releaseUrl;
        @Nullable
        String entityPicture;

        public Map<String, String> appendToProperties(Map<String, String> properties) {
            String installedVersion = this.installedVersion;
            if (installedVersion != null && !installedVersion.isBlank()) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, installedVersion);
            }
            // don't remove the firmwareVersion property; it might be coming from the
            // device as well

            String latestVersion = this.latestVersion;
            if (latestVersion != null) {
                properties.put(PROPERTY_LATEST_VERSION, latestVersion);
            } else {
                properties.remove(PROPERTY_LATEST_VERSION);
            }
            String title = this.title;
            if (title != null) {
                properties.put(PROPERTY_TITLE, title);
            } else {
                properties.remove(title);
            }
            String releaseSummary = this.releaseSummary;
            if (releaseSummary != null) {
                properties.put(PROPERTY_RELEASE_SUMMARY, releaseSummary);
            } else {
                properties.remove(PROPERTY_RELEASE_SUMMARY);
            }
            String releaseUrl = this.releaseUrl;
            if (releaseUrl != null) {
                properties.put(PROPERTY_RELEASE_URL, releaseUrl);
            } else {
                properties.remove(PROPERTY_RELEASE_URL);
            }
            return properties;
        }
    }

    public interface ReleaseStateListener {
        void releaseStateUpdated(ReleaseState newState);
    }

    private final Logger logger = LoggerFactory.getLogger(Update.class);

    private ComponentChannel updateChannel;
    private @Nullable ComponentChannel latestVersionChannel;
    private boolean updatable = false;
    private ReleaseState state = new ReleaseState();
    private @Nullable ReleaseStateListener listener = null;

    public Update(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        TextValue value = new TextValue();
        String commandTopic = config.getCommandTopic();
        String payloadInstall = config.getPayloadInstall();

        var builder = buildChannel(UPDATE_CHANNEL_ID, ComponentChannelType.STRING, value, "Update", this);
        builder.stateTopic(config.getStateTopic(), config.getValueTemplate());
        if (commandTopic != null && payloadInstall != null) {
            updatable = true;
            builder.commandTopic(commandTopic, config.isRetain(), config.getQos());
        }
        updateChannel = builder.build(false);

        String latestVersionTopic = config.getLatestVersionTopic();
        if (latestVersionTopic != null) {
            value = new TextValue();
            latestVersionChannel = buildChannel(LATEST_VERSION_CHANNEL_ID, ComponentChannelType.STRING, value,
                    "Latest Version", this).stateTopic(latestVersionTopic, config.getLatestVersionTemplate())
                    .build(false);
        }

        state.title = config.getTitle();
        state.releaseSummary = config.getReleaseSummary();
        state.releaseUrl = config.getReleaseUrl();

        addJsonAttributesChannel();
    }

    /**
     * Returns if this device can be updated
     */
    public boolean isUpdatable() {
        return updatable;
    }

    /**
     * Trigger an OTA update for this device
     */
    public void doUpdate() {
        if (!updatable) {
            return;
        }
        String commandTopic = Objects.requireNonNull(config.getCommandTopic());
        String payloadInstall = Objects.requireNonNull(config.getPayloadInstall());

        updateChannel.getState().publishValue(new StringType(payloadInstall)).handle((v, ex) -> {
            if (ex != null) {
                logger.debug("Failed publishing value {} to topic {}: {}", payloadInstall, commandTopic,
                        ex.getMessage());
            } else {
                logger.debug("Successfully published value {} to topic {}", payloadInstall, commandTopic);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        var updateFuture = updateChannel.start(connection, scheduler, timeout);
        ComponentChannel latestVersionChannel = this.latestVersionChannel;
        if (latestVersionChannel == null) {
            return updateFuture;
        }

        var latestVersionFuture = latestVersionChannel.start(connection, scheduler, timeout);
        return CompletableFuture.allOf(updateFuture, latestVersionFuture);
    }

    @Override
    public CompletableFuture<@Nullable Void> stop() {
        var updateFuture = updateChannel.stop();
        ComponentChannel latestVersionChannel = this.latestVersionChannel;
        if (latestVersionChannel == null) {
            return updateFuture;
        }

        var latestVersionFuture = latestVersionChannel.stop();
        return CompletableFuture.allOf(updateFuture, latestVersionFuture);
    }

    @Override
    public void updateChannelState(ChannelUID channelUID, State value) {
        switch (channelUID.getIdWithoutGroup()) {
            case UPDATE_CHANNEL_ID:
                String strValue = value.toString();
                try {
                    // check if it's JSON first
                    @Nullable
                    final ReleaseState releaseState = componentContext.getGson().fromJson(strValue, ReleaseState.class);
                    if (releaseState != null) {
                        state = releaseState;
                        notifyReleaseStateUpdated();
                        return;
                    }
                } catch (JsonSyntaxException e) {
                    // Ignore; it's just a string of installed_version
                }
                state.installedVersion = strValue;
                break;
            case LATEST_VERSION_CHANNEL_ID:
                state.latestVersion = value.toString();
                break;
        }
        notifyReleaseStateUpdated();
    }

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        throw new UnsupportedOperationException();
    }

    public void setReleaseStateUpdateListener(ReleaseStateListener listener) {
        this.listener = listener;
        notifyReleaseStateUpdated();
    }

    private void notifyReleaseStateUpdated() {
        var listener = this.listener;
        if (listener != null) {
            listener.releaseStateUpdated(state);
        }
    }
}
