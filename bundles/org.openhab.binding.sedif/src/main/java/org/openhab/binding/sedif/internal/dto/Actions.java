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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Actions} holds authentication information
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class Actions {

    public class Action {
        public @Nullable String id;
        public @Nullable String descriptor;
        public @Nullable String callingDescriptor;
        public Hashtable<String, Object> params = new Hashtable<String, Object>();
    }

    public List<Action> actions = new ArrayList<Action>();
}
