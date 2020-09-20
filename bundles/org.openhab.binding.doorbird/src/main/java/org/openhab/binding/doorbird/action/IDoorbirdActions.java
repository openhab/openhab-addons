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
package org.openhab.binding.doorbird.action;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IDoorbirdActions} defines the interface for all thing actions supported by the binding.
 * These methods, parameters, and return types are explained in {@link DoorbirdActions}.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public interface IDoorbirdActions {

    public void restart();

    public void sipHangup();

    public String getRingTimeLimit();

    public String getCallTimeLimit();

    public String getLastErrorCode();

    public String getLastErrorText();
}
