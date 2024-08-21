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
package org.openhab.binding.icloud.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudDeviceInformation;

/**
 * Classes that implement this interface are interested in device information updates.
 *
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public interface ICloudDeviceInformationListener {
    void deviceInformationUpdate(List<ICloudDeviceInformation> deviceInformationList);
}
