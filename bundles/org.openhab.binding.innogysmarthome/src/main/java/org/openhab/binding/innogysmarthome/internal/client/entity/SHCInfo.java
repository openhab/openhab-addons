/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.entity;

import java.util.List;

import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;

import com.google.api.client.util.Key;

/**
 * Special data structure, which is returned on session initialization by the innogy API.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class SHCInfo {

    @Key("CurrentConfigurationVersion")
    public long currentConfigurationVersion;

    @Key("Data")
    public List<Device> deviceList;
}
