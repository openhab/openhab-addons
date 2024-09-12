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
package org.openhab.binding.mycroft.internal.channels;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;

/**
 * Interface for channel which can handle command
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public interface ChannelCommandHandler {

    void handleCommand(Command command);
}
