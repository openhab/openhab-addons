/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.handler;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectPushApi;
import org.openhab.binding.robonect.internal.model.ExtInfo;
import org.openhab.binding.robonect.internal.model.Extension;
import org.openhab.binding.robonect.internal.model.Extensions;
import org.openhab.binding.robonect.internal.model.Push;
import org.openhab.binding.robonect.internal.model.PushInfo;
import org.openhab.binding.robonect.internal.model.PushServer;
import org.openhab.binding.robonect.internal.model.PushTriggers;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles push and extension endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 * @author Nico Brüttner - Direct push API injection
 */
@NonNullByDefault
final class RobonectPushHandler {
    private static final Pattern PUSH_TRIGGER_CHANNEL_PATTERN = Pattern.compile("push-trigger-(.+)-(name|enter|leave)");
    private static final Pattern PUSH_TRIGGER_COMMAND_PATTERN = Pattern
            .compile("push-trigger-(\\d+)-[a-z0-9_-]+-(enter|leave)");
    private final Logger logger = LoggerFactory.getLogger(RobonectPushHandler.class);
    private final RobonectHandler handler;
    private final RobonectPushApi pushApi;

    /**
     * Creates a push and extension component backed by the Thing handler.
     *
     * @param handler the {@link RobonectHandler} providing state updates
     * @param pushApi the push API for the current client
     */
    RobonectPushHandler(RobonectHandler handler, RobonectPushApi pushApi) {
        this.handler = handler;
        this.pushApi = pushApi;
    }

    /** Updates optional GPIO and output extension state. */
    void refreshExtensionInfo() {
        ExtInfo info = pushApi.getExtInfo();
        Extensions extensions = info.getExt();
        if (!info.isSuccessful() || extensions == null) {
            return;
        }
        updateExtension("extension-gpio-1-", extensions.getGpio1());
        updateExtension("extension-gpio-2-", extensions.getGpio2());
        updateExtension("extension-output-1-", extensions.getOut1());
        updateExtension("extension-output-2-", extensions.getOut2());
    }

    /** Updates non-sensitive push-service settings. */
    void refreshPushInfo() {
        applyPushInfo(pushApi.getPushInfo(), true);
    }

    /** Reads the push mode from the optional web interface when publishing is allowed. */
    void refreshPushMode() {
        if (!handler.isUnofficialApiEnabled() || !handler.canPublishState()) {
            return;
        }
        try {
            applyPushMode(pushApi.getPushModeViaWebInterface());
        } catch (RobonectCommunicationException e) {
            logger.debug("Could not retrieve push mode from the web interface: {}", e.getMessage());
        }
    }

    /**
     * Publishes the push mode, or an undefined state if unavailable, while the unofficial API is enabled.
     *
     * @param mode the push mode, or {@code null} if unavailable
     */
    private void applyPushMode(@Nullable Integer mode) {
        if (handler.isUnofficialApiEnabled()) {
            handler.setState(CHANNEL_PUSH_MODE, mode == null ? UnDefType.UNDEF : new DecimalType(mode));
        }
    }

    /**
     * Validates and writes a push mode through the optional web interface.
     *
     * @param command the command received for the channel
     */
    void handleModeCommand(Command command) {
        if (!(command instanceof DecimalType decimal)) {
            logger.debug("Ignoring push mode command of type {}; DecimalType is expected.",
                    command.getClass().getName());
            return;
        }
        if (!handler.isUnofficialApiEnabled()) {
            return;
        }
        int mode;
        try {
            mode = decimal.toBigDecimal().intValueExact();
        } catch (ArithmeticException e) {
            logger.debug("Ignoring non-integer push mode {}", command);
            return;
        }
        if (mode < 0 || mode > 2) {
            logger.debug("Ignoring invalid push mode {}", mode);
            return;
        }
        applyPushMode(pushApi.updatePushModeViaWebInterface(mode));
    }

    /**
     * Applies push settings and reconciles trigger channels using response completeness.
     *
     * @param info the push-service response to apply
     * @param fullResponse whether the response contains all push triggers
     */
    private void applyPushInfo(PushInfo info, boolean fullResponse) {
        Push push = info.getPush();
        if (!info.isSuccessful() || push == null) {
            return;
        }
        PushServer server = push.getServer();
        if (server != null) {
            handler.setState(CHANNEL_PUSH_URL,
                    server.getUrl() != null ? new StringType(server.getUrl()) : UnDefType.NULL);
            handler.setState(CHANNEL_PUSH_USERNAME,
                    server.getName() != null ? new StringType(server.getName()) : UnDefType.NULL);
        }
        PushTriggers triggers = push.getTrigger();
        if (triggers != null) {
            Integer interval = triggers.getInterval();
            if (interval != null)
                handler.setState(CHANNEL_PUSH_INTERVAL, new QuantityType<>(interval, Units.SECOND));
            List<Channel> channels = new ArrayList<>();
            Map<String, PushTriggers.Trigger> namedTriggers = getNamedPushTriggers(triggers);
            for (Map.Entry<String, PushTriggers.Trigger> entry : namedTriggers.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).toList()) {
                String name = entry.getKey();
                String prefix = "push-trigger-" + name + "-";
                String label = "Push Trigger " + name.substring(0, name.indexOf('-')) + " " + entry.getValue().getName()
                        + " ";
                channels.add(
                        handler.createChannel(prefix + "enter", "Switch", "pushTriggerSwitchType", label + "Enter"));
                channels.add(
                        handler.createChannel(prefix + "leave", "Switch", "pushTriggerSwitchType", label + "Leave"));
            }
            // A write response may contain only the updated trigger; preserve other dynamic channels.
            if (fullResponse) {
                handler.updateDynamicChannels(PUSH_TRIGGER_CHANNEL_PATTERN, channels);
            } else {
                handler.addDynamicChannels(channels);
            }
            for (Map.Entry<String, PushTriggers.Trigger> entry : namedTriggers.entrySet()) {
                String prefix = "push-trigger-" + entry.getKey() + "-";
                PushTriggers.Trigger trigger = entry.getValue();
                handler.updateSwitch(prefix + "enter", trigger.getEnter());
                handler.updateSwitch(prefix + "leave", trigger.getLeave());
            }
        }
    }

    /**
     * Returns triggers keyed by their index and normalized valid name.
     *
     * @param triggers the push triggers returned by the mower
     * @return the triggers indexed by their normalized channel-name suffix
     */
    private Map<String, PushTriggers.Trigger> getNamedPushTriggers(PushTriggers triggers) {
        Map<String, PushTriggers.Trigger> namedTriggers = new HashMap<>();
        for (Map.Entry<String, PushTriggers.Trigger> entry : triggers.getTriggers().entrySet()) {
            String name = entry.getValue().getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            String channelName = name.toLowerCase(Locale.ROOT).replace(' ', '_');
            if (!channelName.matches("[a-z0-9_-]+")) {
                logger.debug("Ignoring push trigger {} with an invalid channel name", entry.getKey());
                continue;
            }
            namedTriggers.put(entry.getKey() + "-" + channelName, entry.getValue());
        }
        return namedTriggers;
    }

    /**
     * Updates one trigger while preserving its other enter or leave setting.
     *
     * @param channelId the channel identifier
     * @param command the command received for the channel
     */
    void handleTriggerCommand(String channelId, Command command) {
        Matcher matcher = PUSH_TRIGGER_COMMAND_PATTERN.matcher(channelId);
        if (!matcher.matches()) {
            return;
        }
        if (!(command instanceof OnOffType onOff)) {
            logger.debug("Ignoring push trigger command of type {}; OnOffType is expected.",
                    command.getClass().getName());
            return;
        }
        PushInfo info = pushApi.getPushInfo();
        Push push = info.getPush();
        PushTriggers triggers = push == null ? null : push.getTrigger();
        PushTriggers.Trigger trigger = triggers == null ? null : triggers.getTriggers().get(matcher.group(1));
        if (!info.isSuccessful() || trigger == null || trigger.getEnter() == null || trigger.getLeave() == null) {
            return;
        }
        boolean enter = matcher.group(2).equals("enter") ? onOff == OnOffType.ON : trigger.getEnter();
        boolean leave = matcher.group(2).equals("leave") ? onOff == OnOffType.ON : trigger.getLeave();
        int value = (enter ? 1 : 0) | (leave ? 2 : 0);
        handler.cancelCommandRefresh("push");
        PushInfo result = pushApi.updatePush("trigger" + matcher.group(1), Integer.toString(value));
        if (result.isSuccessful()) {
            applyPushInfo(result, false);
            handler.scheduleCommandRefresh("push", this::refreshPushInfo);
        }
    }

    /**
     * Writes a supported push service setting and schedules a delayed refresh.
     *
     * @param channelId the channel identifier
     * @param command the command received for the channel
     */
    void handleCommand(String channelId, Command command) {
        if (channelId.equals(CHANNEL_PUSH_PASSWORD)) {
            handler.setState(CHANNEL_PUSH_PASSWORD, UnDefType.UNDEF);
        }
        String parameter;
        String value;
        if (command instanceof StringType string) {
            parameter = switch (channelId) {
                case CHANNEL_PUSH_URL -> "url";
                case CHANNEL_PUSH_USERNAME -> "name";
                case CHANNEL_PUSH_PASSWORD -> "pass";
                default -> null;
            };
            value = string.toFullString();
        } else if (command instanceof Number num && channelId.equals(CHANNEL_PUSH_INTERVAL)) {
            parameter = "interval";
            value = String
                    .valueOf(command instanceof QuantityType<?> quantity ? quantity.toUnit(Units.SECOND).intValue()
                            : num.intValue());
        } else {
            logUnsupportedCommandType(channelId, command);
            return;
        }
        if (parameter != null) {
            handler.cancelCommandRefresh("push");
            PushInfo result = pushApi.updatePush(parameter, value);
            if (result.isSuccessful()) {
                applyPushInfo(result, false);
                handler.scheduleCommandRefresh("push", this::refreshPushInfo);
            }
        } else {
            logUnsupportedCommandType(channelId, command);
        }
    }

    /**
     * Logs a command whose type is unsupported for a recognized push setting channel.
     *
     * @param channelId the push channel identifier
     * @param command the unsupported command
     */
    private void logUnsupportedCommandType(String channelId, Command command) {
        @Nullable
        String expectedType = switch (channelId) {
            case CHANNEL_PUSH_URL, CHANNEL_PUSH_USERNAME, CHANNEL_PUSH_PASSWORD -> "StringType";
            case CHANNEL_PUSH_INTERVAL -> "Number";
            default -> null;
        };
        if (expectedType != null) {
            logger.debug("Ignoring command of type {} for push channel {}; {} is expected.",
                    command.getClass().getName(), channelId, expectedType);
        }
    }

    /**
     * Publishes available fields for one GPIO or output extension.
     *
     * @param prefix the channel identifier prefix
     * @param extension the extension information, or {@code null} if absent
     */
    private void updateExtension(String prefix, @Nullable Extension extension) {
        if (extension == null) {
            return;
        }
        handler.updateSwitch(prefix + "flash", extension.getFlashonerror());
        handler.updateSwitch(prefix + "inverted", extension.getInverted());
        handler.updateSwitch(prefix + "status", extension.getStatus());
    }
}
