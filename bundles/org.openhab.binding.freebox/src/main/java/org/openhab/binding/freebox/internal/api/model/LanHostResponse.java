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
package org.openhab.binding.freebox.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freebox.internal.api.FreeboxResponse;

/**
 * The {@link LanHostResponse} is the Java class used to map the
 * response of the lan browser API
 * https://dev.freebox.fr/sdk/os/lcd/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanHostResponse extends FreeboxResponse<LanHost> {
}
