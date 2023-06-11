/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.cloud.model;

import java.util.List;

public class IconFilter
{
    private Integer page;
    private Integer pageSize;
    private List<IconField> fields;
    private IconOrder order;

    public Integer getPage()
    {
        return page;
    }

    public void setPage(Integer page)
    {
        this.page = page;
    }

    public IconFilter withPage(Integer page)
    {
        this.page = page;
        return this;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
    }

    public IconFilter withPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
        return this;
    }

    public List<IconField> getFields()
    {
        return fields;
    }

    public String getFieldsString()
    {
        if (fields == null || fields.isEmpty())
        {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(fields.get(0).name().toLowerCase());

        for (int i = 1; i < fields.size(); i++)
        {
            builder.append(',').append(fields.get(i).name().toLowerCase());
        }

        return builder.toString();
    }

    public void setFields(List<IconField> fields)
    {
        this.fields = fields;
    }

    public IconFilter withFields(List<IconField> fields)
    {
        this.fields = fields;
        return this;
    }

    public IconOrder getOrder()
    {
        return order;
    }

    public String getOrderString()
    {
        return order == null ? null : order.name().toLowerCase();
    }

    public void setOrder(IconOrder order)
    {
        this.order = order;
    }

    public IconFilter withOrder(IconOrder order)
    {
        this.order = order;
        return this;
    }
}
