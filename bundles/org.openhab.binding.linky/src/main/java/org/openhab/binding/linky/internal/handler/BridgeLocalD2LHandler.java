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
package org.openhab.binding.linky.internal.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link BridgeLocalD2LHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class BridgeLocalD2LHandler extends BridgeLinkyHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeLocalD2LHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob = null;

    public BridgeLocalD2LHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ThingRegistry thingRegistry, ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, thingRegistry, componentContext, gson);
    }

    @Override
    public void initialize() {
        super.initialize();

        pollingJob = scheduler.schedule(this::pollingCode, 3, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the D2LBridgeHandler handler");
        ScheduledFuture<?> job = this.pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            pollingJob = null;
        }

        logger.debug("Shutting down Enedis bridge handler.");
        super.dispose();
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

                        if (client != null) {
                            client.configureBlocking(false);
                            client.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(20000));
                        }
                    }

                    /*
                     * if there is any data while is yet to be read by the server
                     * isReadable returns true. Note that each operation of sendData() is
                     * mapped to a key and only if new data is present and unread
                     * isReadable() will return true
                     */

                    else if (selectionKey.isReadable()) {
                        SocketChannel client = (SocketChannel) selectionKey.channel();
                        ByteBuffer buf = (ByteBuffer) selectionKey.attachment();
                        long bytesRead = client.read(buf);

                        if (bytesRead == -1) {
                            client.close();
                        } else if (bytesRead > 0) {
                            handleRead(buf);
                            selectionKey.interestOps(SelectionKey.OP_READ);
                        }

                        // do something with your data

                        // send ACK
                        // ByteBuffer sendAck = ByteBuffer.wrap(ackByte);
                        // mllpClient.write(sendAck);

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
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException ex) {
                //
            }
        }

        logger.debug("end pooling socket");
    }

    public void handleRead(ByteBuffer byteBuffer) {

        List<Thing> lThing = getThing().getThings();
        for (Thing th : lThing) {
            LinkyLocalHandler handler = (LinkyLocalHandler) th.getHandler();
            if (handler != null) {
                handler.handleRead(byteBuffer);
            }
        }
    }

    public String getAccountUrl() {
        return "";
    }

    @Override
    public double getDivider() {
        return 0.0;
    }

    @Override
    public String getBaseUrl() {
        return "";
    }

    @Override
    public String getContactUrl() {
        return "";
    }

    @Override
    public String getContractUrl() {
        return "";
    }

    @Override
    public String getIdentityUrl() {
        return "";
    }

    @Override
    public String getAddressUrl() {
        return "";
    }

    @Override
    public String getDailyConsumptionUrl() {
        return "";
    }

    @Override
    public String getMaxPowerUrl() {
        return "";
    }

    @Override
    public String getLoadCurveUrl() {
        return "";
    }

    @Override
    public String getTempoUrl() {
        return "";
    }

    @Override
    public String getToken(LinkyRemoteHandler handler) throws LinkyException {
        return "";
    }

    @Override
    public DateTimeFormatter getApiDateFormat() {
        return DateTimeFormatter.BASIC_ISO_DATE;
    }

    @Override
    public DateTimeFormatter getApiDateFormatYearsFirst() {
        return DateTimeFormatter.BASIC_ISO_DATE;
    }

    @Override
    public boolean supportNewApiFormat() {
        return false;
    }

    @Override
    public void connectionInit() {
    }
}
