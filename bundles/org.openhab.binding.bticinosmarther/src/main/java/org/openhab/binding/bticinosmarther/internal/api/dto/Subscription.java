/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@code Subscription} class defines the dto for Smarther API notification subscription object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Subscription {

    @SerializedName("plantId")
    private String plantId;
    @SerializedName("subscriptionId")
    private String subscriptionId;
    @SerializedName("EndPointUrl")
    private String endpointUrl;

    /**
     * Returns the identifier of the plant this subscription relates to.
     *
     * @return a string containing the plant identifier
     */
    public String getPlantId() {
        return plantId;
    }

    /**
     * Returns the notification subscription identifier.
     *
     * @return a string containing the subscription identifier
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Returns the notification endpoint url this subscription maps to.
     *
     * @return a string containing the notification endpoint url
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    @Override
    public String toString() {
        return String.format("plantId=%s, id=%s, endpoint=%s", plantId, subscriptionId, endpointUrl);
    }
}
