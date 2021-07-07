/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissUDPConstants;
import org.openhab.binding.souliss.internal.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.handler.SoulissGenericHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide to take packet, and send it to regular interval to Souliss
 * Network
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SendDispatcherRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SendDispatcherRunnable.class);

    @Nullable
    private SoulissGatewayHandler gwHandler;
    static boolean bPopSuspend = false;
    protected static ArrayList<PacketStruct> packetsList = new ArrayList<>();
    private long startTime = System.currentTimeMillis();
    static int iDelay = 0; // equal to 0 if array is empty
    static int sendMinDelay = 0;

    public SendDispatcherRunnable(Bridge bridge) {
        this.gwHandler = (SoulissGatewayHandler) bridge.getHandler();
    }

    /**
     * Put packet to send in ArrayList PacketList
     */
    public static synchronized void put(DatagramPacket packetToPUT, Logger logger) {
        bPopSuspend = true;
        var bPacchettoGestito = false;
        // estraggo il nodo indirizzato dal pacchetto in ingresso
        // restituisce -1 se il pacchetto non è del tipo SOULISS_UDP_FUNCTION_FORCE
        int node = getNode(packetToPUT);
        if (node >= 0) {
            logger.debug("Push packet in queue - Node {}", node);
        }

        if (packetsList.isEmpty() || node < 0) {
            bPacchettoGestito = false;
        } else {
            // OTTIMIZZATORE
            // scansione lista pacchetti da inviare
            for (var i = 0; i < packetsList.size(); i++) {
                if (node >= 0 && getNode(packetsList.get(i).packet) == node && !packetsList.get(i).isSent()) {
                    // frame per lo stesso nodo già  presente in lista
                    logger.debug("Frame UPD per nodo {} già presente in coda. Esecuzione ottimizzazione.", node);
                    bPacchettoGestito = true;
                    // se il pacchetto da inserire è più corto (o uguale) di
                    // quello in coda allora sovrascrivo i byte del pacchetto
                    // presente nella coda
                    if (packetToPUT.getData().length <= packetsList.get(i).packet.getData().length) {
                        // scorre i byte di comando e se il byte è diverso da
                        // zero sovrascrive il byte presente nel pacchetto in
                        // coda
                        logger.trace("Optimizer. Packet to push: {}", macacoToString(packetToPUT.getData()));
                        logger.trace("Optimizer. Previous frame: {}",
                                macacoToString(packetsList.get(i).packet.getData()));
                        // i valori dei tipici partono dal byte 12 in poi
                        for (var j = 12; j < packetToPUT.getData().length; j++) {
                            // se il j-esimo byte è diverso da zero allora lo
                            // sovrascrivo al byte del pacchetto già presente
                            if (packetToPUT.getData()[j] != 0) {
                                packetsList.get(i).packet.getData()[j] = packetToPUT.getData()[j];
                            }
                        }
                        logger.debug("Optimizer. Previous frame modified to: {}",
                                macacoToString(packetsList.get(i).packet.getData()));
                    } else {
                        // se il pacchetto da inserire è più lungo di quello in
                        // lista allora sovrascrivo i byte del pacchetto da
                        // inserire, poi elimino quello in lista ed inserisco
                        // quello nuovo
                        if (packetToPUT.getData().length > packetsList.get(i).packet.getData().length) {
                            for (var j = 12; j < packetsList.get(i).packet.getData().length; j++) {
                                // se il j-esimo byte è diverso da zero allora
                                // lo sovrascrivo al byte del pacchetto già
                                // presente
                                if ((packetsList.get(i).packet.getData()[j] != 0) && (packetToPUT.getData()[j] == 0)) {
                                    // sovrascrive i byte dell'ultimo frame
                                    // soltanto se il byte è uguale a zero.
                                    // Se è diverso da zero l'ultimo frame
                                    // ha la precedenza e deve sovrascrivere
                                    packetToPUT.getData()[j] = packetsList.get(i).packet.getData()[j];

                                }
                            }
                            // rimuove il pacchetto
                            logger.debug("Optimizer. Remove frame: {}",
                                    macacoToString(packetsList.get(i).packet.getData()));
                            packetsList.remove(i);
                            // inserisce il nuovo
                            logger.debug("Optimizer. Add frame: {}", macacoToString(packetToPUT.getData()));
                            packetsList.add(new PacketStruct(packetToPUT));
                        }
                    }
                }
            }
        }

        if (!bPacchettoGestito) {
            logger.debug("Add packet: {}", macacoToString(packetToPUT.getData()));
            packetsList.add(new PacketStruct(packetToPUT));
        }
        bPopSuspend = false;
    }

    @Override
    public void run() {
        DatagramSocket sender = null;

        try {
            if (checkTime()) {
                PacketStruct sp = pop();
                if (sp != null) {
                    logger.debug(
                            "SendDispatcherJob - Functional Code 0x{} - Packet: {} - Elementi rimanenti in lista: {}",
                            Integer.toHexString(sp.packet.getData()[7]), macacoToString(sp.packet.getData()),
                            packetsList.size());

                    var channel = DatagramChannel.open();
                    sender = channel.socket();
                    sender.setReuseAddress(true);
                    sender.setBroadcast(true);

                    var sa = new InetSocketAddress(230);
                    sender.bind(sa);

                    sender.send(sp.packet);
                }

                // confronta gli stati in memoria con i frame inviati. Se
                // corrispondono cancella il frame dalla lista inviati
                safeSendCheck();

                resetTime();
            }
        } catch (Exception e) {
            logger.warn("{}", e.getMessage());
        } finally {
            if (sender != null && !sender.isClosed()) {
                sender.close();
            }
        }
    }

    /**
     * Get node number from packet
     */
    private static int getNode(DatagramPacket packet) {
        // 7 è il byte del frame VNet al quale trovo il codice comando
        // 10 è il byte del frame VNet al quale trovo l'ID del nodo
        if (packet.getData()[7] == SoulissUDPConstants.SOULISS_UDP_FUNCTION_FORCE) {
            return packet.getData()[10];
        }
        return -1;
    }

    private static String macacoToString(byte[] frame2) {
        byte[] frame = frame2.clone();
        var sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * check frame updates with packetList, where flag "sent" is true. If all
     * commands was executed there delete packet in list.
     * Confronta gli aggiornamenti ricevuti con i frame inviati. Se corrispondono allora cancella il
     * frame nella lista inviati .
     */

    public void safeSendCheck() {
        int node;
        int iSlot;
        @Nullable
        SoulissGenericHandler localTyp;
        String sCmd = "";
        byte bExpected;
        byte bActualItemState;
        String sExpected = "";
        // short sVal = getByteAtSlot(macacoFrame, slot);
        // scansione lista paccetti inviati
        for (var i = 0; i < packetsList.size(); i++) {

            if (packetsList.get(i).isSent()) {
                node = getNode(packetsList.get(i).packet);
                iSlot = 0;
                for (var j = 12; j < packetsList.get(i).packet.getData().length; j++) {
                    // controllo lo slot solo se il comando è diverso da ZERO
                    if (packetsList.get(i).packet.getData()[j] != 0) {
                        // recupero tipico dalla memoria

                        if (this.gwHandler != null) {
                            localTyp = getHandler(node, iSlot, this.logger);

                            if (localTyp == null) {
                                break;
                            }

                            bExpected = localTyp.getExpectedRawState(packetsList.get(i).packet.getData()[j]);

                            // se il valore atteso dal tipico è -1 allora vuol dire che il tipico non supporta la
                            // funzione
                            // secureSend
                            if (bExpected < 0) {
                                localTyp = null;
                            }

                            // traduce il comando inviato con lo stato previsto e
                            // poi fa il confronto con lo stato attuale
                            if (logger.isDebugEnabled() && localTyp != null) {
                                sCmd = Integer.toHexString(packetsList.get(i).packet.getData()[j]);
                                // comando inviato
                                sCmd = sCmd.length() < 2 ? "0x0" + sCmd.toUpperCase() : "0x" + sCmd.toUpperCase();
                                sExpected = Integer.toHexString(bExpected);
                                sExpected = sExpected.length() < 2 ? "0x0" + sExpected.toUpperCase()
                                        : "0x" + sExpected.toUpperCase();
                                logger.debug(
                                        "Compare. Node: {} Slot: {} Node Name: {} Command: {} Expected Souliss State: {} - Actual OH item State: {}",
                                        node, iSlot, localTyp.getLabel(), sCmd, sExpected, localTyp.getRawState());
                            }

                            if (localTyp != null && checkExpectedState(localTyp.getRawState(), bExpected)) {
                                // se il valore del tipico coincide con il valore
                                // trasmesso allora pongo il byte a zero.
                                // quando tutti i byte saranno uguale a zero allora
                                // si
                                // cancella il frame
                                packetsList.get(i).packet.getData()[j] = 0;
                                logger.debug("{} Node: {} Slot: {} - OK Expected State", localTyp.getLabel(), node,
                                        iSlot);
                            } else if (localTyp == null) {
                                if (bExpected < 0) {
                                    // se il tipico non viene gestito allora metto a zero il byte del relativo
                                    // slot
                                    packetsList.get(i).packet.getData()[j] = 0;
                                } else {
                                    // se allo slot j non esiste un tipico allora vuol dire che si tratta di uno slot
                                    // collegato
                                    // al precedente (es: RGB, T31,...)
                                    // allora se lo slot j-1=0 allora anche j puÃ² essere messo a 0
                                    if (packetsList.get(i).packet.getData()[j - 1] == 0) {
                                        packetsList.get(i).packet.getData()[j] = 0;
                                    }
                                }
                            }
                        }
                    }
                    iSlot++;
                }

                // se il valore di tutti i byte che costituiscono il pacchetto è 0 allora rimuovo il pacchetto dalla
                // lista
                // inoltre se è trascorso il timout allora imposto il pacchetto per essere trasmesso nuovamente
                if (checkAllsSlotZero(packetsList.get(i).packet)) {
                    logger.debug("Command packet executed - Removed");
                    packetsList.remove(i);
                } else {
                    // se il frame non è uguale a zero controllo il TIMEOUT e se
                    // è scaduto allora pongo il flag SENT a false
                    long time = System.currentTimeMillis();

                    @Nullable
                    SoulissGatewayHandler localGwHandler = this.gwHandler;
                    if (localGwHandler != null) {
                        if (localGwHandler.gwConfig.timeoutToRequeue < time - packetsList.get(i).getTime()) {
                            if (localGwHandler.gwConfig.timeoutToRemovePacket < time - packetsList.get(i).getTime()) {
                                logger.debug("Packet Execution timeout - Removed");
                                packetsList.remove(i);
                            } else {
                                logger.debug("Packet Execution timeout - Requeued");
                                packetsList.get(i).setSent(false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private SoulissGenericHandler getHandler(int node, int slot, Logger logger) {
        SoulissGatewayHandler localGwHandler = this.gwHandler;

        Iterator<Thing> thingsIterator;
        if (localGwHandler != null) {
            thingsIterator = localGwHandler.getThing().getThings().iterator();
            Thing typ = null;
            while (thingsIterator.hasNext()) {
                typ = thingsIterator.next();
                if (typ.getThingTypeUID().equals(SoulissBindingConstants.TOPICS_THING_TYPE)) {
                    continue;
                }
                String[] sUIDArray = typ.getUID().getAsString().split(":");
                SoulissGenericHandler handler = (SoulissGenericHandler) typ.getHandler();
                if (handler != null) { // execute it only if binding is Souliss and update is for my
                                       // Gateway
                    return handler;
                }
            }
        }
        return null;
    }

    private static boolean checkExpectedState(byte itemState, byte expectedState) {
        // if expected state is null than return true. The frame will not requeued
        if (expectedState <= -1) {
            return true;
        }
        return itemState == expectedState;
    }

    private static boolean checkAllsSlotZero(DatagramPacket packet) {
        var bflag = true;
        for (var j = 12; j < packet.getData().length; j++) {
            if ((packet.getData()[j] != 0)) {
                bflag = false;
            }
        }
        return bflag;
    }

    long t = 0;
    long tPrec = 0;

    /**
     * Pop SocketAndPacket from ArrayList PacketList
     */
    @Nullable
    private synchronized PacketStruct pop() {
        synchronized (this) {
            @Nullable
            SoulissGatewayHandler localGwHandler = this.gwHandler;
            if (localGwHandler != null) {
                // non esegue il pop se bPopSuspend=true
                // bPopSuspend è impostato dal metodo put
                if (!bPopSuspend) {
                    t = System.currentTimeMillis();
                    // riporta l'intervallo al minimo solo se:
                    // - la lunghezza della coda minore o uguale a 1;
                    // - se è trascorso il tempo SEND_DELAY.

                    if (packetsList.size() <= 1) {
                        iDelay = sendMinDelay;
                    } else {
                        iDelay = localGwHandler.gwConfig.sendInterval;

                    }

                    var iPacket = 0;
                    var bFlagWhile = true;
                    // scarta i pacchetti già  inviati
                    while ((iPacket < packetsList.size()) && bFlagWhile) {
                        if (packetsList.get(iPacket).sent) {
                            iPacket++;
                        } else {
                            bFlagWhile = false;
                        }
                    }

                    boolean tFlag = (t - tPrec) >= localGwHandler.gwConfig.sendInterval;

                    // se siamo arrivati alla fine della lista e quindi tutti i
                    // pacchetti sono già  stati inviati allora pongo anche il tFlag
                    // a false (come se il timeout non fosse ancora trascorso)
                    if (iPacket >= packetsList.size()) {
                        tFlag = false;
                    }

                    if ((!packetsList.isEmpty()) && tFlag) {
                        tPrec = System.currentTimeMillis();

                        // estratto il primo elemento della lista
                        PacketStruct sp = packetsList.get(iPacket);

                        // GESTIONE PACCHETTO: eliminato dalla lista oppure
                        // contrassegnato come inviato se è un FORCE
                        if (packetsList.get(iPacket).packet
                                .getData()[7] == SoulissUDPConstants.SOULISS_UDP_FUNCTION_FORCE) {
                            // flag inviato a true
                            packetsList.get(iPacket).setSent(true);
                            // imposto time
                            packetsList.get(iPacket).setTime(System.currentTimeMillis());
                        } else {
                            packetsList.remove(iPacket);
                        }

                        logger.debug("POP: {} packets in memory", packetsList.size());
                        if (logger.isDebugEnabled()) {
                            var iPacketSentCounter = 0;
                            var i = 0;
                            while ((i < packetsList.size())) {
                                if (packetsList.get(i).sent) {
                                    iPacketSentCounter++;
                                }
                                i++;
                            }
                            logger.debug("POP: {}  force frame sent", iPacketSentCounter);
                        }

                        logger.debug("Pop frame {} - Delay for 'SendDispatcherThread' setted to {} mills.",
                                macacoToString(sp.packet.getData()), iDelay);
                        return sp;
                    }
                }
            }
        }
        return null;
    }

    private void resetTime() {
        startTime = System.currentTimeMillis();
    }

    private boolean checkTime() {
        return startTime < (System.currentTimeMillis() - iDelay);
    }
}
