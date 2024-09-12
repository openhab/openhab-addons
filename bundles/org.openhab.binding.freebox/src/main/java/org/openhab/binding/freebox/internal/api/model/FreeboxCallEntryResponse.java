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
package org.openhab.binding.freebox.internal.api.model;

import java.util.List;

/**
 * The {@link FreeboxCallEntryResponse} is the Java class used to map the
 * response of the call API
 * https://dev.freebox.fr/sdk/os/call/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxCallEntryResponse extends FreeboxResponse<List<FreeboxCallEntry>> {
}
