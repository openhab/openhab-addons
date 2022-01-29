/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.am43.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ResponseListener} used a callback for handling the responses of certain commands.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public interface ResponseListener {

    public void receivedResponse(GetBatteryLevelCommand command);

    public void receivedResponse(GetAllCommand command);

    public void receivedResponse(GetLightLevelCommand command);

    public void receivedResponse(GetPositionCommand command);

    public void receivedResponse(GetSpeedCommand command);
}
