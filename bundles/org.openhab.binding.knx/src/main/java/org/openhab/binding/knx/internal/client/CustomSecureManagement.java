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
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

import tuwien.auto.calimero.SerialNumber;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.mgmt.SecureManagement;
import tuwien.auto.calimero.mgmt.TransportLayer;
import tuwien.auto.calimero.mgmt.TransportLayerImpl;
import tuwien.auto.calimero.secure.Security;

/**
 * This class is to provide access to protected constructors in the Calimero library.
 * Reason is to provide custom KNX keyring data.
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@NonNullByDefault
public class CustomSecureManagement extends SecureManagement {
    public CustomSecureManagement(final TransportLayer transportLayer, final Security security)
            throws KNXLinkClosedException {
        // super(link, secureManagement) is not yet available in Calimero 2.5.1
        super((TransportLayerImpl) transportLayer, SerialNumber.Zero, 0, security.deviceToolKeys());

        // instance of Security has been created in ctor of SecureManagement, but only using deviceToolKeys
        // no need to clear, just copy (otherwise SAL would lack the group keys)
        final Security sal = security();
        sal.groupKeys().putAll(security.groupKeys());
        sal.groupSenders().putAll(security.groupSenders());
    }

    public final Security security() {
        return super.security();
    }
}
