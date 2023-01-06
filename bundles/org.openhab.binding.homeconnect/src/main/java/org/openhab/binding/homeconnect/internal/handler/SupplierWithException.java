/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;

/**
 * Custom supplier implementation with exceptions.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface SupplierWithException<T> {
    T get() throws CommunicationException, ApplianceOfflineException, AuthorizationException;
}
