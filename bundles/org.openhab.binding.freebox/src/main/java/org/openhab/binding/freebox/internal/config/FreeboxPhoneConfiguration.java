/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.config;

/**
 * The {@link FreeboxPhoneConfiguration} is responsible for holding
 * configuration informations associated to a Freebox Phone thing type
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxPhoneConfiguration {

    public static final String REFRESH_PHONE_INTERVAL = "refreshPhoneInterval";
    public static final String REFRESH_PHONE_CALLS_INTERVAL = "refreshPhoneCallsInterval";

    public Integer refreshPhoneInterval;
    public Integer refreshPhoneCallsInterval;
}
