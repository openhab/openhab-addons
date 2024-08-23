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
package org.openhab.binding.boschshc.internal.services.bypass.dto;

/**
 * Configuration object of a bypass configuration.
 * <p>
 * Example JSON:
 * 
 * <pre>
 * "configuration": {
 *   "enabled": false,
 *   "timeout": 5,
 *   "infinite": false
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class BypassConfiguration {

    public boolean enabled;

    public int timeout;

    public boolean infinite;
}
