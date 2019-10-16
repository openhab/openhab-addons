/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemm;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmBaseOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmEjpOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmHcOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemmTempoOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIcc;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccBaseOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccEjpOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccHcOption;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccTempoOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLong;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongBaseOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongEjpOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongHcOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongTempoOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmShort;
import org.openhab.binding.teleinfo.internal.reader.common.FrameBaseOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameEjpOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameHcOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameTempoOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameTempoOption.CouleurDemain;
import org.openhab.binding.teleinfo.internal.reader.common.FrameTempoOption.ProgrammeCircuit1;
import org.openhab.binding.teleinfo.internal.reader.common.FrameTempoOption.ProgrammeCircuit2;
import org.openhab.binding.teleinfo.internal.reader.common.Hhphc;
import org.openhab.binding.teleinfo.internal.reader.common.Ptec;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.ConvertionException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.FrameUtil;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.converter.Converter;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.converter.CouleurDemainConverter;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.converter.FloatConverter;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.converter.HhphcConverter;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.converter.IntegerConverter;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.converter.PtecConverter;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.converter.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InputStream for Teleinfo {@link Frame} in serial port format.
 */
/**
 * The {@link TeleinfoInputStream} class is an {@link InputStream} to decode/read Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class TeleinfoInputStream extends InputStream {

    public static long DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME = 33400;
    public static long DEFAULT_TIMEOUT_READING_FRAME = 33400;

    private static final Logger logger = LoggerFactory.getLogger(TeleinfoInputStream.class);
    private static final Map<Class<?>, Converter> LABEL_VALUE_CONVERTERS;

    private BufferedReader bufferedReader = null;
    private InputStream teleinfoInputStream = null;
    private String groupLine = null;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private long waitNextHeaderFrameTimeoutInMs;
    private long readingFrameTimeoutInMs;

    static {
        LABEL_VALUE_CONVERTERS = new HashMap<>();
        LABEL_VALUE_CONVERTERS.put(Integer.class, new IntegerConverter());
        LABEL_VALUE_CONVERTERS.put(String.class, new StringConverter());
        LABEL_VALUE_CONVERTERS.put(Float.class, new FloatConverter());
        LABEL_VALUE_CONVERTERS.put(Ptec.class, new PtecConverter());
        LABEL_VALUE_CONVERTERS.put(Hhphc.class, new HhphcConverter());
        LABEL_VALUE_CONVERTERS.put(CouleurDemain.class, new CouleurDemainConverter());
    }

    public TeleinfoInputStream(final InputStream teleinfoInputStream) {
        this(teleinfoInputStream, DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME, DEFAULT_TIMEOUT_READING_FRAME);
    }

    public TeleinfoInputStream(final InputStream teleinfoInputStream, long waitNextHeaderFrameTimeoutInMs,
            long readingFrameTimeoutInMs) {
        if (teleinfoInputStream == null) {
            throw new IllegalArgumentException("Teleinfo inputStream not null");
        }

        this.waitNextHeaderFrameTimeoutInMs = waitNextHeaderFrameTimeoutInMs;
        this.readingFrameTimeoutInMs = readingFrameTimeoutInMs;

        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(teleinfoInputStream, "ASCII"));
            this.teleinfoInputStream = teleinfoInputStream;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        groupLine = null;
    }

    @Override
    public void close() throws IOException {
        logger.debug("close() [start]");
        bufferedReader.close();
        executorService.shutdownNow();
        super.close();
        logger.debug("close() [end]");
    }

    /**
     * Returns the next frame.
     *
     * @return the next frame or null if end of stream
     * @throws InvalidFrameException if the read data from
     * @throws TimeoutException if the delay to read a complete frame is expired (33,4 ms) or if the delay to find
     *             the
     *             header of next frame is expired (33,4 ms)
     * @throws IOException
     */
    public synchronized Frame readNextFrame() throws InvalidFrameException, TimeoutException, IOException {
        logger.debug("readNextFrame() [start]");

        // seek the next header frame
        Future<Void> seekNextHeaderFrameTask = executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                while (!isHeaderFrame(groupLine)) {
                    groupLine = bufferedReader.readLine();
                    if (logger.isTraceEnabled()) {
                        logger.trace("groupLine = {}", groupLine);
                    }
                    if (groupLine == null) { // end of stream
                        logger.trace("end of stream reached !");
                        return null;
                    }
                }

                logger.trace("header frame found !");
                return null;
            }
        });
        try {
            logger.debug("seeking the next header frame...");
            logger.trace("waitNextHeaderFrameTimeoutInMs = {}", waitNextHeaderFrameTimeoutInMs);
            seekNextHeaderFrameTask.get(Long.MAX_VALUE, TimeUnit.MICROSECONDS); // FIXME

            if (groupLine == null) { // end of stream
                return null;
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            rethrowTaskExecutionException(e);
            return null; // FIXME best way ?
        }

        Future<Map<Label, Object>> nextFrameFuture = executorService.submit(new Callable<Map<Label, Object>>() {
            @Override
            public Map<Label, Object> call() throws Exception {
                // read label values
                Map<Label, Object> frameValues = new HashMap<>();
                while ((groupLine = bufferedReader.readLine()) != null && !isHeaderFrame(groupLine)) {
                    logger.trace("groupLine = {}", groupLine);

                    String[] groupLineTokens = groupLine.split("\\s");
                    if (groupLineTokens.length != 2 && groupLineTokens.length != 3) {
                        final String error = String.format("The groupLine '%1$s' is incomplete", groupLine);
                        throw new InvalidFrameException(error);
                    }
                    String labelStr = groupLineTokens[0];
                    String valueString = groupLineTokens[1];

                    // verify integrity (through checksum)
                    char checksum = (groupLineTokens.length == 3 ? groupLineTokens[2].charAt(0) : ' ');
                    char computedChecksum = FrameUtil.computeGroupLineChecksum(labelStr, valueString);
                    if (computedChecksum != checksum) {
                        logger.trace("computedChecksum = {}", computedChecksum);
                        logger.trace("checksum = {}", checksum);
                        final String error = String.format(
                                "The groupLine '%s' is corrupted (integrity not checked). Actual checksum: '%s' / Expected checksum: '%s'",
                                groupLine, checksum, computedChecksum);
                        throw new InvalidFrameException(error);
                    }

                    final Label label;
                    try {
                        label = Label.valueOf(labelStr);
                    } catch (IllegalArgumentException e) {
                        final String error = String.format("The label '%s' is unknown", labelStr);
                        throw new InvalidFrameException(error);
                    }

                    Class<?> labelType = label.getType();
                    Converter converter = LABEL_VALUE_CONVERTERS.get(labelType);
                    if (converter == null) {
                        final String error = String.format("No converter founded for '%s' label type", labelType);
                        throw new IllegalStateException(error);
                    }
                    try {
                        Object value = converter.convert(valueString);

                        frameValues.put(label, value);
                    } catch (ConvertionException e) {
                        final String error = String.format("An error occurred during '%s' value conversion",
                                valueString);
                        throw new InvalidFrameException(error, e);
                    }
                }

                return frameValues;
            }
        });

        try {
            logger.debug("reading data frame...");
            logger.trace("readingFrameTimeoutInMs = {}", readingFrameTimeoutInMs);
            Map<Label, Object> frameValues = nextFrameFuture.get(Long.MAX_VALUE, TimeUnit.MICROSECONDS); // FIXME

            // build the frame from map values
            final Frame frame = buildFrame(frameValues);
            frame.setTimestamp(LocalDate.now());
            frame.setId(UUID.randomUUID());

            logger.debug("readNextFrame() [end]");
            return frame;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            rethrowTaskExecutionException(e);
            return null; // FIXME best way ?
        }
    }

    public long getWaitNextHeaderFrameTimeoutInMs() {
        return waitNextHeaderFrameTimeoutInMs;
    }

    public void setWaitNextHeaderFrameTimeoutInMs(long waitNextHeaderFrameTimeoutInMs) {
        this.waitNextHeaderFrameTimeoutInMs = waitNextHeaderFrameTimeoutInMs;
    }

    public long getReadingFrameTimeoutInMs() {
        return readingFrameTimeoutInMs;
    }

    public void setReadingFrameTimeoutInMs(long readingFrameTimeoutInMs) {
        this.readingFrameTimeoutInMs = readingFrameTimeoutInMs;
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("The 'read()' is not supported");
    }

    private boolean isHeaderFrame(final String line) {
        // A new teleinfo trame begin with '3' and '2' bytes (END OF TEXT et START OF TEXT)
        return (line != null && line.length() > 1 && line.codePointAt(0) == 3 && line.codePointAt(1) == 2);
    }

    private Frame buildFrame(final Map<Label, Object> frameValues) throws InvalidFrameException {
        if (frameValues.containsKey(Label.IINST1)) {
            if (frameValues.containsKey(Label.IMAX1)) {
                return buildFrameCbetmLong(frameValues);
            } else {
                return buildFrameCbetmShort(frameValues);
            }
        } else if (frameValues.containsKey(Label.PAPP)) {
            return buildFrameCbemmEvolutionIcc(frameValues);
        } else {
            return buildFrameCbemm(frameValues);
        }
    }

    private FrameCbetmLong buildFrameCbetmLong(final Map<Label, Object> frameValues) throws InvalidFrameException {
        logger.trace("buildFrameCbetmLong(Map<Label, Object>) [start]");
        final FrameCbetmLong frameCbetm;
        String optionTarif = getRequiredLabelValue(Label.OPTARIF, String.class, frameValues);
        if ("BASE".equals(optionTarif)) {
            frameCbetm = buildFrameCbetmLongBaseOption(frameValues);
        } else if ("HC..".equals(optionTarif)) {
            frameCbetm = buildFrameCbetmLongHcOption(frameValues);
        } else if ("EJP.".equals(optionTarif)) {
            frameCbetm = buildFrameCbetmLongEjpOption(frameValues);
        } else if (optionTarif.startsWith("BBR") && optionTarif.length() == 4) {
            ProgrammeCircuit1 prgCircuit1 = convertProgrammeCircuit1(optionTarif.charAt(3));
            ProgrammeCircuit2 prgCircuit2 = convertProgrammeCircuit2(optionTarif.charAt(3));
            frameCbetm = buildFrameCbetmLongTempoOption(frameValues, prgCircuit1, prgCircuit2);
        } else {
            final String error = String.format("The option Tarif '%s' is not supported", optionTarif);
            throw new InvalidFrameException(error);
        }
        logger.trace("buildFrameCbetmLong(Map<Label, Object>) [end]");
        return frameCbetm;
    }

    private void setCbetmCommonFrameFields(final FrameCbetmLong frame, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("setCbetmCommonFrameFields(Frame, Map<Label, Object>) [start]");
        frame.setAdco(getRequiredLabelValue(Label.ADCO, String.class, frameValues));
        frame.setIsousc(getRequiredLabelValue(Label.ISOUSC, Integer.class, frameValues));
        frame.setIinst1(getRequiredLabelValue(Label.IINST1, Integer.class, frameValues));
        frame.setIinst2(getRequiredLabelValue(Label.IINST2, Integer.class, frameValues));
        frame.setIinst3(getRequiredLabelValue(Label.IINST3, Integer.class, frameValues));
        frame.setImax1(getRequiredLabelValue(Label.IMAX1, Integer.class, frameValues));
        frame.setImax2(getRequiredLabelValue(Label.IMAX2, Integer.class, frameValues));
        frame.setImax3(getRequiredLabelValue(Label.IMAX3, Integer.class, frameValues));
        frame.setPtec(getRequiredLabelValue(Label.PTEC, Ptec.class, frameValues));
        frame.setPmax(getRequiredLabelValue(Label.PMAX, Integer.class, frameValues));
        frame.setPapp(getRequiredLabelValue(Label.PAPP, Integer.class, frameValues));
        frame.setMotdetat(getRequiredLabelValue(Label.MOTDETAT, String.class, frameValues));
        frame.setPpot(getRequiredLabelValue(Label.PPOT, String.class, frameValues));
        logger.trace("setCbetmCommonFrameFields(Frame, Map<Label, Object>) [end]");
    }

    private FrameCbetmLongBaseOption buildFrameCbetmLongBaseOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbetmBaseOption(Map<Label, Object>) [start]");
        FrameCbetmLongBaseOption frame = new FrameCbetmLongBaseOption();
        setCbetmCommonFrameFields(frame, frameValues);
        setFrameBaseOptionFields(frame, frameValues);
        logger.trace("buildFrameCbetmBaseOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbetmLongHcOption buildFrameCbetmLongHcOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbetmHcOption(Map<Label, Object>) [start]");
        FrameCbetmLongHcOption frame = new FrameCbetmLongHcOption();
        setCbetmCommonFrameFields(frame, frameValues);
        setFrameHcOptionFields(frame, frameValues);
        logger.trace("buildFrameCbetmHcOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbetmLongEjpOption buildFrameCbetmLongEjpOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbetmEjpOption(Map<Label, Object>) [start]");
        FrameCbetmLongEjpOption frame = new FrameCbetmLongEjpOption();
        setCbetmCommonFrameFields(frame, frameValues);
        setFrameEjpOptionFields(frame, frameValues);
        logger.trace("buildFrameCbetmEjpOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbetmLongTempoOption buildFrameCbetmLongTempoOption(final Map<Label, Object> frameValues,
            ProgrammeCircuit1 prgCircuit1, ProgrammeCircuit2 prgCircuit2) throws InvalidFrameException {
        logger.trace("buildFrameCbetmTempoOption(Map<Label, Object>) [start]");
        FrameCbetmLongTempoOption frame = new FrameCbetmLongTempoOption();
        setCbetmCommonFrameFields(frame, frameValues);
        setFrameTempoOptionFields(frame, frameValues, prgCircuit1, prgCircuit2);
        logger.trace("buildFrameCbetmTempoOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbetmShort buildFrameCbetmShort(final Map<Label, Object> frameValues) throws InvalidFrameException {
        logger.trace("buildFrameCbetmShort(Map<Label, Object>) [start]");
        FrameCbetmShort frame = new FrameCbetmShort();
        frame.setAdco(getRequiredLabelValue(Label.ADCO, String.class, frameValues));
        frame.setIinst1(getRequiredLabelValue(Label.IINST1, Integer.class, frameValues));
        frame.setIinst2(getRequiredLabelValue(Label.IINST2, Integer.class, frameValues));
        frame.setIinst3(getRequiredLabelValue(Label.IINST3, Integer.class, frameValues));
        frame.setAdir1(getOptionalLabelValue(Label.ADIR1, Integer.class, frameValues));
        frame.setAdir2(getOptionalLabelValue(Label.ADIR2, Integer.class, frameValues));
        frame.setAdir3(getOptionalLabelValue(Label.ADIR3, Integer.class, frameValues));
        logger.trace("buildFrameCbetmShort(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemm buildFrameCbemm(final Map<Label, Object> frameValues) throws InvalidFrameException {
        logger.trace("buildFrameCbemm(Map<Label, Object>) [start]");
        final FrameCbemm frameCbemm;
        String optionTarif = getRequiredLabelValue(Label.OPTARIF, String.class, frameValues);
        if ("BASE".equals(optionTarif)) {
            frameCbemm = buildFrameCbemmBaseOption(frameValues);
        } else if ("HC..".equals(optionTarif)) {
            frameCbemm = buildFrameCbemmHcOption(frameValues);
        } else if ("EJP.".equals(optionTarif)) {
            frameCbemm = buildFrameCbemmEjpOption(frameValues);
        } else if (optionTarif.startsWith("BBR") && optionTarif.length() == 4) {
            ProgrammeCircuit1 prgCircuit1 = convertProgrammeCircuit1(optionTarif.charAt(3));
            ProgrammeCircuit2 prgCircuit2 = convertProgrammeCircuit2(optionTarif.charAt(3));
            frameCbemm = buildFrameCbemmTempoOption(frameValues, prgCircuit1, prgCircuit2);
        } else {
            final String error = String.format("The option Tarif '%s' is not supported", optionTarif);
            throw new InvalidFrameException(error);
        }
        logger.trace("buildFrameCbemm(Map<Label, Object>) [end]");
        return frameCbemm;
    }

    private void setCbemmCommonFrameFields(final FrameCbemm frame, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("setCbemmCommonFrameFields(Frame, Map<Label, Object>) [start]");
        frame.setAdco(getRequiredLabelValue(Label.ADCO, String.class, frameValues));
        frame.setIsousc(getRequiredLabelValue(Label.ISOUSC, Integer.class, frameValues));
        frame.setIinst(getRequiredLabelValue(Label.IINST, Integer.class, frameValues));
        frame.setImax(getOptionalLabelValue(Label.IMAX, Integer.class, frameValues));
        frame.setPtec(getRequiredLabelValue(Label.PTEC, Ptec.class, frameValues));
        frame.setAdps(getOptionalLabelValue(Label.ADPS, Integer.class, frameValues));
        frame.setMotdetat(getRequiredLabelValue(Label.MOTDETAT, String.class, frameValues));
        logger.trace("setCbemmCommonFrameFields(Frame, Map<Label, Object>) [end]");
    }

    private FrameCbemmBaseOption buildFrameCbemmBaseOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmBaseOption(Map<Label, Object>) [start]");
        FrameCbemmBaseOption frame = new FrameCbemmBaseOption();
        setCbemmCommonFrameFields(frame, frameValues);
        setFrameBaseOptionFields(frame, frameValues);
        logger.trace("buildFrameCbemmBaseOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemmHcOption buildFrameCbemmHcOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmHcOption(Map<Label, Object>) [start]");
        FrameCbemmHcOption frame = new FrameCbemmHcOption();
        setCbemmCommonFrameFields(frame, frameValues);
        setFrameHcOptionFields(frame, frameValues);
        logger.trace("buildFrameCbemmHcOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemmEjpOption buildFrameCbemmEjpOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmEjpOption(Map<Label, Object>) [start]");
        FrameCbemmEjpOption frame = new FrameCbemmEjpOption();
        setCbemmCommonFrameFields(frame, frameValues);
        setFrameEjpOptionFields(frame, frameValues);
        logger.trace("buildFrameCbemmEjpOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemmTempoOption buildFrameCbemmTempoOption(final Map<Label, Object> frameValues,
            ProgrammeCircuit1 prgCircuit1, ProgrammeCircuit2 prgCircuit2) throws InvalidFrameException {
        logger.trace("buildFrameCbemmTempoOption(Map<Label, Object>) [start]");
        FrameCbemmTempoOption frame = new FrameCbemmTempoOption();
        setCbemmCommonFrameFields(frame, frameValues);
        setFrameTempoOptionFields(frame, frameValues, prgCircuit1, prgCircuit2);
        logger.trace("buildFrameCbemmTempoOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemmEvolutionIcc buildFrameCbemmEvolutionIcc(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmEvolutionIcc(Map<Label, Object>) [start]");
        final FrameCbemmEvolutionIcc frameCbemmEvoIcc;
        String optionTarif = getRequiredLabelValue(Label.OPTARIF, String.class, frameValues);
        if ("BASE".equals(optionTarif)) {
            frameCbemmEvoIcc = buildFrameCbemmEvolutionIccBaseOption(frameValues);
        } else if ("HC..".equals(optionTarif)) {
            frameCbemmEvoIcc = buildFrameCbemmEvolutionIccHcOption(frameValues);
        } else if ("EJP.".equals(optionTarif)) {
            frameCbemmEvoIcc = buildFrameCbemmEvolutionIccEjpOption(frameValues);
        } else if (optionTarif.startsWith("BBR") && optionTarif.length() == 4) {
            ProgrammeCircuit1 prgCircuit1 = convertProgrammeCircuit1(optionTarif.charAt(3));
            ProgrammeCircuit2 prgCircuit2 = convertProgrammeCircuit2(optionTarif.charAt(3));
            frameCbemmEvoIcc = buildFrameCbemmEvolutionIccTempoOption(frameValues, prgCircuit1, prgCircuit2);
        } else {
            final String error = String.format("The option Tarif '%s' is not supported", optionTarif);
            throw new InvalidFrameException(error);
        }

        logger.trace("buildFrameCbemmEvolutionIcc(Map<Label, Object>) [end]");
        return frameCbemmEvoIcc;
    }

    private void setCbemmEvolutionIccCommonFrameFields(final FrameCbemmEvolutionIcc frame,
            final Map<Label, Object> frameValues) throws InvalidFrameException {
        logger.trace("setCbemmEvolutionIccCommonFrameFields(Frame, Map<Label, Object>) [start]");
        setCbemmCommonFrameFields(frame, frameValues);
        frame.setPapp(getRequiredLabelValue(Label.PAPP, Integer.class, frameValues));
        logger.trace("setCbemmEvolutionIccCommonFrameFields(Frame, Map<Label, Object>) [end]");
    }

    private FrameCbemmEvolutionIccBaseOption buildFrameCbemmEvolutionIccBaseOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmEvolutionIccBaseOption(Map<Label, Object>) [start]");
        FrameCbemmEvolutionIccBaseOption frame = new FrameCbemmEvolutionIccBaseOption();
        setCbemmEvolutionIccCommonFrameFields(frame, frameValues);
        setFrameBaseOptionFields(frame, frameValues);
        logger.trace("buildFrameCbemmEvolutionIccBaseOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemmEvolutionIccHcOption buildFrameCbemmEvolutionIccHcOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmEvolutionIccHcOption(Map<Label, Object>) [start]");
        FrameCbemmEvolutionIccHcOption frame = new FrameCbemmEvolutionIccHcOption();
        setCbemmEvolutionIccCommonFrameFields(frame, frameValues);
        setFrameHcOptionFields(frame, frameValues);
        logger.trace("buildFrameCbemmEvolutionIccHcOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemmEvolutionIccTempoOption buildFrameCbemmEvolutionIccTempoOption(
            final Map<Label, Object> frameValues, ProgrammeCircuit1 prgCircuit1, ProgrammeCircuit2 prgCircuit2)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmEvolutionIccTempoOption(Map<Label, Object>) [start]");
        FrameCbemmEvolutionIccTempoOption frame = new FrameCbemmEvolutionIccTempoOption();
        setCbemmEvolutionIccCommonFrameFields(frame, frameValues);
        setFrameTempoOptionFields(frame, frameValues, prgCircuit1, prgCircuit2);
        logger.trace("buildFrameCbemmEvolutionIccTempoOption(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameCbemmEvolutionIccEjpOption buildFrameCbemmEvolutionIccEjpOption(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("buildFrameCbemmEvolutionIccEjpOption(Map<Label, Object>) [start]");
        FrameCbemmEvolutionIccEjpOption frame = new FrameCbemmEvolutionIccEjpOption();
        setCbemmEvolutionIccCommonFrameFields(frame, frameValues);
        setFrameEjpOptionFields(frame, frameValues);
        logger.trace("buildFrameCbemmEvolutionIccEjpOption(Map<Label, Object>) [end]");
        return frame;
    }

    private void setFrameBaseOptionFields(final FrameBaseOption frameBaseOption, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("setFrameBaseOptionFields(FrameBaseOption) [start]");
        frameBaseOption.setBase(getRequiredLabelValue(Label.BASE, Integer.class, frameValues));
        logger.trace("setFrameBaseOptionFields(FrameBaseOption) [end]");
    }

    private void setFrameHcOptionFields(final FrameHcOption frameHcOption, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("setFrameHcOptionFields(FrameHcOption) [start]");
        frameHcOption.setHchc(getRequiredLabelValue(Label.HCHC, Integer.class, frameValues));
        frameHcOption.setHchp(getRequiredLabelValue(Label.HCHP, Integer.class, frameValues));
        frameHcOption.setHhphc(getRequiredLabelValue(Label.HHPHC, Hhphc.class, frameValues));
        logger.trace("setFrameHcOptionFields(FrameHcOption) [end]");
    }

    private void setFrameEjpOptionFields(final FrameEjpOption frameEjpOption, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        logger.trace("setFrameEjpOptionFields(FrameEjpOption) [start]");
        frameEjpOption.setEjphn(getRequiredLabelValue(Label.EJPHN, Integer.class, frameValues));
        frameEjpOption.setEjphpm(getRequiredLabelValue(Label.EJPHPM, Integer.class, frameValues));
        frameEjpOption.setPejp(getOptionalLabelValue(Label.PEJP, Integer.class, frameValues));
        logger.trace("setFrameEjpOptionFields(FrameEjpOption) [end]");
    }

    private void setFrameTempoOptionFields(final FrameTempoOption frameTempoOption,
            final Map<Label, Object> frameValues, ProgrammeCircuit1 prgCircuit1, ProgrammeCircuit2 prgCircuit2)
            throws InvalidFrameException {
        logger.trace("setFrameTempoOptionFields(FrameTempoOption) [start]");
        frameTempoOption.setBbrhpjr(getRequiredLabelValue(Label.BBRHPJR, Integer.class, frameValues));
        frameTempoOption.setBbrhcjr(getRequiredLabelValue(Label.BBRHCJR, Integer.class, frameValues));
        frameTempoOption.setBbrhpjw(getRequiredLabelValue(Label.BBRHPJW, Integer.class, frameValues));
        frameTempoOption.setBbrhcjw(getRequiredLabelValue(Label.BBRHCJW, Integer.class, frameValues));
        frameTempoOption.setBbrhpjb(getRequiredLabelValue(Label.BBRHPJB, Integer.class, frameValues));
        frameTempoOption.setBbrhcjb(getRequiredLabelValue(Label.BBRHCJB, Integer.class, frameValues));
        frameTempoOption.setDemain(getOptionalLabelValue(Label.DEMAIN, CouleurDemain.class, frameValues));
        frameTempoOption.setHhphc(getRequiredLabelValue(Label.HHPHC, Hhphc.class, frameValues));
        frameTempoOption.setProgrammeCircuit1(prgCircuit1);
        frameTempoOption.setProgrammeCircuit2(prgCircuit2);
        logger.trace("setFrameTempoOptionFields(FrameTempoOption) [end]");
    }

    @SuppressWarnings("unchecked")
    private <T> T getRequiredLabelValue(Label label, Class<T> dataType, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        if (!frameValues.containsKey(label)) {
            final String error = String.format("The required label '%1$s' is missing in frame", label);
            throw new InvalidFrameException(error);
        }

        return (T) frameValues.get(label);
    }

    @SuppressWarnings("unchecked")
    private <T> T getOptionalLabelValue(Label label, Class<T> dataType, final Map<Label, Object> frameValues) {
        return (T) frameValues.get(label);
    }

    private ProgrammeCircuit1 convertProgrammeCircuit1(char value) {
        String prgCircuit1 = computeProgrammeCircuitBinaryValue(value).substring(3, 5);
        switch (prgCircuit1) {
            case "01":
                return ProgrammeCircuit1.A;
            case "10":
                return ProgrammeCircuit1.B;
            case "11":
                return ProgrammeCircuit1.C;
            default:
                final String error = String.format("Programme circuit 1 '%s' is unknown", prgCircuit1);
                throw new IllegalStateException(error);
        }
    }

    private ProgrammeCircuit2 convertProgrammeCircuit2(char value) {
        String prgCircuit2 = computeProgrammeCircuitBinaryValue(value).substring(5, 8);
        switch (prgCircuit2) {
            case "000":
                return ProgrammeCircuit2.P0;
            case "001":
                return ProgrammeCircuit2.P1;
            case "010":
                return ProgrammeCircuit2.P2;
            case "011":
                return ProgrammeCircuit2.P3;
            case "100":
                return ProgrammeCircuit2.P4;
            case "101":
                return ProgrammeCircuit2.P5;
            case "110":
                return ProgrammeCircuit2.P6;
            case "111":
                return ProgrammeCircuit2.P7;
            default:
                final String error = String.format("Programme circuit 2 '%s' is unknown", prgCircuit2);
                throw new IllegalStateException(error);
        }
    }

    private String computeProgrammeCircuitBinaryValue(char value) {
        return StringUtils.leftPad(Integer.toBinaryString(value), 8, "0");
    }

    private Exception rethrowTaskExecutionException(ExecutionException e)
            throws InvalidFrameException, IOException, TimeoutException {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFrameException) {
            throw (InvalidFrameException) cause;
        } else if (cause instanceof IOException) {
            throw (IOException) cause;
        } else if (cause instanceof TimeoutException) {
            throw (TimeoutException) cause;
        } else {
            throw new IllegalStateException(e);
        }
    }
}
