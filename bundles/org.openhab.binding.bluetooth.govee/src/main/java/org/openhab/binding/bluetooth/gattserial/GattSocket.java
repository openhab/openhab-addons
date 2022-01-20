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
package org.openhab.binding.bluetooth.gattserial;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public abstract class GattSocket<T extends GattMessage, R extends GattMessage> {

    private static final Future<?> COMPLETED_FUTURE = CompletableFuture.completedFuture(null);

    private final Deque<MessageProcessor> messageProcessors = new ConcurrentLinkedDeque<>();

    public void registerMessageHandler(MessageHandler<T, R> messageHandler) {
        // we need to use a dummy future since ConcurrentHashMap doesn't allow null values
        messageProcessors.addFirst(new MessageProcessor(messageHandler, COMPLETED_FUTURE));
    }

    protected abstract ScheduledExecutorService getScheduler();

    public void sendMessage(MessageServicer<T, R> messageServicer) {
        T message = messageServicer.createMessage();

        CompletableFuture<@Nullable Void> messageFuture = sendMessage(message);

        Future<?> timeoutFuture = getScheduler().schedule(() -> {
            messageFuture.completeExceptionally(new TimeoutException("Timeout while waiting for response"));
        }, messageServicer.getTimeout(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        MessageProcessor processor = new MessageProcessor(messageServicer, timeoutFuture);
        messageProcessors.addLast(processor);

        messageFuture.whenComplete((v, ex) -> {
            if (ex instanceof CompletionException) {
                ex = ex.getCause();
            }
            if (ex != null) {
                if (messageServicer.handleFailedMessage(message, ex)) {
                    timeoutFuture.cancel(false);
                    messageProcessors.remove(processor);
                }
            }
        });
    }

    public CompletableFuture<@Nullable Void> sendMessage(T message) {
        List<byte[]> packets = createPackets(message);
        var futures = packets.stream()//
                .map(this::sendPacket)//
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    protected List<byte[]> createPackets(T message) {
        return List.of(message.getPayload());
    }

    protected abstract void parsePacket(byte[] packet, Consumer<R> messageHandler);

    protected abstract CompletableFuture<@Nullable Void> sendPacket(byte[] value);

    public void receivePacket(byte[] packet) {
        parsePacket(packet, this::handleMessage);
    }

    private void handleMessage(R message) {
        for (Iterator<MessageProcessor> it = messageProcessors.iterator(); it.hasNext();) {
            MessageProcessor processor = it.next();
            if (processor.messageHandler.handleReceivedMessage(message)) {
                processor.timeoutFuture.cancel(false);
                it.remove();
                // we want to return after the first message servicer handles the message
                if (processor.timeoutFuture != COMPLETED_FUTURE) {
                    return;
                }
            }
        }
    }

    private class MessageProcessor {
        private MessageHandler<T, R> messageHandler;
        private Future<?> timeoutFuture;

        public MessageProcessor(MessageHandler<T, R> messageHandler, Future<?> timeoutFuture) {
            this.messageHandler = messageHandler;
            this.timeoutFuture = timeoutFuture;
        }
    }
}
