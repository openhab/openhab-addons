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
package org.openhab.binding.digitalstrom.internal.lib.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;

/**
 * Model used in test cases.
 *
 * @author Rouven Schürch - Initial contribution
 *
 */
@NonNullByDefault
public class OutputChannel {
    public OutputChannel(OutputChannelEnum outputChannel) {
        super();
        this.channelID = outputChannel.getChannelId();
        this.name = outputChannel.getName();
        this.id = outputChannel.getName();
        this.index = outputChannel.getChannelId();
    }

    int channelID;
    String name;
    String id;
    int index;
}
