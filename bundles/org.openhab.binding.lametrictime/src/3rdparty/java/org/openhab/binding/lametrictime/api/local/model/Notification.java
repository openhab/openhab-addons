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
package org.openhab.binding.lametrictime.api.local.model;

import java.time.LocalDateTime;

public class Notification
{
    private String id;
    private String type;
    private LocalDateTime created;
    private LocalDateTime expirationDate;
    private String priority;
    private String iconType;
    private Integer lifetime;
    private NotificationModel model;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Notification withId(String id)
    {
        this.id = id;
        return this;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Notification withType(String type)
    {
        this.type = type;
        return this;
    }

    public LocalDateTime getCreated()
    {
        return created;
    }

    public void setCreated(LocalDateTime created)
    {
        this.created = created;
    }

    public Notification withCreated(LocalDateTime created)
    {
        this.created = created;
        return this;
    }

    public LocalDateTime getExpirationDate()
    {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public Notification withExpirationDate(LocalDateTime expirationDate)
    {
        this.expirationDate = expirationDate;
        return this;
    }

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public Notification withPriority(String priority)
    {
        this.priority = priority;
        return this;
    }

    public String getIconType()
    {
        return iconType;
    }

    public void setIconType(String iconType)
    {
        this.iconType = iconType;
    }

    public Notification withIconType(String iconType)
    {
        this.iconType = iconType;
        return this;
    }

    public Integer getLifetime()
    {
        return lifetime;
    }

    public void setLifetime(Integer lifetime)
    {
        this.lifetime = lifetime;
    }

    public Notification withLifetime(Integer lifetime)
    {
        this.lifetime = lifetime;
        return this;
    }

    public NotificationModel getModel()
    {
        return model;
    }

    public void setModel(NotificationModel model)
    {
        this.model = model;
    }

    public Notification withModel(NotificationModel model)
    {
        this.model = model;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Notification [id=");
        builder.append(id);
        builder.append(", type=");
        builder.append(type);
        builder.append(", created=");
        builder.append(created);
        builder.append(", expirationDate=");
        builder.append(expirationDate);
        builder.append(", priority=");
        builder.append(priority);
        builder.append(", iconType=");
        builder.append(iconType);
        builder.append(", lifetime=");
        builder.append(lifetime);
        builder.append(", model=");
        builder.append(model);
        builder.append("]");
        return builder.toString();
    }
}
