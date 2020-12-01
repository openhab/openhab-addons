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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.SonyUtil;

/**
 * The class represents a sony device capability. The capability describes the device and then describes the services
 * within the device. The class will only be used to serialize the definition.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyDeviceCapability {
    /** The model name of the device */
    private @Nullable String modelName;

    /** The base URL to the device */
    private @Nullable URL baseURL;

    /** A list of service capabilities */
    private @Nullable List<@Nullable SonyServiceCapability> services;

    /**
     * Empty constructor for deserialization
     */
    public SonyDeviceCapability() {
    }

    /**
     * Constructs the capability from the parameters
     *
     * @param modelName a non-null, non-empty model name
     * @param baseURL a non-null base url
     * @param services a non-null, possibly empty list of services
     */
    public SonyDeviceCapability(final String modelName, final URL baseURL, final List<SonyServiceCapability> services) {
        Validate.notEmpty(modelName, "modelName cannot be empty");
        Objects.requireNonNull(baseURL, "baseURL cannot be null");
        Objects.requireNonNull(services, "services cannot be null");

        this.modelName = modelName;
        this.baseURL = baseURL;
        this.services = new ArrayList<>(services);
    }

    /**
     * Returns the model name of the device
     *
     * @return the possibly null, possibly empty model name
     */
    public @Nullable String getModelName() {
        return modelName;
    }

    /**
     * Returns the service capabilities
     * 
     * @return a non-null, possibly empy list of service capabilities
     */
    public List<SonyServiceCapability> getServices() {
        return Collections.unmodifiableList(SonyUtil.convertNull(services));
    }

    @Override
    public String toString() {
        return "SonyDeviceCapability [modelName=" + modelName + ", baseURL=" + baseURL + ", services=" + services + "]";
    }
}
