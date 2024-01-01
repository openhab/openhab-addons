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
package org.openhab.binding.solax.internal.connectivity.rawdata;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RawDataBean} is interface which should be implemented by all types of raw information that is retrieved
 * (the idea is to retrieve a raw data from a Solax inverter locally or their cloud API)
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface RawDataBean {
    @Nullable
    String getRawData();
}
