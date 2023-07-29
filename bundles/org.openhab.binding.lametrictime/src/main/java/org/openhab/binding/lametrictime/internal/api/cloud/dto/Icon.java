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
package org.openhab.binding.lametrictime.internal.api.cloud.dto;

/**
 * Pojo for icon.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Icon {
    private Integer id;
    private String title;
    private String code;
    private IconType type;
    private String category;
    private String url;
    private Thumb thumb;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Icon withId(Integer id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Icon withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Icon withCode(String code) {
        this.code = code;
        return this;
    }

    public IconType getType() {
        return type;
    }

    public void setType(IconType type) {
        this.type = type;
    }

    public Icon withType(IconType type) {
        this.type = type;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Icon withCategory(String category) {
        this.category = category;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Icon withUrl(String url) {
        this.url = url;
        return this;
    }

    public Thumb getThumb() {
        return thumb;
    }

    public void setThumb(Thumb thumb) {
        this.thumb = thumb;
    }

    public Icon withThumb(Thumb thumb) {
        this.thumb = thumb;
        return this;
    }
}
