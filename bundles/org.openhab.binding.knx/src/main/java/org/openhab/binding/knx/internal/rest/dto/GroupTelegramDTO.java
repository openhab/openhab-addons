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

/**
 * What became of a group telegram the page asked to send: {@code action} is {@code read} or {@code write},
 * {@code state} is {@code sent} for a write that went out and {@code queued} for a read request that waits for its
 * turn in the bridge's read queue.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class GroupTelegramDTO {
    public String action;
    public String state;
}
