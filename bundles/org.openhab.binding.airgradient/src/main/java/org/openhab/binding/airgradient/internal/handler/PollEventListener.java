/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airgradient.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airgradient.internal.model.Measure;

/**
 * Interface for listening to polls.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public interface PollEventListener {

    /**
     * Called when a poll has happened.
     *
     * @param measures Measures that has been read in a successful poll
     */
    public void pollEvent(List<Measure> measures);
}
