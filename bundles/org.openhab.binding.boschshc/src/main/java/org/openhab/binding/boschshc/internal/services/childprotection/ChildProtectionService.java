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
package org.openhab.binding.boschshc.internal.services.childprotection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;

/**
 * Service to activate and deactivate child protection.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class ChildProtectionService extends BoschSHCService<ChildProtectionServiceState> {

    public static final String CHILD_PROTECTION_SERVICE_NAME = "ChildProtection";

    public ChildProtectionService() {
        super(CHILD_PROTECTION_SERVICE_NAME, ChildProtectionServiceState.class);
    }
}
