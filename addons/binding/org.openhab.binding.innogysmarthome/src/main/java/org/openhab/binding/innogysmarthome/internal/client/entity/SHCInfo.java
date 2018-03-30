/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
