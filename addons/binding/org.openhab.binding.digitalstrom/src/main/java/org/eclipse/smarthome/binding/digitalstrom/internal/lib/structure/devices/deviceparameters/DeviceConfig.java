/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters;

/**
 * The {@link DeviceConfig} saves device configurations.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - add missing java-doc
 * @author Matthias Siegele - add missing java-doc
 */
public interface DeviceConfig {

    /**
     * Returns the digitalSTROM-Device parameter class.
     *
     * @return configuration class
     */
    int getConfigurationClass();

    /**
     * Returns the digitalSTROM-Device configuration index.
     *
     * @return configuration index
     */
    int getIndex();

    /**
     * Returns the digitalSTROM-Device configuration value.
     *
     * @return configuration value
     */
    int getValue();
}
