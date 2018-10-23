/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal.mail.adapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.openhab.binding.gmailparadoxparser.internal.GmailParadoxParserHandlerFactory;

import com.google.api.services.gmail.model.Message;

/**
 * The {@link GmailParadoxParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public interface MailAdapter {
    static final String QUERY_UNREAD = "label:unread";
    static final String QUERY_READ = "label:read";
    static final String INITIAL_QUERY = "newer_than:24h";

    public List<Message> retrieveMessages(String query) throws IOException, UnsupportedEncodingException;

    public List<String> retrieveMessagesContents(List<Message> messages) throws IOException;

    public void markMessagesRead(List<Message> messages) throws IOException;

    default public List<String> retrieveAndMarkRead(String query) throws IOException {
        List<Message> retrievedMessages = retrieveMessages(query);
        markMessagesRead(retrievedMessages);
        return retrieveMessagesContents(retrievedMessages);
    }
}
