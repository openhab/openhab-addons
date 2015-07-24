/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Java Bean to represent a JSON response to a <code>getmeasure</code> API
 * method call.
 * <p>
 * Sample response:
 *
 * <pre>
 * {"status":"ok",
 *  "body":[{"beg_time":1367965202,
 *           "value":[[23.2,
 *                     64,
 *                     1254,
 *                     1011.5,
 *                     34]]}],
 *  "time_exec":0.0072271823883057,
 *  "time_server":1367965438}
 * </pre>
 *
 * @author Andreas Brenk
 * @author Gaël L'hopital
 * @since 1.4.0
 */

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeasurementBody extends AbstractMessage {

    protected List<List<BigDecimal>> value;
    protected long beg_time; // must be kept as long for valid deserialization of timestamp

    public List<List<BigDecimal>> getValues() {
        return this.value;
    }

    public long getBegTime() {
        return this.beg_time;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = createToStringBuilder();
        builder.appendSuper(super.toString());

        builder.append("values", getValues());
        builder.append("begTime", getBegTime());

        return builder.toString();
    }

    public Calendar getTimeStamp() {
        Calendar result = null;
        result = Calendar.getInstance();
        result.setTimeInMillis(getBegTime() * 1000);

        return result;
    }

}
