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
package org.openhab.binding.pjlinkdevice.internal.device.command.identification;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * This command is used for retrieving device information as described in
 * <a href="https://pjlink.jbmia.or.jp/english/data_cl2/PJLink_5-1.pdf">[PJLinkSpec]</a> chapters:
 * 4.8. Lamp number/ lighting hour query
 * 4.10. Projector/Display name query
 * 4.11. Manufacture name information query
 * 4.12. Product name information query
 * 4.13. Other information query
 * 4.14. Class information query
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
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

        public String getPJLinkCommandPrefix() {
            return this.prefix;
        }
    }

    private IdentificationProperty identificationProperty;

    public IdentificationCommand(PJLinkDevice pjLinkDevice, IdentificationProperty identificationProperty) {
        super(pjLinkDevice);
        this.identificationProperty = identificationProperty;
    }

    public IdentificationProperty getIdentificationProperty() {
        return identificationProperty;
    }

    @Override
    protected IdentificationRequest createRequest() {
        return new IdentificationRequest(this);
    }

    @Override
    protected IdentificationResponse parseResponse(String response) throws ResponseException {
        return new IdentificationResponse(this, response);
    }
}
