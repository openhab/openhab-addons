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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents notification of an API mapping update (and includes some general setting information).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NotifySettingUpdateApiMapping {
    /**
     * Developer note: there are API information that I'm ignoring for now:
     * "apiMappingUpdate": {
     * "commandApi": {
     * "name": "",
     * "version": ""
     * },
     * "currentValue": "track",
     * "getApi": {
     * "name": "getPlaybackModeSettings",
     * "version": "1.0"
     * },
     * "service": "avContent",
     * "setApi": {
     * "name": "setPlaybackModeSettings",
     * "version": "1.0"
     * },
     * "target": "repeatType",
     * "targetSuppl": "",
     * "uri": "storage:usb1"
     * },
     */

    /** The current value of the setting */
    private @Nullable String currentValue;

    /** The target of the setting */
    private @Nullable String target;

    /** The target supplement of the setting */
    private @Nullable String targetSuppl;

    /** The URI the setting may apply to */
    private @Nullable String uri;

    /** The service the setting may apply to */
    private @Nullable String service;

    /** The command API */
    private @Nullable NotifySettingUpdateApi commandApi;

    /** The get API */
    private @Nullable NotifySettingUpdateApi getApi;

    /** The set API */
    private @Nullable NotifySettingUpdateApi setApi;

    /**
     * Constructor used for deserialization only
     */
    public NotifySettingUpdateApiMapping() {
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
     * The setting's target supplement
     * 
     * @return the setting's target supplement
     */
    public @Nullable String getTargetSuppl() {
        return targetSuppl;
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
     * The uri to apply the setting to
     * 
     * @return the uri to apply the setting to
     */
    public @Nullable String getService() {
        return service;
    }

    /**
     * Get's the command API
     * 
     * @return the command API
     */
    public @Nullable NotifySettingUpdateApi getCommandApi() {
        return commandApi;
    }

    /**
     * Get's the get API
     * 
     * @return the get API
     */
    public @Nullable NotifySettingUpdateApi getGetApi() {
        return getApi;
    }

    /**
     * Get's the set API
     * 
     * @return the set API
     */
    public @Nullable NotifySettingUpdateApi getSetApi() {
        return setApi;
    }

    @Override
    public String toString() {
        return "NotifySettingUpdateApiMapping [commandApi=" + commandApi + ", currentValue=" + currentValue
                + ", getApi=" + getApi + ", service=" + service + ", setApi=" + setApi + ", target=" + target
                + ", targetSuppl=" + targetSuppl + ", uri=" + uri + "]";
    }
}
