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
package org.openhab.binding.smarther.internal.api.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smarther.internal.util.StringUtil;

/**
 * Smarther API Location data class.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class Location {

    private String plantId;
    private String name;
    private @Nullable String subscriptionId;
    private @Nullable String endpointUrl;

    private Location(Plant plant, @Nullable Subscription subscription) {
        super();
        this.plantId = plant.getId();
        this.name = plant.getName();
        if (subscription != null) {
            this.subscriptionId = subscription.getSubscriptionId();
            this.endpointUrl = subscription.getEndpointUrl();
        }
    }

    public static Location fromPlant(Plant plant, @Nullable Subscription subscription) {
        return new Location(plant, subscription);
    }

    public static Location fromPlant(Plant plant) {
        return new Location(plant, null);
    }

    public static Location fromPlant(Plant plant, Optional<Subscription> subscription) {
        return (subscription.isPresent()) ? new Location(plant, subscription.get()) : new Location(plant, null);
    }

    public String getPlantId() {
        return plantId;
    }

    public String getName() {
        return name;
    }

    public boolean hasSubscription() {
        return !StringUtil.isBlank(subscriptionId);
    }

    public void setSubscription(String subscriptionId, String endpointUrl) {
        this.subscriptionId = subscriptionId;
        this.endpointUrl = endpointUrl;
    }

    public void unsetSubscription() {
        this.subscriptionId = null;
        this.endpointUrl = null;
    }

    @Nullable
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Nullable
    public String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * Converts the list into a string with comma separated location names.
     *
     * @param locations The locations list to be converted.
     * @return A string containing the location names, comma separated (or null, if the list is null or empty).
     */
    public static String toNameString(List<Location> locations) {
        if (locations.isEmpty()) {
            return "N/A";
        } else {
            return locations.stream().map(a -> String.valueOf(a.getName())).collect(Collectors.joining(", "));
        }
    }

    @Override
    public String toString() {
        return String.format("plantId=%s, name=%s, subscriptionId=%s, endpointUrl=%s", plantId, name, subscriptionId,
                endpointUrl);
    }

}
