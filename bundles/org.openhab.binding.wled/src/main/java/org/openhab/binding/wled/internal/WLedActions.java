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
package org.openhab.binding.wled.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedActions} is responsible for Actions.
 *
 * @author Matthew Skinner - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = WLedActions.class)
@ThingActionsScope(name = "wled")
@NonNullByDefault
public class WLedActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable WLedBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (WLedBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "save state to preset", description = "Save a WLED state to a preset slot")
    public void savePreset(
            @ActionInput(name = "presetNumber", label = "Preset Slot", description = "Number for the preset slot you wish to use") int presetNumber) {
        savePreset(presetNumber, "");
    }

    public static void savePreset(@Nullable ThingActions actions, int presetNumber) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.savePreset(presetNumber, "");
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "save state to preset", description = "Save a WLED state to a preset slot")
    public void savePreset(
            @ActionInput(name = "presetNumber", label = "Preset Slot", description = "Number for the preset slot you wish to use") int presetNumber,
            @ActionInput(name = "presetName", label = "Preset Name", description = "Name for the preset that you wish to use") String presetName) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.savePreset(presetNumber, presetName);
        }
    }

    public static void savePreset(@Nullable ThingActions actions, int presetNumber, String presetName) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.savePreset(presetNumber, presetName);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "delete preset", description = "Delete a WLED preset slot")
    public void deletePreset(
            @ActionInput(name = "presetNumber", label = "Preset Slot", description = "Number of the preset slot you wish to delete") int presetNumber) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.deletePreset(presetNumber);
        }
    }

    public static void deletePreset(@Nullable ThingActions actions, int presetNumber) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.deletePreset(presetNumber);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "reboot WLED", description = "Reboot the WLED device")
    public void reboot() {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.reboot();
        }
    }

    public static void reboot(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.reboot();
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "send JSON state", description = "Send a raw JSON object to the WLED json/state API")
    public void sendState(
            @ActionInput(name = "json", label = "JSON", description = "A JSON object as described in the WLED JSON API documentation") String json) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.sendState(json);
        }
    }

    public static void sendState(@Nullable ThingActions actions, String json) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.sendState(json);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "get presets", description = "Get a text with a line for each WLED preset and playlist, with the timing and effects")
    public @ActionOutput(name = "presets", type = "java.lang.String") String getPresets() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getPresetsText();
    }

    public static String getPresets(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getPresets();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "get presets as JSON", description = "Get the presets.json of the WLED device with all settings of every preset")
    public @ActionOutput(name = "presetsJson", type = "java.lang.String") String getPresetsJson() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getPresetsJson();
    }

    public static String getPresetsJson(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getPresetsJson();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "save preset from JSON", description = "Save a preset with the content that you supply, like on, bri, transition (in 0.1 seconds) and seg")
    public void savePresetState(
            @ActionInput(name = "presetNumber", label = "Preset Slot", description = "Number for the preset slot you wish to use") int presetNumber,
            @ActionInput(name = "presetName", label = "Preset Name", description = "Name for the preset") String presetName,
            @ActionInput(name = "json", label = "JSON", description = "The state of the preset as JSON, for example {\"on\":true,\"bri\":128,\"transition\":20,\"seg\":[{\"id\":0,\"fx\":9}]}") String json) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.savePresetState(presetNumber, presetName, json);
        }
    }

    public static void savePresetState(@Nullable ThingActions actions, int presetNumber, String presetName,
            String json) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.savePresetState(presetNumber, presetName, json);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "save playlist", description = "Save a playlist that shows presets one after the other")
    public void savePlaylist(
            @ActionInput(name = "presetNumber", label = "Preset Slot", description = "Number for the slot you wish to use") int presetNumber,
            @ActionInput(name = "playlistName", label = "Playlist Name", description = "Name for the playlist") String playlistName,
            @ActionInput(name = "presets", label = "Presets", description = "The preset slots, separated by commas like 1,2,3") String presets,
            @ActionInput(name = "durations", label = "Durations", description = "Seconds to show each preset, separated by commas. One value is used for all presets") String durations,
            @ActionInput(name = "transitions", label = "Transitions", description = "Seconds to fade to each preset, separated by commas. One value is used for all presets") String transitions,
            @ActionInput(name = "repeat", label = "Repeat", description = "How often the playlist runs, 0 is forever") int repeat,
            @ActionInput(name = "endPreset", label = "End Preset", description = "Preset to apply when the playlist is done, 0 keeps the last one") int endPreset) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.savePlaylist(presetNumber, playlistName, presets, durations, transitions, repeat, endPreset);
        }
    }

    public static void savePlaylist(@Nullable ThingActions actions, int presetNumber, String playlistName,
            String presets, String durations, String transitions, int repeat, int endPreset) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.savePlaylist(presetNumber, playlistName, presets, durations, transitions, repeat, endPreset);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "get settings", description = "Get all settings of the WLED device as JSON, like the /json/cfg of the device")
    public @ActionOutput(name = "settings", type = "java.lang.String") String getSettings() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getSettingsJson();
    }

    public static String getSettings(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getSettings();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "get setting", description = "Get one setting of the WLED device")
    public @ActionOutput(name = "value", type = "java.lang.String") String getSetting(
            @ActionInput(name = "path", label = "Path", description = "The names of the setting separated by dots, like um.AudioReactive.sync.mode") String path) {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getSetting(path);
    }

    public static String getSetting(@Nullable ThingActions actions, String path) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getSetting(path);
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "set settings", description = "Change settings of the WLED device, the JSON only needs the settings that change")
    public void setSettings(
            @ActionInput(name = "json", label = "JSON", description = "The settings to change, like {\"um\":{\"AudioReactive\":{\"sync\":{\"mode\":1}}}}") String json) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.setSettings(json);
        }
    }

    public static void setSettings(@Nullable ThingActions actions, String json) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.setSettings(json);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "set setting", description = "Change one setting of the WLED device")
    public void setSetting(
            @ActionInput(name = "path", label = "Path", description = "The names of the setting separated by dots, like um.AudioReactive.sync.mode") String path,
            @ActionInput(name = "value", label = "Value", description = "The new value like 1, true or Kitchen") String value) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.setSetting(path, value);
        }
    }

    public static void setSetting(@Nullable ThingActions actions, String path, String value) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.setSetting(path, value);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "get state", description = "Get the raw /json/state of the WLED device")
    public @ActionOutput(name = "state", type = "java.lang.String") String getStateJson() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getStateJson();
    }

    public static String getStateJson(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getStateJson();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "get info", description = "Get the raw /json/info of the WLED device")
    public @ActionOutput(name = "info", type = "java.lang.String") String getInfoJson() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getInfoJson();
    }

    public static String getInfoJson(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getInfoJson();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "get nodes", description = "Get the other WLED devices that this device has found in the network")
    public @ActionOutput(name = "nodes", type = "java.lang.String") String getNodes() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getNodesJson();
    }

    public static String getNodes(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getNodes();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "get live LED colors", description = "Get the colors that every LED shows right now as JSON")
    public @ActionOutput(name = "leds", type = "java.lang.String") String getLiveLeds() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getLiveJson();
    }

    public static String getLiveLeds(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getLiveLeds();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "get effect data", description = "Get what each effect uses, like the names of the sliders and colors")
    public @ActionOutput(name = "effectData", type = "java.lang.String") String getEffectData() {
        WLedBridgeHandler localHandler = handler;
        return localHandler == null ? "" : localHandler.getEffectDataJson();
    }

    public static String getEffectData(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            return wLedActions.getEffectData();
        }
        throw new IllegalArgumentException("Instance is not a WLED class.");
    }

    @RuleAction(label = "sync clock", description = "Set the clock of the WLED device to the time of openHAB")
    public void syncClock() {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.syncClock();
        }
    }

    public static void syncClock(@Nullable ThingActions actions) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.syncClock();
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }
}
