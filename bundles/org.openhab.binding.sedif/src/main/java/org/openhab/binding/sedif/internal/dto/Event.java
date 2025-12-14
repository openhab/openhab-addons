/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sedif.internal.dto;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Event} holds authentication information
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class Event {
    public class Attributes {
        public Hashtable<String, Object> values = new Hashtable<String, Object>();
    }

    public @Nullable String descriptor;
    public @Nullable Attributes attributes;
}
