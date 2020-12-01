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
package org.openhab.binding.sony.internal.providers.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;

/**
 * The class represents a sony device service capability. The capability describes the service and the
 * methods/notifications for the service. The class will only be used to serialize the definition.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyServiceCapability {
    /** The service name */
    private @Nullable String serviceName;

    /** The service version */
    private @Nullable String version;

    /** The transport used for this service */
    private @Nullable String transport;

    /** The methods defined for the service */
    private @Nullable List<@Nullable ScalarWebMethod> methods;

    /** The notifications that can be sent from the service */
    private @Nullable List<@Nullable ScalarWebMethod> notifications;

    /**
     * Empty constructor for deserialization
     */
    public SonyServiceCapability() {
    }

    /**
     * Constructs the capability from the parameters
     *
     * @param serviceName a non-null, non-empty service name
     * @param version a non-null, non-empty service version
     * @param transport a non-null, non-empty transport
     * @param methods a non-null, possibly empty list of methods
     * @param notifications a non-null, possibly empty list of notifications
     */
    public SonyServiceCapability(final String serviceName, final String version, final String transport,
            final List<ScalarWebMethod> methods, final List<ScalarWebMethod> notifications) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        Validate.notEmpty(version, "version cannot be empty");
        Validate.notEmpty(transport, "transport cannot be empty");
        Objects.requireNonNull(methods, "methods cannot be null");
        Objects.requireNonNull(notifications, "notifications cannot be null");

        this.serviceName = serviceName;
        this.version = version;
        this.transport = transport;
        this.methods = new ArrayList<>(methods);
        this.notifications = new ArrayList<>(notifications);
    }

    /**
     * Returns the service name for this capability
     * 
     * @return a possibly null, possibly empty service name
     */
    public @Nullable String getServiceName() {
        return serviceName;
    }

    /**
     * Returns the service version for this capability
     * 
     * @return a possibly null, possibly empty service version
     */
    public @Nullable String getVersion() {
        return version;
    }

    /**
     * Returns the transport to use with this capability
     * 
     * @return a possibly null, possibly empty transport to use
     */
    public @Nullable String getTransport() {
        return transport;
    }

    /**
     * Returns the methods (if any) for this service
     * 
     * @return a non-null, but possibly empty list of methods
     */
    public List<ScalarWebMethod> getMethods() {
        return Collections.unmodifiableList(SonyUtil.convertNull(methods));
    }

    /**
     * Returns the notifications (if any) for this service
     * 
     * @return a non-null, but possibly empty list of notifications
     */
    public List<ScalarWebMethod> getNotifications() {
        return Collections.unmodifiableList(SonyUtil.convertNull(notifications));
    }

    @Override
    public String toString() {
        return "SonyServiceCapability [serviceName=" + serviceName + ", version=" + version + ", transport=" + transport
                + ", methods=" + methods + ", notifications=" + notifications + "]";
    }
}
