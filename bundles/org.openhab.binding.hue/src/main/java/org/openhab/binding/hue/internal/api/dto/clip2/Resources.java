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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for CLIP 2 to retrieve a list of generic resources from the bridge.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Resources {
    private List<Error> errors = new ArrayList<>();
    private List<Resource> data = new ArrayList<>();

    public List<String> getErrors() {
        return errors.stream().map(Error::getDescription).collect(Collectors.toList());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<Resource> getResources() {
        return data;
    }
}
