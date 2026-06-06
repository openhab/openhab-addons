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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * OtaUpdateInfo holds update information for a device
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OtaUpdateInfo {

    /** Vendor ID of the device that has an update available */
    public int vendorId;

    /** Product ID of the device that has an update available */
    public int productId;

    /** Software version of the device that has an update available */
    public int softwareVersion;

    /** Software Version String of the device that has an update available */
    public String softwareVersionString;

    /** ReleaseNotesUrl for the update */
    public @Nullable String releaseNotesUrl;

    /** SpecificationVersion of the device that has an update available */
    public @Nullable Integer specificationVersion;

    /**
     * Constructor
     * 
     * @param vendorId
     * @param productId
     * @param softwareVersion
     * @param softwareVersionString
     * @param releaseNotesUrl
     * @param specificationVersion
     */
    public OtaUpdateInfo(int vendorId, int productId, int softwareVersion, String softwareVersionString,
            @Nullable String releaseNotesUrl, @Nullable Integer specificationVersion) {
        this.vendorId = vendorId;
        this.productId = productId;
        this.softwareVersion = softwareVersion;
        this.softwareVersionString = softwareVersionString;
        this.releaseNotesUrl = releaseNotesUrl;
        this.specificationVersion = specificationVersion;
    }
}
