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
 * A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain device
 * is able to play. &lt;br /&gt; Specifically, it defines the supported &lt;see
 * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\&quot;&gt;containers&lt;/see&gt; and
 * &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\&quot;&gt;codecs&lt;/see&gt; (video
 * and/or audio, including codec profiles and levels) the device is able to direct play (without transcoding or
 * remuxing), as well as which &lt;see
 * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\&quot;&gt;containers/codecs to transcode
 * to&lt;/see&gt; in case it isn&#39;t.
 */
@JsonPropertyOrder({ DeviceProfile.JSON_PROPERTY_NAME, DeviceProfile.JSON_PROPERTY_ID,
        DeviceProfile.JSON_PROPERTY_MAX_STREAMING_BITRATE, DeviceProfile.JSON_PROPERTY_MAX_STATIC_BITRATE,
        DeviceProfile.JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE,
        DeviceProfile.JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE, DeviceProfile.JSON_PROPERTY_DIRECT_PLAY_PROFILES,
        DeviceProfile.JSON_PROPERTY_TRANSCODING_PROFILES, DeviceProfile.JSON_PROPERTY_CONTAINER_PROFILES,
        DeviceProfile.JSON_PROPERTY_CODEC_PROFILES, DeviceProfile.JSON_PROPERTY_SUBTITLE_PROFILES })

public class DeviceProfile {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_MAX_STREAMING_BITRATE = "MaxStreamingBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxStreamingBitrate;

    public static final String JSON_PROPERTY_MAX_STATIC_BITRATE = "MaxStaticBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxStaticBitrate;

    public static final String JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE = "MusicStreamingTranscodingBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer musicStreamingTranscodingBitrate;

    public static final String JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE = "MaxStaticMusicBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxStaticMusicBitrate;

    public static final String JSON_PROPERTY_DIRECT_PLAY_PROFILES = "DirectPlayProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<DirectPlayProfile> directPlayProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_TRANSCODING_PROFILES = "TranscodingProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<TranscodingProfile> transcodingProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_CONTAINER_PROFILES = "ContainerProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<ContainerProfile> containerProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_CODEC_PROFILES = "CodecProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<CodecProfile> codecProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_SUBTITLE_PROFILES = "SubtitleProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<SubtitleProfile> subtitleProfiles = new ArrayList<>();

    public DeviceProfile() {
    }

    public DeviceProfile name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of this device profile. User profiles must have a unique name.
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

    public DeviceProfile id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the unique internal identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public DeviceProfile maxStreamingBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate) {
        this.maxStreamingBitrate = maxStreamingBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for all streamed content.
     * 
     * @return maxStreamingBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_STREAMING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaxStreamingBitrate() {
        return maxStreamingBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_STREAMING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxStreamingBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate) {
        this.maxStreamingBitrate = maxStreamingBitrate;
    }

    public DeviceProfile maxStaticBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticBitrate) {
        this.maxStaticBitrate = maxStaticBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for statically streamed content (&#x3D; direct played files).
     * 
     * @return maxStaticBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_STATIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaxStaticBitrate() {
        return maxStaticBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_STATIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxStaticBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticBitrate) {
        this.maxStaticBitrate = maxStaticBitrate;
    }

    public DeviceProfile musicStreamingTranscodingBitrate(
            @org.eclipse.jdt.annotation.NonNull Integer musicStreamingTranscodingBitrate) {
        this.musicStreamingTranscodingBitrate = musicStreamingTranscodingBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for transcoded music streams.
     * 
     * @return musicStreamingTranscodingBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMusicStreamingTranscodingBitrate() {
        return musicStreamingTranscodingBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMusicStreamingTranscodingBitrate(
            @org.eclipse.jdt.annotation.NonNull Integer musicStreamingTranscodingBitrate) {
        this.musicStreamingTranscodingBitrate = musicStreamingTranscodingBitrate;
    }

    public DeviceProfile maxStaticMusicBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticMusicBitrate) {
        this.maxStaticMusicBitrate = maxStaticMusicBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for statically streamed (&#x3D; direct played) music files.
     * 
     * @return maxStaticMusicBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaxStaticMusicBitrate() {
        return maxStaticMusicBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxStaticMusicBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticMusicBitrate) {
        this.maxStaticMusicBitrate = maxStaticMusicBitrate;
    }

    public DeviceProfile directPlayProfiles(
            @org.eclipse.jdt.annotation.NonNull List<DirectPlayProfile> directPlayProfiles) {
        this.directPlayProfiles = directPlayProfiles;
        return this;
    }

    public DeviceProfile addDirectPlayProfilesItem(DirectPlayProfile directPlayProfilesItem) {
        if (this.directPlayProfiles == null) {
            this.directPlayProfiles = new ArrayList<>();
        }
        this.directPlayProfiles.add(directPlayProfilesItem);
        return this;
    }

    /**
     * Gets or sets the direct play profiles.
     * 
     * @return directPlayProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DIRECT_PLAY_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<DirectPlayProfile> getDirectPlayProfiles() {
        return directPlayProfiles;
    }

    @JsonProperty(JSON_PROPERTY_DIRECT_PLAY_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDirectPlayProfiles(@org.eclipse.jdt.annotation.NonNull List<DirectPlayProfile> directPlayProfiles) {
        this.directPlayProfiles = directPlayProfiles;
    }

    public DeviceProfile transcodingProfiles(
            @org.eclipse.jdt.annotation.NonNull List<TranscodingProfile> transcodingProfiles) {
        this.transcodingProfiles = transcodingProfiles;
        return this;
    }

    public DeviceProfile addTranscodingProfilesItem(TranscodingProfile transcodingProfilesItem) {
        if (this.transcodingProfiles == null) {
            this.transcodingProfiles = new ArrayList<>();
        }
        this.transcodingProfiles.add(transcodingProfilesItem);
        return this;
    }

    /**
     * Gets or sets the transcoding profiles.
     * 
     * @return transcodingProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODING_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TranscodingProfile> getTranscodingProfiles() {
        return transcodingProfiles;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODING_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingProfiles(
            @org.eclipse.jdt.annotation.NonNull List<TranscodingProfile> transcodingProfiles) {
        this.transcodingProfiles = transcodingProfiles;
    }

    public DeviceProfile containerProfiles(
            @org.eclipse.jdt.annotation.NonNull List<ContainerProfile> containerProfiles) {
        this.containerProfiles = containerProfiles;
        return this;
    }

    public DeviceProfile addContainerProfilesItem(ContainerProfile containerProfilesItem) {
        if (this.containerProfiles == null) {
            this.containerProfiles = new ArrayList<>();
        }
        this.containerProfiles.add(containerProfilesItem);
        return this;
    }

    /**
     * Gets or sets the container profiles. Failing to meet these optional conditions causes transcoding to occur.
     * 
     * @return containerProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTAINER_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ContainerProfile> getContainerProfiles() {
        return containerProfiles;
    }

    @JsonProperty(JSON_PROPERTY_CONTAINER_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainerProfiles(@org.eclipse.jdt.annotation.NonNull List<ContainerProfile> containerProfiles) {
        this.containerProfiles = containerProfiles;
    }

    public DeviceProfile codecProfiles(@org.eclipse.jdt.annotation.NonNull List<CodecProfile> codecProfiles) {
        this.codecProfiles = codecProfiles;
        return this;
    }

    public DeviceProfile addCodecProfilesItem(CodecProfile codecProfilesItem) {
        if (this.codecProfiles == null) {
            this.codecProfiles = new ArrayList<>();
        }
        this.codecProfiles.add(codecProfilesItem);
        return this;
    }

    /**
     * Gets or sets the codec profiles.
     * 
     * @return codecProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CODEC_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<CodecProfile> getCodecProfiles() {
        return codecProfiles;
    }

    @JsonProperty(JSON_PROPERTY_CODEC_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodecProfiles(@org.eclipse.jdt.annotation.NonNull List<CodecProfile> codecProfiles) {
        this.codecProfiles = codecProfiles;
    }

    public DeviceProfile subtitleProfiles(@org.eclipse.jdt.annotation.NonNull List<SubtitleProfile> subtitleProfiles) {
        this.subtitleProfiles = subtitleProfiles;
        return this;
    }

    public DeviceProfile addSubtitleProfilesItem(SubtitleProfile subtitleProfilesItem) {
        if (this.subtitleProfiles == null) {
            this.subtitleProfiles = new ArrayList<>();
        }
        this.subtitleProfiles.add(subtitleProfilesItem);
        return this;
    }

    /**
     * Gets or sets the subtitle profiles.
     * 
     * @return subtitleProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUBTITLE_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SubtitleProfile> getSubtitleProfiles() {
        return subtitleProfiles;
    }

    @JsonProperty(JSON_PROPERTY_SUBTITLE_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleProfiles(@org.eclipse.jdt.annotation.NonNull List<SubtitleProfile> subtitleProfiles) {
        this.subtitleProfiles = subtitleProfiles;
    }

    /**
     * Return true if this DeviceProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceProfile deviceProfile = (DeviceProfile) o;
        return Objects.equals(this.name, deviceProfile.name) && Objects.equals(this.id, deviceProfile.id)
                && Objects.equals(this.maxStreamingBitrate, deviceProfile.maxStreamingBitrate)
                && Objects.equals(this.maxStaticBitrate, deviceProfile.maxStaticBitrate)
                && Objects.equals(this.musicStreamingTranscodingBitrate, deviceProfile.musicStreamingTranscodingBitrate)
                && Objects.equals(this.maxStaticMusicBitrate, deviceProfile.maxStaticMusicBitrate)
                && Objects.equals(this.directPlayProfiles, deviceProfile.directPlayProfiles)
                && Objects.equals(this.transcodingProfiles, deviceProfile.transcodingProfiles)
                && Objects.equals(this.containerProfiles, deviceProfile.containerProfiles)
                && Objects.equals(this.codecProfiles, deviceProfile.codecProfiles)
                && Objects.equals(this.subtitleProfiles, deviceProfile.subtitleProfiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, maxStreamingBitrate, maxStaticBitrate, musicStreamingTranscodingBitrate,
                maxStaticMusicBitrate, directPlayProfiles, transcodingProfiles, containerProfiles, codecProfiles,
                subtitleProfiles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceProfile {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    maxStreamingBitrate: ").append(toIndentedString(maxStreamingBitrate)).append("\n");
        sb.append("    maxStaticBitrate: ").append(toIndentedString(maxStaticBitrate)).append("\n");
        sb.append("    musicStreamingTranscodingBitrate: ").append(toIndentedString(musicStreamingTranscodingBitrate))
                .append("\n");
        sb.append("    maxStaticMusicBitrate: ").append(toIndentedString(maxStaticMusicBitrate)).append("\n");
        sb.append("    directPlayProfiles: ").append(toIndentedString(directPlayProfiles)).append("\n");
        sb.append("    transcodingProfiles: ").append(toIndentedString(transcodingProfiles)).append("\n");
        sb.append("    containerProfiles: ").append(toIndentedString(containerProfiles)).append("\n");
        sb.append("    codecProfiles: ").append(toIndentedString(codecProfiles)).append("\n");
        sb.append("    subtitleProfiles: ").append(toIndentedString(subtitleProfiles)).append("\n");
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
