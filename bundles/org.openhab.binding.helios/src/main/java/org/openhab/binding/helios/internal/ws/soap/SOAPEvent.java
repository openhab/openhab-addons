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
package org.openhab.binding.helios.internal.ws.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openhab.binding.helios.internal.handler.HeliosHandler27;

/**
 * Helios SOAP Protocol Message
 *
 * @author Karel Goderis - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HeliosEvent", namespace = HeliosHandler27.HELIOS_URI, propOrder = { "timestamp", "eventName", "data" })
@XmlRootElement(name = "Msg")
public class SOAPEvent {

    @XmlElement(name = "Timestamp", namespace = HeliosHandler27.HELIOS_URI)
    protected String timestamp;

    @XmlElement(name = "EventName", namespace = HeliosHandler27.HELIOS_URI)
    protected String eventName;

    @XmlElement(name = "Data", namespace = HeliosHandler27.HELIOS_URI)
    protected SOAPDataField data;

    public SOAPDataField getData() {
        return data;
    }

    public void setData(SOAPDataField data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SOAPEvent)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        SOAPEvent event = (SOAPEvent) obj;

        if (event.getEventName().equals(eventName) && event.getTimestamp().equals(timestamp)) {
            Object eventData = event.getData();

            if (data instanceof SOAPKeyPressed) {
                return ((SOAPKeyPressed) data).getKeyCode().equals(((SOAPKeyPressed) eventData).getKeyCode());
            }

            if (data instanceof SOAPCallStateChanged) {
                return ((SOAPCallStateChanged) data).getDirection()
                        .equals(((SOAPCallStateChanged) eventData).getDirection())
                        && ((SOAPCallStateChanged) data).getState()
                                .equals(((SOAPCallStateChanged) eventData).getState());
            }

            if (data instanceof SOAPCardEntered) {
                return ((SOAPCardEntered) data).getCard().equals(((SOAPCardEntered) eventData).getCard())
                        && ((SOAPCardEntered) data).getValid().equals(((SOAPCardEntered) eventData).getValid());
            }

            if (data instanceof SOAPCodeEntered) {
                return ((SOAPCodeEntered) data).getCode().equals(((SOAPCodeEntered) eventData).getCode())
                        && ((SOAPCodeEntered) data).getValid().equals(((SOAPCodeEntered) eventData).getValid());
            }

            if (data instanceof SOAPDeviceState) {
                return ((SOAPDeviceState) data).getState().equals(((SOAPDeviceState) eventData).getState());
            }
        }

        return false;
    }
}
