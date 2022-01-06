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
package org.openhab.binding.bticinosmarther.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code SmartherBridgeConfiguration} class defines the internal configuration of a {@code SmartherBridgeHandler}
 * instance.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class SmartherBridgeConfiguration {

    private String subscriptionKey;
    private String clientId;
    private String clientSecret;
    private boolean useNotifications;
    private int statusRefreshPeriod;
    private String notificationUrl;
    private List<String> notifications;

    /**
     * Returns the Legrand/Bticino product subscription key.
     *
     * @return a string containing the subscription key
     */
    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    /**
     * Sets the Legrand/Bticino product subscription key.
     *
     * @param subscriptionKey
     *            the new product subscription key
     */
    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    /**
     * Returns the Legrand/Bticino user account client identifier.
     *
     * @return a string containing the client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the Legrand/Bticino user account client identifier.
     *
     * @param clientId
     *            the new client identifier
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns the Legrand/Bticino user account client secret.
     *
     * @return a string containing the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the Legrand/Bticino user account client secret.
     *
     * @param clientSecret
     *            the new client secret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Tells whether the Bridge subscribes to receive modules status notifications.
     *
     * @return {@code true} if the notifications are turned on, {@code false} otherwise
     */
    public boolean isUseNotifications() {
        return useNotifications;
    }

    /**
     * Sets whether the Bridge subscribes to receive modules status notifications.
     *
     * @param useNotifications
     *            {@code true} if the notifications are turned on, {@code false} otherwise
     */
    public void setUseNotifications(boolean useNotifications) {
        this.useNotifications = useNotifications;
    }

    /**
     * Returns the Bridge status refresh period (in minutes).
     *
     * @return the Bridge status refresh period
     */
    public int getStatusRefreshPeriod() {
        return statusRefreshPeriod;
    }

    /**
     * Sets the Bridge status refresh period (in minutes).
     *
     * @param statusRefreshPeriod
     *            the new Bridge status refresh period
     */
    public void setStatusRefreshPeriod(int statusRefreshPeriod) {
        this.statusRefreshPeriod = statusRefreshPeriod;
    }

    /**
     * Returns the notification url for this Bridge.
     *
     * @return a string containing the notification url
     */
    public String getNotificationUrl() {
        return notificationUrl;
    }

    /**
     * Sets the notification url for this Bridge.
     *
     * @param notificationUrl
     *            the new notification url
     */
    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    /**
     * Adds a notification identifier to the Bridge notifications list.
     *
     * @param notificationId
     *            the notification identifier to add
     *
     * @return the new Bridge notifications list
     */
    public List<String> addNotification(String notificationId) {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        if (!notifications.contains(notificationId)) {
            notifications.add(notificationId);
        }
        return notifications;
    }

    /**
     * Removes a notification identifier from the Bridge notifications list.
     *
     * @param notificationId
     *            the notification identifier to remove
     *
     * @return the new Bridge notifications list
     */
    public List<String> removeNotification(String notificationId) {
        if (notifications != null) {
            notifications.remove(notificationId);
        }
        return notifications;
    }

    /**
     * Returns the current Bridge notifications list.
     *
     * @return the current Bridge notifications list
     */
    public List<String> getNotifications() {
        return (notifications != null) ? notifications : Collections.emptyList();
    }

    /**
     * Sets a new Bridge notifications list.
     *
     * @param notifications
     *            the new notifications list to set
     */
    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }
}
