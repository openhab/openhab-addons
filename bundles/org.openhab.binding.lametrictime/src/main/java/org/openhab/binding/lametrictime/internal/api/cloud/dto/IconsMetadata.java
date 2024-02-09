/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Pojo for icons metadata.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class IconsMetadata {
    private Integer totalIconCount;
    private Integer page;
    private Integer pageSize;
    private Integer pageCount;

    public Integer getTotalIconCount() {
        return totalIconCount;
    }

    public void setTotalIconCount(Integer totalIconCount) {
        this.totalIconCount = totalIconCount;
    }

    public IconsMetadata withTotalIconCount(Integer totalIconCount) {
        this.totalIconCount = totalIconCount;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public IconsMetadata withPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public IconsMetadata withPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public IconsMetadata withPageCount(Integer pageCount) {
        this.pageCount = pageCount;
        return this;
    }
}
