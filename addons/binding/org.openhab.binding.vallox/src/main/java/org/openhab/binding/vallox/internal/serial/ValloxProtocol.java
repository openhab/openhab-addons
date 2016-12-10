/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal.serial;

/**
 * Some internal constants for the serial interface for
 * the vallox unit.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public class ValloxProtocol {

    public static final int BAUDRATE = 9600;
    public static final int LENGTH = 6; // always 6
    public static final byte DOMAIN = 1; // always 1

    // Addresses for sender and receiver
    public static final byte ADDRESS_MAINBOARDS = 0x10;
    public static final byte ADDRESS_MASTER = (byte) (ADDRESS_MAINBOARDS + 1);

    public static final byte ADDRESS_PANELS = 0x20;

    public static final byte ADDRESS_PANEL1 = (byte) (ADDRESS_PANELS + 1);
    public static final byte ADDRESS_PANEL2 = (byte) (ADDRESS_PANELS + 2);
    public static final byte ADDRESS_PANEL3 = (byte) (ADDRESS_PANELS + 3);
    public static final byte ADDRESS_PANEL4 = (byte) (ADDRESS_PANELS + 4);
    public static final byte ADDRESS_PANEL5 = (byte) (ADDRESS_PANELS + 5);
    public static final byte ADDRESS_PANEL6 = (byte) (ADDRESS_PANELS + 6);
    public static final byte ADDRESS_PANEL7 = (byte) (ADDRESS_PANELS + 7);
    public static final byte ADDRESS_LON = (byte) (ADDRESS_PANELS + 8);
    public static final byte ADDRESS_PANEL8 = (byte) (ADDRESS_PANELS + 9);

    static final byte FAN_SPEED_MAPPING[] = { 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, (byte) 0xFF };

    static final byte TEMPERATURE_MAPPING[] = { -74, -70, -66, -62, -59, -56, -54, -52, -50, -48, -47, -46, -44, -43,
            -42, -41, -40, -39, -38, -37, -36, -35, -34, -33, -33, -32, -31, -30, -30, -29, -28, -28, -27, -27, -26,
            -25, -25, -24, -24, -23, -23, -22, -22, -21, -21, -20, -20, -19, -19, -19, -18, -18, -17, -17, -16, -16,
            -16, -15, -15, -14, -14, -14, -13, -13, -12, -12, -12, -11, -11, -11, -10, -10, -9, -9, -9, -8, -8, -8, -7,
            -7, -7, -6, -6, -6, -5, -5, -5, -4, -4, -4, -3, -3, -3, -2, -2, -2, -1, -1, -1, -1, 0, 0, 0, 1, 1, 1, 2, 2,
            2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 13,
            13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 18, 18, 18, 19, 19, 19, 20, 20, 21, 21, 21, 22, 22, 22,
            23, 23, 24, 24, 24, 25, 25, 26, 26, 27, 27, 27, 28, 28, 29, 29, 30, 30, 31, 31, 32, 32, 33, 33, 34, 34, 35,
            35, 36, 36, 37, 37, 38, 38, 39, 40, 40, 41, 41, 42, 43, 43, 44, 45, 45, 46, 47, 48, 49, 49, 50, 51, 52, 53,
            53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 65, 66, 68, 69, 71, 73, 75, 77, 79, 81, 82, 86, 90, 93, 97, 100,
            100, 100, 100, 100, 100, 100, 100, 100 };
}
