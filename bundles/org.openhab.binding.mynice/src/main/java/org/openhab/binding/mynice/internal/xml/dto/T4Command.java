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
package org.openhab.binding.mynice.internal.xml.dto;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enum lists all handled T4 commands
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum T4Command {
    MDAx(1),
    MDAy(2),
    MDAz(3),
    MDA0(4),
    MDA1(5),
    MDA2(6),
    MDA3(7),
    MDBi(11),
    MDBj(12),
    MDBk(13),
    MDBl(14),
    MDBm(15),
    MDEw(16),
    MDEx(17),
    MDEy(18),
    MDEz(19),
    MDE0(20),
    MDE1(21),
    MDE2(22),
    MDE3(23),
    MDE4(24),
    MDE5(25),
    MDFh(26);

    private int bitPosition;

    private T4Command(int bitPosition) {
        this.bitPosition = bitPosition;
    }

    public static T4Command fromCode(String commandCode) {
        return Stream.of(T4Command.values()).filter(command -> command.name().equalsIgnoreCase(commandCode)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown T4 command code (%s)".formatted(commandCode)));
    }

    public static List<T4Command> fromBitmask(int bitmask) {
        return Stream.of(T4Command.values()).filter(command -> ((1 << command.bitPosition) & bitmask) != 0).toList();
    }
}
