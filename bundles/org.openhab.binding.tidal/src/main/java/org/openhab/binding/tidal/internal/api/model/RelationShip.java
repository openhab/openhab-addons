package org.openhab.binding.tidal.internal.api.model;

import java.util.Hashtable;

public class RelationShip {
    private Link<BaseEntry> shares;
    private Link<BaseEntry> albums;
    private Link<BaseEntry> trackStatistics;
    private Link<BaseEntry> similarTracks;
    private Link<BaseEntry> similarAlbums;
    private Link<BaseEntry> artists;
    private Link<BaseEntry> genres;
    private Link<BaseEntry> owners;
    private Link<BaseEntry> lyrics;
    private Link<BaseEntry> coverArt;
    private Link<BaseEntry> profileArt;
    private Link<BaseEntry> items;
    private Link<BaseEntry> providers;
    private Link<BaseEntry> sourceFile;
    private Link<BaseEntry> radio;

    public Link<BaseEntry> getRadio() {
        return radio;
    }

    public Link<BaseEntry> getSourceFile() {
        return sourceFile;
    }

    public Link<BaseEntry> getLyrics() {
        return lyrics;
    }

    public Link<BaseEntry> getTrackStatistics() {
        return trackStatistics;
    }

    public Link<BaseEntry> getSimilarTracks() {
        return similarTracks;
    }

    public Link<BaseEntry> getAlbums() {
        return albums;
    }

    public Link<BaseEntry> getSimilarAlbums() {
        return similarAlbums;
    }

    public Link<BaseEntry> getArtists() {
        return artists;
    }

    public Link<BaseEntry> getGenres() {
        return genres;
    }

    public Link<BaseEntry> getOwners() {
        return owners;
    }

    public Link<BaseEntry> getCoverArt() {
        return coverArt;
    }

    public Link<BaseEntry> getProfileArt() {
        return profileArt;
    }

    public Link<BaseEntry> getItems() {
        return items;
    }

    public Link<BaseEntry> getProviders() {
        return providers;
    }

    public void resolveDeps(Hashtable<String, BaseEntry> dict) {
        if (shares != null) {
            shares.resolveDeps(dict);
        }

        if (albums != null) {
            albums.resolveDeps(dict);
        }

        if (trackStatistics != null) {
            trackStatistics.resolveDeps(dict);
        }

        if (similarTracks != null) {
            similarTracks.resolveDeps(dict);
        }

        if (similarAlbums != null) {
            similarAlbums.resolveDeps(dict);
        }

        if (artists != null) {
            artists.resolveDeps(dict);
        }

        if (genres != null) {
            genres.resolveDeps(dict);
        }

        if (owners != null) {
            owners.resolveDeps(dict);
        }

        if (lyrics != null) {
            lyrics.resolveDeps(dict);
        }

        if (coverArt != null) {
            coverArt.resolveDeps(dict);
        }

        if (profileArt != null) {
            profileArt.resolveDeps(dict);
        }

        if (items != null) {
            items.resolveDeps(dict);
        }

        if (providers != null) {
            providers.resolveDeps(dict);
        }

        if (sourceFile != null) {
            sourceFile.resolveDeps(dict);
        }

        if (radio != null) {
            radio.resolveDeps(dict);
        }
    }
}
