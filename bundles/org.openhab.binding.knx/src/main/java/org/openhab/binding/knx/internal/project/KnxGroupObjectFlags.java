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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The effective flags of a group object: each flag from the first level that sets it, the object instance of
 * the project, then the object reference, then the object of the application program. A flag no level sets is
 * off.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxGroupObjectFlags(boolean read, boolean write, boolean communication, boolean transmit, boolean update,
        boolean readOnInit) {
}
