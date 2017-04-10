package org.openhab.binding.mailserver.internal;

import org.openhab.binding.mailserver.handler.MailServerBridgeHandler;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

/**
 * The {@link SmtpReceivedMessageHandlerFactory} stands up an instance of the message handler
 *
 * @author Jereme Guenther - Initial contribution
 */
public class SmtpReceivedMessageHandlerFactory implements MessageHandlerFactory {
    /**
     * The Bridge that spawned this factory instance.
     */
    public MailServerBridgeHandler h = null;

    /**
     * The default method that gets called to create the message handler
     *
     * @param ctx, the context the message will be in
     */
    @Override
    public MessageHandler create(MessageContext ctx) {
        return new SmtpReceivedMessageHandler(ctx, h);
    }

}
