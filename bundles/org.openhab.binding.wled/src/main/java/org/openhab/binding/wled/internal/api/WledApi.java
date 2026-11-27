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
package org.openhab.binding.wled.internal.api;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.StateOption;

/**
 * The {@link WledApi} is the JSON API methods that can be extended for different firmware versions.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public interface WledApi {
    void update() throws ApiException;

    void initialize() throws ApiException;

    int getFirmwareVersion() throws ApiException;

    String sendGetRequest(String string) throws ApiException;

    /**
     * Turns on/off ALL segments
     */
    void setGlobalOn(boolean bool) throws ApiException;

    /**
     * Turns on/off just THIS segment
     */
    void setMasterOn(boolean bool, int segmentIndex) throws ApiException;

    /**
     * Sets the brightness of ALL segments
     */
    void setGlobalBrightness(PercentType percent) throws ApiException;

    /**
     * Sets the brightness of just THIS segment
     */
    void setMasterBrightness(PercentType percent, int segmentIndex) throws ApiException;

    /**
     * Stops any running FX and instantly changes the segment to the desired colour
     */
    void setMasterHSB(HSBType hsbType, int segmentIndex) throws ApiException;

    void setEffect(String string, int segmentIndex) throws ApiException;

    void setPreset(String string) throws ApiException;

    void setPalette(String string, int segmentIndex) throws ApiException;

    void setFxIntencity(PercentType percentType, int segmentIndex) throws ApiException;

    void setFxSpeed(PercentType percentType, int segmentIndex) throws ApiException;

    void setSleep(boolean bool) throws ApiException;

    void setSleepMode(String value) throws ApiException;

    void setSleepDuration(BigDecimal time) throws ApiException;

    void setSleepTargetBrightness(PercentType percent) throws ApiException;

    void setUdpSend(boolean bool) throws ApiException;

    void setUdpReceive(boolean bool) throws ApiException;

    void setTransitionTime(BigDecimal time) throws ApiException;

    void setPresetCycle(boolean bool) throws ApiException;

    void setPrimaryColor(HSBType hsbType, int segmentIndex) throws ApiException;

    void setSecondaryColor(HSBType hsbType, int segmentIndex) throws ApiException;

    void setTertiaryColor(HSBType hsbType, int segmentIndex) throws ApiException;

    void setWhiteOnly(PercentType percentType, int segmentIndex) throws ApiException;

    void setLegacyWhite(String whiteChannel, PercentType brightness, int segmentIndex) throws ApiException;

    void setMirror(boolean bool, int segmentIndex) throws ApiException;

    void setReverse(boolean bool, int segmentIndex) throws ApiException;

    void setLiveOverride(String value) throws ApiException;

    void setGrouping(int value, int segmentIndex) throws ApiException;

    void setSpacing(int value, int segmentIndex) throws ApiException;

    /**
     * Saves a preset to the position number with the supplied name. If the supplied name is an empty String then the
     * name 'Preset x' will be used by default using the position number given.
     *
     */
    void savePreset(int position, String presetName) throws ApiException;

    /**
     * Sets the white spectrum (warm to cold) of THIS segment, only for strips that have CCT support.
     */
    void setWhiteTemperature(PercentType percent, int segmentIndex) throws ApiException;

    /**
     * Sets one of the 3 custom effect sliders (1 to 3) of THIS segment, firmware 0.14.0 or newer.
     */
    void setCustomSlider(int sliderNumber, PercentType percent, int segmentIndex) throws ApiException;

    /**
     * Sets one of the 3 custom effect options (1 to 3) of THIS segment, firmware 0.14.0 or newer.
     */
    void setOption(int optionNumber, boolean bool, int segmentIndex) throws ApiException;

    void setFreeze(boolean bool, int segmentIndex) throws ApiException;

    void setSelected(boolean bool, int segmentIndex) throws ApiException;

    /**
     * Sets the first LED of the segment.
     */
    void setSegmentStart(int value, int segmentIndex) throws ApiException;

    /**
     * Sets the end of the segment, this LED is the first one that is NOT part of the segment.
     */
    void setSegmentStop(int value, int segmentIndex) throws ApiException;

    void setSegmentName(String name, int segmentIndex) throws ApiException;

    /**
     * @return The raw JSON of /json/state, /json/info, /json/cfg (all settings), /json/nodes (other WLED devices that
     *         were found), /json/live (the colors of the LEDs) and /json/fxdata (what each effect uses).
     */
    String getStateJson() throws ApiException;

    String getInfoJson() throws ApiException;

    String getConfigJson() throws ApiException;

    String getNodesJson() throws ApiException;

    String getLiveJson() throws ApiException;

    String getEffectDataJson() throws ApiException;

    /**
     * Changes settings of the device, the JSON only needs to contain the settings that change, like
     * {"id":{"name":"Kitchen"}}. Some settings only work after a reboot.
     */
    void setConfig(String json) throws ApiException;

    /**
     * Sets the clock of the device, in seconds since 1970.
     */
    void setTime(long epochSeconds) throws ApiException;

    /**
     * The groups are a bit mask, group 1 is 1, group 2 is 2, group 3 is 4 and so on to group 8 that is 128.
     */
    void setSyncSendGroups(int groups) throws ApiException;

    void setSyncReceiveGroups(int groups) throws ApiException;

    void setMainSegment(int segmentIndex) throws ApiException;

    void setLedMap(int ledMap) throws ApiException;

    /**
     * Turns the AudioReactive usermod on or off.
     */
    void setAudioReactive(boolean bool) throws ApiException;

    /**
     * Sets the audio sync of the AudioReactive usermod, 0 is off, 1 sends the audio and 2 receives it.
     */
    void setAudioSyncMode(String mode) throws ApiException;

    void setMapping(String mapping, int segmentIndex) throws ApiException;

    void setSoundSimulation(String type, int segmentIndex) throws ApiException;

    /**
     * @return All presets and playlists of the device as the raw JSON from presets.json.
     */
    String getPresetsJson() throws ApiException;

    /**
     * @return A readable text with one line for each preset or playlist, including timing and effects.
     */
    String getPresetsText() throws ApiException;

    /**
     * Saves a preset to the position, the content of the preset is the supplied JSON state instead of the current
     * state of the lights. This can contain everything WLED allows in a state, like on, bri, transition (in 0.1
     * seconds) and the seg array.
     */
    void savePresetState(int position, String presetName, String stateJson) throws ApiException;

    /**
     * Saves a playlist to the position. The durations and transitions are in seconds, a list with one value is used for
     * all presets.
     *
     * @param repeat how often the playlist runs, 0 is forever
     * @param endPreset the preset that is applied after the playlist ended, 0 keeps the last one
     */
    void savePlaylist(int position, String playlistName, List<Integer> presets, List<Double> durationsSeconds,
            List<Double> transitionsSeconds, int repeat, int endPreset) throws ApiException;

    /**
     * Deletes the preset in the position number. Needs firmware 0.13.0 or newer.
     */
    void deletePreset(int position) throws ApiException;

    /**
     * Reboots the WLED device. Needs firmware 0.13.0 or newer.
     */
    void reboot() throws ApiException;

    /**
     * Sends a raw JSON object to the /json/state endpoint of the device, this can be used for any feature of WLED that
     * the binding has no channel for.
     */
    void sendState(String json) throws ApiException;

    List<StateOption> getUpdatedFxList();

    List<StateOption> getUpdatedPaletteList();

    List<String> getSegmentNames();
}
