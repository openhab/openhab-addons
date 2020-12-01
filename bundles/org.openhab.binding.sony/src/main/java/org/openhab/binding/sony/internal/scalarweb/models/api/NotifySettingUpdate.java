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

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a notification of a general setting update (differs slightly from the GeneralSetting)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NotifySettingUpdate {
    /** Whether the setting is currently available */
    private @Nullable Boolean isAvailable;

    /** The title of the setting */
    private @Nullable String title;

    /** The title text ID of the setting */
    private @Nullable String titleTextID;

    /** The type of setting (boolean, etc) */
    private @Nullable String type;

    /** The device UI info (picker/slider) */
    private @Nullable String deviceUIInfo;

    /** The mapping update */
    private @Nullable NotifySettingUpdateApiMapping apiMappingUpdate;

    /**
     * Constructor used for deserialization only
     */
    public NotifySettingUpdate() {
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
     * The api mapping update
     * 
     * @return the apiMappingUpdate mapping update
     */
    public @Nullable NotifySettingUpdateApiMapping getApiMappingUpdate() {
        return apiMappingUpdate;
    }

    @Override
    public String toString() {
        return "NotificationGeneralSetting [apiMappingUpdate=" + apiMappingUpdate + ", deviceUIInfo=" + deviceUIInfo
                + ", isAvailable=" + isAvailable + ", title=" + title + ", titleTextID=" + titleTextID + ", type="
                + type + "]";
    }
}
