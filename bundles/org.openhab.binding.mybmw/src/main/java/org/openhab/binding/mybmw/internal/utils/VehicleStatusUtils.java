/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.utils;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.dto.properties.CBS;

/**
 * The {@link VehicleStatusUtils} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleStatusUtils {

    public static String getNextServiceDate(List<CBS> cbsMessageList) {
        if (cbsMessageList.isEmpty()) {
            return Constants.NULL_DATE;
        } else {
            ZonedDateTime farFuture = ZonedDateTime.now().plusYears(100);
            ZonedDateTime serviceDate = farFuture;
            for (CBS service : cbsMessageList) {
                ZonedDateTime d = ZonedDateTime.parse(service.dateTime);
                if (d.isBefore(serviceDate)) {
                    serviceDate = d;
                }
            }
            if (serviceDate.equals(farFuture)) {
                return Constants.NULL_DATE;
            } else {
                return serviceDate.format(Converter.DATE_INPUT_PATTERN);
            }
        }
    }

    /**
     * public static int getNextServiceMileage(VehicleStatus vStatus) {
     * if (vStatus.cbsData == null) {
     * return -1;
     * }
     * if (vStatus.cbsData.isEmpty()) {
     * return -1;
     * } else {
     * int serviceMileage = Integer.MAX_VALUE;
     * for (int i = 0; i < vStatus.cbsData.size(); i++) {
     * CBSMessage entry = vStatus.cbsData.get(i);
     * if (entry.cbsRemainingMileage != -1) {
     * if (entry.cbsRemainingMileage < serviceMileage) {
     * serviceMileage = entry.cbsRemainingMileage;
     * }
     * }
     * }
     * if (serviceMileage != Integer.MAX_VALUE) {
     * return serviceMileage;
     * } else {
     * return -1;
     * }
     * }
     * }
     */

    /**
     * public static String checkControlActive(VehicleStatus vStatus) {
     * if (vStatus.checkControlMessages == null) {
     * return UNDEF;
     * }
     * if (vStatus.checkControlMessages.isEmpty()) {
     * return NOT_ACTIVE;
     * } else {
     * return ACTIVE;
     * }
     * }
     */
}
