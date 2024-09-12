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
package org.openhab.binding.omnilink.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BridgeOfflineException} defines an exception for when the OmniLink
 * Bridge is offline or unavailable.
 *
 * @author Craig Hamilton - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class BridgeOfflineException extends Exception {
    public BridgeOfflineException(Exception e) {
        super(e);
    }
}
