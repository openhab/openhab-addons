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
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.calimero.link.KNXLinkClosedException;
import io.calimero.mgmt.ManagementClient;
import io.calimero.mgmt.ManagementProceduresImpl;
import io.calimero.mgmt.TransportLayer;

/**
 * This class is to provide access to protected constructors in the Calimero library.
 * Reason is to provide custom KNX keyring data.
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@NonNullByDefault
public class CustomManagementProceduresImpl extends ManagementProceduresImpl {
    public CustomManagementProceduresImpl(final ManagementClient mgmtClient, final TransportLayer transportLayer)
            throws KNXLinkClosedException {
        // super(mgmtClient, transportLayer) is protected
        super(mgmtClient, transportLayer);
    }
}
