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
package org.openhab.binding.atagone.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Gson DTO for the {@code pair_reply} object returned by {@code POST /pair}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class PairReplyDTO {
    public int seqnr;
    /** 1=pending (press Accept on device), 2=granted, 3=denied. */
    public int acc_status;
}
