/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.extensionservice.marketplace;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is an exception that can be thrown by {@link MarketplaceExtensionHandler}s if some operation fails.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class MarketplaceHandlerException extends Exception {

    private static final long serialVersionUID = -5652014141471618161L;

    /**
     * Main constructor
     *
     * @param message A message describing the issue
     */
    public MarketplaceHandlerException(String message) {
        super(message);
    }
}
