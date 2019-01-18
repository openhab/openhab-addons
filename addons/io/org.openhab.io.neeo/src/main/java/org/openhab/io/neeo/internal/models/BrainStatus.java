/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * This class represents the status of the brain.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class BrainStatus {

    /** The brain identifier */
    private final String brainId;

    /** The brain name */
    private final String brainName;

    /** The brain url */
    private final String brainUrl;

    /** The callback url */
    private final String callbackUrl;

    /** The firmware version */
    private final String firmwareVersion;

    /** Whether the brain is connected (true) or not */
    private final boolean connected;

    /**
     * Creates a new brain status
     *
     * @param brainId the non-empty brain id
     * @param brainName the non-empty brain name
     * @param brainUrl the non-empty brain url
     * @param callbackUrl the non-empty callback url
     * @param firmwareVersion the non-empty firmware version of the brain
     * @param connected true if connected, false otherwise
     */
    public BrainStatus(String brainId, String brainName, String brainUrl, String callbackUrl, String firmwareVersion,
            boolean connected) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        NeeoUtil.requireNotEmpty(brainName, "brainName cannot be empty");
        NeeoUtil.requireNotEmpty(brainUrl, "brainUrl cannot be empty");
        NeeoUtil.requireNotEmpty(callbackUrl, "callbackUrl cannot be empty");
        NeeoUtil.requireNotEmpty(firmwareVersion, "firmwareVersion cannot be empty");

        this.brainId = brainId;
        this.brainName = brainName;
        this.brainUrl = brainUrl;
        this.callbackUrl = callbackUrl;
        this.firmwareVersion = firmwareVersion;
        this.connected = connected;
    }

    /**
     * Gets the brain identifier
     *
     * @return the brain identifier
     */
    public String getBrainId() {
        return brainId;
    }

    /**
     * Gets the brain name
     *
     * @return the brain name
     */
    public String getBrainName() {
        return brainName;
    }

    /**
     * Gets the brain url
     *
     * @return the brain url
     */
    public String getBrainUrl() {
        return brainUrl;
    }

    /**
     * Gets the callback url
     *
     * @return the callback url
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Gets the firmware version of the brain
     *
     * @return the firmware version
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Returns true if connected, false otherwise
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String toString() {
        return "BrainStatus [brainId=" + brainId + ", brainName=" + brainName + ", brainUrl=" + brainUrl
                + ", callbackUrl=" + callbackUrl + ", connected=" + connected + "]";
    }

}
