/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.api.service.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService.SpexorAuthGrantState;

/**
 * Listener to received change events if the authroization was dropped or gained.
 *
 * @author Marc Fischer - Initial contribution
 *
 */
@NonNullByDefault
public interface SpexorAuthorizationProcessListener {

    void changedState(SpexorAuthGrantState oldState, SpexorAuthGrantState newState);
}
