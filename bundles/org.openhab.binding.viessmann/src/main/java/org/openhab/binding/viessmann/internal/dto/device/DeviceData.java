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
package org.openhab.binding.viessmann.internal.dto.device;

import java.util.List;

/**
 * The {@link DeviceData} provides all data of a device
 *
 * @author Ronny Grun - Initial contribution
 */
public class DeviceData {
    public String gatewaySerial;
    public String id;
    public String installationId;
    public String boilerSerial;
    public String boilerSerialEditor;
    public String bmuSerial;
    public String bmuSerialEditor;
    public String createdAt;
    public String editedAt;
    public String modelId;
    public String status;
    public String deviceType;
    public List<String> roles = null;

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }
}
