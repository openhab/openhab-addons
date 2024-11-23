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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO that contains an API Action entry.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ActionEntry {
    private @NonNullByDefault({}) ResourceReference target;
    private @NonNullByDefault({}) Resource action;

    public ResourceReference getTarget() {
        return target;
    }

    public Resource getAction() {
        return action;
    }
}
