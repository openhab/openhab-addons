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
package org.openhab.binding.viessmann.internal.dto.installation;

import java.util.List;

/**
 * The {@link Device} provides a device on the installation
 *
 * @author Ronny Grun - Initial contribution
 */
public class Device {
    public String gatewaySerial;
    public String id;
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
    public boolean isBoilerSerialEditable;
    public String brand;
    public String translationKey;
}
