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

import java.time.OffsetDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * RemoteSubtitleInfo
 */
@JsonPropertyOrder({ RemoteSubtitleInfo.JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME,
        RemoteSubtitleInfo.JSON_PROPERTY_ID, RemoteSubtitleInfo.JSON_PROPERTY_PROVIDER_NAME,
        RemoteSubtitleInfo.JSON_PROPERTY_NAME, RemoteSubtitleInfo.JSON_PROPERTY_FORMAT,
        RemoteSubtitleInfo.JSON_PROPERTY_AUTHOR, RemoteSubtitleInfo.JSON_PROPERTY_COMMENT,
        RemoteSubtitleInfo.JSON_PROPERTY_DATE_CREATED, RemoteSubtitleInfo.JSON_PROPERTY_COMMUNITY_RATING,
        RemoteSubtitleInfo.JSON_PROPERTY_FRAME_RATE, RemoteSubtitleInfo.JSON_PROPERTY_DOWNLOAD_COUNT,
        RemoteSubtitleInfo.JSON_PROPERTY_IS_HASH_MATCH, RemoteSubtitleInfo.JSON_PROPERTY_AI_TRANSLATED,
        RemoteSubtitleInfo.JSON_PROPERTY_MACHINE_TRANSLATED, RemoteSubtitleInfo.JSON_PROPERTY_FORCED,
        RemoteSubtitleInfo.JSON_PROPERTY_HEARING_IMPAIRED })

public class RemoteSubtitleInfo {
    public static final String JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME = "ThreeLetterISOLanguageName";
    @org.eclipse.jdt.annotation.NonNull
    private String threeLetterISOLanguageName;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_PROVIDER_NAME = "ProviderName";
    @org.eclipse.jdt.annotation.NonNull
    private String providerName;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_FORMAT = "Format";
    @org.eclipse.jdt.annotation.NonNull
    private String format;

    public static final String JSON_PROPERTY_AUTHOR = "Author";
    @org.eclipse.jdt.annotation.NonNull
    private String author;

    public static final String JSON_PROPERTY_COMMENT = "Comment";
    @org.eclipse.jdt.annotation.NonNull
    private String comment;

    public static final String JSON_PROPERTY_DATE_CREATED = "DateCreated";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateCreated;

    public static final String JSON_PROPERTY_COMMUNITY_RATING = "CommunityRating";
    @org.eclipse.jdt.annotation.NonNull
    private Float communityRating;

    public static final String JSON_PROPERTY_FRAME_RATE = "FrameRate";
    @org.eclipse.jdt.annotation.NonNull
    private Float frameRate;

    public static final String JSON_PROPERTY_DOWNLOAD_COUNT = "DownloadCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer downloadCount;

    public static final String JSON_PROPERTY_IS_HASH_MATCH = "IsHashMatch";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isHashMatch;

    public static final String JSON_PROPERTY_AI_TRANSLATED = "AiTranslated";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean aiTranslated;

    public static final String JSON_PROPERTY_MACHINE_TRANSLATED = "MachineTranslated";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean machineTranslated;

    public static final String JSON_PROPERTY_FORCED = "Forced";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean forced;

    public static final String JSON_PROPERTY_HEARING_IMPAIRED = "HearingImpaired";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hearingImpaired;

    public RemoteSubtitleInfo() {
    }

    public RemoteSubtitleInfo threeLetterISOLanguageName(
            @org.eclipse.jdt.annotation.NonNull String threeLetterISOLanguageName) {
        this.threeLetterISOLanguageName = threeLetterISOLanguageName;
        return this;
    }

    /**
     * Get threeLetterISOLanguageName
     * 
     * @return threeLetterISOLanguageName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getThreeLetterISOLanguageName() {
        return threeLetterISOLanguageName;
    }

    @JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThreeLetterISOLanguageName(@org.eclipse.jdt.annotation.NonNull String threeLetterISOLanguageName) {
        this.threeLetterISOLanguageName = threeLetterISOLanguageName;
    }

    public RemoteSubtitleInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public RemoteSubtitleInfo providerName(@org.eclipse.jdt.annotation.NonNull String providerName) {
        this.providerName = providerName;
        return this;
    }

    /**
     * Get providerName
     * 
     * @return providerName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProviderName() {
        return providerName;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderName(@org.eclipse.jdt.annotation.NonNull String providerName) {
        this.providerName = providerName;
    }

    public RemoteSubtitleInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public RemoteSubtitleInfo format(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
        return this;
    }

    /**
     * Get format
     * 
     * @return format
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFormat() {
        return format;
    }

    @JsonProperty(JSON_PROPERTY_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFormat(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
    }

    public RemoteSubtitleInfo author(@org.eclipse.jdt.annotation.NonNull String author) {
        this.author = author;
        return this;
    }

    /**
     * Get author
     * 
     * @return author
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUTHOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAuthor() {
        return author;
    }

    @JsonProperty(JSON_PROPERTY_AUTHOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAuthor(@org.eclipse.jdt.annotation.NonNull String author) {
        this.author = author;
    }

    public RemoteSubtitleInfo comment(@org.eclipse.jdt.annotation.NonNull String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Get comment
     * 
     * @return comment
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COMMENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getComment() {
        return comment;
    }

    @JsonProperty(JSON_PROPERTY_COMMENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setComment(@org.eclipse.jdt.annotation.NonNull String comment) {
        this.comment = comment;
    }

    public RemoteSubtitleInfo dateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    /**
     * Get dateCreated
     * 
     * @return dateCreated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DATE_CREATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    @JsonProperty(JSON_PROPERTY_DATE_CREATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public RemoteSubtitleInfo communityRating(@org.eclipse.jdt.annotation.NonNull Float communityRating) {
        this.communityRating = communityRating;
        return this;
    }

    /**
     * Get communityRating
     * 
     * @return communityRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COMMUNITY_RATING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getCommunityRating() {
        return communityRating;
    }

    @JsonProperty(JSON_PROPERTY_COMMUNITY_RATING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommunityRating(@org.eclipse.jdt.annotation.NonNull Float communityRating) {
        this.communityRating = communityRating;
    }

    public RemoteSubtitleInfo frameRate(@org.eclipse.jdt.annotation.NonNull Float frameRate) {
        this.frameRate = frameRate;
        return this;
    }

    /**
     * Get frameRate
     * 
     * @return frameRate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FRAME_RATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getFrameRate() {
        return frameRate;
    }

    @JsonProperty(JSON_PROPERTY_FRAME_RATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFrameRate(@org.eclipse.jdt.annotation.NonNull Float frameRate) {
        this.frameRate = frameRate;
    }

    public RemoteSubtitleInfo downloadCount(@org.eclipse.jdt.annotation.NonNull Integer downloadCount) {
        this.downloadCount = downloadCount;
        return this;
    }

    /**
     * Get downloadCount
     * 
     * @return downloadCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DOWNLOAD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getDownloadCount() {
        return downloadCount;
    }

    @JsonProperty(JSON_PROPERTY_DOWNLOAD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownloadCount(@org.eclipse.jdt.annotation.NonNull Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public RemoteSubtitleInfo isHashMatch(@org.eclipse.jdt.annotation.NonNull Boolean isHashMatch) {
        this.isHashMatch = isHashMatch;
        return this;
    }

    /**
     * Get isHashMatch
     * 
     * @return isHashMatch
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_HASH_MATCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsHashMatch() {
        return isHashMatch;
    }

    @JsonProperty(JSON_PROPERTY_IS_HASH_MATCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsHashMatch(@org.eclipse.jdt.annotation.NonNull Boolean isHashMatch) {
        this.isHashMatch = isHashMatch;
    }

    public RemoteSubtitleInfo aiTranslated(@org.eclipse.jdt.annotation.NonNull Boolean aiTranslated) {
        this.aiTranslated = aiTranslated;
        return this;
    }

    /**
     * Get aiTranslated
     * 
     * @return aiTranslated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AI_TRANSLATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAiTranslated() {
        return aiTranslated;
    }

    @JsonProperty(JSON_PROPERTY_AI_TRANSLATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAiTranslated(@org.eclipse.jdt.annotation.NonNull Boolean aiTranslated) {
        this.aiTranslated = aiTranslated;
    }

    public RemoteSubtitleInfo machineTranslated(@org.eclipse.jdt.annotation.NonNull Boolean machineTranslated) {
        this.machineTranslated = machineTranslated;
        return this;
    }

    /**
     * Get machineTranslated
     * 
     * @return machineTranslated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MACHINE_TRANSLATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getMachineTranslated() {
        return machineTranslated;
    }

    @JsonProperty(JSON_PROPERTY_MACHINE_TRANSLATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMachineTranslated(@org.eclipse.jdt.annotation.NonNull Boolean machineTranslated) {
        this.machineTranslated = machineTranslated;
    }

    public RemoteSubtitleInfo forced(@org.eclipse.jdt.annotation.NonNull Boolean forced) {
        this.forced = forced;
        return this;
    }

    /**
     * Get forced
     * 
     * @return forced
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FORCED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getForced() {
        return forced;
    }

    @JsonProperty(JSON_PROPERTY_FORCED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setForced(@org.eclipse.jdt.annotation.NonNull Boolean forced) {
        this.forced = forced;
    }

    public RemoteSubtitleInfo hearingImpaired(@org.eclipse.jdt.annotation.NonNull Boolean hearingImpaired) {
        this.hearingImpaired = hearingImpaired;
        return this;
    }

    /**
     * Get hearingImpaired
     * 
     * @return hearingImpaired
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HEARING_IMPAIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getHearingImpaired() {
        return hearingImpaired;
    }

    @JsonProperty(JSON_PROPERTY_HEARING_IMPAIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHearingImpaired(@org.eclipse.jdt.annotation.NonNull Boolean hearingImpaired) {
        this.hearingImpaired = hearingImpaired;
    }

    /**
     * Return true if this RemoteSubtitleInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteSubtitleInfo remoteSubtitleInfo = (RemoteSubtitleInfo) o;
        return Objects.equals(this.threeLetterISOLanguageName, remoteSubtitleInfo.threeLetterISOLanguageName)
                && Objects.equals(this.id, remoteSubtitleInfo.id)
                && Objects.equals(this.providerName, remoteSubtitleInfo.providerName)
                && Objects.equals(this.name, remoteSubtitleInfo.name)
                && Objects.equals(this.format, remoteSubtitleInfo.format)
                && Objects.equals(this.author, remoteSubtitleInfo.author)
                && Objects.equals(this.comment, remoteSubtitleInfo.comment)
                && Objects.equals(this.dateCreated, remoteSubtitleInfo.dateCreated)
                && Objects.equals(this.communityRating, remoteSubtitleInfo.communityRating)
                && Objects.equals(this.frameRate, remoteSubtitleInfo.frameRate)
                && Objects.equals(this.downloadCount, remoteSubtitleInfo.downloadCount)
                && Objects.equals(this.isHashMatch, remoteSubtitleInfo.isHashMatch)
                && Objects.equals(this.aiTranslated, remoteSubtitleInfo.aiTranslated)
                && Objects.equals(this.machineTranslated, remoteSubtitleInfo.machineTranslated)
                && Objects.equals(this.forced, remoteSubtitleInfo.forced)
                && Objects.equals(this.hearingImpaired, remoteSubtitleInfo.hearingImpaired);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threeLetterISOLanguageName, id, providerName, name, format, author, comment, dateCreated,
                communityRating, frameRate, downloadCount, isHashMatch, aiTranslated, machineTranslated, forced,
                hearingImpaired);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RemoteSubtitleInfo {\n");
        sb.append("    threeLetterISOLanguageName: ").append(toIndentedString(threeLetterISOLanguageName)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    format: ").append(toIndentedString(format)).append("\n");
        sb.append("    author: ").append(toIndentedString(author)).append("\n");
        sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
        sb.append("    dateCreated: ").append(toIndentedString(dateCreated)).append("\n");
        sb.append("    communityRating: ").append(toIndentedString(communityRating)).append("\n");
        sb.append("    frameRate: ").append(toIndentedString(frameRate)).append("\n");
        sb.append("    downloadCount: ").append(toIndentedString(downloadCount)).append("\n");
        sb.append("    isHashMatch: ").append(toIndentedString(isHashMatch)).append("\n");
        sb.append("    aiTranslated: ").append(toIndentedString(aiTranslated)).append("\n");
        sb.append("    machineTranslated: ").append(toIndentedString(machineTranslated)).append("\n");
        sb.append("    forced: ").append(toIndentedString(forced)).append("\n");
        sb.append("    hearingImpaired: ").append(toIndentedString(hearingImpaired)).append("\n");
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
