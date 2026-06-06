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
package org.openhab.binding.twilio.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO representing a Twilio phone number from the IncomingPhoneNumbers API.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioPhoneNumberInfo {

    public final String sid;
    public final String phoneNumber;
    public final String friendlyName;

    public TwilioPhoneNumberInfo(String sid, String phoneNumber, String friendlyName) {
        this.sid = sid;
        this.phoneNumber = phoneNumber;
        this.friendlyName = friendlyName;
    }
}
