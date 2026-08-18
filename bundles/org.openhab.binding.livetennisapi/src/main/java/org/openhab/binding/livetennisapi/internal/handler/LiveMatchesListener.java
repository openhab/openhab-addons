/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.livetennisapi.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.livetennisapi.internal.api.dto.Match;

/**
 * Implemented by thing handlers that consume the live match snapshot polled by the account bridge, so one API request
 * per poll cycle serves all things of the bridge.
 *
 * @author Ben Synapse - Initial contribution
 */
@NonNullByDefault
public interface LiveMatchesListener {

    /** Called after every successful bridge poll with all matches currently in progress. */
    void onLiveMatches(List<Match> liveMatches);
}
