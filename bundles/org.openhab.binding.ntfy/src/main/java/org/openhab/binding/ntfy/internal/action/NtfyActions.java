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
package org.openhab.binding.ntfy.internal.action;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ntfy.internal.NtfyTopicHandler;
import org.openhab.binding.ntfy.internal.network.NtfyMessage;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Provides the actions for the ntfy API.
 *
 * @author Christian Kittel - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NtfyActions.class)
@ThingActionsScope(name = "ntfy")
@NonNullByDefault
public class NtfyActions implements ThingActions {

    private @Nullable NtfyTopicHandler handler;
    private NtfyMessage ntfyMessage = new NtfyMessage();

    private void resetNtfyMessage() {
        ntfyMessage = new NtfyMessage();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (NtfyTopicHandler) handler;
        resetNtfyMessage();
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    /**
     * Sends a simple message directly to the topic.
     *
     * @param message the message content to send
     * @return the id of the created message or an empty string on failure
     * @throws URISyntaxException
     */
    @RuleAction(label = "send a simple message", description = "Send a message to the ntfy topic.")
    public @ActionOutput(label = "Message ID", type = "java.lang.String") String sendNtfyMessage(
            @ActionInput(name = "message") String message) throws URISyntaxException {
        final NtfyTopicHandler handler = this.handler;
        if (handler == null) {
            return "";
        }
        final NtfyMessage ntfyMessage = new NtfyMessage();
        ntfyMessage.setMessage(message);
        return handler.sendMessage(ntfyMessage);
    }

    /**
     * Static helper to call {@link #sendNtfyMessage(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param message the message content
     * @return the id of the created message or an empty string on failure
     * @throws URISyntaxException
     */
    public static String sendNtfyMessage(ThingActions actions, String message) throws URISyntaxException {
        return ((NtfyActions) actions).sendNtfyMessage(message);
    }

    /**
     * Sets the message content on the internal builder instance and returns this
     * actions instance to allow fluent chaining in automation rules.
     *
     * @param message the message content to set
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "set the message", description = "Set the message content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withMessage(
            @ActionInput(name = "message") String message) {
        ntfyMessage.setMessage(message);
        return this;
    }

    /**
     * Static helper to call {@link #withMessage(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param message the message content
     * @return the modified ThingActions builder instance
     */
    public static ThingActions withMessage(ThingActions actions, String message) {
        return ((NtfyActions) actions).withMessage(message);
    }

    /**
     * Sets the numeric priority for the message builder (1..5).
     *
     * @param priority the priority value
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "set the priority of the message", description = "Set the priority of the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withPriority(
            @ActionInput(name = "priority") int priority) {
        ntfyMessage.setPriority(priority);
        return this;
    }

    /**
     * Static helper to call {@link #withPriority(int)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param priority the priority value
     * @return the modified ThingActions builder instance
     */
    public static ThingActions withPriority(ThingActions actions, int priority) {
        return ((NtfyActions) actions).withPriority(priority);
    }

    /**
     * Adds a tag to the internal message builder.
     *
     * @param tag the tag to add
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "add a tag to the message", description = "Add a tag to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withTag(
            @ActionInput(name = "tag") String tag) {
        ntfyMessage.addTag(tag);
        return this;
    }

    /**
     * Static helper to call {@link #withTag(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param tag the tag to add
     * @return the modified ThingActions builder instance
     */
    public static ThingActions withTag(ThingActions actions, String tag) {
        return ((NtfyActions) actions).withTag(tag);
    }

    /**
     * Sets an icon URL on the internal message builder.
     *
     * @param url the icon URL
     * @return this {@link ThingActions} builder instance
     * @throws URISyntaxException when the provided URL cannot be parsed
     */
    @RuleAction(label = "add an icon to the message", description = "Add an icon to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withIcon(
            @ActionInput(name = "url") String url) throws URISyntaxException {
        ntfyMessage.setIcon(url);
        return this;
    }

    /**
     * Static helper to call {@link #withIcon(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param url the icon URL
     * @return the modified ThingActions builder instance
     * @throws URISyntaxException when the provided URL cannot be parsed
     */
    public static ThingActions withIcon(ThingActions actions, String url) throws URISyntaxException {
        return ((NtfyActions) actions).withIcon(url);
    }

    /**
     * Sets an attachment URL and optional filename on the message builder.
     *
     * @param url the attachment URL
     * @param filename optional filename to present to recipients
     * @return this {@link ThingActions} builder instance
     * @throws URISyntaxException when the provided URL cannot be parsed
     */
    @RuleAction(label = "add an attachment to the message", description = "Add an attachment to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withAttachment(
            @ActionInput(name = "url") String url, @ActionInput(name = "filename") @Nullable String filename)
            throws URISyntaxException {
        ntfyMessage.setAttachment(url, filename);
        return this;
    }

    /**
     * Static helper to call {@link #withAttachment(String, String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param url the attachment URL
     * @param filename optional filename
     * @return the modified ThingActions builder instance
     * @throws URISyntaxException when the provided URL cannot be parsed
     */
    public static ThingActions withAttachment(ThingActions actions, String url, @Nullable String filename)
            throws URISyntaxException {
        return ((NtfyActions) actions).withAttachment(url, filename);
    }

    /**
     * Adds a view action to the internal message builder which opens the supplied URL.
     *
     * @param label the label for the action
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param url the URL to open
     * @return this {@link ThingActions} builder instance
     * @throws MalformedURLException when the provided URL is not valid
     */
    @RuleAction(label = "add a view action to the message", description = "Add a view action to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withViewAction(
            @ActionInput(name = "label") String label,
            @ActionInput(name = "clearNotification") Boolean clearNotification, @ActionInput(name = "url") String url)
            throws MalformedURLException {
        ntfyMessage.addViewAction(label, clearNotification, url);
        return this;
    }

    /**
     * Static helper to call {@link #withViewAction(String, Boolean, String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param label the action label
     * @param clearNotification whether to clear notification
     * @param url the URL to open
     * @return the modified ThingActions builder instance
     * @throws MalformedURLException when the provided URL is not valid
     */
    public static ThingActions withViewAction(ThingActions actions, String label, Boolean clearNotification, String url)
            throws MalformedURLException {
        return ((NtfyActions) actions).withViewAction(label, clearNotification, url);
    }

    /**
     * Adds a copy action to the message builder which copies a value to the clipboard.
     *
     * @param label the label for the action
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param value the value to copy
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "add a copy action to the message", description = "Add a copy action to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withCopyAction(
            @ActionInput(name = "label") String label,
            @ActionInput(name = "clearNotification") Boolean clearNotification,
            @ActionInput(name = "value") String value) {
        ntfyMessage.addCopyAction(label, clearNotification, value);
        return this;
    }

    /**
     * Static helper to call {@link #withCopyAction(String, Boolean, String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param label the action label
     * @param clearNotification whether to clear the notification
     * @param value the value to copy
     * @return the modified ThingActions builder instance
     * @throws MalformedURLException never thrown by this wrapper but declared for API compatibility
     */
    public static ThingActions withCopyAction(ThingActions actions, String label, Boolean clearNotification,
            String value) throws MalformedURLException {
        return ((NtfyActions) actions).withCopyAction(label, clearNotification, value);
    }

    /**
     * Adds an HTTP action to the message builder which will perform a HTTP call when executed.
     *
     * @param label the label for the action
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param url the URL to call
     * @param method optional HTTP method
     * @param headers optional headers
     * @param body optional request body
     * @return this {@link ThingActions} builder instance
     * @throws MalformedURLException when the provided URL is not valid
     */
    @RuleAction(label = "add a HTTP action to the message", description = "Add a HTTP action to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withHttpAction(
            @ActionInput(name = "label") String label,
            @ActionInput(name = "clearNotification") Boolean clearNotification, @ActionInput(name = "url") String url,
            @ActionInput(name = "method") @Nullable String method,
            @ActionInput(name = "headers") @Nullable String headers, @ActionInput(name = "body") @Nullable String body)
            throws MalformedURLException {
        ntfyMessage.addHttpAction(label, clearNotification, url, method, headers, body);
        return this;
    }

    /**
     * Static helper to call {@link #withHttpAction(String, Boolean, String, String, String, String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param label the action label
     * @param clearNotification whether to clear the notification
     * @param url the URL to call
     * @param method optional method
     * @param headers optional headers
     * @param body optional body
     * @return the modified ThingActions builder instance
     * @throws MalformedURLException when the provided URL is not valid
     */
    public static ThingActions withHttpAction(ThingActions actions, String label, Boolean clearNotification, String url,
            @Nullable String method, @Nullable String headers, @Nullable String body) throws MalformedURLException {
        return ((NtfyActions) actions).withHttpAction(label, clearNotification, url, method, headers, body);
    }

    /**
     * Adds a broadcast action to the internal message builder.
     *
     * @param label the action label
     * @param clearNotification whether the notification should be cleared when the action is executed
     * @param params parameters passed to the broadcast action
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "add a broadcast action to the message", description = "Add a broadcast action to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withBroadcastAction(
            @ActionInput(name = "label") String label,
            @ActionInput(name = "clearNotification") Boolean clearNotification,
            @ActionInput(name = "params") @Nullable String params) {
        ntfyMessage.addBroadcastAction(label, clearNotification, params);
        return this;
    }

    /**
     * Static helper to call {@link #withBroadcastAction(String, Boolean, String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param label the action label
     * @param clearNotification whether to clear the notification
     * @param params parameters for the broadcast
     * @return the modified ThingActions builder instance
     * @throws MalformedURLException never thrown by this wrapper but declared for API compatibility
     */
    public static ThingActions withBroadcastAction(ThingActions actions, String label, Boolean clearNotification,
            @Nullable String params) throws MalformedURLException {
        return ((NtfyActions) actions).withBroadcastAction(label, clearNotification, params);
    }

    /**
     * Sets an optional delivery delay on the internal message builder.
     * message is published.
     *
     * @param delay the provider-specific delay string (may be null)
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "add a delay to the message", description = "Add a delay for the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withDelay(
            @ActionInput(name = "delay") @Nullable String delay) {
        ntfyMessage.setDelay(delay);
        return this;
    }

    /**
     * Static helper to call {@link #withDelay(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param delay the provider-specific delay string (may be null)
     * @return the modified ThingActions builder instance
     */
    public static ThingActions withDelay(ThingActions actions, @Nullable String delay) {
        return ((NtfyActions) actions).withDelay(delay);
    }

    /**
     * Sets the sequence id on the builder which can be used to later identify or
     * delete the message on the server.
     *
     * @param sequenceId the sequence id or {@code null}
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "add a sequence ID to the message", description = "Add a sequence ID to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withSequenceId(
            @ActionInput(name = "sequenceId") @Nullable String sequenceId) {
        ntfyMessage.setSequenceId(sequenceId);
        return this;
    }

    /**
     * Static helper to call {@link #withSequenceId(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param sequenceId the sequence id
     * @return the modified ThingActions builder instance
     */
    public static ThingActions withSequenceId(ThingActions actions, @Nullable String sequenceId) {
        return ((NtfyActions) actions).withSequenceId(sequenceId);
    }

    /**
     * Adds a title to the internal message builder.
     *
     * @param title the title
     * @return this {@link ThingActions} builder instance
     */
    @RuleAction(label = "add a title to the message", description = "Add a title to the content for the builder.")
    public @ActionOutput(label = "Ntfy Actions", type = "org.openhab.core.thing.binding.ThingActions") ThingActions withTitle(
            @ActionInput(name = "title") String title) {
        ntfyMessage.setTitle(title);
        return this;
    }

    /**
     * Static helper to call {@link #withTitle(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param title the title
     * @return the modified ThingActions builder instance
     */
    public static ThingActions withTitle(ThingActions actions, String title) {
        return ((NtfyActions) actions).withTitle(title);
    }

    /**
     * Sends the message constructed via the builder-style methods and returns the
     * created message id.
     *
     * @return the created message id or an empty string on failure
     * @throws URISyntaxException
     */
    @RuleAction(label = "send built message", description = "Send the message configured via builder methods.")
    public @ActionOutput(label = "Message ID", type = "java.lang.String") String send() throws URISyntaxException {
        final NtfyTopicHandler handler = this.handler;
        if (handler == null) {
            return "";
        }

        if (!ntfyMessage.hasMessage() || ntfyMessage.getMessage().isBlank()) {
            throw new IllegalStateException(
                    "Cannot send message without content. Please set a message via withMessage() before sending.");
        }

        return handler.sendMessage(ntfyMessage);
    }

    /**
     * Static helper to call {@link #send()} from rule code.
     *
     * @param actions the ThingActions instance
     * @return the created message id or an empty string on failure
     * @throws URISyntaxException
     */
    public static String send(ThingActions actions) throws URISyntaxException {
        return ((NtfyActions) actions).send();
    }

    /**
     * Sends the content of a local file to the configured topic using the
     * {@link NtfyTopicHandler#sendFile(String, String, String)} method.
     *
     * @param file the filesystem path to the file to send
     * @param filename optional filename to present to recipients (may be null)
     * @param sequenceId optional sequence id to associate with the message
     * @return the id of the created message or an empty string on failure
     * @throws URISyntaxException when the constructed request URI is invalid
     */
    @RuleAction(label = "send a file", description = "Send a file with the given sequence ID.")
    public @ActionOutput(label = "Message ID", type = "java.lang.String") String send(
            @ActionInput(name = "file") String file, @ActionInput(name = "filename") @Nullable String filename,
            @ActionInput(name = "sequenceId") @Nullable String sequenceId) throws URISyntaxException {
        final NtfyTopicHandler handler = this.handler;
        if (handler == null) {
            return "";
        }

        return handler.sendFile(file, filename, sequenceId);
    }

    /**
     * Static helper to call {@link #send(String, String, String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param file the filesystem path to the file to send
     * @param filename optional filename to present to recipients (may be null)
     * @param sequenceId optional sequence id to associate with the message
     * @return the id of the created message or an empty string on failure
     * @throws URISyntaxException when the constructed request URI is invalid
     */
    public static String send(ThingActions actions, String file, @Nullable String filename, @Nullable String sequenceId)
            throws URISyntaxException {
        return ((NtfyActions) actions).send(file, filename, sequenceId);
    }

    /**
     * Deletes a message with the provided sequence id.
     *
     * @param sequenceId the sequence id of the message to delete
     * @return {@code true} when deletion succeeded
     * @throws URISyntaxException when the constructed request URI is invalid
     */
    @RuleAction(label = "delete a message", description = "Delete the message with the given sequence ID.")
    public @ActionOutput(label = "Deletion of the message was successful", type = "java.lang.Boolean") boolean delete(
            @ActionInput(name = "sequenceId") String sequenceId) throws URISyntaxException {
        final NtfyTopicHandler handler = this.handler;
        if (handler == null || sequenceId.isBlank()) {
            return false;
        }

        return handler.deleteMessage(sequenceId);
    }

    /**
     * Static helper to call {@link #delete(String)} from rule code.
     *
     * @param actions the ThingActions instance
     * @param sequenceId the sequence id to delete
     * @return {@code true} when deletion succeeded
     * @throws URISyntaxException when the constructed request URI is invalid
     */
    public static boolean delete(ThingActions actions, String sequenceId) throws URISyntaxException {
        return ((NtfyActions) actions).delete(sequenceId);
    }

    /**
     * Clears the current in-progress message builder state.
     */
    @RuleAction(label = "clear ntfy message builder", description = "Reset the current ntfy message builder state.")
    public void clearNtfyMessageBuilder() {
        resetNtfyMessage();
    }

    /**
     * Static helper to call {@link #clearNtfyMessageBuilder()} from rule code.
     *
     * @param actions the ThingActions instance
     */
    public static void clearNtfyMessageBuilder(ThingActions actions) {
        ((NtfyActions) actions).clearNtfyMessageBuilder();
    }
}
