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

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Bypass service state for Door/Window Contact II
 * <p>
 * Example JSON:
 * 
 * <pre>
 * {
 *   "@type": "bypassState",
 *   "state": "BYPASS_INACTIVE",
 *   "configuration": {
 *     "enabled": false,
 *     "timeout": 5,
 *     "infinite": false
 *   }
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class BypassServiceState extends BoschSHCServiceState {

    public BypassServiceState() {
        super("bypassState");
    }

    public BypassState state;

    public BypassConfiguration configuration;
}
