/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.metofficedatahub.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SiteApiFeatureCollection} is a Java class used as a DTO to hold the response to the Site Specific API.
 *
 * @author David Goodyear - Initial contribution
 */
public class SiteApiFeatureCollection extends SiteApiTypedResponseObject {

    public static final String TYPE_SITE_API_FEATURE_COLLECTION = "FeatureCollection";

    @Override
    public String getExpectedType() {
        return TYPE_SITE_API_FEATURE_COLLECTION;
    }

    @SerializedName("features")
    private SiteApiFeature[] feature;

    public SiteApiFeature[] getFeature() {
        return feature;
    }

    public SiteApiFeatureProperties getFirstProperties() {
        if (feature == null || feature.length == 0) {
            return null;
        }

        return feature[0].getProperties();
    }
}
