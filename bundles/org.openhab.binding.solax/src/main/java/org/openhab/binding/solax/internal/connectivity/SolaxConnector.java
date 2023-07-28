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
package org.openhab.binding.solax.internal.connectivity;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SolaxConnector} is interface for connecting to the Solax endpoints (cloud API or local IP)
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface SolaxConnector {

    @Nullable
    String retrieveData() throws IOException;
}
