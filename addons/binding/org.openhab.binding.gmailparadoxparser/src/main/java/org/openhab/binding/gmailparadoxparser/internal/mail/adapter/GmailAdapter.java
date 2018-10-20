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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.openhab.binding.gmailparadoxparser.internal.GmailParadoxParserHandlerFactory;
import org.openhab.binding.gmailparadoxparser.internal.ParadoxStatesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.BatchModifyMessagesRequest;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;

/**
 * The {@link GmailParadoxParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class GmailAdapter implements MailAdapter {
    private static final String USER = "polyopenhabtest@gmail.com";
    private static final String APPLICATION_NAME = "Gmail Paradox mail parser";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    private static Gmail googleService;
    private static final Logger logger = LoggerFactory.getLogger(ParadoxStatesCache.class);

    private String user;

    public static void main(String... args) throws IOException, GeneralSecurityException {
        MailAdapter adapter = new GmailAdapter();
        List<String> mailContents = adapter.retrieveAllMessagesContentsAndMarkAllRead(QUERY_UNREAD);
        for (String mail : mailContents) {
            logger.debug("Mail contents:");
            logger.debug(mail);
        }
    }

    public GmailAdapter() throws GeneralSecurityException, IOException {
        this(USER, CREDENTIALS_FILE_PATH, logger);
    }

    public GmailAdapter(String user, String credentialsFile, Logger logger)
            throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        if (googleService == null) {
            logger.info("Initializing Google Gmail service...");
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            googleService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME).build();
        } else {
            logger.info("Google Gmail service already initialized.");
        }
        this.user = user;
    }

    @Override
    public List<Message> retrieveMessages(String query) throws IOException, UnsupportedEncodingException {
        ListMessagesResponse listResponse = googleService.users().messages().list(user).setQ(query).execute();
        return listResponse.getMessages();
    }

    @Override
    public ArrayList<String> retrieveAllMessagesContents(List<Message> messages) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        if (messages == null || messages.isEmpty()) {
            logger.debug("No new emails found.");
        } else {
            logger.debug("Retrieved " + (messages.size() + 1) + " messages.");
            for (Message message : messages) {
                String msgId = message.getId();
                Message mail = googleService.users().messages().get(user, msgId).setFormat("full").execute();
                MessagePart payload = mail.getPayload();
                if (payload == null) {
                    logger.info("Payload is null");
                    continue;
                }

                MessagePartBody body = payload.getBody();
                if (body == null) {
                    logger.info("body is null");
                    continue;
                }
                String encodedContent = body.getData();
                if (encodedContent == null) {
                    logger.debug("body data is null. Will try to get message part [0] body");
                    encodedContent = payload.getParts().get(0).getBody().getData();
                }

                String content = new String(Base64.decodeBase64(encodedContent.getBytes()), "UTF-8");

                result.add(content);
            }
        }
        return result;
    }

    @Override
    public void markMessagesRead(List<Message> messages) throws IOException {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<String> msgIds = retrieveMessageIds(messages);
        BatchModifyMessagesRequest modifyRequest = new BatchModifyMessagesRequest().setIds(msgIds)
                // // .setAddLabelIds();
                .setRemoveLabelIds(Collections.singletonList("UNREAD"));
        googleService.users().messages().batchModify(user, modifyRequest).execute();
    }

    private List<String> retrieveMessageIds(List<Message> messages) {
        List<String> msgIds = new ArrayList<>();
        for (Message message : messages) {
            msgIds.add(message.getId());
        }
        return msgIds;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GmailAdapter.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline").build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
}