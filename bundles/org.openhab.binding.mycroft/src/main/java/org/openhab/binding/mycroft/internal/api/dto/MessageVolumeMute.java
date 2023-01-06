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
package org.openhab.binding.mycroft.internal.api.dto;

import org.openhab.binding.mycroft.internal.api.MessageType;

/**
 * This message asks Mycroft to mute the volume
 *
 * @author Gwendal Roulleau - Initial contribution
 */
public class MessageVolumeMute extends BaseMessage {

    public Data data = new Data();

    public MessageVolumeMute() {
        this.type = MessageType.mycroft_volume_mute;
    }

    public static class Data {
        public Boolean speak_message = false;
    }
}
