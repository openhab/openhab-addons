/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.automation.java223.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.ServiceGetter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Get a service from the OSGi service registry.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@Component(service = ServiceGetter.class)
@NonNullByDefault
@SuppressWarnings("unused")
public class ServiceGetterImpl implements ServiceGetter {

    private final BundleContext bundleContext;

    @Activate
    public ServiceGetterImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    @Nullable
    public <T> T getService(Class<T> tClass) {
        ServiceReference<T> serviceReference = bundleContext.getServiceReference(tClass);
        if (serviceReference != null) {
            return bundleContext.getService(serviceReference);
        } else {
            return null;
        }
    }
}
