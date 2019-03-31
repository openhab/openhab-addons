/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal;

/**
 * The {@link SleepIQConfigStatusMessage} defines the keys to be used for configuration status messages.
 *
 * @author Gregory Moyer - Initial contribution
 *
 */
public interface SleepIQConfigStatusMessage {
    public static final String USERNAME_MISSING = "missing-username-configuration";
    public static final String PASSWORD_MISSING = "missing-password-configuration";
}
