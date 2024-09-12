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

    void receivedResponse(byte[] bytes);

    void receivedResponse(GetVersionCommand command);

    void receivedResponse(GetFanspeedCommand command);

    void receivedResponse(GetOperationmodeCommand command);

    void receivedResponse(GetPowerstateCommand command);

    void receivedResponse(GetSetpointCommand command);

    void receivedResponse(GetIndoorOutoorTemperatures command);

    void receivedResponse(SetPowerstateCommand command);

    void receivedResponse(SetSetpointCommand command);

    void receivedResponse(SetOperationmodeCommand command);

    void receivedResponse(SetFanspeedCommand command);

    void receivedResponse(GetOperationHoursCommand command);

    void receivedResponse(GetEyeBrightnessCommand command);

    void receivedResponse(SetEyeBrightnessCommand command);

    void receivedResponse(GetCleanFilterIndicatorCommand command);
}
