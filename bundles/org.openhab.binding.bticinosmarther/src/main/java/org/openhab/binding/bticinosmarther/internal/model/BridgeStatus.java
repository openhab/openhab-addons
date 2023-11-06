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
package org.openhab.binding.bticinosmarther.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@code BridgeStatus} class defines the internal status of a Smarther Bridge.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class BridgeStatus {

    private long apiCallsHandled;
    private long notificationsReceived;
    private long notificationsRejected;

    /**
     * Constructs a new {@code BridgeStatus}.
     */
    public BridgeStatus() {
        this.apiCallsHandled = 0;
        this.notificationsReceived = 0;
        this.notificationsRejected = 0;
    }

    /**
     * Returns the total number of API gateway calls made by the bridge.
     *
     * @return the total number of API calls made.
     */
    public long getApiCallsHandled() {
        return apiCallsHandled;
    }

    /**
     * Increment the total number of API gateway calls made by the bridge.
     *
     * @return the total number of API calls made, after the increment.
     */
    public long incrementApiCallsHandled() {
        return ++apiCallsHandled;
    }

    /**
     * Sets the total number of API gateway calls made by the bridge.
     *
     * @param totalNumber
     *            the total number of API calls to be set as made
     */
    public void setApiCallsHandled(long totalNumber) {
        this.apiCallsHandled = totalNumber;
    }

    /**
     * Returns the total number of module status notifications received by the bridge.
     *
     * @return the total number of received notifications.
     */
    public long getNotificationsReceived() {
        return notificationsReceived;
    }

    /**
     * Increment the total number of module status notifications received by the bridge.
     *
     * @return the total number of received notification, after the increment.
     */
    public long incrementNotificationsReceived() {
        return ++notificationsReceived;
    }

    /**
     * Sets the total number of module status notifications received by the bridge.
     *
     * @param totalNumber
     *            the total number of notifications to be set as received
     */
    public void setNotificationsReceived(long totalNumber) {
        this.notificationsReceived = totalNumber;
    }

    /**
     * Returns the total number of module status notifications rejected by the bridge.
     *
     * @return the total number of rejected notifications.
     */
    public long getNotificationsRejected() {
        return notificationsRejected;
    }

    /**
     * Increment the total number of module status notifications rejected by the bridge.
     *
     * @return the total number of rejected notification, after the increment.
     */
    public long incrementNotificationsRejected() {
        return ++notificationsRejected;
    }

    /**
     * Sets the total number of module status notifications rejected by the bridge.
     *
     * @param totalNumber
     *            the total number of notifications to be set as rejected
     */
    public void setNotificationsRejected(long totalNumber) {
        this.notificationsRejected = totalNumber;
    }

    @Override
    public String toString() {
        return String.format("apiCallsHandled=%s, notifsReceived=%s, notifsRejected=%s", apiCallsHandled,
                notificationsReceived, notificationsRejected);
    }
}
