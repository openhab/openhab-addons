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
package org.openhab.binding.matter.internal.client.dto.ws;

import java.math.BigInteger;

/**
 * ExposedFabricInformation represents Matter Fabric information from a device.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ExposedFabricInformation {
    public Integer fabricIndex;
    public String fabricId;
    public BigInteger nodeId;
    public String rootNodeId;
    public byte[] operationalId;
    public byte[] rootPublicKey;
    public Integer rootVendorId;
    public byte[] rootCert;
    public byte[] identityProtectionKey;
    public byte[] operationalIdentityProtectionKey;
    public byte[] intermediateCACert;
    public byte[] operationalCert;
    public String label;
}
