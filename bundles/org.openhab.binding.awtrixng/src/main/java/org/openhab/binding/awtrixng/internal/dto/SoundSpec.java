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
package org.openhab.binding.awtrixng.internal.dto;

/**
 * The {@code sound} object of a notification and the payload of {@code cmd/audio/play}. Exactly one of
 * {@code file}, {@code rtttl} or {@code track} is expected to be set.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class SoundSpec {
    public String file;
    public String rtttl;
    public Integer track;
    public Boolean loop;
}
