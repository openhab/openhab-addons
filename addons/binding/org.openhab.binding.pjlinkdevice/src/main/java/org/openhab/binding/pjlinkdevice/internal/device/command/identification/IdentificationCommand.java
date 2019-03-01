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

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class IdentificationCommand extends AbstractCommand<IdentificationRequest, IdentificationResponse> {

    public enum IdentificationProperty {
        NAME("NAME"),
        MANUFACTURER("INF1"),
        MODEL("INF2"),
        CLASS("CLSS"),
        OTHER_INFORMATION("INFO"),
        LAMP_HOURS("LAMP");

        private String prefix;

        private IdentificationProperty(String prefix) {
            this.prefix = prefix;
        }

        public String getJPLinkCommandPrefix() {
            return this.prefix;
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
