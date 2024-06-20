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
package org.openhab.binding.emotiva.internal.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The EmotivaUpdate message type. Received if an {@link EmotivaUpdateRequest} sent to a device.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaUpdate")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaUpdateResponse extends AbstractNotificationDTO {

    @SuppressWarnings("unused")
    public EmotivaUpdateResponse() {
    }

    public EmotivaUpdateResponse(List<EmotivaPropertyDTO> properties) {
        this.properties = properties;
    }
}
