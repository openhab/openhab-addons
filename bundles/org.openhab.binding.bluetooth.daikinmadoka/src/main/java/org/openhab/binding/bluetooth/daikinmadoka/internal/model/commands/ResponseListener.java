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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Command responses that the listener must implement
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public interface ResponseListener {

    public void receivedResponse(byte[] bytes);

    public void receivedResponse(GetVersionCommand command);

    public void receivedResponse(GetFanspeedCommand command);

    public void receivedResponse(GetOperationmodeCommand command);

    public void receivedResponse(GetPowerstateCommand command);

    public void receivedResponse(GetSetpointCommand command);

    public void receivedResponse(GetIndoorOutoorTemperatures command);

    public void receivedResponse(SetPowerstateCommand command);

    public void receivedResponse(SetSetpointCommand command);

    public void receivedResponse(SetOperationmodeCommand command);

    public void receivedResponse(SetFanspeedCommand command);

    public void receivedResponse(GetOperationHoursCommand command);

    public void receivedResponse(GetEyeBrightnessCommand command);

    public void receivedResponse(SetEyeBrightnessCommand command);

    public void receivedResponse(GetCleanFilterIndicatorCommand command);
}
