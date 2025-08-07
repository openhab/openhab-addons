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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class TrickplayOptions.
 */
@JsonPropertyOrder({ TrickplayOptions.JSON_PROPERTY_ENABLE_HW_ACCELERATION,
        TrickplayOptions.JSON_PROPERTY_ENABLE_HW_ENCODING,
        TrickplayOptions.JSON_PROPERTY_ENABLE_KEY_FRAME_ONLY_EXTRACTION, TrickplayOptions.JSON_PROPERTY_SCAN_BEHAVIOR,
        TrickplayOptions.JSON_PROPERTY_PROCESS_PRIORITY, TrickplayOptions.JSON_PROPERTY_INTERVAL,
        TrickplayOptions.JSON_PROPERTY_WIDTH_RESOLUTIONS, TrickplayOptions.JSON_PROPERTY_TILE_WIDTH,
        TrickplayOptions.JSON_PROPERTY_TILE_HEIGHT, TrickplayOptions.JSON_PROPERTY_QSCALE,
        TrickplayOptions.JSON_PROPERTY_JPEG_QUALITY, TrickplayOptions.JSON_PROPERTY_PROCESS_THREADS })

public class TrickplayOptions {
    public static final String JSON_PROPERTY_ENABLE_HW_ACCELERATION = "EnableHwAcceleration";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableHwAcceleration;

    public static final String JSON_PROPERTY_ENABLE_HW_ENCODING = "EnableHwEncoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableHwEncoding;

    public static final String JSON_PROPERTY_ENABLE_KEY_FRAME_ONLY_EXTRACTION = "EnableKeyFrameOnlyExtraction";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableKeyFrameOnlyExtraction;

    public static final String JSON_PROPERTY_SCAN_BEHAVIOR = "ScanBehavior";
    @org.eclipse.jdt.annotation.NonNull
    private TrickplayScanBehavior scanBehavior;

    public static final String JSON_PROPERTY_PROCESS_PRIORITY = "ProcessPriority";
    @org.eclipse.jdt.annotation.NonNull
    private ProcessPriorityClass processPriority;

    public static final String JSON_PROPERTY_INTERVAL = "Interval";
    @org.eclipse.jdt.annotation.NonNull
    private Integer interval;

    public static final String JSON_PROPERTY_WIDTH_RESOLUTIONS = "WidthResolutions";
    @org.eclipse.jdt.annotation.NonNull
    private List<Integer> widthResolutions = new ArrayList<>();

    public static final String JSON_PROPERTY_TILE_WIDTH = "TileWidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer tileWidth;

    public static final String JSON_PROPERTY_TILE_HEIGHT = "TileHeight";
    @org.eclipse.jdt.annotation.NonNull
    private Integer tileHeight;

    public static final String JSON_PROPERTY_QSCALE = "Qscale";
    @org.eclipse.jdt.annotation.NonNull
    private Integer qscale;

    public static final String JSON_PROPERTY_JPEG_QUALITY = "JpegQuality";
    @org.eclipse.jdt.annotation.NonNull
    private Integer jpegQuality;

    public static final String JSON_PROPERTY_PROCESS_THREADS = "ProcessThreads";
    @org.eclipse.jdt.annotation.NonNull
    private Integer processThreads;

    public TrickplayOptions() {
    }

    public TrickplayOptions enableHwAcceleration(@org.eclipse.jdt.annotation.NonNull Boolean enableHwAcceleration) {
        this.enableHwAcceleration = enableHwAcceleration;
        return this;
    }

    /**
     * Gets or sets a value indicating whether or not to use HW acceleration.
     * 
     * @return enableHwAcceleration
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_HW_ACCELERATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableHwAcceleration() {
        return enableHwAcceleration;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_HW_ACCELERATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableHwAcceleration(@org.eclipse.jdt.annotation.NonNull Boolean enableHwAcceleration) {
        this.enableHwAcceleration = enableHwAcceleration;
    }

    public TrickplayOptions enableHwEncoding(@org.eclipse.jdt.annotation.NonNull Boolean enableHwEncoding) {
        this.enableHwEncoding = enableHwEncoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether or not to use HW accelerated MJPEG encoding.
     * 
     * @return enableHwEncoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_HW_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableHwEncoding() {
        return enableHwEncoding;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_HW_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableHwEncoding(@org.eclipse.jdt.annotation.NonNull Boolean enableHwEncoding) {
        this.enableHwEncoding = enableHwEncoding;
    }

    public TrickplayOptions enableKeyFrameOnlyExtraction(
            @org.eclipse.jdt.annotation.NonNull Boolean enableKeyFrameOnlyExtraction) {
        this.enableKeyFrameOnlyExtraction = enableKeyFrameOnlyExtraction;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to only extract key frames. Significantly faster, but is not compatible
     * with all decoders and/or video files.
     * 
     * @return enableKeyFrameOnlyExtraction
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_KEY_FRAME_ONLY_EXTRACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableKeyFrameOnlyExtraction() {
        return enableKeyFrameOnlyExtraction;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_KEY_FRAME_ONLY_EXTRACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableKeyFrameOnlyExtraction(
            @org.eclipse.jdt.annotation.NonNull Boolean enableKeyFrameOnlyExtraction) {
        this.enableKeyFrameOnlyExtraction = enableKeyFrameOnlyExtraction;
    }

    public TrickplayOptions scanBehavior(@org.eclipse.jdt.annotation.NonNull TrickplayScanBehavior scanBehavior) {
        this.scanBehavior = scanBehavior;
        return this;
    }

    /**
     * Gets or sets the behavior used by trickplay provider on library scan/update.
     * 
     * @return scanBehavior
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SCAN_BEHAVIOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TrickplayScanBehavior getScanBehavior() {
        return scanBehavior;
    }

    @JsonProperty(JSON_PROPERTY_SCAN_BEHAVIOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScanBehavior(@org.eclipse.jdt.annotation.NonNull TrickplayScanBehavior scanBehavior) {
        this.scanBehavior = scanBehavior;
    }

    public TrickplayOptions processPriority(@org.eclipse.jdt.annotation.NonNull ProcessPriorityClass processPriority) {
        this.processPriority = processPriority;
        return this;
    }

    /**
     * Gets or sets the process priority for the ffmpeg process.
     * 
     * @return processPriority
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROCESS_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ProcessPriorityClass getProcessPriority() {
        return processPriority;
    }

    @JsonProperty(JSON_PROPERTY_PROCESS_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProcessPriority(@org.eclipse.jdt.annotation.NonNull ProcessPriorityClass processPriority) {
        this.processPriority = processPriority;
    }

    public TrickplayOptions interval(@org.eclipse.jdt.annotation.NonNull Integer interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Gets or sets the interval, in ms, between each new trickplay image.
     * 
     * @return interval
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getInterval() {
        return interval;
    }

    @JsonProperty(JSON_PROPERTY_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInterval(@org.eclipse.jdt.annotation.NonNull Integer interval) {
        this.interval = interval;
    }

    public TrickplayOptions widthResolutions(@org.eclipse.jdt.annotation.NonNull List<Integer> widthResolutions) {
        this.widthResolutions = widthResolutions;
        return this;
    }

    public TrickplayOptions addWidthResolutionsItem(Integer widthResolutionsItem) {
        if (this.widthResolutions == null) {
            this.widthResolutions = new ArrayList<>();
        }
        this.widthResolutions.add(widthResolutionsItem);
        return this;
    }

    /**
     * Gets or sets the target width resolutions, in px, to generates preview images for.
     * 
     * @return widthResolutions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_WIDTH_RESOLUTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Integer> getWidthResolutions() {
        return widthResolutions;
    }

    @JsonProperty(JSON_PROPERTY_WIDTH_RESOLUTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWidthResolutions(@org.eclipse.jdt.annotation.NonNull List<Integer> widthResolutions) {
        this.widthResolutions = widthResolutions;
    }

    public TrickplayOptions tileWidth(@org.eclipse.jdt.annotation.NonNull Integer tileWidth) {
        this.tileWidth = tileWidth;
        return this;
    }

    /**
     * Gets or sets number of tile images to allow in X dimension.
     * 
     * @return tileWidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TILE_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTileWidth() {
        return tileWidth;
    }

    @JsonProperty(JSON_PROPERTY_TILE_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTileWidth(@org.eclipse.jdt.annotation.NonNull Integer tileWidth) {
        this.tileWidth = tileWidth;
    }

    public TrickplayOptions tileHeight(@org.eclipse.jdt.annotation.NonNull Integer tileHeight) {
        this.tileHeight = tileHeight;
        return this;
    }

    /**
     * Gets or sets number of tile images to allow in Y dimension.
     * 
     * @return tileHeight
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TILE_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTileHeight() {
        return tileHeight;
    }

    @JsonProperty(JSON_PROPERTY_TILE_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTileHeight(@org.eclipse.jdt.annotation.NonNull Integer tileHeight) {
        this.tileHeight = tileHeight;
    }

    public TrickplayOptions qscale(@org.eclipse.jdt.annotation.NonNull Integer qscale) {
        this.qscale = qscale;
        return this;
    }

    /**
     * Gets or sets the ffmpeg output quality level.
     * 
     * @return qscale
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_QSCALE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getQscale() {
        return qscale;
    }

    @JsonProperty(JSON_PROPERTY_QSCALE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setQscale(@org.eclipse.jdt.annotation.NonNull Integer qscale) {
        this.qscale = qscale;
    }

    public TrickplayOptions jpegQuality(@org.eclipse.jdt.annotation.NonNull Integer jpegQuality) {
        this.jpegQuality = jpegQuality;
        return this;
    }

    /**
     * Gets or sets the jpeg quality to use for image tiles.
     * 
     * @return jpegQuality
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_JPEG_QUALITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getJpegQuality() {
        return jpegQuality;
    }

    @JsonProperty(JSON_PROPERTY_JPEG_QUALITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setJpegQuality(@org.eclipse.jdt.annotation.NonNull Integer jpegQuality) {
        this.jpegQuality = jpegQuality;
    }

    public TrickplayOptions processThreads(@org.eclipse.jdt.annotation.NonNull Integer processThreads) {
        this.processThreads = processThreads;
        return this;
    }

    /**
     * Gets or sets the number of threads to be used by ffmpeg.
     * 
     * @return processThreads
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROCESS_THREADS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getProcessThreads() {
        return processThreads;
    }

    @JsonProperty(JSON_PROPERTY_PROCESS_THREADS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProcessThreads(@org.eclipse.jdt.annotation.NonNull Integer processThreads) {
        this.processThreads = processThreads;
    }

    /**
     * Return true if this TrickplayOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrickplayOptions trickplayOptions = (TrickplayOptions) o;
        return Objects.equals(this.enableHwAcceleration, trickplayOptions.enableHwAcceleration)
                && Objects.equals(this.enableHwEncoding, trickplayOptions.enableHwEncoding)
                && Objects.equals(this.enableKeyFrameOnlyExtraction, trickplayOptions.enableKeyFrameOnlyExtraction)
                && Objects.equals(this.scanBehavior, trickplayOptions.scanBehavior)
                && Objects.equals(this.processPriority, trickplayOptions.processPriority)
                && Objects.equals(this.interval, trickplayOptions.interval)
                && Objects.equals(this.widthResolutions, trickplayOptions.widthResolutions)
                && Objects.equals(this.tileWidth, trickplayOptions.tileWidth)
                && Objects.equals(this.tileHeight, trickplayOptions.tileHeight)
                && Objects.equals(this.qscale, trickplayOptions.qscale)
                && Objects.equals(this.jpegQuality, trickplayOptions.jpegQuality)
                && Objects.equals(this.processThreads, trickplayOptions.processThreads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enableHwAcceleration, enableHwEncoding, enableKeyFrameOnlyExtraction, scanBehavior,
                processPriority, interval, widthResolutions, tileWidth, tileHeight, qscale, jpegQuality,
                processThreads);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TrickplayOptions {\n");
        sb.append("    enableHwAcceleration: ").append(toIndentedString(enableHwAcceleration)).append("\n");
        sb.append("    enableHwEncoding: ").append(toIndentedString(enableHwEncoding)).append("\n");
        sb.append("    enableKeyFrameOnlyExtraction: ").append(toIndentedString(enableKeyFrameOnlyExtraction))
                .append("\n");
        sb.append("    scanBehavior: ").append(toIndentedString(scanBehavior)).append("\n");
        sb.append("    processPriority: ").append(toIndentedString(processPriority)).append("\n");
        sb.append("    interval: ").append(toIndentedString(interval)).append("\n");
        sb.append("    widthResolutions: ").append(toIndentedString(widthResolutions)).append("\n");
        sb.append("    tileWidth: ").append(toIndentedString(tileWidth)).append("\n");
        sb.append("    tileHeight: ").append(toIndentedString(tileHeight)).append("\n");
        sb.append("    qscale: ").append(toIndentedString(qscale)).append("\n");
        sb.append("    jpegQuality: ").append(toIndentedString(jpegQuality)).append("\n");
        sb.append("    processThreads: ").append(toIndentedString(processThreads)).append("\n");
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
