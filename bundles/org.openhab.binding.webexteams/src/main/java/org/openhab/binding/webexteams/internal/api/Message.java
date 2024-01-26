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
package org.openhab.binding.webexteams.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * a <code>Message</code> that is sent or received through the API.
 * 
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
public class Message {
    private @Nullable String id;
    private @Nullable String roomId;
    private @Nullable String toPersonEmail;
    private @Nullable String text;
    private @Nullable String markdown;
    private @Nullable String file;

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Nullable
    public String getToPersonEmail() {
        return toPersonEmail;
    }

    public void setToPersonEmail(String toPersonEmail) {
        this.toPersonEmail = toPersonEmail;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    @Nullable
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
