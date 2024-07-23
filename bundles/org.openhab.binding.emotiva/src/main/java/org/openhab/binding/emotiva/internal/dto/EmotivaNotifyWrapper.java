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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Emotiva Notify message type. 2.x version of protocol uses command type as prefix in each line in the body, while 3.x
 * users property as prefix with name="commandType". 2.x is handled as a element with a special handler unmarshall
 * handler in {@link org.openhab.binding.emotiva.internal.protocol.EmotivaXmlUtils}, while 3.x qualifies as a proper xml
 * element and can be properly unmarshalled by
 * JAXB.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaNotify")
public class EmotivaNotifyWrapper extends AbstractNotificationDTO {

    @XmlAttribute
    private String sequence;

    @SuppressWarnings("unused")
    public EmotivaNotifyWrapper() {
    }

    public EmotivaNotifyWrapper(String sequence, List<EmotivaPropertyDTO> properties) {
        this.sequence = sequence;
        this.properties = properties;
    }

    public String getSequence() {
        return sequence;
    }
}
