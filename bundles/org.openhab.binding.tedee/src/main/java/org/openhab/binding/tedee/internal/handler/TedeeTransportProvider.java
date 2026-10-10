/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

package org.openhab.binding.tedee.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tedee.internal.api.TedeeClient;

/**
 * 1
 * Handler for a Cloud connection to the Tedee Cloud
 *
 * @author Alex Goll - Initial contribution
 */

@NonNullByDefault
public interface TedeeTransportProvider {

    @Nullable
    TedeeClient getClient();

    boolean ready();
}
