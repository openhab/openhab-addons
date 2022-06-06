/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.publicholiday.internal.publicholiday.definitions;

/**
 * Eastern related holiday definitions
 * 
 * @author Martin Güthle - Initial contribution
 */
public enum EasternRelatedHolidayDef {
    GOOD_FRIDAY(-2, "Good Friday"),
    EASTER_SUNDAY(0, "Easter Sunday"),
    EASTER_MONDAY(1, "Easter Monday"),
    ASCENSION_DAY(39, "Acension Day"),
    WHIT_SUNDAY(49, "Whit Sunday"),
    WHIT_MONDAY(50, "Whit Monday"),
    CORPUS_CHRISTI(60, "Corpus Christi");

    public final int offset;
    public final String name;

    private EasternRelatedHolidayDef(int offset, String name) {
        this.offset = offset;
        this.name = name;
    }
}
