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
 * This class represents the bravia internet video link information
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class BivlInfo {
    /** The Bravia Internet Video Link (BIVL) service id */
    private @Nullable String serviceId;

    /** The BIVL asset ID */
    private @Nullable String assetId;

    /** The BIVL provider */
    private @Nullable String provider;

    /**
     * Constructor used for deserialization only
     */
    public BivlInfo() {
    }

    /**
     * Returns the service ID
     * 
     * @return possibly null, possibly empty service id
     */
    public @Nullable String getServiceId() {
        return serviceId;
    }

    /**
     * Returns the asset ID
     * 
     * @return possibly null, possibly empty asset id
     */
    public @Nullable String getAssetId() {
        return assetId;
    }

    /**
     * Returns the provider ID
     * 
     * @return possibly null, possibly empty provider id
     */
    public @Nullable String getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return "BivlInfo [serviceId=" + serviceId + ", assetId=" + assetId + ", provider=" + provider + "]";
    }
}
