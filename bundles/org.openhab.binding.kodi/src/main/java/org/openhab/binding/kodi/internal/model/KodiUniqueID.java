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
package org.openhab.binding.kodi.internal.model;

/**
 * Class representing a Kodi UniqueID
 *
 * @author Meng Yiqi - Initial contribution
 */
public class KodiUniqueID {
    private String imdb;
    private String tmdb;
    private String imdbtvshow;
    private String tmdbtvshow;
    private String tmdbepisode;
    private String douban;

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

    public String getTmdbepisode() {
        return tmdbepisode;
    }

    public void setTmdbepisode(String tmdbepisode) {
        this.tmdbtvshow = tmdbepisode;
    }

    public String getDouban() {
        return douban;
    }

    public void setDouban(String douban) {
        this.douban = douban;
    }
}
