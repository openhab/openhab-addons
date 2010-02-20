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
package org.openhab.binding.onkyo.internal.automation.modules;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link OnkyoThingActions} defines the interface for all thing actions supported by the binding.
 *
 * @author Laurent Garnier - initial contribution
 */
@NonNullByDefault
public interface OnkyoThingActions {

    public void sendRawCommand(@Nullable String command, @Nullable String value);
}
