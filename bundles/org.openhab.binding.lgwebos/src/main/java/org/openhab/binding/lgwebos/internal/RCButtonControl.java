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
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Handles rcButton Control Command. This allows to send IR Remote Control button presses to the TV.
 *
 * @author Robert Brodt - Initial contribution
 */
@NonNullByDefault
public class RCButtonControl extends BaseChannelHandler<CommandConfirmation> {

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        if (RefreshType.REFRESH == command) {
            return;
        }
        handler.getSocket().sendRCButton(command.toString(), getDefaultResponseListener());
    }
}
