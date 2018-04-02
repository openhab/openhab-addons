/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * This class represents the status of the brain.
 *
 * @author Tim Roberts
 */
public class BrainStatus {

    /** The brain identifier */
    private final String brainId;

    /** The brain name */
    private final String brainName;

    /** The brain url */
    private final String brainUrl;

    /** The callback url */
    private final String callbackUrl;

    /** Whether the brain is connected (true) or not */
    private final boolean connected;

    /**
     * Creates a new brain status
     *
     * @param brainId the non-empty brain id
     * @param brainName the non-empty brain name
     * @param brainUrl the non-empty brain url
     * @param callbackUrl the non-empty callback url
     * @param connected true if connected, false otherwise
     */
    public BrainStatus(String brainId, String brainName, String brainUrl, String callbackUrl, boolean connected) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        NeeoUtil.requireNotEmpty(brainName, "brainName cannot be empty");
        NeeoUtil.requireNotEmpty(brainUrl, "brainUrl cannot be empty");
        NeeoUtil.requireNotEmpty(callbackUrl, "callbackUrl cannot be empty");

        this.brainId = brainId;
        this.brainName = brainName;
        this.brainUrl = brainUrl;
        this.callbackUrl = callbackUrl;
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
