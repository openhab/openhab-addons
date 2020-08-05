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
package org.openhab.binding.volvooncall.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IVolvoOnCallActions} defines the interface for all thing actions supported by the binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface IVolvoOnCallActions {
    public void honkBlinkCommand(Boolean honk, Boolean blink);

    public void preclimatizationStopCommand();

    public void heaterStopCommand();

    public void heaterStartCommand();

    public void preclimatizationStartCommand();

    public void engineStartCommand(@Nullable Integer runtime);

    public void openCarCommand();

    public void closeCarCommand();

}
