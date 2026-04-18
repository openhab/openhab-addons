/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dahuadoor.internal.sip;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Callback interface for SIP events.
 * Implemented by DahuaDoorBaseHandler to receive SIP notifications.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public interface SipEventListener {

    /**
     * Called when SIP registration succeeds (200 OK received).
     */
    void onRegistrationSuccess();

    /**
     * Called when SIP registration fails (401 persists, network error, etc.).
     *
     * @param reason Error description
     */
    void onRegistrationFailed(String reason);

    /**
     * Called when INVITE received from VTO (doorbell pressed).
     *
     * @param callerId SIP URI of caller (e.g., "sip:8001@172.18.1.111")
     */
    void onInviteReceived(String callerId);

    /**
     * Called when VTO sends CANCEL (timeout or other device answered).
     */
    void onCallCancelled();

    /**
     * Called when incoming call was accepted and ACK received.
     */
    void onCallActive();

    /**
     * Called when local side initiated call termination.
     */
    void onCallTerminating();

    /**
     * Called when call has ended and SIP dialog is fully closed.
     */
    void onCallEnded();
}
