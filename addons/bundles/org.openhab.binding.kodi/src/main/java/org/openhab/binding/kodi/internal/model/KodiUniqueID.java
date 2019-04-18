/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.kodi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class representing a Kodi UniqueID
 *
 * @author m17design - Initial contribution
 */
@NonNullByDefault
public class KodiUniqueID {
    private @NonNullByDefault({}) String douban;
    private @NonNullByDefault({}) String imdb;
    private @NonNullByDefault({}) String tmdb;
    private @NonNullByDefault({}) String imdbtvshow;
    private @NonNullByDefault({}) String tmdbtvshow;

    public String getDouban() {
        return douban;
    }

    public void setDouban(String douban) {
        this.douban = douban;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getTmdb() {
        return tmdb;
    }

    public void setTmdb(String tmdb) {
        this.tmdb = tmdb;
    }

    public String getImdbtvshow() {
        return imdbtvshow;
    }

    public void setImdbtvshow(String imdbtvshow) {
        this.imdbtvshow = imdbtvshow;
    }

    public String getTmdbtvshow() {
        return tmdbtvshow;
    }

    public void setTmdbtvshow(String tmdbtvshow) {
        this.tmdbtvshow = tmdbtvshow;
    }
}
