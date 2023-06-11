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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Basic command interface allowing to execute the command.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public interface Command<ResponseType extends Response<?>> {
    public ResponseType execute() throws ResponseException, IOException, AuthenticationException;
}
