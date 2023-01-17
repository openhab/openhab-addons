/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mail;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mail.internal.MailBuilder;

/**
 * The {@link MailBuilderTest} class defines tests for the {@link MailBuilder} class
 *
 * @author Jan N. Klug - Initial contribution
 */

public class MailBuilderTest {

    private static final String TEST_STRING = "test";
    private static final String TEST_EMAIL = "foo@bar.zinga";

    private static final String HEADER_1_KEY = "key_one";
    private static final String HEADER_1_VAL = "value_one";
    private static final String HEADER_2_KEY = "key_two";
    private static final String HEADER_2_VAL = "value_two";

    @Test
    public void illegalToAddressThrowsException() {
        assertThrows(AddressException.class, () -> new MailBuilder("foo bar.zinga"));
    }

    @Test
    public void illegalFromAddressThrowsException() {
        assertThrows(EmailException.class, () -> new MailBuilder("TEST_EMAIL").withSender("foo bar.zinga").build());
    }

    @Test
    public void illegalURLThrowsException() {
        assertThrows(MalformedURLException.class,
                () -> new MailBuilder("TEST_EMAIL").withURLAttachment("foo bar.zinga"));
    }

    @Test
    public void withTextOnlyReturnsSimpleEmail() throws AddressException, EmailException {
        MailBuilder builder = new MailBuilder(TEST_EMAIL);
        Email mail = builder.withText("boo").build();
        assertThat(mail, instanceOf(SimpleEmail.class));
    }

    @Test
    public void withURLAttachmentReturnsMultiPartEmail()
            throws AddressException, EmailException, MalformedURLException {
        MailBuilder builder = new MailBuilder(TEST_EMAIL);
        String url = Path.of("src/test/resources/attachment.txt").toUri().toURL().toString();
        Email mail = builder.withText("boo").withURLAttachment(url).build();
        assertThat(mail, instanceOf(MultiPartEmail.class));
    }

    @Test
    public void withHtmlReturnsHtmlEmail() throws AddressException, EmailException {
        MailBuilder builder = new MailBuilder(TEST_EMAIL);
        Email mail = builder.withHtml("<html>test</html>").build();
        assertThat(mail, instanceOf(HtmlEmail.class));
    }

    @Test
    public void fieldsSetInMail() throws EmailException, MessagingException, IOException {
        MailBuilder builder = new MailBuilder(TEST_EMAIL);

        assertEquals("(no subject)", builder.build().getSubject());
        assertEquals(TEST_STRING, builder.withSubject(TEST_STRING).build().getSubject());

        assertEquals(TEST_EMAIL, builder.withSender(TEST_EMAIL).build().getFromAddress().getAddress());

        assertEquals(TEST_EMAIL, builder.build().getToAddresses().get(0).getAddress());
        assertEquals(2, builder.withRecipients(TEST_EMAIL).build().getToAddresses().size());
    }

    @Test
    public void withHeaders() throws EmailException, MessagingException, IOException {
        MailBuilder builder = new MailBuilder(TEST_EMAIL);
        Email mail = builder.withHeader(HEADER_1_KEY, HEADER_1_VAL).withHeader(HEADER_2_KEY, HEADER_2_VAL).build();

        Map<String, String> headers = mail.getHeaders();

        assertEquals(2, headers.size());
        assertEquals(HEADER_2_VAL, headers.get(HEADER_2_KEY));
        assertEquals(HEADER_1_VAL, headers.get(HEADER_1_KEY));
    }
}
