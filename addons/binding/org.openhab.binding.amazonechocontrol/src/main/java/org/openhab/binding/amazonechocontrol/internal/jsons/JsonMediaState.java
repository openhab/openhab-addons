package org.openhab.binding.amazonechocontrol.internal.jsons;

public class JsonMediaState {

    public String clientId;
    public String contentId;
    public String contentType;
    public String currentState;
    public String imageURL;
    public boolean isDisliked;
    public boolean isLiked;
    public boolean looping;
    public String mediaOwnerCustomerId;
    public boolean muted;
    public String programId;
    public int progressSeconds;
    public String providerId;
    public QueueEntry[] queue;
    public String queueId;
    public Integer queueSize;
    public String radioStationId;
    public int radioVariety;
    public String referenceId;
    public String service;
    public boolean shuffling;
    public int timeLastShuffled;
    public int volume;

    public class QueueEntry {

        public String album;
        public String albumAsin;
        public String artist;
        public String asin;
        public String cardImageURL;
        public String contentId;
        public String contentType;
        public int durationSeconds;
        public boolean feedbackDisabled;
        public String historicalId;
        public String imageURL;
        public int index;
        public boolean isAd;
        public boolean isDisliked;
        public boolean isFreeWithPrime;
        public boolean isLiked;
        public String programId;
        public String programName;
        public String providerId;
        public String queueId;
        public String radioStationCallSign;
        public String radioStationId;
        public String radioStationLocation;
        public String radioStationSlogan;
        public String referenceId;
        public String service;
        public String startTime;
        public String title;
        public String trackId;
        public String trackStatus;

    }
}
