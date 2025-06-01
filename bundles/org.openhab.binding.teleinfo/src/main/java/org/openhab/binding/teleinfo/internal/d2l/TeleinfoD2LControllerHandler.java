/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.d2l;

import static java.time.temporal.ChronoField.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoElectricityMeterHandler;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

/**
 * {@link BridgeLocalD2LHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TeleinfoD2LControllerHandler extends TeleinfoAbstractControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(TeleinfoD2LControllerHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
    private static final DateTimeFormatter LOCALDATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    private static final DateTimeFormatter LOCALDATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd['T'][' ']HH:mm").optionalStart().appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2).optionalStart().appendFraction(NANO_OF_SECOND, 0, 9, true).toFormatter();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class,
                    (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                            .parse(json.getAsJsonPrimitive().getAsString(), DATE_FORMATTER))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> LocalDate
                            .parse(json.getAsJsonPrimitive().getAsString(), LOCALDATE_FORMATTER))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
                        try {
                            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(),
                                    LOCALDATETIME_FORMATTER);
                        } catch (DateTimeParseException ex) {
                            return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), LOCALDATE_FORMATTER)
                                    .atStartOfDay();
                        }
                    })
            .create();

    public TeleinfoD2LControllerHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        pollingJob = scheduler.schedule(this::pollingCode, 3, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the LocalD2L bridge handler");
        ScheduledFuture<?> job = this.pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            pollingJob = null;
        }

        logger.debug("Shutting down Enedis bridge handler.");
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void pollingCode() {
        updateStatus(ThingStatus.ONLINE);
        Selector selector = null;
        ServerSocketChannel socket = null;

        try {
            // define an assign a selector
            selector = Selector.open();

            socket = ServerSocketChannel.open();

            Configuration thingConfig = getConfig();

            Object listenningPort = thingConfig.get("listenningPort");

            // specify the port and host to connect to
            InetSocketAddress serverSocketAddr = new InetSocketAddress(Integer.valueOf((String) listenningPort));

            socket.bind(serverSocketAddr);

            // to set our server as non-blocking.
            socket.configureBlocking(false);
            socket.register(selector, SelectionKey.OP_ACCEPT);

            while (getThing().getStatus() == ThingStatus.ONLINE) {
                // logger.debug("pool socket");
                if (selector.select(3000) == 0) {
                    continue;
                }

                // define a set of selectable keys
                Set<SelectionKey> selKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selKeys.iterator();

                // iterate over the selected keys
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();

                    /*
                     * if both the server and the client have binded to a port and
                     * both are ready to share data with one another isAcceptable()
                     * will return true
                     */

                    if (selectionKey.isAcceptable()) {
                        SocketChannel client = socket.accept();
                        logger.info("Accept: {} {}", client.getLocalAddress(), client.getRemoteAddress());

                        client.configureBlocking(false);
                        client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                        client.setOption(StandardSocketOptions.TCP_NODELAY, false);
                        client.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(20000));
                    }

                    /*
                     * if there is any data while is yet to be read by the server
                     * isReadable returns true. Note that each operation of sendData() is
                     * mapped to a key and only if new data is present and unread
                     * isReadable() will return true
                     */

                    else if (selectionKey.isReadable()) {
                        try {
                            SocketChannel client = (SocketChannel) selectionKey.channel();
                            ByteBuffer buf = (ByteBuffer) selectionKey.attachment();
                            long bytesRead = client.read(buf);

                            if (bytesRead == -1) {
                                logger.trace("Close on bytesRead==-1 {} {}", client.getLocalAddress(),
                                        client.getRemoteAddress());
                                client.close();
                            } else if (bytesRead > 0) {
                                boolean res = handleRead(buf);
                                selectionKey.interestOps(SelectionKey.OP_READ);
                                if (res) {
                                    logger.trace("Close on res=true {} {}", client.getLocalAddress(),
                                            client.getRemoteAddress());
                                    client.close();
                                }
                            }
                        } catch (SocketException ex) {
                            logger.debug("Error during reading socket, retry ", ex);
                        } catch (Exception ex) {
                            logger.debug("Error during reading, retry ", ex);
                        }
                    } else if (selectionKey.isWritable()) {
                        logger.debug("Writable");

                        ByteBuffer buf = (ByteBuffer) selectionKey.attachment();
                        buf.flip(); // Prepare buffer for writing
                        SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                        clientChannel.write(buf);
                        if (!buf.hasRemaining()) { // Buffer completely written?
                            // Nothing left, so no longer interested in writes
                            selectionKey.interestOps(SelectionKey.OP_READ);
                        }
                        buf.compact(); // Make room for more data to be read in
                    }
                }
                // once read, each key is removed from the operation.
                keyIterator.remove();
            }
        } catch (Exception ex) {
            logger.debug("errors occured in data reception loop", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ex.getMessage());
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException ex) {
                logger.debug("IO Exception occured during socket closing", ex);
            }
        }

        logger.debug("end pooling socket");
    }

    public boolean handleRead(ByteBuffer byteBuffer) {
        boolean res = false;
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // int version = byteBuffer.get(0);
        int length = byteBuffer.getShort(2);
        long idd2l = byteBuffer.getLong(4);

        if (byteBuffer.position() < length) {
            // We have incomplete data, wait next read on buffer
            return false;
        }

        // Look if we have a thing that is declared for this idd2l
        TeleinfoElectricityMeterHandler handler = getHandlerForIdd2l(idd2l);
        if (handler != null) {
            // if so, get appKey and ivKey and decode the buffer
            String appKey = handler.getAppKey();
            String ivKey = handler.getIvKey();

            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

                byte[] bytesKey = new BigInteger("7F" + appKey, 16).toByteArray();
                SecretKeySpec key = new SecretKeySpec(bytesKey, 1, bytesKey.length - 1, "AES");

                byte[] bytesIv = new BigInteger(ivKey, 16).toByteArray();
                IvParameterSpec iv = new IvParameterSpec(bytesIv);

                // cipher.init(Cipher.DECRYPT_MODE, key, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, iv);

                byte[] bufferToDecode = new byte[length];
                byteBuffer.get(16, bufferToDecode, 0, length - 16);
                byte[] plainText = cipher.doFinal(bufferToDecode);

                ByteBuffer byteBufferDecode = ByteBuffer.wrap(plainText);
                byteBufferDecode.order(ByteOrder.LITTLE_ENDIAN);
                int payloadLength = byteBufferDecode.getShort(18);
                int payloadType = byteBufferDecode.get(20) & 0x7f;

                String st1 = new String(plainText, 22, payloadLength);
                logger.info("frame with payload: {}", payloadType);

                if (payloadType == 0x03) {
                    // PUSH_JSON request
                    Type type = new TypeToken<Map<String, String>>() {
                    }.getType();

                    Map<String, String> r1 = gson.fromJson(st1, type);
                    if (r1 != null) {
                        Frame frame = new Frame();
                        for (String channelName : r1.keySet()) {

                            try {
                                Label label = Label.getEnum(channelName);
                                String valueString = r1.get(channelName);
                                String timestampString = null;

                                if (valueString != null) {
                                    int delimiterPos = valueString.indexOf('|');
                                    if (delimiterPos > 0) {
                                        timestampString = valueString.substring(0, delimiterPos);
                                        valueString = valueString.substring(delimiterPos + 1);
                                    } else if (delimiterPos == 0) {
                                        timestampString = "";
                                        valueString = "";
                                    }
                                    frame.put(label, valueString);

                                    if (timestampString != null) {
                                        frame.putTimestamp(label, timestampString);
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                final String error = String.format("The label '%s' is unknown", channelName);
                                throw new InvalidFrameException(error);
                            }
                        }

                        fireOnFrameReceivedEvent(frame);
                        // handler.handleFrame(frame);
                        // TeleinfoReceiveThreadListener listener = this.listener;
                        // listener.onFrameReceived(frame);

                        res = true;
                    }
                }
            } catch (Exception ex) {
                logger.debug("ex: {}", ex.toString(), ex);
            }
        }

        return res;
    }

    public @Nullable TeleinfoElectricityMeterHandler getHandlerForIdd2l(long idd2l) {
        List<Thing> lThing = getThing().getThings();

        for (Thing th : lThing) {
            if (th.getHandler() instanceof TeleinfoElectricityMeterHandler handler) {
                if (idd2l == handler.getIdd2l()) {
                    return handler;
                }
            }
        }

        return null;
    }

    public @Nullable TeleinfoElectricityMeterHandler getHandlerForPrmId(String prmId) {
        List<Thing> lThing = getThing().getThings();

        for (Thing th : lThing) {
            if (th.getHandler() instanceof TeleinfoElectricityMeterHandler handler) {
                if (prmId.equals(handler.getAdco())) {
                    return handler;
                }
            }
        }

        return null;
    }
}
