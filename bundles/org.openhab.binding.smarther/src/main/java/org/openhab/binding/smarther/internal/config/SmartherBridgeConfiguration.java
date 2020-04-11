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
package org.openhab.binding.smarther.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link SmartherBridgeConfiguration} is the class used to match the SmartherBridgeHandler
 * configuration.
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

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isUseNotifications() {
        return useNotifications;
    }

    public void setUseNotifications(boolean useNotifications) {
        this.useNotifications = useNotifications;
    }

    public int getStatusRefreshPeriod() {
        return statusRefreshPeriod;
    }

    public void setStatusRefreshPeriod(int statusRefreshPeriod) {
        this.statusRefreshPeriod = statusRefreshPeriod;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public List<String> addNotification(String notificationId) {
        if (notifications == null) {
            notifications = new ArrayList<String>();
        }
        if (!notifications.contains(notificationId)) {
            notifications.add(notificationId);
        }
        return notifications;
    }

    public List<String> removeNotification(String notificationId) {
        if (notifications != null) {
            notifications.remove(notificationId);
        }
        return notifications;
    }

    public List<String> getNotifications() {
        return (notifications != null) ? notifications : Collections.emptyList();
    }

    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }

}
