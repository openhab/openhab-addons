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
package org.openhab.binding.vdr.internal.svdrp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPAudioTrack} contains SVDRP Audio Track list for current channel
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPAudioTrack {
    private int id;
    private String language;
    private String description;
    private boolean active;

    /**
     *
     * @param id
     * @param language
     * @param title
     * @param active
     */
    public SVDRPAudioTrack(int id, String language, String title, boolean active) {
        super();
        this.id = id;
        this.language = language;
        this.description = title;
        this.active = active;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getLanguage() {
        return language;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }
}
