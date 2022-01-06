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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The {@link SOAPDataFieldAdapter} is a helper class that is used to
 * unmarshal some 'variable' SOAP messages
 *
 * @author Karel Goderis - Initial contribution
 */
public class SOAPDataFieldAdapter extends XmlAdapter<SOAPDataFieldAdapter.AdaptedHeliosDataField, SOAPDataField> {

    public static class AdaptedHeliosDataField {

        @XmlElement
        public String State;

        @XmlElement
        public String Direction;

        @XmlElement
        public String Code;

        @XmlElement
        public String Valid;

        @XmlElement
        public String Key;

        @XmlElement
        public String Card;
    }

    @Override
    public AdaptedHeliosDataField marshal(SOAPDataField arg0) throws Exception {
        return null;
    }

    @Override
    public SOAPDataField unmarshal(AdaptedHeliosDataField arg0) throws Exception {
        if (null == arg0) {
            return null;
        }
        if (null != arg0.Card && null != arg0.Valid) {
            SOAPCardEntered heliosCardEntered = new SOAPCardEntered();
            heliosCardEntered.setCard(arg0.Card);
            heliosCardEntered.setValid(arg0.Valid);
            return heliosCardEntered;
        }
        if (null != arg0.Code && null != arg0.Valid) {
            SOAPCodeEntered heliosCodeEntered = new SOAPCodeEntered();
            heliosCodeEntered.setCode(arg0.Code);
            heliosCodeEntered.setValid(arg0.Valid);
            return heliosCodeEntered;
        }
        if (null != arg0.Key) {
            SOAPKeyPressed heliosKeyPressed = new SOAPKeyPressed();
            heliosKeyPressed.setKeyCode(arg0.Key);
            return heliosKeyPressed;
        }
        if (null != arg0.State && null != arg0.Direction) {
            SOAPCallStateChanged heliosCallStateChanged = new SOAPCallStateChanged();
            heliosCallStateChanged.setState(arg0.State);
            heliosCallStateChanged.setDirection(arg0.Direction);
            return heliosCallStateChanged;
        }
        if (null != arg0.State) {
            SOAPDeviceState heliosDeviceState = new SOAPDeviceState();
            heliosDeviceState.setState(arg0.State);
            return heliosDeviceState;
        }
        return null;
    }
}
