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
package org.openhab.binding.dwdunwetter.internal.dto;

import java.util.Comparator;
import java.util.Objects;

/**
 * Comperator to sort a Warning first by Severity, second by the onSet date.
 *
 * @author Martin Koehler - Initial contribution
 */
public class SeverityComparator implements Comparator<DwdWarningData> {

    @Override
    public int compare(DwdWarningData o1, DwdWarningData o2) {
        Comparator.comparingInt(d -> ((DwdWarningData) d).getSeverity().getOrder());
        Comparator.comparing(DwdWarningData::getOnset);

        int result = Integer.compare(o1.getSeverity().getOrder(), o2.getSeverity().getOrder());
        if (result == 0) {
            if (Objects.equals(o1.getOnset(), o2.getOnset())) {
                return 0;
            } else if (o1.getOnset() == null) {
                return -1;
            } else if (o2.getOnset() == null) {
                return 1;
            }
            return o1.getOnset().compareTo(o2.getOnset());
        }
        return result;
    }
}
