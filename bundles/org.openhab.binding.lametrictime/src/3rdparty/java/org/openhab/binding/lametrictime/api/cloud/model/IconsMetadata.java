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

public class IconsMetadata
{
    private Integer totalIconCount;
    private Integer page;
    private Integer pageSize;
    private Integer pageCount;

    public Integer getTotalIconCount()
    {
        return totalIconCount;
    }

    public void setTotalIconCount(Integer totalIconCount)
    {
        this.totalIconCount = totalIconCount;
    }

    public IconsMetadata withTotalIconCount(Integer totalIconCount)
    {
        this.totalIconCount = totalIconCount;
        return this;
    }

    public Integer getPage()
    {
        return page;
    }

    public void setPage(Integer page)
    {
        this.page = page;
    }

    public IconsMetadata withPage(Integer page)
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

    public IconsMetadata withPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(Integer pageCount)
    {
        this.pageCount = pageCount;
    }

    public IconsMetadata withPageCount(Integer pageCount)
    {
        this.pageCount = pageCount;
        return this;
    }
}
