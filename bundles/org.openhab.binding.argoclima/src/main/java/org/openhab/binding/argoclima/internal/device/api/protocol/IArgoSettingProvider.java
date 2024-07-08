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
package org.openhab.binding.argoclima.internal.device.api.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.configuration.IScheduleConfigurationProvider;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.IArgoCommandableElement.IArgoElement;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;

/**
 * Interface for accessing HVAC-specific settings (knobs that can be controlled or report status)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public interface IArgoSettingProvider {
    /**
     * Retrieve a concrete HVAC protocol element by its kind
     *
     * @param type The kind of element (setting) to return
     * @return The controllable element of requested kind
     * @throws RuntimeException In case the element is N/A
     */
    public ArgoApiDataElement<IArgoElement> getSetting(ArgoDeviceSettingType type);

    /**
     * Get the schedule provider (for configuring schedule timers)
     *
     * @return Current schedule provider
     */
    public IScheduleConfigurationProvider getScheduleProvider();
}
