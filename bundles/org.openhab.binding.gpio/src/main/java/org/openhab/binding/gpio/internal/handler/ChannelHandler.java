/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.gpio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;

/**
 * The {@link ChannelHandler} provides an interface for different pin
 * configuration handlers
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface ChannelHandler {

    void handleCommand(Command command);
}
