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
 * The {@link SiteApiFeature} is a Java class used as a DTO to hold part of the response to the Site Specific API.
 *
 * @author David Goodyear - Initial contribution
 */
public class SiteApiFeature extends SiteApiTypedResponseObject {

    public static final String TYPE_SITE_API_FEATURE = "Feature";

    @Override
    public String getExpectedType() {
        return TYPE_SITE_API_FEATURE;
    }

    @SerializedName("geometry")
    private SiteApiFeaturePoint geometry;

    public SiteApiFeaturePoint getGeometry() {
        return geometry;
    }

    @SerializedName("properties")
    private SiteApiFeatureProperties properties;

    public SiteApiFeatureProperties getProperties() {
        return properties;
    }
}
