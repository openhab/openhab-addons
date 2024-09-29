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
package org.openhab.binding.tr064.internal.soap;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;

/**
 * The {@link SOAPRequest} is a wrapper for SOAP requests
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SOAPRequest {
    public SCPDServiceType service;
    public String soapAction;
    public Map<String, String> arguments = Map.of();

    public SOAPRequest(SCPDServiceType service, String soapAction) {
        this.service = service;
        this.soapAction = soapAction;
    }

    public SOAPRequest(SCPDServiceType service, String soapAction, Map<String, String> arguments) {
        this.service = service;
        this.soapAction = soapAction;
        this.arguments = arguments;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SOAPRequest that = (SOAPRequest) o;

        if (!service.equals(that.service)) {
            return false;
        }
        if (!soapAction.equals(that.soapAction)) {
            return false;
        }

        return arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        int result = service.hashCode();
        result = 31 * result + soapAction.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SOAPRequest{" + "service=" + service + ", soapAction='" + soapAction + '\'' + ", arguments=" + arguments
                + '}';
    }
}
