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
package org.openhab.binding.sony.internal.scalarweb.models;

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.gson.ScalarWebEventDeserializer;

import com.google.gson.JsonArray;

/**
 * This class represents a web scalar event result (sent to us from the device). This result will be created by the
 * {@link ScalarWebEventDeserializer} when deserializing the event.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebEvent extends AbstractScalarResponse {
    // audio notifications
    public static final String NOTIFYVOLUMEINFORMATION = "notifyVolumeInformation";
    public static final String NOTIFYWIRELESSSURROUNDINFO = "notifyWirelessSurroundInfo";

    // AV Notifications
    public static final String NOTIFYPLAYINGCONTENTINFO = "notifyPlayingContentInfo";
    public static final String NOTIFYEXTERNALTERMINALSTATUS = "notifyExternalTerminalStatus";
    public static final String NOTIFYAVAILABLEPLAYBACKFUNCTION = "notifyAvailablePlaybackFunction";

    // system notifications
    public static final String NOTIFYPOWERSTATUS = "notifyPowerStatus";
    public static final String NOTIFYSTORAGESTATUS = "notifyStorageStatus";
    public static final String NOTIFYSWUPDATEINFO = "notifySWUpdateInfo";
    public static final String NOTIFYSETTINGSUPDATE = "notifySettingsUpdate";

    /** The method name for the event */
    private @Nullable String method;

    /** The parameters for the event */
    private @Nullable JsonArray params;

    /** The event version */
    private @Nullable String version;

    /**
     * Empty constructor used for deserialization
     */
    public ScalarWebEvent() {
    }

    /**
     * Instantiates a new scalar web event
     *
     * @param methodName the non-null, non-empty method name
     * @param parmas the non-null, possibly empty parameters
     * @param version the non-null, non-empty version
     */
    public ScalarWebEvent(final String method, final JsonArray params, final String version) {
        Validate.notEmpty(method, "method cannot be empty");
        Objects.requireNonNull(params, "params cannot be null");
        Validate.notEmpty(version, "version cannot be empty");

        this.method = method;
        this.params = params;
        this.version = version;
    }

    /**
     * Gets the method name
     *
     * @return the method name
     */
    public @Nullable String getMethod() {
        return method;
    }

    /**
     * Gets the version
     *
     * @return the version
     */
    public @Nullable String getVersion() {
        return version;
    }

    @Override
    protected @Nullable JsonArray getPayload() {
        return params;
    }

    @Override
    public String toString() {
        return "ScalarWebEvent [method=" + method + ", params=" + params + ", version=" + version + "]";
    }
}
