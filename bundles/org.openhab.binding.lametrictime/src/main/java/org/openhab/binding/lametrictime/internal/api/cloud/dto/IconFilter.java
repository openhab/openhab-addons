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

import java.util.List;

/**
 * Pojo for icon filter.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class IconFilter {
    private Integer page;
    private Integer pageSize;
    private List<IconField> fields;
    private IconOrder order;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public IconFilter withPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public IconFilter withPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public List<IconField> getFields() {
        return fields;
    }

    public String getFieldsString() {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(fields.get(0).name().toLowerCase());

        for (int i = 1; i < fields.size(); i++) {
            builder.append(',').append(fields.get(i).name().toLowerCase());
        }

        return builder.toString();
    }

    public void setFields(List<IconField> fields) {
        this.fields = fields;
    }

    public IconFilter withFields(List<IconField> fields) {
        this.fields = fields;
        return this;
    }

    public IconOrder getOrder() {
        return order;
    }

    public String getOrderString() {
        return order == null ? null : order.name().toLowerCase();
    }

    public void setOrder(IconOrder order) {
        this.order = order;
    }

    public IconFilter withOrder(IconOrder order) {
        this.order = order;
        return this;
    }
}
