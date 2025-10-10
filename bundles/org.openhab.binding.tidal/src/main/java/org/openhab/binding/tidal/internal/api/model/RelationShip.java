package org.openhab.binding.tidal.internal.api.model;

public class RelationShip {
    private Link shares;
    private Link albums;
    private Link trackStatistics;
    private Link similarTracks;
    private Link similarAlbums;
    private Link artists;
    private Link genres;
    private Link owners;
    private Link lyrics;
    private Link coverArt;
    private Link items;
    private Link providers;
    private Link sourceFile;
    private Link radio;

    public Link getRadio() {
        return radio;
    }

    public Link getSourceFile() {
        return sourceFile;
    }

    public Link getLyrics() {
        return lyrics;
    }

    public Link getTrackStatistics() {
        return trackStatistics;
    }

    public Link getSimilarTracks() {
        return similarTracks;
    }

    public Link getAlbums() {
        return albums;
    }

    public Link getSimilarAlbums() {
        return similarAlbums;
    }

    public Link getArtists() {
        return artists;
    }

    public Link getGenres() {
        return genres;
    }

    public Link getOwners() {
        return owners;
    }

    public Link getCoverArt() {
        return coverArt;
    }

    public Link getItems() {
        return items;
    }

    public Link getProviders() {
        return providers;
    }
}
