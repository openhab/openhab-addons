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
 * {@link D2LBridgeHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class D2LBridgeHandler extends LinkyBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(D2LBridgeHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob = null;

    public D2LBridgeHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ThingRegistry thingRegistry, ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, thingRegistry, componentContext, gson);
    }

    @Override
    public void initialize() {
        super.initialize();

        pollingJob = scheduler.schedule(this::pollingCode, 5, TimeUnit.SECONDS);
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

        try {
            // define an assign a selector
            Selector selector = Selector.open();

            ServerSocketChannel socket = ServerSocketChannel.open();

            // specify the port and host to connect to
            InetSocketAddress serverSocketAddr = new InetSocketAddress(7845);

            socket.bind(serverSocketAddr);

            // to set our server as non-blocking.
            socket.configureBlocking(false);
            socket.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                if (selector.select(3000) == 0) {
                    continue;
                }

                // define a set of selectable keys
                Set<SelectionKey> selKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selKeys.iterator();

                // iterate over the selected keys
                while (keyIterator.hasNext()) {
                    SelectionKey myKey = keyIterator.next();

                    /*
                     * if both the server and the client have binded to a port and
                     * both are ready to share data with one another isAcceptable()
                     * will return true
                     */

                    if (myKey.isAcceptable()) {
                        SocketChannel client = socket.accept();

                        client.configureBlocking(false);
                        client.register(myKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(20000));
                    }

                    /*
                     * if there is any data while is yet to be read by the server
                     * isReadable returns true. Note that each operation of sendData() is
                     * mapped to a key and only if new data is present and unread
                     * isReadable() will return true
                     */

                    else if (myKey.isReadable()) {
                        SocketChannel client = (SocketChannel) myKey.channel();
                        ByteBuffer buf = (ByteBuffer) myKey.attachment();
                        long bytesRead = client.read(buf);

                        if (bytesRead == -1) {
                            client.close();
                        } else if (bytesRead > 0) {
                            handleRead(buf);
                            myKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }

                        // do something with your data

                        // send ACK
                        // ByteBuffer sendAck = ByteBuffer.wrap(ackByte);
                        // mllpClient.write(sendAck);

                    }
                }
                // once read, each key is removed from the operation.
                keyIterator.remove();

            }
        } catch (Exception ex) {
            logger.error("errors occured in data reception loop", ex);
        }
    }

    public void handleRead(ByteBuffer byteBuffer) {

        List<Thing> lThing = getThing().getThings();
        for (Thing th : lThing) {
            LinkyHandlerDirect handler = (LinkyHandlerDirect) th.getHandler();
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
    public String getToken(LinkyHandler handler) throws LinkyException {
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
