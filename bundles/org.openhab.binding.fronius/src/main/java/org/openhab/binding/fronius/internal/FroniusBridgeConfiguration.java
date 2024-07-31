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
package org.openhab.binding.fronius.internal;

/**
 * The {@link FroniusBridgeConfiguration} is the class used to match the
 * bridge configuration.
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class FroniusBridgeConfiguration {
    public String hostname;
    public String username;
    public String password;
    public Integer refreshInterval;
}
