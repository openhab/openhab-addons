/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.kaleidescape.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IKaleidescapeThingActions} defines the interface for all thing actions supported by the binding.
 * These methods, parameters, and return types are explained in {@link KaleidescapeThingActions}.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public interface IKaleidescapeThingActions {

    void sendKCommand(String kCommand);
}
