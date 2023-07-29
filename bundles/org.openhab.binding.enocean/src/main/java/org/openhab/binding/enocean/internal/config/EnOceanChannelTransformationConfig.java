/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.config;

import org.openhab.core.config.core.Configuration;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanChannelTransformationConfig extends Configuration {

    public String transformationType;
    public String transformationFunction;

    public EnOceanChannelTransformationConfig() {
        put("transformationType", "");
        put("transformationFunction", "");
    }
}
