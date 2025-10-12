package org.openhab.binding.tidal.internal.api.model;

import java.util.Hashtable;

public class RelationShip {
    private Link<BaseEntry> shares;
    private Link<BaseEntry> albums;
    private Link<BaseEntry> trackStatistics;
    private Link<BaseEntry> similarTracks;
    private Link<BaseEntry> similarAlbums;
    private Link<Artist> artists;
    private Link<BaseEntry> genres;
    private Link<BaseEntry> owners;
    private Link<BaseEntry> lyrics;
    private Link<Artwork> coverArt;
    private Link<Artwork> profileArt;
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

    public Link<Artist> getArtists() {
        return artists;
    }

    public Link<BaseEntry> getGenres() {
        return genres;
    }

    public Link<BaseEntry> getOwners() {
        return owners;
    }

    public Link<Artwork> getCoverArt() {
        return coverArt;
    }

    public Link<Artwork> getProfileArt() {
        return profileArt;
    }

    public Link<BaseEntry> getItems() {
        return items;
    }

    public Link<BaseEntry> getProviders() {
        return providers;
    }

    public void resolveDeps(Hashtable<String, BaseEntry> dict) {
        if (artists != null) {
            artists.resolveDeps(dict);
        }
        if (coverArt != null) {
            coverArt.resolveDeps(dict);
        }
        if (profileArt != null) {
            profileArt.resolveDeps(dict);
        }

    }
}
