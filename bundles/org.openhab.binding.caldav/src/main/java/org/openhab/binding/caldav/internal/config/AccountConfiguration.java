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
package org.openhab.binding.caldav.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AccountConfiguration {
    public String url = "";
    public String username = "";
    public String password = "";
    public String authType = "AUTO";
    public String discoveryMode = "AUTO";
    public String calendarHome = "";
    public int refreshInterval = 300;
    public int requestTimeout = 30;
    public boolean verifyCertificate = true;
    public String syncMode = "AUTO";
    public int maxPastDays = 30;
    public int maxFutureDays = 365;
    public boolean readOnly = true;
}
