/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NtfyMessage} contains the message information
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfyMessage {

    private @Nullable String message;
    private int priority = 3;
    private HashSet<String> tags = new HashSet<>();
    private HashSet<ActionButtonBase> actions = new HashSet<>();
    private @Nullable URI clickAction;
    private @Nullable URI icon;
    private @Nullable URI attachment;
    private @Nullable String attachmentFilename;
    private @Nullable String sequenceId;
    private @Nullable String delay;

    /**
     * Sets the message body to be sent.
     *
     * @param message the message text
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the message body.
     *
     * @return the message text (never null when called after checking {@link #hasMessage()})
     */
    public String getMessage() {
        return java.util.Objects.requireNonNull(message);
    }

    /**
     * Checks whether a message body has been set.
     *
     * @return {@code true} when a message is present, {@code false} otherwise
     */
    public boolean hasMessage() {
        final String message = this.message;
        return message != null;
    }

    /**
     * Sets the message priority. Allowed range is 1..5.
     *
     * @param priority the priority value
     * @throws IllegalArgumentException when the priority is out of range
     */
    public void setPriority(int priority) {
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException();
        }
        this.priority = priority;
    }

    /**
     * Returns the configured priority for the message.
     *
     * @return the priority value
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Sets the click action URL for the message.
     *
     * @param url the URL to open when the notification is clicked
     * @throws URISyntaxException when the provided URL cannot be parsed
     */
    public void setClickAction(String url) throws URISyntaxException {
        this.clickAction = new URI(url);
    }

    /**
     * Returns the click action URL.
     *
     * @return the click action URI
     */
    public URI getClickAction() {
        return java.util.Objects.requireNonNull(clickAction);
    }

    /**
     * Checks whether a click action is configured.
     *
     * @return {@code true} when a click action URI is present
     */
    public boolean hasClickAction() {
        return clickAction != null;
    }

    /**
     * Adds a tag to the message.
     *
     * @param tag the tag to add
     */
    public void addTag(String tag) {
        tags.add(tag);
    }

    /**
     * Returns the configured tags for the message.
     *
     * @return a set of tag strings
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Sets an icon URI for the message.
     *
     * @param url the icon URL
     * @throws URISyntaxException when the provided URL cannot be parsed
     */
    public void setIcon(String url) throws URISyntaxException {
        this.icon = new URI(url);
    }

    /**
     * Returns the icon URI for the message.
     *
     * @return the icon URI
     */
    public URI getIcon() {
        return java.util.Objects.requireNonNull(icon);
    }

    /**
     * Checks whether an icon is configured for the message.
     *
     * @return {@code true} when an icon URI is present
     */
    public boolean hasIcon() {
        return icon != null;
    }

    /**
     * Sets an attachment URL and optional filename for the message.
     *
     * @param url the attachment URL
     * @param filename optional filename to present to recipients
     * @throws URISyntaxException when the provided URL cannot be parsed
     */
    public void setAttachment(String url, @Nullable String filename) throws URISyntaxException {
        this.attachment = new URI(url);
        setFilename(filename);
    }

    /**
     * Sets an optional filename.
     *
     * @param filename optional filename to present to recipients
     */
    public void setFilename(@Nullable String filename) {
        this.attachmentFilename = filename;
    }

    /**
     * Returns the attachment URI.
     *
     * @return the attachment URI
     */
    public URI getAttachment() {
        return java.util.Objects.requireNonNull(attachment);
    }

    /**
     * Returns the optional attachment filename provided when setting the attachment.
     *
     * @return the attachment filename or {@code null}
     */
    public @Nullable String getAttachmentFilename() {
        return attachmentFilename;
    }

    /**
     * Checks whether an attachment is configured for the message.
     *
     * @return {@code true} when an attachment URI is present
     */
    public boolean hasAttachment() {
        return attachment != null;
    }

    /**
     * Adds a view action to the message. The view action will open the supplied URL.
     *
     * @param label the label for the action
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param url the URL to open
     * @throws MalformedURLException when the provided URL is not valid
     */
    public void addViewAction(String label, Boolean clearNotification, String url) throws MalformedURLException {
        actions.add(new ViewActionButton(label, clearNotification, url));
    }

    /**
     * Adds a copy action to the message which copies the provided value to the clipboard.
     *
     * @param label the label for the action
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param value the value to copy
     */
    public void addCopyAction(String label, Boolean clearNotification, String value) {
        actions.add(new CopyActionButton(label, clearNotification, value));
    }

    /**
     * Adds an HTTP action to the message which performs a HTTP request when executed.
     *
     * @param label the label for the action
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param url the URL to call
     * @param method optional HTTP method
     * @param headers optional headers
     * @param body optional body
     * @throws MalformedURLException when the provided URL is not valid
     */
    public void addHttpAction(String label, Boolean clearNotification, String url, @Nullable String method,
            @Nullable String headers, @Nullable String body) throws MalformedURLException {
        actions.add(new HttpActionButton(label, clearNotification, url, method, headers, body));
    }

    /**
     * Adds a broadcast action to the message.
     *
     * @param label the label for the action
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param params optional parameters for the broadcast action
     */
    public void addBroadcastAction(String label, Boolean clearNotification, @Nullable String params) {
        actions.add(new BroadcastActionButton(label, clearNotification, params));
    }

    /**
     * Returns the set of action buttons configured for this message.
     *
     * @return a set of {@link ActionButtonBase}
     */
    public Set<ActionButtonBase> getActions() {
        return actions;
    }

    /**
     * Sets an optional sequence id for the message. This can be used for later
     * deletion of the message.
     *
     * @param id the sequence id or {@code null}
     */
    public void setSequenceId(@Nullable String id) {
        this.sequenceId = id;
    }

    /**
     * Returns the configured sequence id for the message.
     *
     * @return the sequence id
     */
    public String getSequenceId() {
        return java.util.Objects.requireNonNull(sequenceId);
    }

    /**
     * Checks whether a non-blank sequence id is configured for the message.
     *
     * @return {@code true} when a sequence id is present and not blank
     */
    public boolean hasSequenceId() {
        return sequenceId != null && !sequenceId.isBlank();
    }

    /**
     * Sets an optional delivery delay for the message. The delay value is a
     * provider-specific string (for example a duration) and may be null.
     *
     * @param delay the delay string or {@code null}
     */
    public void setDelay(@Nullable String delay) {
        this.delay = delay;
    }

    /**
     * Returns the configured delay string.
     *
     * @return the delay string
     */
    public String getDelay() {
        return java.util.Objects.requireNonNull(delay);
    }

    /**
     * Checks whether a non-blank delay value has been set.
     *
     * @return {@code true} when a delay is present and not blank
     */
    public boolean hasDelay() {
        return delay != null && !delay.isBlank();
    }
}
