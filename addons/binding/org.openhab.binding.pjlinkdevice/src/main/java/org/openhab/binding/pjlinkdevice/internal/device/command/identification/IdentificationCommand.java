/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.device.command.identification;

import java.util.HashMap;

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class IdentificationCommand extends AbstractCommand<IdentificationRequest, IdentificationResponse> {

    public enum IdentificationProperty {
        NAME,
        MANUFACTURER,
        MODEL,
        CLASS,
        OTHER_INFORMATION,
        LAMP_HOURS;

        public String getJPLinkCommandPrefix() {
            final HashMap<IdentificationProperty, String> texts = new HashMap<IdentificationProperty, String>();
            texts.put(NAME, "NAME");
            texts.put(MANUFACTURER, "INF1");
            texts.put(MODEL, "INF2");
            texts.put(CLASS, "CLSS");
            texts.put(OTHER_INFORMATION, "INFO");
            texts.put(LAMP_HOURS, "LAMP");
            return texts.get(this);
        }
    }

    protected IdentificationProperty identificationProperty;

    public IdentificationCommand(PJLinkDevice pjLinkDevice, IdentificationProperty identificationProperty) {
        super(pjLinkDevice);
        this.identificationProperty = identificationProperty;
    }

    @Override
    protected IdentificationRequest createRequest() {
        return new IdentificationRequest(this);

    }

    @Override
    protected IdentificationResponse parseResponse(String response) throws ResponseException {
        IdentificationResponse result = new IdentificationResponse(this);
        result.parse(response);
        return result;
    }

}
