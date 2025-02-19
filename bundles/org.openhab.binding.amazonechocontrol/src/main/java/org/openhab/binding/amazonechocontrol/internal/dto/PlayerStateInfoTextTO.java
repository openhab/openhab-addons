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
package org.openhab.binding.amazonechocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link PlayerStateInfoTextTO} encapsulates the info text section of a player info
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PlayerStateInfoTextTO {
    public boolean multiLineMode;
    public String subText1;
    public String subText2;
    public String title;

    @Override
    public @NonNull String toString() {
        return "PlayerStateInfoTextTO{multiLineMode=" + multiLineMode + ", subText1='" + subText1 + "', subText2='"
                + subText2 + "', title='" + title + "'}";
    }
}
