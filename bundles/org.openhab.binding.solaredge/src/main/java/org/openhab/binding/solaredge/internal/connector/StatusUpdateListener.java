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
package org.openhab.binding.solaredge.internal.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * callback interface to update the status of the {@link WebInterface}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface StatusUpdateListener {

    void update(CommunicationStatus status);
}
