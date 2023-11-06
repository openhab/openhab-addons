/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.pixometer.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ImageMeta} class is the representing java model for the json result for Image Meta Data from the pixometer
 * api
 *
 * @author Jerome Luckenbach - Initial Contribution
 *
 */
@NonNullByDefault
public class ImageMeta {

    private @NonNullByDefault({}) Integer id;
    private @NonNullByDefault({}) List<Annotation> annotations = null;
    private @NonNullByDefault({}) String image;
    private @NonNullByDefault({}) String imageDownload;
    private @NonNullByDefault({}) String cameraModel;
    private @NonNullByDefault({}) Boolean flash;
    private @NonNullByDefault({}) Integer frameNumber;
    private @NonNullByDefault({}) Double secondsSinceDetection;
    private @NonNullByDefault({}) Double secondsSinceStart;
    private @NonNullByDefault({}) Double lat;
    private @NonNullByDefault({}) Double lng;
    private @NonNullByDefault({}) String osVersion;
    private @NonNullByDefault({}) String pixolusVersion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageDownload() {
        return imageDownload;
    }

    public void setImageDownload(String imageDownload) {
        this.imageDownload = imageDownload;
    }

    public String getCameraModel() {
        return cameraModel;
    }

    public void setCameraModel(String cameraModel) {
        this.cameraModel = cameraModel;
    }

    public Boolean getFlash() {
        return flash;
    }

    public void setFlash(Boolean flash) {
        this.flash = flash;
    }

    public Integer getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(Integer frameNumber) {
        this.frameNumber = frameNumber;
    }

    public Double getSecondsSinceDetection() {
        return secondsSinceDetection;
    }

    public void setSecondsSinceDetection(Double secondsSinceDetection) {
        this.secondsSinceDetection = secondsSinceDetection;
    }

    public Double getSecondsSinceStart() {
        return secondsSinceStart;
    }

    public void setSecondsSinceStart(Double secondsSinceStart) {
        this.secondsSinceStart = secondsSinceStart;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getPixolusVersion() {
        return pixolusVersion;
    }

    public void setPixolusVersion(String pixolusVersion) {
        this.pixolusVersion = pixolusVersion;
    }
}
