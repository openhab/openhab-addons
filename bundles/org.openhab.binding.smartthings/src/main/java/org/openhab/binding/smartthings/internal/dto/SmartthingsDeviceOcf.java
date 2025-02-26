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
package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for Ocf device description
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class SmartthingsDeviceOcf {
    public String ocfDeviceType;
    public String name;
    public String specVersion;
    public String verticalDomainSpecVersion;
    public String manufacturerName;
    public String modelNumber;
    public String platformVersion;
    public String platformOS;
    public String hwVersion;
    public String firmwareVersion;
    public String vendorId;
    public String lastSignupTime;
    public Boolean transferCandidate;
    public Boolean additionalAuthCodeRequired;
}
