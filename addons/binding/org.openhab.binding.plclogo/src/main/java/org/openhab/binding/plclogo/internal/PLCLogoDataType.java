/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.internal;

import java.util.Calendar;

/**
 * The {@link PLCLogoDataType} describes data types supported.
 *
 * @author Alexander Falkenstern - Initial contribution
 */

public enum PLCLogoDataType {
    INVALID,
    BIT,
    WORD,
    DWORD;

    public static int getBytesCount(final PLCLogoDataType type) {
        int count = -1;
        switch (type) {
            case BIT: {
                count = 1;
                break;
            }
            case DWORD: {
                count = 4;
                break;
            }
            case WORD: {
                count = 2;
                break;
            }
            default:
            case INVALID: {
                break;
            }
        }
        return count;
    }

    public static Calendar getRtcAt(byte[] buffer, int pos) {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR) / 100;
        calendar.set(Calendar.YEAR, 100 * year + buffer[pos]);
        calendar.set(Calendar.MONTH, buffer[pos + 1] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, buffer[pos + 2]);
        calendar.set(Calendar.HOUR_OF_DAY, buffer[pos + 3]);
        calendar.set(Calendar.MINUTE, buffer[pos + 4]);
        calendar.set(Calendar.SECOND, buffer[pos + 5]);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

}
