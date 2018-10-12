package org.openhab.binding.gmailparadoxparser.gmail.adapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.gmail.model.Message;

public interface GmailAdapter {

    public List<Message> retrieveMessages(String query) throws IOException, UnsupportedEncodingException;

    public ArrayList<String> retrieveAllMessagesContents(List<Message> messages) throws IOException;

    public void markMessagesRead(List<Message> msgIds) throws IOException;

}
