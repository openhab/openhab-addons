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
package org.openhab.binding.sony.internal.upnp.models;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the deserialized results of an UPNP services. The following is an example of the
 * results that will be deserialized:
 *
 * <pre>
 * {@code
     need example
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class UpnpServiceList {
    /** The list of services */
    @XStreamImplicit
    private @Nullable List<UpnpService> services;

    /**
     * The list of services
     * 
     * @return a non-null, possibly empty list of services
     */
    public List<UpnpService> getServices() {
        return services == null ? Collections.emptyList() : Collections.unmodifiableList(services);
    }
}
