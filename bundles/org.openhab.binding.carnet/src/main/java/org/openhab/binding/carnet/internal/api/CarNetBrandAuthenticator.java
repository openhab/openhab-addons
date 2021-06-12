/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarException;

/**
 * {@link CarNetBrandAuthenticator} defines the interface for brand specific authentication support/flow
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface CarNetBrandAuthenticator {
    public String updateAuthorizationUrl(String url) throws CarException;
}
