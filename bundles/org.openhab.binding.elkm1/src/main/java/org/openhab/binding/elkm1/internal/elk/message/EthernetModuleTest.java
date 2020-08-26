/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.elkm1.internal.elk.message;

import java.util.Calendar;
import java.util.Date;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * This is called on the ethernet line when we have an ethernet result to do something about.
 *
 * @author David Bennett - Initial Contribution
 */
public class EthernetModuleTest extends ElkMessage {
    private Date dateFromElk;
    private boolean daylightSavings;
    private boolean clock24Hour;
    private boolean europeanDateFormat;

    public EthernetModuleTest(String data) {
        super(ElkCommand.EthernetModuleTest);

        int second = Integer.valueOf(data.substring(0, 2));
        int minute = Integer.valueOf(data.substring(2, 4));
        int hour = Integer.valueOf(data.substring(4, 6));
        int dayOfMonth = Integer.valueOf(data.substring(7, 9));
        int month = Integer.valueOf(data.substring(9, 11));
        int year = Integer.valueOf(data.substring(11, 13));
        int daylightSavings = Integer.valueOf(data.substring(13, 14));
        int clockMode = Integer.valueOf(data.substring(14, 15));
        int dateDisplayMode = Integer.valueOf(data.substring(15, 16));
        Calendar cal = Calendar.getInstance();

        cal.set(year, month, dayOfMonth, hour, minute, second);
        dateFromElk = cal.getTime();
        this.daylightSavings = daylightSavings == 1;
        clock24Hour = clockMode == 1;
        europeanDateFormat = dateDisplayMode == 1;
    }

    public Date getDateFromElk() {
        return dateFromElk;
    }

    public boolean isDaylightSavings() {
        return daylightSavings;
    }

    public boolean isClock24Hour() {
        return clock24Hour;
    }

    public boolean isEuropeanDateFormat() {
        return europeanDateFormat;
    }

    @Override
    protected String getData() {
        return null;
    }
}
