/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.NAME_SEPARATOR;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;

/**
 * The {@code Location} class defines the dto for Smarther API location object.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class Location {

    private String plantId;
    private String name;
    private @Nullable String subscriptionId;
    private @Nullable String endpointUrl;

    /**
     * Constructs a new {@code Location} with the given plant and subscription.
     *
     * @param plant
     *            the location plant to use
     * @param subscription
     *            the notification subscription endpoint to use, may be {@code null}
     */
    private Location(Plant plant, @Nullable Subscription subscription) {
        super();
        this.plantId = plant.getId();
        this.name = plant.getName();
        if (subscription != null) {
            this.subscriptionId = subscription.getSubscriptionId();
            this.endpointUrl = subscription.getEndpointUrl();
        }
    }

    /**
     * Returns a new {@code Location} with the given plant and subscription.
     *
     * @param plant
     *            the location plant to use
     * @param subscription
     *            the notification subscription endpoint to use, may be {@code null}
     *
     * @return the newly created Location object
     */
    public static Location fromPlant(Plant plant, @Nullable Subscription subscription) {
        return new Location(plant, subscription);
    }

    /**
     * Returns a new {@code Location} with the given plant and no subscription.
     *
     * @param plant
     *            the location plant to use
     *
     * @return the newly created Location object
     */
    public static Location fromPlant(Plant plant) {
        return new Location(plant, null);
    }

    /**
     * Returns a new {@code Location} with the given plant and optional subscription.
     *
     * @param plant
     *            the location plant to use
     * @param subscription
     *            the optional notification subscription endpoint to use, may contain no subscription
     *
     * @return the newly created Location object
     */
    public static Location fromPlant(Plant plant, Optional<Subscription> subscription) {
        return (subscription.isPresent()) ? new Location(plant, subscription.get()) : new Location(plant, null);
    }

    /**
     * Returns the plant identifier associated with this location.
     *
     * @return a string containing the plant identifier
     */
    public String getPlantId() {
        return plantId;
    }

    /**
     * Returns the plant name associated with this location.
     *
     * @return a string containing the plant name
     */
    public String getName() {
        return name;
    }

    /**
     * Tells whether the location has an associated subscription.
     *
     * @return {@code true} if the location has a subscription, {@code false} otherwise
     */
    public boolean hasSubscription() {
        return !StringUtil.isBlank(subscriptionId);
    }

    /**
     * Sets the notification subscription details for the location.
     *
     * @param subscriptionId
     *            the subscription identifier to use
     * @param endpointUrl
     *            the notification endpoint to use
     */
    public void setSubscription(String subscriptionId, String endpointUrl) {
        this.subscriptionId = subscriptionId;
        this.endpointUrl = endpointUrl;
    }

    /**
     * Unsets the notification subscription details for the location.
     * I.e. resets all of its details to {@code null}.
     */
    public void unsetSubscription() {
        this.subscriptionId = null;
        this.endpointUrl = null;
    }

    /**
     * Returns the notification subscription identifier for this location.
     *
     * @return a string containing the subscription identifier, may be {@code null}
     */
    public @Nullable String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Returns the notification endpoint for this location.
     *
     * @return a string containing the notification endpoint, may be {@code null}
     */
    public @Nullable String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * Converts a list of {@link Location} objects into a string containing the location names, comma separated.
     *
     * @param locations
     *            the list of location objects to be converted, may be {@code null}
     *
     * @return a string containing the comma separated location names, or {@code null} if the list is {@code null} or
     *         empty.
     */
    public static @Nullable String toNameString(@Nullable List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return null;
        }
        return locations.stream().map(a -> String.valueOf(a.getName())).collect(Collectors.joining(NAME_SEPARATOR));
    }

    @Override
    public String toString() {
        return String.format("plantId=%s, name=%s, subscriptionId=%s, endpointUrl=%s", plantId, name, subscriptionId,
                endpointUrl);
    }
}
