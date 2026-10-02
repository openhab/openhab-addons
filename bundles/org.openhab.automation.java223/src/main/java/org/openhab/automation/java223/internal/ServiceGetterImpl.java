/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;

/**
 * Get a service from the OSGi service registry.
 * Why should we implement a service instead of exposing the implementation directly to the user?
 * The implementation requires OSGi-related classes.
 * As the common package is exposed to the end user, we shouldn't force him to add various OSGi dependencies
 * in its IDE for its script to show with no errors.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@Component
@NonNullByDefault
public class ServiceGetterImpl implements ServiceGetter {

    @Override
    @Nullable
    public <T> T getService(Class<T> clazz) {
        Bundle bundle = FrameworkUtil.getBundle(clazz);
        if (bundle != null) {
            BundleContext bc = bundle.getBundleContext();
            if (bc != null) {
                ServiceReference<T> ref = bc.getServiceReference(clazz);
                if (ref != null) {
                    T result = bc.getService(ref);
                    if (result != null) {
                        bc.ungetService(ref);
                    }
                    return result;
                }
            }
        }
        return null;
    }
}
