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

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.timetable.TimetablesV1Impl.HttpCallable;

/**
 * Factory for {@link TimetablesV1Api}.
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public interface TimetablesV1ApiFactory {

    /**
     * Creates a new instance of the {@link TimetablesV1Api}.
     */
    TimetablesV1Api create(final String clientId, final String clientSecret, final HttpCallable httpCallable)
            throws JAXBException;
}
