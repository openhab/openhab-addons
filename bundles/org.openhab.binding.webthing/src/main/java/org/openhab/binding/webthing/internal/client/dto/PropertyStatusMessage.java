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
package org.openhab.binding.webthing.internal.client.dto;

import java.util.Map;

/**
 * Web Thing WebSocket API property status message. Refer https://iot.mozilla.org/wot/#propertystatus-message
 *
 * @author Gregor Roth - Initial contribution
 */
public class PropertyStatusMessage {

    public String messageType = "<undefined>";

    public Map<String, Object> data = Map.of();

    @Override
    public String toString() {
        return "PropertyStatusMessage{" + "messageType='" + messageType + '\'' + ", data=" + data + '}';
    }
}
