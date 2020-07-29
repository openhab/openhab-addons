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
package org.openhab.binding.lcn.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ILcnModuleActions} defines the interface for all thing actions supported by the binding.
 * These methods, parameters, and return types are explained in {@link LcnModuleActions}.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public interface ILcnModuleActions {
    void hitKey(@Nullable String table, int key, @Nullable String action);

    void flickerOutput(int output, int depth, int ramp, int count);

    void sendDynamicText(int row, @Nullable String textInput);

    void startRelayTimer(int relaynumber, double duration);
}
