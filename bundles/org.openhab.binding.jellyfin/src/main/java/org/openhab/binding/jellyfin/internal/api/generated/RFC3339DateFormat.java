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
package org.openhab.binding.jellyfin.internal.api.generated;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.util.StdDateFormat;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RFC3339DateFormat extends DateFormat {
    private static final long serialVersionUID = 1L;
    private static final TimeZone TIMEZONE_Z = TimeZone.getTimeZone("UTC");

    private final StdDateFormat fmt = new StdDateFormat().withTimeZone(TIMEZONE_Z).withColonInTimeZone(true);

    public RFC3339DateFormat() {
        this.calendar = new GregorianCalendar();
        this.numberFormat = new DecimalFormat();
    }

    @Override
    public Date parse(String source) {
        return parse(source, new ParsePosition(0));
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        return fmt.parse(source, pos);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return fmt.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
