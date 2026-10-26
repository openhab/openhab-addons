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

import java.util.List;

/**
 * The capabilities JSON published retained on {@code <prefix>/state/capabilities}. It lists the names this clock
 * accepts, so option lists can follow the firmware instead of being hard coded.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class Capabilities {
    public static class Font {
        public String name = "";
    }

    public List<String> effects = List.of();
    public List<String> transitions = List.of();
    public List<String> overlays = List.of();
    public List<Font> fonts = List.of();
}
