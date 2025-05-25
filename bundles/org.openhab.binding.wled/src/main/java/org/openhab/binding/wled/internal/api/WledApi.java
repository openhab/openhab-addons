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

    List<StateOption> getUpdatedFxList();

    List<StateOption> getUpdatedPaletteList();

    List<String> getSegmentNames();
}
