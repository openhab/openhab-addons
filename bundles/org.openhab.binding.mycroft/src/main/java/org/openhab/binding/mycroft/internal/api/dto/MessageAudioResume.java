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
 * This message asks Mycroft to send resume command to
 * its underlying player
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
public class MessageAudioResume extends BaseMessage {

    public MessageAudioResume() {
        this.type = MessageType.mycroft_audio_service_resume;
    }
}
