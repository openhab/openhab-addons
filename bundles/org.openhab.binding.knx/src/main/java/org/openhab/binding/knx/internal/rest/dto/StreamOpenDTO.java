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
package org.openhab.binding.knx.internal.rest.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The data of the stream's first event {@code open}: the number the stream goes on from and the monitor the numbers
 * belong to; {@code reset} true when the numbers the caller gave belong to another monitor or lie ahead of it.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class StreamOpenDTO {
    public long lastNumber;
    public String monitorId;
    public @Nullable Boolean reset;
}
