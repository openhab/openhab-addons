/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;

/**
 * This class represents the service protocols for a given service.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ServiceProtocol {
    /** The service name */
    private final String serviceName;

    /** The service protocols (immutable) */
    private final Set<String> protocols;

    /**
     * Creates the service protocol from the name and protocols
     * 
     * @param serviceName a non-null, non-empty service name
     * @param protocols a non-null, possibly empty set of protocols
     */
    public ServiceProtocol(final String serviceName, final Set<String> protocols) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        Objects.requireNonNull(protocols, "protocols cannot be null");

        this.serviceName = serviceName;
        this.protocols = Collections.unmodifiableSet(new HashSet<>(protocols));
    }

    /**
     * Returns the service name
     * 
     * @return a non-null, non-empty service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Returns the protocols for the service
     * 
     * @return a non-null, possibly empty set of protocols
     */
    public Set<String> getProtocols() {
        return protocols;
    }

    /**
     * Determines if the web socket protocol ({@link SonyTransport#WEBSOCKET}) is one of the protocols
     * 
     * @return true if the web socket protocol is found, false otherwise
     */
    public boolean hasWebsocketProtocol() {
        return protocols.contains(SonyTransportFactory.WEBSOCKET);
    }

    /**
     * Determines if the http protocol ({@link SonyTransport#HTTP}) is one of the protocols
     * 
     * @return true if the http protocol is found, false otherwise
     */
    public boolean hasHttpProtocol() {
        return protocols.contains(SonyTransportFactory.HTTP);
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServiceProtocol other = (ServiceProtocol) obj;
        return StringUtils.equals(serviceName, other.serviceName);
    }

    @Override
    public String toString() {
        return "ServiceProtocol [serviceName=" + serviceName + ", protocols=" + protocols + "]";
    }
}
