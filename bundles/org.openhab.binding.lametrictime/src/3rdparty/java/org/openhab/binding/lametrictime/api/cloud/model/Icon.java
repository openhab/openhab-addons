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

public class Icon
{
    private Integer id;
    private String title;
    private String code;
    private IconType type;
    private String category;
    private String url;
    private Thumb thumb;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Icon withId(Integer id)
    {
        this.id = id;
        return this;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Icon withTitle(String title)
    {
        this.title = title;
        return this;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public Icon withCode(String code)
    {
        this.code = code;
        return this;
    }

    public IconType getType()
    {
        return type;
    }

    public void setType(IconType type)
    {
        this.type = type;
    }

    public Icon withType(IconType type)
    {
        this.type = type;
        return this;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public Icon withCategory(String category)
    {
        this.category = category;
        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Icon withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public Thumb getThumb()
    {
        return thumb;
    }

    public void setThumb(Thumb thumb)
    {
        this.thumb = thumb;
    }

    public Icon withThumb(Thumb thumb)
    {
        this.thumb = thumb;
        return this;
    }
}
