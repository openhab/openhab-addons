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
package org.openhab.extensionservice.marketplace.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is the parent object that holds the category of the market.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@NonNullByDefault
public class Marketplace {

    /**
     * The category of the marketplace
     */
    public Category[] categories = new Category[0];
}
