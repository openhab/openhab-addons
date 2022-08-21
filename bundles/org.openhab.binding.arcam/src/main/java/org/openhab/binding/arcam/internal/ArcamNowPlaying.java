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
package org.openhab.binding.arcam.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ArcamNowPlaying} POJO holds information about the current track that is playing.
 * This is used to collect multiple data fields that are send via subsequent messages.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamNowPlaying {
    public String track = "";
    public String album = "";
    public String artist = "";
    public String application = "";
    public String sampleRate = "";
    public String audioEncoder = "";

    public int rowNr;
}
