package org.openhab.binding.gmailparadoxparser.gmail.adapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.google.api.services.gmail.model.Message;

public interface MailAdapter {
    static final String QUERY_UNREAD = "label:unread ";
    static final String QUERY_READ = "label:read ";

    public List<Message> retrieveMessages(String query) throws IOException, UnsupportedEncodingException;

    public List<String> retrieveAllMessagesContents(List<Message> messages) throws IOException;

    public void markMessagesRead(List<Message> messages) throws IOException;

    default public List<String> retrieveAllMessagesContentsAndMarkAllRead(String query) throws IOException {
        List<Message> retrievedMessages = retrieveMessages(query);
        markMessagesRead(retrievedMessages);
        return retrieveAllMessagesContents(retrievedMessages);
    }
}
