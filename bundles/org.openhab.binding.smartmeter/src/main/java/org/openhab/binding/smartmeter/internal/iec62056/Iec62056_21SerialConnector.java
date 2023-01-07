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
package org.openhab.binding.smartmeter.internal.iec62056;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.connectors.ConnectorBase;
import org.openhab.binding.smartmeter.internal.helper.Baudrate;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.Iec21Port;
import org.openmuc.j62056.Iec21Port.Builder;
import org.openmuc.j62056.ModeDListener;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

/**
 * This connector reads meter values with IEC62056-21 protocol.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class Iec62056_21SerialConnector extends ConnectorBase<DataMessage> {

    private final Logger logger = LoggerFactory.getLogger(Iec62056_21SerialConnector.class);
    private int baudrate;
    private int baudrateChangeDelay;
    private ProtocolMode protocolMode;
    @Nullable
    private Iec21Port iec21Port;

    public Iec62056_21SerialConnector(Supplier<SerialPortManager> serialPortManagerSupplier, String portName,
            int baudrate, int baudrateChangeDelay, ProtocolMode protocolMode) {
        super(portName);
        this.baudrate = baudrate;
        this.baudrateChangeDelay = baudrateChangeDelay;
        this.protocolMode = protocolMode;
    }

    @Override
    protected boolean applyPeriod() {
        return protocolMode != ProtocolMode.D;
    }

    @Override
    protected boolean applyRetryHandling() {
        return protocolMode != ProtocolMode.D;
    }

    @Override
    protected Publisher<?> getRetryPublisher(Duration period, Publisher<Throwable> attempts) {
        if (protocolMode == ProtocolMode.D) {
            return Flowable.empty();
        } else {
            return super.getRetryPublisher(period, attempts);
        }
    }

    @Override
    protected DataMessage readNext(byte @Nullable [] initMessage) throws IOException {
        if (iec21Port != null) {
            DataMessage dataMessage = iec21Port.read();
            logger.debug("Datamessage read: {}", dataMessage);
            return dataMessage;
        }
        throw new IOException("SerialPort was not yet created!");
    }

    @Override
    protected void emitValues(byte @Nullable [] initMessage, FlowableEmitter<@Nullable DataMessage> emitter)
            throws IOException {
        switch (protocolMode) {
            case ABC:
                super.emitValues(initMessage, emitter);
                break;
            case D:
                if (iec21Port != null) {
                    iec21Port.listen(new ModeDListener() {

                        @Override
                        public void newDataMessage(@Nullable DataMessage dataMessage) {
                            logger.debug("Datamessage read: {}", dataMessage);
                            emitter.onNext(dataMessage);
                        }

                        @Override
                        public void exceptionWhileListening(@Nullable Exception e) {
                            logger.warn("Exception while listening for mode D data message", e);
                        }
                    });
                }
                break;
            case SML:
                throw new IOException("SML mode not supported");
        }
    }

    @Override
    public void openConnection() throws IOException {
        Builder iec21Builder = new Iec21Port.Builder(getPortName());
        if (Baudrate.fromBaudrate(this.baudrate) != Baudrate.AUTO) {
            iec21Builder.setInitialBaudrate(this.baudrate);
        }
        iec21Builder.setBaudRateChangeDelay(baudrateChangeDelay);
        iec21Builder.enableVerboseMode(true);
        iec21Port = iec21Builder.buildAndOpen();
    }

    @Override
    public void closeConnection() {
        if (iec21Port != null) {
            iec21Port.close();
        }
    }
}
