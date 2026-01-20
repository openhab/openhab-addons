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
package org.openhab.binding.dirigera.internal.handler.matter;

import static org.openhab.binding.dirigera.internal.Constants.*;
import static org.openhab.binding.dirigera.internal.interfaces.Model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.binding.dirigera.internal.model.MatterModel;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Matter3ButtonCotroller3} is the custom handling for BILRESA 3-Button with 3 groups. For each group a
 * separate handler is created to handle the complexity.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Matter3ButtonCotroller extends BaseMatterHandler {
    private static final Map<String, String> TRIGGER_MAPPING = Map.of("singlePress", "SINGLE_PRESS", "doublePress",
            "DOUBLE_PRESS", "longPress", "LONG_PRESS");
    private final Logger logger = LoggerFactory.getLogger(Matter3ButtonCotroller.class);
    private final Map<String, List<String>> modeLinkCandidateMapping = Map.of("light",
            List.of(DEVICE_TYPE_LIGHT, DEVICE_TYPE_OUTLET), "speaker", List.of(DEVICE_TYPE_SPEAKER), "notConfigured",
            List.of());
    private Map<String, String> triggerChannelMapping = new HashMap<>();
    private Map<String, String> groupControllerMapping = new HashMap<>();
    private boolean linkUpdateCycle = false;

    public Matter3ButtonCotroller(Thing thing) {
        super(thing);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        config = getConfigAs(BaseDeviceConfiguration.class);
        if (!getGateway()) {
            return;
        }
        configure();
        super.initialize();
    }

    /**
     * Custom configuration of the 3 button sub-devices. Given device id has 2 additional ids for the other buttons.
     */
    @Override
    protected void configure() {
        // check if device is already configured
        if (deviceModelMap.isEmpty()) {
            String relationId = gateway().model().getRelationId(config.id);
            Map<String, String> connectedDevices = gateway().model().getRelations(relationId);
            connectedDevices.forEach((key, value) -> {
                int subDeviceId = Character.getNumericValue(key.charAt(key.length() - 1));
                String group = "switch-" + (subDeviceId / 3);
                groupControllerMapping.put(group, key);
                createChannelIfNecessary(group + "#" + CHANNEL_LINKS, CHANNEL_LINKS, CoreItemFactory.STRING);
                createChannelIfNecessary(group + "#" + CHANNEL_LINK_CANDIDATES, CHANNEL_LINK_CANDIDATES,
                        CoreItemFactory.STRING);
                createChannelIfNecessary(group + "#" + CHANNEL_CONTROL_MODE, "mode-2-options", CoreItemFactory.NUMBER);
                createTriggerChannels(group, relationId, subDeviceId);
            });
        }
    }

    private void createTriggerChannels(String group, String relationId, int subDeviceId) {
        for (int i = subDeviceId; i > (subDeviceId - 3); i--) {
            String deviceId = relationId + "_" + i;
            deviceModelMap.put(deviceId, new MatterModel(deviceId, thing.getThingTypeUID().getId(), true));
            triggerChannelMapping.put(deviceId, createTriggerChannel(group, i));
        }
    }

    private String createTriggerChannel(String group, int number) {
        var buttonName = switch (number % 3) {
            case 0 -> "Button Press";
            case 1 -> "Scroll Down";
            case 2 -> "Scroll Up";
            default -> "Button " + number;
        };
        String triggerChannelName = group + "#" + buttonName.toLowerCase(Locale.ENGLISH).replace(" ", "-");
        createChannelIfNecessary(triggerChannelName, "system.button", "", buttonName,
                "Triggers for " + buttonName.toLowerCase(Locale.ENGLISH));
        return triggerChannelName;
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);

        // handle remotePress events
        String channelName = triggerChannelMapping.get(update.optString(JSON_KEY_DEVICE_ID));
        String clickPattern = TRIGGER_MAPPING.get(update.optString(EVENT_KEY_CLICK_PATTER));
        if (channelName != null && clickPattern != null) {
            logger.warn("Button {} pressed: {}", channelName, clickPattern);
            triggerChannel(channelName, clickPattern);
        }

        // change link candidates id control-mode switched
        JSONObject attributes = update.optJSONObject(JSON_KEY_ATTRIBUTES);
        if (attributes != null) {
            String controlMode = attributes.optString(ATTRIBUTES_KEY_CONTROL_MODE);
            String deviceId = update.optString(JSON_KEY_DEVICE_ID);
            logger.debug("handleUpdate: deviceId={}, controlMode={}", deviceId, controlMode);
            if (!controlMode.isBlank() && !deviceId.isBlank()) {
                // update state
                String targetChannel = controllerToGroup(deviceId) + "#" + CHANNEL_CONTROL_MODE;
                logger.debug("handleUpdate: deviceId={}, controlMode={}", deviceId, controlModeToNumber(controlMode));
                super.updateState(new ChannelUID(thing.getUID(), targetChannel),
                        new DecimalType(controlModeToNumber(controlMode)));
                List<String> candidateTypes = modeLinkCandidateMapping.get(controlMode);
                if (candidateTypes != null) {
                    linkHandlerMap.put(deviceId, new LinkHandler(this, deviceId, candidateTypes));
                    gateway().updateLinks();
                    logger.trace("Link candidate types for control-mode {}: {}", controlMode, candidateTypes);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
            return;
        }
        logger.warn("handleCommand: channelUID={}, command={}", channelUID, command);
        String channelId = channelUID.getIdWithoutGroup();
        String group = channelUID.getGroupId();
        if (group == null) {
            logger.warn("handleCommand: group is null for channelUID={}", channelUID);
            return;
        }
        String targetController = groupToControllerId(group);
        if (CHANNEL_CONTROL_MODE.equals(channelId)) {
            if (command instanceof DecimalType decimal) {
                String targetValue = numberToControlMode(decimal.intValue());
                logger.warn("handleCommand: setting control mode of group {} (controller {}) to {}", group,
                        targetController, targetValue);
                JSONObject attributesObj = new JSONObject();
                attributesObj.put(ATTRIBUTES_KEY_CONTROL_MODE, targetValue);
                JSONObject patchObj = new JSONObject();
                patchObj.put(JSON_KEY_ATTRIBUTES, attributesObj);
                super.sendPatch(targetController, patchObj);
            }
        } else if (CHANNEL_LINKS.equals(channelId) || CHANNEL_LINK_CANDIDATES.equals(channelId)) {
            LinkHandler linkHandler = linkHandlerMap.get(targetController);
            if (linkHandler == null) {
                logger.warn("handleCommand: no link handler found for controller {}", targetController);
                return;
            } else {
                linkHandler.handleCommand(channelUID, command);
            }
        } else {
            logger.warn("handleCommand: passing command to super for channel {}", channelId);
            super.handleCommand(channelUID, command);
        }
    }

    private String groupToControllerId(String group) {
        return groupControllerMapping.getOrDefault(group, "");
    }
    // String orElse = groupControllerMapping.entrySet().stream().filter(entry -> deviceId.equals(entry.getValue()))
    // .map(Map.Entry::getKey).findFirst().orElse("");
    // return orElse;

    private String controllerToGroup(String deviceId) {
        String orElse = groupControllerMapping.entrySet().stream().filter(entry -> deviceId.equals(entry.getValue()))
                .map(Map.Entry::getKey).findFirst().orElse("");
        return (orElse == null) ? "" : orElse;
    }

    private String numberToControlMode(int number) {
        return switch (number) {
            case 0 -> "notConfigured";
            case 1 -> "shortcut";
            case 2 -> "light";
            case 3 -> "speaker";
            default -> "notConfigured";
        };
    }

    private int controlModeToNumber(String mode) {
        return switch (mode) {
            case "notConfigured" -> 0;
            case "shortcut" -> 1;
            case "light" -> 2;
            case "speaker" -> 3;
            default -> 0;
        };
    }

    @Override
    public void updateLinksStart() {
        linkUpdateCycle = true;
        super.updateLinksStart();
    }

    /**
     * Adds a soft link towards the device which has the link stored in his attributes
     *
     * @param device id of the device which contains this link
     */
    @Override
    public void addSoftlink(String linkSourceId, String linkTargetId) {
        LinkHandler linkHandler = linkHandlerMap.get(linkTargetId);
        if (linkHandler != null) {
            linkHandler.addSoftlink(linkSourceId);
        }
    }

    /**
     * Collect all informations from link handlers, combine them and update state and command options
     */
    @Override
    public void updateLinksDone() {
        if (!linkUpdateCycle) {
            return;
        }
        linkHandlerMap.forEach((deviceId, linkHandler) -> {
            String group = controllerToGroup(deviceId);
            ChannelUID linkChannelUUID = new ChannelUID(getThing().getUID(), group + "#" + CHANNEL_LINKS);
            gateway().getCommandProvider().setCommandOptions(linkChannelUUID, linkHandler.getLinkOptions());
            updateState(linkChannelUUID, StringType.valueOf(getCommandLabels(linkHandler.getLinkOptions()).toString()));

            ChannelUID candidateChannelUUID = new ChannelUID(getThing().getUID(),
                    group + "#" + CHANNEL_LINK_CANDIDATES);
            gateway().getCommandProvider().setCommandOptions(candidateChannelUUID, linkHandler.getCandidateOptions());
            updateState(candidateChannelUUID,
                    StringType.valueOf(getCommandLabels(linkHandler.getCandidateOptions()).toString()));
        });
        linkUpdateCycle = false;
    }

    private List<String> getCommandLabels(List<CommandOption> options) {
        return options.stream().map(CommandOption::getLabel).filter(Objects::nonNull).toList();
    }

    @Override
    public String getNameForId(String deviceId) {
        return thing.getLabel() + " " + controllerToGroup(deviceId);
    }
}
