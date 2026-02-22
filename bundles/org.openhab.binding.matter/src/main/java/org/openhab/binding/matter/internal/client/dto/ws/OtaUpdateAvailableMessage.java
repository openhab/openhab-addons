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
package org.openhab.binding.matter.internal.client.dto.ws;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * UpdateAvailableMessage is a message that is sent when an update is available for a device
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OtaUpdateAvailableMessage extends OtaUpdateInfo {
    /** Node ID of the device that has an update available */
    public BigInteger nodeId;

    /**
     * Constructor
     * 
     * @param nodeId
     * @param vendorId
     * @param productId
     * @param softwareVersion
     * @param softwareVersionString
     * @param releaseNotesUrl
     * @param specificationVersion
     */
    public OtaUpdateAvailableMessage(BigInteger nodeId, int vendorId, int productId, int softwareVersion,
            String softwareVersionString, @Nullable String releaseNotesUrl, @Nullable Integer specificationVersion) {
        super(vendorId, productId, softwareVersion, softwareVersionString, releaseNotesUrl, specificationVersion);
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "OtaUpdateAvailableMessage [nodeId=" + nodeId + ", vendorId=" + vendorId + ", productId=" + productId
                + ", softwareVersion=" + softwareVersion + ", softwareVersionString=" + softwareVersionString
                + ", releaseNotesUrl=" + releaseNotesUrl + ", specificationVersion=" + specificationVersion + "]";
    }
}
