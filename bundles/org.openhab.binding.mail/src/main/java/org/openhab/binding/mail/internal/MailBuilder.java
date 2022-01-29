/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.FileDataSource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MailBuilder} class provides a builder for an mail.
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class MailBuilder {
    private String sender = "";
    private List<InternetAddress> recipients = new ArrayList<>();
    private List<URL> attachmentURLs = new ArrayList<>();
    private List<File> attachmentFiles = new ArrayList<>();
    private String subject = "(no subject)";
    private String text = "";
    private String html = "";
    private Map<String, String> headers = new HashMap<>();

    /**
     * Create a new MailBuilder
     *
     * @param recipients comma separated sequence of addresses (must follow RFC822 syntax)
     * @throws AddressException on invalid recipient address
     */
    public MailBuilder(String recipients) throws AddressException {
        this.recipients.addAll(Arrays.asList(InternetAddress.parse(recipients)));
    }

    /**
     * Add one or more recipients
     *
     * @param recipients comma separated sequence of addresses (must follow RFC822 syntax)
     * @return a MailBuilder
     * @throws AddressException on invalid recipient address
     */
    public MailBuilder withRecipients(String recipients) throws AddressException {
        this.recipients.addAll(Arrays.asList(InternetAddress.parse(recipients)));
        return this;
    }

    /**
     * Set the sender address
     *
     * @param sender address (must follow RFC822 syntax)
     * @return a MailBuilder
     */
    public MailBuilder withSender(String sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Set the mail subject
     *
     * @param subject String containing the subject
     * @return a MailBuilder
     */
    public MailBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Set the plain text content
     *
     * @param text String containing the text
     * @return a MailBuilder
     */
    public MailBuilder withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Set the HTML content
     *
     * @param html a String containing HTML (syntax not checked)
     * @return a MailBuilder
     */
    public MailBuilder withHtml(String html) {
        this.html = html;
        return this;
    }

    /**
     * Attach an URL
     *
     * @param urlString the URL as String
     * @return a MailBuilder
     * @throws MalformedURLException if url has invalid format
     */
    public MailBuilder withURLAttachment(String urlString) throws MalformedURLException {
        attachmentURLs.add(new URL(urlString));
        return this;
    }

    /**
     * Attach a file
     *
     * @param path String with path to local file
     * @return a MailBuilder
     */
    public MailBuilder withFileAttachment(String path) {
        attachmentFiles.add(new File(path));
        return this;
    }

    public MailBuilder withHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Build the Mail
     *
     * @return instance of Email
     * @throws EmailException if something goes wrong
     */
    public Email build() throws EmailException {
        Email mail;

        if (attachmentURLs.isEmpty() && attachmentFiles.isEmpty() && html.isEmpty()) {
            // text mail without attachments
            mail = new SimpleEmail();
            mail.setCharset(EmailConstants.UTF_8);
            if (!text.isEmpty()) {
                mail.setMsg(text);
            }
        } else if (html.isEmpty()) {
            // text mail with attachments
            MultiPartEmail multipartMail = new MultiPartEmail();
            multipartMail.setCharset(EmailConstants.UTF_8);
            if (!text.isEmpty()) {
                multipartMail.setMsg(text);
            }
            for (File file : attachmentFiles) {
                multipartMail.attach(file);
            }
            for (URL url : attachmentURLs) {
                String fileName = url.toString().replaceFirst(".*/([^/?]+).*", "$1");
                multipartMail.attach(url, fileName, fileName, EmailAttachment.ATTACHMENT);
            }
            mail = multipartMail;
        } else {
            // html email
            HtmlEmail htmlMail = new HtmlEmail();
            htmlMail.setCharset(EmailConstants.UTF_8);
            if (!text.isEmpty()) {
                // alternate text supplied
                htmlMail.setTextMsg(text);
                htmlMail.setHtmlMsg(html);
            } else {
                htmlMail.setMsg(html);
            }
            for (File file : attachmentFiles) {
                htmlMail.attach(new FileDataSource(file), "", "");
            }
            for (URL url : attachmentURLs) {
                EmailAttachment attachment = new EmailAttachment();
                attachment.setURL(url);
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                htmlMail.attach(attachment);
            }
            mail = htmlMail;
        }

        mail.setTo(recipients);
        mail.setSubject(subject);

        if (!sender.isEmpty()) {
            mail.setFrom(sender);
        }

        headers.forEach((name, value) -> mail.addHeader(name, value));

        return mail;
    }
}
