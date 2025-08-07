/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class UserConfiguration.
 */
@JsonPropertyOrder({ UserConfiguration.JSON_PROPERTY_AUDIO_LANGUAGE_PREFERENCE,
        UserConfiguration.JSON_PROPERTY_PLAY_DEFAULT_AUDIO_TRACK,
        UserConfiguration.JSON_PROPERTY_SUBTITLE_LANGUAGE_PREFERENCE,
        UserConfiguration.JSON_PROPERTY_DISPLAY_MISSING_EPISODES, UserConfiguration.JSON_PROPERTY_GROUPED_FOLDERS,
        UserConfiguration.JSON_PROPERTY_SUBTITLE_MODE, UserConfiguration.JSON_PROPERTY_DISPLAY_COLLECTIONS_VIEW,
        UserConfiguration.JSON_PROPERTY_ENABLE_LOCAL_PASSWORD, UserConfiguration.JSON_PROPERTY_ORDERED_VIEWS,
        UserConfiguration.JSON_PROPERTY_LATEST_ITEMS_EXCLUDES, UserConfiguration.JSON_PROPERTY_MY_MEDIA_EXCLUDES,
        UserConfiguration.JSON_PROPERTY_HIDE_PLAYED_IN_LATEST,
        UserConfiguration.JSON_PROPERTY_REMEMBER_AUDIO_SELECTIONS,
        UserConfiguration.JSON_PROPERTY_REMEMBER_SUBTITLE_SELECTIONS,
        UserConfiguration.JSON_PROPERTY_ENABLE_NEXT_EPISODE_AUTO_PLAY,
        UserConfiguration.JSON_PROPERTY_CAST_RECEIVER_ID })

public class UserConfiguration {
    public static final String JSON_PROPERTY_AUDIO_LANGUAGE_PREFERENCE = "AudioLanguagePreference";
    @org.eclipse.jdt.annotation.NonNull
    private String audioLanguagePreference;

    public static final String JSON_PROPERTY_PLAY_DEFAULT_AUDIO_TRACK = "PlayDefaultAudioTrack";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean playDefaultAudioTrack;

    public static final String JSON_PROPERTY_SUBTITLE_LANGUAGE_PREFERENCE = "SubtitleLanguagePreference";
    @org.eclipse.jdt.annotation.NonNull
    private String subtitleLanguagePreference;

    public static final String JSON_PROPERTY_DISPLAY_MISSING_EPISODES = "DisplayMissingEpisodes";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean displayMissingEpisodes;

    public static final String JSON_PROPERTY_GROUPED_FOLDERS = "GroupedFolders";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> groupedFolders = new ArrayList<>();

    public static final String JSON_PROPERTY_SUBTITLE_MODE = "SubtitleMode";
    @org.eclipse.jdt.annotation.NonNull
    private SubtitlePlaybackMode subtitleMode;

    public static final String JSON_PROPERTY_DISPLAY_COLLECTIONS_VIEW = "DisplayCollectionsView";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean displayCollectionsView;

    public static final String JSON_PROPERTY_ENABLE_LOCAL_PASSWORD = "EnableLocalPassword";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableLocalPassword;

    public static final String JSON_PROPERTY_ORDERED_VIEWS = "OrderedViews";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> orderedViews = new ArrayList<>();

    public static final String JSON_PROPERTY_LATEST_ITEMS_EXCLUDES = "LatestItemsExcludes";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> latestItemsExcludes = new ArrayList<>();

    public static final String JSON_PROPERTY_MY_MEDIA_EXCLUDES = "MyMediaExcludes";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> myMediaExcludes = new ArrayList<>();

    public static final String JSON_PROPERTY_HIDE_PLAYED_IN_LATEST = "HidePlayedInLatest";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hidePlayedInLatest;

    public static final String JSON_PROPERTY_REMEMBER_AUDIO_SELECTIONS = "RememberAudioSelections";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean rememberAudioSelections;

    public static final String JSON_PROPERTY_REMEMBER_SUBTITLE_SELECTIONS = "RememberSubtitleSelections";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean rememberSubtitleSelections;

    public static final String JSON_PROPERTY_ENABLE_NEXT_EPISODE_AUTO_PLAY = "EnableNextEpisodeAutoPlay";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableNextEpisodeAutoPlay;

    public static final String JSON_PROPERTY_CAST_RECEIVER_ID = "CastReceiverId";
    @org.eclipse.jdt.annotation.NonNull
    private String castReceiverId;

    public UserConfiguration() {
    }

    public UserConfiguration audioLanguagePreference(
            @org.eclipse.jdt.annotation.NonNull String audioLanguagePreference) {
        this.audioLanguagePreference = audioLanguagePreference;
        return this;
    }

    /**
     * Gets or sets the audio language preference.
     * 
     * @return audioLanguagePreference
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUDIO_LANGUAGE_PREFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAudioLanguagePreference() {
        return audioLanguagePreference;
    }

    @JsonProperty(JSON_PROPERTY_AUDIO_LANGUAGE_PREFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioLanguagePreference(@org.eclipse.jdt.annotation.NonNull String audioLanguagePreference) {
        this.audioLanguagePreference = audioLanguagePreference;
    }

    public UserConfiguration playDefaultAudioTrack(@org.eclipse.jdt.annotation.NonNull Boolean playDefaultAudioTrack) {
        this.playDefaultAudioTrack = playDefaultAudioTrack;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [play default audio track].
     * 
     * @return playDefaultAudioTrack
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAY_DEFAULT_AUDIO_TRACK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getPlayDefaultAudioTrack() {
        return playDefaultAudioTrack;
    }

    @JsonProperty(JSON_PROPERTY_PLAY_DEFAULT_AUDIO_TRACK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayDefaultAudioTrack(@org.eclipse.jdt.annotation.NonNull Boolean playDefaultAudioTrack) {
        this.playDefaultAudioTrack = playDefaultAudioTrack;
    }

    public UserConfiguration subtitleLanguagePreference(
            @org.eclipse.jdt.annotation.NonNull String subtitleLanguagePreference) {
        this.subtitleLanguagePreference = subtitleLanguagePreference;
        return this;
    }

    /**
     * Gets or sets the subtitle language preference.
     * 
     * @return subtitleLanguagePreference
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUBTITLE_LANGUAGE_PREFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSubtitleLanguagePreference() {
        return subtitleLanguagePreference;
    }

    @JsonProperty(JSON_PROPERTY_SUBTITLE_LANGUAGE_PREFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleLanguagePreference(@org.eclipse.jdt.annotation.NonNull String subtitleLanguagePreference) {
        this.subtitleLanguagePreference = subtitleLanguagePreference;
    }

    public UserConfiguration displayMissingEpisodes(
            @org.eclipse.jdt.annotation.NonNull Boolean displayMissingEpisodes) {
        this.displayMissingEpisodes = displayMissingEpisodes;
        return this;
    }

    /**
     * Get displayMissingEpisodes
     * 
     * @return displayMissingEpisodes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISPLAY_MISSING_EPISODES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDisplayMissingEpisodes() {
        return displayMissingEpisodes;
    }

    @JsonProperty(JSON_PROPERTY_DISPLAY_MISSING_EPISODES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayMissingEpisodes(@org.eclipse.jdt.annotation.NonNull Boolean displayMissingEpisodes) {
        this.displayMissingEpisodes = displayMissingEpisodes;
    }

    public UserConfiguration groupedFolders(@org.eclipse.jdt.annotation.NonNull List<UUID> groupedFolders) {
        this.groupedFolders = groupedFolders;
        return this;
    }

    public UserConfiguration addGroupedFoldersItem(UUID groupedFoldersItem) {
        if (this.groupedFolders == null) {
            this.groupedFolders = new ArrayList<>();
        }
        this.groupedFolders.add(groupedFoldersItem);
        return this;
    }

    /**
     * Get groupedFolders
     * 
     * @return groupedFolders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GROUPED_FOLDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getGroupedFolders() {
        return groupedFolders;
    }

    @JsonProperty(JSON_PROPERTY_GROUPED_FOLDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupedFolders(@org.eclipse.jdt.annotation.NonNull List<UUID> groupedFolders) {
        this.groupedFolders = groupedFolders;
    }

    public UserConfiguration subtitleMode(@org.eclipse.jdt.annotation.NonNull SubtitlePlaybackMode subtitleMode) {
        this.subtitleMode = subtitleMode;
        return this;
    }

    /**
     * An enum representing a subtitle playback mode.
     * 
     * @return subtitleMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUBTITLE_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SubtitlePlaybackMode getSubtitleMode() {
        return subtitleMode;
    }

    @JsonProperty(JSON_PROPERTY_SUBTITLE_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleMode(@org.eclipse.jdt.annotation.NonNull SubtitlePlaybackMode subtitleMode) {
        this.subtitleMode = subtitleMode;
    }

    public UserConfiguration displayCollectionsView(
            @org.eclipse.jdt.annotation.NonNull Boolean displayCollectionsView) {
        this.displayCollectionsView = displayCollectionsView;
        return this;
    }

    /**
     * Get displayCollectionsView
     * 
     * @return displayCollectionsView
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISPLAY_COLLECTIONS_VIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDisplayCollectionsView() {
        return displayCollectionsView;
    }

    @JsonProperty(JSON_PROPERTY_DISPLAY_COLLECTIONS_VIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayCollectionsView(@org.eclipse.jdt.annotation.NonNull Boolean displayCollectionsView) {
        this.displayCollectionsView = displayCollectionsView;
    }

    public UserConfiguration enableLocalPassword(@org.eclipse.jdt.annotation.NonNull Boolean enableLocalPassword) {
        this.enableLocalPassword = enableLocalPassword;
        return this;
    }

    /**
     * Get enableLocalPassword
     * 
     * @return enableLocalPassword
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_LOCAL_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableLocalPassword() {
        return enableLocalPassword;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_LOCAL_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableLocalPassword(@org.eclipse.jdt.annotation.NonNull Boolean enableLocalPassword) {
        this.enableLocalPassword = enableLocalPassword;
    }

    public UserConfiguration orderedViews(@org.eclipse.jdt.annotation.NonNull List<UUID> orderedViews) {
        this.orderedViews = orderedViews;
        return this;
    }

    public UserConfiguration addOrderedViewsItem(UUID orderedViewsItem) {
        if (this.orderedViews == null) {
            this.orderedViews = new ArrayList<>();
        }
        this.orderedViews.add(orderedViewsItem);
        return this;
    }

    /**
     * Get orderedViews
     * 
     * @return orderedViews
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ORDERED_VIEWS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getOrderedViews() {
        return orderedViews;
    }

    @JsonProperty(JSON_PROPERTY_ORDERED_VIEWS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOrderedViews(@org.eclipse.jdt.annotation.NonNull List<UUID> orderedViews) {
        this.orderedViews = orderedViews;
    }

    public UserConfiguration latestItemsExcludes(@org.eclipse.jdt.annotation.NonNull List<UUID> latestItemsExcludes) {
        this.latestItemsExcludes = latestItemsExcludes;
        return this;
    }

    public UserConfiguration addLatestItemsExcludesItem(UUID latestItemsExcludesItem) {
        if (this.latestItemsExcludes == null) {
            this.latestItemsExcludes = new ArrayList<>();
        }
        this.latestItemsExcludes.add(latestItemsExcludesItem);
        return this;
    }

    /**
     * Get latestItemsExcludes
     * 
     * @return latestItemsExcludes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LATEST_ITEMS_EXCLUDES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getLatestItemsExcludes() {
        return latestItemsExcludes;
    }

    @JsonProperty(JSON_PROPERTY_LATEST_ITEMS_EXCLUDES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLatestItemsExcludes(@org.eclipse.jdt.annotation.NonNull List<UUID> latestItemsExcludes) {
        this.latestItemsExcludes = latestItemsExcludes;
    }

    public UserConfiguration myMediaExcludes(@org.eclipse.jdt.annotation.NonNull List<UUID> myMediaExcludes) {
        this.myMediaExcludes = myMediaExcludes;
        return this;
    }

    public UserConfiguration addMyMediaExcludesItem(UUID myMediaExcludesItem) {
        if (this.myMediaExcludes == null) {
            this.myMediaExcludes = new ArrayList<>();
        }
        this.myMediaExcludes.add(myMediaExcludesItem);
        return this;
    }

    /**
     * Get myMediaExcludes
     * 
     * @return myMediaExcludes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MY_MEDIA_EXCLUDES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getMyMediaExcludes() {
        return myMediaExcludes;
    }

    @JsonProperty(JSON_PROPERTY_MY_MEDIA_EXCLUDES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMyMediaExcludes(@org.eclipse.jdt.annotation.NonNull List<UUID> myMediaExcludes) {
        this.myMediaExcludes = myMediaExcludes;
    }

    public UserConfiguration hidePlayedInLatest(@org.eclipse.jdt.annotation.NonNull Boolean hidePlayedInLatest) {
        this.hidePlayedInLatest = hidePlayedInLatest;
        return this;
    }

    /**
     * Get hidePlayedInLatest
     * 
     * @return hidePlayedInLatest
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HIDE_PLAYED_IN_LATEST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getHidePlayedInLatest() {
        return hidePlayedInLatest;
    }

    @JsonProperty(JSON_PROPERTY_HIDE_PLAYED_IN_LATEST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHidePlayedInLatest(@org.eclipse.jdt.annotation.NonNull Boolean hidePlayedInLatest) {
        this.hidePlayedInLatest = hidePlayedInLatest;
    }

    public UserConfiguration rememberAudioSelections(
            @org.eclipse.jdt.annotation.NonNull Boolean rememberAudioSelections) {
        this.rememberAudioSelections = rememberAudioSelections;
        return this;
    }

    /**
     * Get rememberAudioSelections
     * 
     * @return rememberAudioSelections
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REMEMBER_AUDIO_SELECTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRememberAudioSelections() {
        return rememberAudioSelections;
    }

    @JsonProperty(JSON_PROPERTY_REMEMBER_AUDIO_SELECTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRememberAudioSelections(@org.eclipse.jdt.annotation.NonNull Boolean rememberAudioSelections) {
        this.rememberAudioSelections = rememberAudioSelections;
    }

    public UserConfiguration rememberSubtitleSelections(
            @org.eclipse.jdt.annotation.NonNull Boolean rememberSubtitleSelections) {
        this.rememberSubtitleSelections = rememberSubtitleSelections;
        return this;
    }

    /**
     * Get rememberSubtitleSelections
     * 
     * @return rememberSubtitleSelections
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REMEMBER_SUBTITLE_SELECTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRememberSubtitleSelections() {
        return rememberSubtitleSelections;
    }

    @JsonProperty(JSON_PROPERTY_REMEMBER_SUBTITLE_SELECTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRememberSubtitleSelections(@org.eclipse.jdt.annotation.NonNull Boolean rememberSubtitleSelections) {
        this.rememberSubtitleSelections = rememberSubtitleSelections;
    }

    public UserConfiguration enableNextEpisodeAutoPlay(
            @org.eclipse.jdt.annotation.NonNull Boolean enableNextEpisodeAutoPlay) {
        this.enableNextEpisodeAutoPlay = enableNextEpisodeAutoPlay;
        return this;
    }

    /**
     * Get enableNextEpisodeAutoPlay
     * 
     * @return enableNextEpisodeAutoPlay
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_NEXT_EPISODE_AUTO_PLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableNextEpisodeAutoPlay() {
        return enableNextEpisodeAutoPlay;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_NEXT_EPISODE_AUTO_PLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableNextEpisodeAutoPlay(@org.eclipse.jdt.annotation.NonNull Boolean enableNextEpisodeAutoPlay) {
        this.enableNextEpisodeAutoPlay = enableNextEpisodeAutoPlay;
    }

    public UserConfiguration castReceiverId(@org.eclipse.jdt.annotation.NonNull String castReceiverId) {
        this.castReceiverId = castReceiverId;
        return this;
    }

    /**
     * Gets or sets the id of the selected cast receiver.
     * 
     * @return castReceiverId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CAST_RECEIVER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCastReceiverId() {
        return castReceiverId;
    }

    @JsonProperty(JSON_PROPERTY_CAST_RECEIVER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCastReceiverId(@org.eclipse.jdt.annotation.NonNull String castReceiverId) {
        this.castReceiverId = castReceiverId;
    }

    /**
     * Return true if this UserConfiguration object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserConfiguration userConfiguration = (UserConfiguration) o;
        return Objects.equals(this.audioLanguagePreference, userConfiguration.audioLanguagePreference)
                && Objects.equals(this.playDefaultAudioTrack, userConfiguration.playDefaultAudioTrack)
                && Objects.equals(this.subtitleLanguagePreference, userConfiguration.subtitleLanguagePreference)
                && Objects.equals(this.displayMissingEpisodes, userConfiguration.displayMissingEpisodes)
                && Objects.equals(this.groupedFolders, userConfiguration.groupedFolders)
                && Objects.equals(this.subtitleMode, userConfiguration.subtitleMode)
                && Objects.equals(this.displayCollectionsView, userConfiguration.displayCollectionsView)
                && Objects.equals(this.enableLocalPassword, userConfiguration.enableLocalPassword)
                && Objects.equals(this.orderedViews, userConfiguration.orderedViews)
                && Objects.equals(this.latestItemsExcludes, userConfiguration.latestItemsExcludes)
                && Objects.equals(this.myMediaExcludes, userConfiguration.myMediaExcludes)
                && Objects.equals(this.hidePlayedInLatest, userConfiguration.hidePlayedInLatest)
                && Objects.equals(this.rememberAudioSelections, userConfiguration.rememberAudioSelections)
                && Objects.equals(this.rememberSubtitleSelections, userConfiguration.rememberSubtitleSelections)
                && Objects.equals(this.enableNextEpisodeAutoPlay, userConfiguration.enableNextEpisodeAutoPlay)
                && Objects.equals(this.castReceiverId, userConfiguration.castReceiverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audioLanguagePreference, playDefaultAudioTrack, subtitleLanguagePreference,
                displayMissingEpisodes, groupedFolders, subtitleMode, displayCollectionsView, enableLocalPassword,
                orderedViews, latestItemsExcludes, myMediaExcludes, hidePlayedInLatest, rememberAudioSelections,
                rememberSubtitleSelections, enableNextEpisodeAutoPlay, castReceiverId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserConfiguration {\n");
        sb.append("    audioLanguagePreference: ").append(toIndentedString(audioLanguagePreference)).append("\n");
        sb.append("    playDefaultAudioTrack: ").append(toIndentedString(playDefaultAudioTrack)).append("\n");
        sb.append("    subtitleLanguagePreference: ").append(toIndentedString(subtitleLanguagePreference)).append("\n");
        sb.append("    displayMissingEpisodes: ").append(toIndentedString(displayMissingEpisodes)).append("\n");
        sb.append("    groupedFolders: ").append(toIndentedString(groupedFolders)).append("\n");
        sb.append("    subtitleMode: ").append(toIndentedString(subtitleMode)).append("\n");
        sb.append("    displayCollectionsView: ").append(toIndentedString(displayCollectionsView)).append("\n");
        sb.append("    enableLocalPassword: ").append(toIndentedString(enableLocalPassword)).append("\n");
        sb.append("    orderedViews: ").append(toIndentedString(orderedViews)).append("\n");
        sb.append("    latestItemsExcludes: ").append(toIndentedString(latestItemsExcludes)).append("\n");
        sb.append("    myMediaExcludes: ").append(toIndentedString(myMediaExcludes)).append("\n");
        sb.append("    hidePlayedInLatest: ").append(toIndentedString(hidePlayedInLatest)).append("\n");
        sb.append("    rememberAudioSelections: ").append(toIndentedString(rememberAudioSelections)).append("\n");
        sb.append("    rememberSubtitleSelections: ").append(toIndentedString(rememberSubtitleSelections)).append("\n");
        sb.append("    enableNextEpisodeAutoPlay: ").append(toIndentedString(enableNextEpisodeAutoPlay)).append("\n");
        sb.append("    castReceiverId: ").append(toIndentedString(castReceiverId)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
