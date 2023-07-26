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
package org.openhab.binding.deutschebahn.internal.timetable;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Timetable;

/**
 * Interface for timetables API in V1.
 * 
 * @see <a href="https://developers.deutschebahn.com/db-api-marketplace/apis/product/timetables">DB API Marketplace</a>
 *
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public interface TimetablesV1Api {

    /**
     * Requests the timetable with the planned data for the given station and time.
     * Calls the "/plan" endpoint of the rest-service.
     *
     * REST-endpoint documentation: (from
     * {@see https://developers.deutschebahn.com/db-api-marketplace/apis/product/timetables}).
     * Returns a Timetable object (see Timetable) that contains planned data for the specified station (evaNo)
     * within the hourly time slice given by date (format YYMMDD) and hour (format HH). The data includes stops
     * for all trips that arrive or depart within that slice. There is a small overlap between slices since some
     * trips arrive in one slice and depart in another.
     *
     * Planned data does never contain messages. On event level, planned data contains the 'plannned' attributes pt, pp,
     * ps and ppth
     * while the 'changed' attributes ct, cp, cs and cpth are absent.
     *
     * Planned data is generated many hours in advance and is static, i.e. it does never change.
     * It should be cached by web caches.public interface allows access to information about a station.
     *
     * @param evaNo The Station EVA-number.
     * @param time The time for which the timetable is requested. It will be requested for the given day and hour.
     *
     * @return The {@link Timetable} containing the planned arrivals and departues.
     */
    Timetable getPlan(String evaNo, Date time) throws IOException;

    /**
     * Requests all known changes in the timetable for the given station.
     * Calls the "/fchg" endpoint of the rest-service.
     *
     * REST-endpoint documentation: (from
     * {@see https://developers.deutschebahn.com/db-api-marketplace/apis/product/timetables}).
     * Returns a Timetable object (see Timetable) that contains all known changes for the station given by evaNo.
     *
     * The data includes all known changes from now on until undefinitely into the future. Once changes become obsolete
     * (because their trip departs from the station) they are removed from this resource.
     *
     * Changes may include messages. On event level, they usually contain one or more of the 'changed' attributes ct,
     * cp, cs or cpth.
     * Changes may also include 'planned' attributes if there is no associated planned data for the change (e.g. an
     * unplanned stop or trip).
     *
     * Full changes are updated every 30s and should be cached for that period by web caches.
     *
     * @param evaNo The Station EVA-number.
     *
     * @return The {@link Timetable} containing all known changes for the given station.
     */
    Timetable getFullChanges(String evaNo) throws IOException;

    /**
     * Requests the timetable with the planned data for the given station and time.
     * Calls the "/plan" endpoint of the rest-service.
     *
     * REST-endpoint documentation: (from
     * {@see https://developers.deutschebahn.com/db-api-marketplace/apis/product/timetables}).
     * Returns a Timetable object (see Timetable) that contains all recent changes for the station given by evaNo.
     * Recent changes are always a subset of the full changes. They may equal full changes but are typically much
     * smaller.
     * Data includes only those changes that became known within the last 2 minutes.
     *
     * A client that updates its state in intervals of less than 2 minutes should load full changes initially and then
     * proceed to periodically load only the recent changes in order to save bandwidth.
     *
     * Recent changes are updated every 30s as well and should be cached for that period by web caches.
     *
     * @param evaNo The Station EVA-number.
     *
     * @return The {@link Timetable} containing recent changes (from last two minutes) for the given station.
     */
    Timetable getRecentChanges(String evaNo) throws IOException;
}
