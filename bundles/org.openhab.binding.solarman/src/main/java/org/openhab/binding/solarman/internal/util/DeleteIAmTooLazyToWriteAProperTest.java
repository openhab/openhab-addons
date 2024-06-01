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
package org.openhab.binding.solarman.internal.util;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Catalin Sanda - Initial contribution
 */
public class DeleteIAmTooLazyToWriteAProperTest {
    public static void main(String[] args) {
        List<Integer> registers = List.of(0x0016, 0x0017, 0x0018);
        Map<Integer, byte[]> readRegistersMap = Map.of(0x0016,
                ByteBuffer.allocate(2).putShort(0, (short) 0x1707).array(), 0x0017,
                ByteBuffer.allocate(2).putShort(0, (short) 0x1B0A).array(), 0x0018,
                ByteBuffer.allocate(2).putShort(0, (short) 0x3415).array());

        String stringValue = StreamUtils.zip(IntStream.range(0, registers.size()).boxed(),
                registers.stream().map(readRegistersMap::get).map(v -> ByteBuffer.wrap(v).getShort()),
                StreamUtils.Tuple::new).map(t -> {
                    int index = t.a();
                    short rawVal = t.b();

                    return switch (index) {
                        case 0 -> (rawVal >> 8) + "/" + (rawVal & 0xFF) + "/";
                        case 1 -> (rawVal >> 8) + " " + (rawVal & 0xFF) + ":";
                        case 2 -> (rawVal >> 8) + ":" + (rawVal & 0xFF);
                        default -> (rawVal >> 8) + "" + (rawVal & 0xFF);
                    };
                }).collect(Collectors.joining());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/M/d H:m:s");
        LocalDateTime dateTime = LocalDateTime.parse("23/7/2 5:6:7", formatter);

        System.err.println(stringValue);
        System.err.println(dateTime);
    }
}
