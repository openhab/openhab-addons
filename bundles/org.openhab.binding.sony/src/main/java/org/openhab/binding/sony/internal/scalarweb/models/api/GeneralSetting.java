/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents information about a specific sony general setting. Sony has created this as a simplified way of
 * representing and setting various settings (sound, video, etc)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class GeneralSetting {
    /** A boolean setting target */
    public static final String BOOLEANTARGET = "booleanTarget";

    /** A double setting target */
    public static final String DOUBLETARGET = "doubleNumberTarget";

    /** An enum setting target */
    public static final String ENUMTARGET = "enumTarget";

    /** An integer setting target */
    public static final String INTEGERTARGET = "integerTarget";

    /** A string setting target */
    public static final String STRINGTARGET = "stringTarget";

    /** The DEFAULT value for boolean ON */
    public static final String DEFAULTON = "on";

    /** The DEFAULT value for boolean OFF */
    public static final String DEFAULTOFF = "off";

    /** The slider deviceUI type for a setting */
    public static final String SLIDER = "slider";

    /** A picker deviceUI type for a setting */
    public static final String PICKER = "picker";

    /** The various constants for sound settings */
    public static final String SOUNDSETTING_SPEAKER = "speaker";
    public static final String SOUNDSETTING_SPEAKERHDMI = "speaker_hdmi";
    public static final String SOUNDSETTING_HDMI = "hdmi";
    public static final String SOUNDSETTING_AUDIOSYSTEM = "audioSystem";

    /** Whether the setting is currently available */
    private @Nullable Boolean isAvailable;

    /** The current value of the setting */
    private @Nullable String currentValue;

    /** The target of the setting */
    private @Nullable String target;

    /** The title of the setting */
    private @Nullable String title;

    /** The title text ID of the setting */
    private @Nullable String titleTextID;

    /** The type of setting (boolean, etc) */
    private @Nullable String type;

    /** The device UI info (picker/slider) */
    private @Nullable String deviceUIInfo;

    /** The URI the setting may apply to */
    private @Nullable String uri;

    /** The candidates for the setting (think enums or min/max/step values) */
    private @Nullable List<@Nullable GeneralSettingsCandidate> candidate;

    /**
     * Constructor used for deserialization only
     */
    public GeneralSetting() {
    }

    /**
     * Constructs a setting form the parameters
     * 
     * @param target a non-null, non-empty target
     * @param uri a possibly null, possibly empty uri
     * @param type a possibly null, possibly empty type
     * @param currentValue a possibly null, possibly empty current value
     */
    public GeneralSetting(final String target, @Nullable final String uri, @Nullable final String type,
            @Nullable final String currentValue) {
        Validate.notEmpty(target, "target cannot be empty");
        this.target = target;
        this.uri = uri;
        this.type = type;
        this.currentValue = currentValue;
    }

    /**
     * Whether the setting is currently available
     * 
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return isAvailable == null || BooleanUtils.isTrue(isAvailable);
    }

    /**
     * Gets the current setting value
     * 
     * @return the curent setting value
     */
    public @Nullable String getCurrentValue() {
        return currentValue;
    }

    /**
     * The setting's target
     * 
     * @return the setting's target
     */
    public @Nullable String getTarget() {
        return target;
    }

    /**
     * The setting's title
     * 
     * @return the setting's title
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * The title text identifier
     * 
     * @return the title text identifier
     */
    public @Nullable String getTitleTextID() {
        return titleTextID;
    }

    /**
     * The setting type
     * 
     * @return the setting type
     */
    public @Nullable String getType() {
        return type;
    }

    /**
     * The setting's UI information
     * 
     * @return the setting UI information
     */
    public @Nullable String getDeviceUIInfo() {
        return deviceUIInfo;
    }

    /**
     * The uri to apply the setting to
     * 
     * @return the uri to apply the setting to
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Whether the setting's UI is a slider
     * 
     * @return true if a slider, false otherwise
     */
    public boolean isUiSlider() {
        return StringUtils.contains(deviceUIInfo, SLIDER);
    }

    /**
     * Whether the setting's UI is a picker
     * 
     * @return true if a picker, false otherwise
     */
    public boolean isUiPicker() {
        return StringUtils.contains(deviceUIInfo, PICKER);
    }

    /**
     * Get's the candidates for the setting
     * 
     * @return the setting's candidates
     */
    public @Nullable List<@Nullable GeneralSettingsCandidate> getCandidate() {
        return candidate;
    }

    @Override
    public String toString() {
        return "SoundSetting [isAvailable=" + isAvailable + ", currentValue=" + currentValue + ", target=" + target
                + ", title=" + title + ", titleTextID=" + titleTextID + ", type=" + type + ", deviceUIInfo="
                + deviceUIInfo + ", candidate=" + candidate + "]";
    }
}
