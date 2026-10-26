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

import org.eclipse.jdt.annotation.Nullable;

/**
 * One colored piece of a text, as sent in the {@code text} array.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class TextFragment {
    public String text;
    public @Nullable String color;

    public TextFragment(String text, @Nullable String color) {
        this.text = text;
        this.color = color;
    }
}
