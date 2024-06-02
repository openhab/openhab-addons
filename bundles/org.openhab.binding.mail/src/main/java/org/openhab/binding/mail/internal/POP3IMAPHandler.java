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
package org.openhab.binding.mail.internal;

import static org.openhab.binding.mail.internal.MailBindingConstants.CHANNEL_TYPE_UID_FOLDER_MAILCOUNT;
import static org.openhab.binding.mail.internal.MailBindingConstants.CHANNEL_TYPE_UID_MAIL_CONTENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mail.internal.config.POP3IMAPConfig;
import org.openhab.binding.mail.internal.config.POP3IMAPContentChannelConfig;
import org.openhab.binding.mail.internal.config.POP3IMAPMailCountChannelConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPInputStream;

/**
 * The {@link POP3IMAPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class POP3IMAPHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(POP3IMAPHandler.class);

    private @NonNullByDefault({}) POP3IMAPConfig config;
    private @Nullable ScheduledFuture<?> refreshTask;
    private final String baseProtocol;
    private String protocol = "imap";

    public POP3IMAPHandler(Thing thing) {
        super(thing);
        baseProtocol = thing.getThingTypeUID().getId(); // pop3 or imap
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(POP3IMAPConfig.class);

        protocol = baseProtocol;

        if (config.security == ServerSecurity.SSL) {
            protocol = protocol.concat("s");
        }

        if (config.port == 0) {
            switch (protocol) {
                case "imap" -> config.port = 143;
                case "imaps" -> config.port = 993;
                case "pop3" -> config.port = 110;
                case "pop3s" -> config.port = 995;
                default -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    return;
                }
            }
        }

        refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        if (refreshTask != null) {
            if (!refreshTask.isCancelled()) {
                refreshTask.cancel(true);
            }
        }
    }

    private void refresh() {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        Properties props = new Properties();
        props.setProperty("mail." + baseProtocol + ".starttls.enable", "true");
        props.setProperty("mail.store.protocol", protocol);
        Session session = Session.getInstance(props);

        try (Store store = session.getStore()) {
            store.connect(config.hostname, config.port, config.username, config.password);

            for (Channel channel : thing.getChannels()) {
                if (CHANNEL_TYPE_UID_FOLDER_MAILCOUNT.equals(channel.getChannelTypeUID())) {
                    final POP3IMAPMailCountChannelConfig channelConfig = channel.getConfiguration()
                            .as(POP3IMAPMailCountChannelConfig.class);
                    final String folderName = channelConfig.folder;
                    if (folderName == null || folderName.isEmpty()) {
                        logger.info("missing or empty folder name in channel {}", channel.getUID());
                    } else {
                        try (Folder mailbox = store.getFolder(folderName)) {
                            mailbox.open(Folder.READ_ONLY);
                            if (channelConfig.type == MailCountChannelType.TOTAL) {
                                updateState(channel.getUID(), new DecimalType(mailbox.getMessageCount()));
                            } else {
                                updateState(channel.getUID(), new DecimalType(
                                        mailbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false)).length));
                            }
                        }
                    }
                } else if (CHANNEL_TYPE_UID_MAIL_CONTENT.equals(channel.getChannelTypeUID())) {
                    final POP3IMAPContentChannelConfig channelConfig = channel.getConfiguration()
                            .as(POP3IMAPContentChannelConfig.class);
                    final String folderName = channelConfig.folder;
                    if (folderName == null || folderName.isEmpty()) {
                        logger.info("missing or empty folder name in channel '{}'", channel.getUID());
                    } else {
                        try (Folder mailbox = store.getFolder(folderName)) {
                            mailbox.open(channelConfig.markAsRead ? Folder.READ_WRITE : Folder.READ_ONLY);
                            Message[] messages = mailbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                            for (Message message : messages) {
                                String subject = message.getSubject();
                                Address[] senders = message.getFrom();
                                String sender = senders == null ? ""
                                        : Stream.of(senders).map(Address::toString).collect(Collectors.joining(","));
                                logger.debug("Processing `{}` from `{}`", subject, sender);
                                if (!channelConfig.subject.isBlank() && !subject.matches(channelConfig.subject)) {
                                    logger.trace("Subject '{}' did not pass subject filter", subject);
                                    continue;
                                }
                                if (!channelConfig.sender.isBlank() && !sender.matches(channelConfig.sender)) {
                                    logger.trace("Sender '{}' did not pass filter '{}'", subject, channelConfig.sender);
                                    continue;
                                }
                                Object rawContent = message.getContent();
                                String contentAsString;
                                if (rawContent instanceof String str) {
                                    logger.trace("Detected plain text message");
                                    contentAsString = str;
                                } else if (rawContent instanceof MimeMessage mimeMessage) {
                                    logger.trace("Detected MIME message");
                                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                                        mimeMessage.writeTo(os);
                                        contentAsString = os.toString();
                                    }
                                } else if (rawContent instanceof MimeMultipart mimeMultipart) {
                                    logger.trace("Detected MIME multipart message");
                                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                                        mimeMultipart.writeTo(os);
                                        contentAsString = os.toString();
                                    }
                                } else if (rawContent instanceof IMAPInputStream imapInputStream) {
                                    logger.trace("Detected IMAPInputStream message");
                                    try {
                                        contentAsString = new String(imapInputStream.readAllBytes());
                                    } catch (IOException e) {
                                        logger.warn("Could not read from stream: {}", e.getMessage(), e);
                                        continue;
                                    }
                                } else {
                                    logger.warn(
                                            "Failed to convert mail content from '{}' with subject '{}', to String: {}",
                                            sender, subject, rawContent.getClass());
                                    continue;
                                }
                                logger.trace("Found content '{}'", contentAsString);
                                new ChannelTransformation(channelConfig.transformation).apply(contentAsString)
                                        .ifPresent(result -> updateState(channel.getUID(), new StringType(result)));
                            }
                        }
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            logger.info("Failed refreshing IMAP for thing '{}': {}", thing.getUID(), e.getMessage());
        }
    }
}
