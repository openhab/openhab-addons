/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    public abstract void update() throws ApiException;

    public abstract void initialize() throws ApiException;

    public abstract int getFirmwareVersion() throws ApiException;

    public abstract String sendGetRequest(String string) throws ApiException;

    /**
     * Turns on/off ALL segments
     */
    public abstract void setGlobalOn(boolean bool) throws ApiException;

    /**
     * Turns on/off just THIS segment
     */
    public abstract void setMasterOn(boolean bool, int segmentIndex) throws ApiException;

    /**
     * Sets the brightness of ALL segments
     */
    public abstract void setGlobalBrightness(PercentType percent) throws ApiException;

    /**
     * Sets the brightness of just THIS segment
     */
    public abstract void setMasterBrightness(PercentType percent, int segmentIndex) throws ApiException;

    /**
     * Stops any running FX and instantly changes the segment to the desired colour
     */
    public abstract void setMasterHSB(HSBType hsbType, int segmentIndex) throws ApiException;

    public abstract void setEffect(String string, int segmentIndex) throws ApiException;

    public abstract void setPreset(String string) throws ApiException;

    public abstract void setPalette(String string, int segmentIndex) throws ApiException;

    public abstract void setFxIntencity(PercentType percentType, int segmentIndex) throws ApiException;

    public abstract void setFxSpeed(PercentType percentType, int segmentIndex) throws ApiException;

    public abstract void setSleep(boolean bool) throws ApiException;

    public abstract void setSleepMode(String value) throws ApiException;

    public abstract void setSleepDuration(BigDecimal time) throws ApiException;

    public abstract void setSleepTargetBrightness(PercentType percent) throws ApiException;

    public abstract void setUdpSend(boolean bool) throws ApiException;

    public abstract void setUdpRecieve(boolean bool) throws ApiException;

    public abstract void setTransitionTime(BigDecimal time) throws ApiException;

    public abstract void setPresetCycle(boolean bool) throws ApiException;

    public abstract void setPrimaryColor(HSBType hsbType, int segmentIndex) throws ApiException;

    public abstract void setSecondaryColor(HSBType hsbType, int segmentIndex) throws ApiException;

    public abstract void setTertiaryColor(HSBType hsbType, int segmentIndex) throws ApiException;

    public abstract void setWhiteOnly(PercentType percentType, int segmentIndex) throws ApiException;

    public abstract void setMirror(boolean bool, int segmentIndex) throws ApiException;

    public abstract void setReverse(boolean bool, int segmentIndex) throws ApiException;

    public abstract void setLiveOverride(String value) throws ApiException;

    public abstract void setGrouping(int value, int segmentIndex) throws ApiException;

    public abstract void setSpacing(int value, int segmentIndex) throws ApiException;

    /**
     * Saves a preset to the position number with the supplied name. If the supplied name is an empty String then the
     * name 'Preset x' will be used by default using the position number given.
     *
     */
    public abstract void savePreset(int position, String presetName) throws ApiException;

    public abstract List<StateOption> getUpdatedFxList();

    public abstract List<StateOption> getUpdatedPaletteList();

    public abstract List<String> getSegmentNames();
}
