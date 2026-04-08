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
package org.openhab.binding.twilio.internal.config;

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for a Twilio Phone Number thing.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioPhoneConfiguration {

    public @Nullable String phoneNumber;
    public String voiceGreeting = DEFAULT_VOICE_GREETING;
    public String gatherResponse = DEFAULT_GATHER_RESPONSE;
    public int responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
}
