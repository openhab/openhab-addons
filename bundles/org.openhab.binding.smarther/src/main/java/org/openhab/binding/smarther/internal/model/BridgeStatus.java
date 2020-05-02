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
package org.openhab.binding.smarther.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Smarther API BridgeStatus data class.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class BridgeStatus {

    private long apiCallsHandled;
    private long notificationsReceived;
    private long notificationsRejected;

    public BridgeStatus() {
        this.apiCallsHandled = 0;
        this.notificationsReceived = 0;
        this.notificationsRejected = 0;
    }

    public long getApiCallsHandled() {
        return apiCallsHandled;
    }

    public long incrementApiCallsHandled() {
        return ++apiCallsHandled;
    }

    public void setApiCallsHandled(long apiCallsHandled) {
        this.apiCallsHandled = apiCallsHandled;
    }

    public long getNotificationsReceived() {
        return notificationsReceived;
    }

    public long incrementNotificationsReceived() {
        return ++notificationsReceived;
    }

    public void setNotificationsReceived(long notificationsReceived) {
        this.notificationsReceived = notificationsReceived;
    }

    public long getNotificationsRejected() {
        return notificationsRejected;
    }

    public long incrementNotificationsRejected() {
        return ++notificationsRejected;
    }

    public void setNotificationsRejected(long notificationsRejected) {
        this.notificationsRejected = notificationsRejected;
    }

    @Override
    public String toString() {
        return String.format("apiCallsHandled=%s, notifsReceived=%s, notifsRejected=%s", apiCallsHandled,
                notificationsReceived, notificationsRejected);
    }

}
