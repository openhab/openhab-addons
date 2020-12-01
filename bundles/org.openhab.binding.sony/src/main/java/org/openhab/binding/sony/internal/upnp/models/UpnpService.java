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

import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.net.NetUtil;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the deserialized results of an UPNP service. The following is an example of the
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
@XStreamAlias("service")
public class UpnpService {

    /** The service identifier */
    @XStreamAlias("serviceId")
    private final @Nullable String serviceId;

    /** The service type */
    @XStreamAlias("serviceType")
    private final @Nullable String serviceType;

    /** The scpd url */
    @XStreamAlias("SCPDURL")
    private final @Nullable String scpdUrl;

    /** The control url */
    @XStreamAlias("controlURL")
    private final @Nullable String controlUrl;

    /**
     * Constructs a UpnpService
     * 
     * @param serviceId a non-null service ID
     * @param serviceType a non-null service type
     * @param scpdUrl a non-null scpd URL
     * @param controlUrl a non-null control URL
     */
    public UpnpService(final String serviceId, final String serviceType, final String scpdUrl,
            final String controlUrl) {
        Validate.notEmpty(serviceId, "serviceId cannot be empty");
        Validate.notEmpty(serviceType, "serviceType cannot be empty");
        Validate.notEmpty(scpdUrl, "scpdUrl cannot be empty");
        Validate.notEmpty(controlUrl, "controlUrl cannot be empty");

        this.serviceId = serviceId;
        this.serviceType = serviceType;
        this.scpdUrl = scpdUrl;
        this.controlUrl = controlUrl;
    }

    /**
     * Gets the SCPD URL given the base URL
     * 
     * @param baseUrl the non-null base url to use as a reference
     *
     * @return the control url
     */
    public @Nullable URL getScpdUrl(final URL baseUrl) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");

        final String localScpdUrl = scpdUrl;
        return localScpdUrl == null || StringUtils.isEmpty(localScpdUrl) ? null : NetUtil.getUrl(baseUrl, localScpdUrl);
    }

    /**
     * Gets the control url
     * 
     * @param baseUrl the non-null base url to use as a reference
     * @return the control url
     */
    public @Nullable URL getControlUrl(final URL baseUrl) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");

        final String localControlUrl = controlUrl;
        return localControlUrl == null || StringUtils.isEmpty(localControlUrl) ? null
                : NetUtil.getUrl(baseUrl, localControlUrl);
    }

    /**
     * Gets the service type
     *
     * @return the service type
     */
    public @Nullable String getServiceType() {
        return serviceType;
    }

    /**
     * Gets the service id
     *
     * @return the service id
     */
    public @Nullable String getServiceId() {
        return serviceId;
    }
}
