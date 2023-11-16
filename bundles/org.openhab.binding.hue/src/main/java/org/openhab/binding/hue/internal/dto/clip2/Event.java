/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto.clip2;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

/**
 * DTO for CLIP 2 event stream objects.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Event {
    public static final Type EVENT_LIST_TYPE = new TypeToken<List<Event>>() {
    }.getType();

    private @Nullable List<Resource> data = new ArrayList<>();

    public List<Resource> getData() {
        List<Resource> data = this.data;
        return Objects.nonNull(data) ? data : List.of();
    }
}
