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
package org.openhab.binding.lutron.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;

/**
 * The {@link KeypadComponent} interface is used to access enums describing the possible components
 * in a given keypad model.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public interface KeypadComponent {

    int id();

    String channel();

    String description();

    ComponentType type();
}
