/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.facerecognition.handler;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.openhab.binding.facerecognition.FaceRecognitionBindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FaceRecognitionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Philipp Meisberger - Initial contribution
 */
public class FaceRecognitionHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FaceRecognitionHandler.class);
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();
    private final HttpClient httpClient;
    private final ChannelUID imageChannelUID;
    private final ChannelUID accessGrantedImageChannelUID;
    private final ChannelUID accessGrantedUsernameChannelUID;
    private final ChannelUID accessGrantedScoreChannelUID;
    private final ChannelUID accessGrantedTimeChannelUID;
    private final ChannelUID accessDeniedImageChannelUID;
    private final ChannelUID accessDeniedUsernameChannelUID;
    private final ChannelUID accessDeniedScoreChannelUID;
    private final ChannelUID accessDeniedTimeChannelUID;
    private final static String MODELS_FILE = "/var/lib/openhab2/etc/org.openhab.binding.facerecognition.models.xml";
    private final static String FACE_CLASSIFIER_FILE = "/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml";
    private ScheduledFuture<?> job;
    private CascadeClassifier faceCascade;
    private Mat accessDeniedFace;
    private BigDecimal interval;
    private BigDecimal threshold;

    public FaceRecognitionHandler(Thing thing) {
        super(thing);
        httpClient = new HttpClient();
        httpClient.setConnectTimeout(5000);

        // Cache channel UIDs
        imageChannelUID = new ChannelUID(thing.getUID(), CHANNEL_IMAGE);
        accessGrantedImageChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_GRANTED_IMAGE);
        accessGrantedUsernameChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_GRANTED_USERNAME);
        accessGrantedScoreChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_GRANTED_SCORE);
        accessGrantedTimeChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_GRANTED_TIME);
        accessDeniedImageChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_DENIED_IMAGE);
        accessDeniedUsernameChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_DENIED_USERNAME);
        accessDeniedScoreChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_DENIED_SCORE);
        accessDeniedTimeChannelUID = new ChannelUID(thing.getUID(), CHANNEL_ACCESS_DENIED_TIME);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (channelUID.equals(accessDeniedUsernameChannelUID)) {

            // Train unknown face
            if (command.getClass().equals(StringType.class) && !command.toString().isEmpty()
                    && !command.toString().equalsIgnoreCase(UNKNOWN_USER_NAME) && (accessDeniedFace != null)) {

                MatVector images = new MatVector(1);
                images.put(0, accessDeniedFace);

                // Setup label
                Mat labels = new Mat(1, 1, CV_32SC1);
                IntBuffer labelsBuffer = labels.createBuffer();
                int label = command.toString().toLowerCase().hashCode();
                labelsBuffer.put(0, label);

                // Train face
                faceRecognizer.update(images, labels);
                faceRecognizer.setLabelInfo(label, command.toString());
                faceRecognizer.save(MODELS_FILE);
                logger.debug("Trained face of user \"" + command.toString() + "\"");
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing {}::{}", getThing().getLabel(), getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);

        try {
            // Read configuration
            URI camera = new URI((String) getConfig().get("camera"));
            String username = (String) getConfig().get("username");
            String password = (String) getConfig().get("password");
            interval = (BigDecimal) getConfig().get("interval");
            threshold = (BigDecimal) getConfig().get("threshold");

            // Load models file
            File file = new File(MODELS_FILE);

            if (file.exists()) {
                faceRecognizer.load(MODELS_FILE);
            }

            // Load face cascade
            faceCascade = new CascadeClassifier(FACE_CLASSIFIER_FILE);

            // Add credentials for HTTP Digest and Basic authentication
            AuthenticationStore auth = httpClient.getAuthenticationStore();
            auth.clearAuthentications();
            auth.addAuthentication(new BasicAuthentication(camera, Authentication.ANY_REALM, username, password));
            auth.addAuthentication(new DigestAuthentication(camera, Authentication.ANY_REALM, username, password));
            httpClient.start();

            // Start making periodically images
            job = service.scheduleAtFixedRate(() -> snapshot(camera), 0, interval.longValue(), TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing {}::{}", getThing().getLabel(), getThing().getUID());

        if (!job.isDone()) {
            logger.debug("Cancelling snapshot job");
            job.cancel(true);
        }

        try {
            httpClient.stop();

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        faceCascade.close();
    }

    private void snapshot(final URI uri) {

        try {
            // File not found?
            if (faceCascade.empty()) {
                throw new FileNotFoundException("\"" + FACE_CLASSIFIER_FILE + "\" not found!");
            }

            // Try to get image from camera
            Request request = httpClient.newRequest(uri);
            request.accept("image/jpeg");
            ContentResponse response = request.send();

            // Error occurred?
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new HttpResponseException(response.getReason(), response);
            }

            // Get image from HTTP response
            byte[] imageData = response.getContent();

            // Publish raw image?
            if (isLinked(CHANNEL_IMAGE)) {
                updateState(imageChannelUID, new RawType(imageData, "image/jpeg"));
            }

            // Convert image to gray-scale
            Mat imageDataMat = new Mat(imageData);
            Mat grayScaleImage = imdecode(imageDataMat, CV_LOAD_IMAGE_GRAYSCALE);

            // Detect face in image
            RectVector faces = new RectVector();
            faceCascade.detectMultiScale(grayScaleImage, faces);

            // Try to find known face in image
            for (int i = 0; i < faces.size(); i++) {

                // Extract face from image
                Rect faceRect = faces.get(i);
                Mat face = new Mat(grayScaleImage, faceRect);

                IntPointer label = new IntPointer(1);
                DoublePointer score = new DoublePointer(1);

                File file = new File(MODELS_FILE);
                boolean facesTrained = file.exists();

                // Faces trained yet?
                if (facesTrained) {

                    // Try to recognize user
                    faceRecognizer.predict(face, label, score);
                } else {
                    logger.debug("No faces trained yet");

                    // Set default unknown user values
                    label.put(UNKNOWN_USER_LABEL);
                    score.put(Double.MAX_VALUE);
                }

                // User recognized?
                if (facesTrained && (score.get(0) <= threshold.intValue())) {

                    if (isLinked(CHANNEL_ACCESS_GRANTED_IMAGE)) {
                        ByteBuffer outputBuffer = ByteBuffer.allocate(face.arraySize());
                        imencode(".jpg", face, outputBuffer);
                        updateState(accessGrantedImageChannelUID, new RawType(outputBuffer.array(), "image/jpeg"));
                    }

                    if (isLinked(CHANNEL_ACCESS_GRANTED_USERNAME)) {
                        updateState(accessGrantedUsernameChannelUID,
                                new StringType(faceRecognizer.getLabelInfo(label.get(0)).getString()));
                    }

                    if (isLinked(CHANNEL_ACCESS_GRANTED_SCORE)) {
                        updateState(accessGrantedScoreChannelUID, new DecimalType(score.get(0)));
                    }

                    if (isLinked(CHANNEL_ACCESS_GRANTED_TIME)) {
                        updateState(accessGrantedTimeChannelUID, new DateTimeType());
                    }

                } else {

                    if (isLinked(CHANNEL_ACCESS_DENIED_IMAGE)) {
                        accessDeniedFace = new Mat(face);
                        ByteBuffer outputBuffer = ByteBuffer.allocate(accessDeniedFace.arraySize());
                        imencode(".jpg", accessDeniedFace, outputBuffer);
                        updateState(accessDeniedImageChannelUID, new RawType(outputBuffer.array(), "image/jpeg"));
                    }

                    if (isLinked(CHANNEL_ACCESS_DENIED_USERNAME)) {
                        String user;

                        // User not known?
                        if (label.get(0) == UNKNOWN_USER_LABEL) {
                            user = UNKNOWN_USER_NAME;
                        } else {
                            user = faceRecognizer.getLabelInfo(label.get(0)).getString();
                        }

                        updateState(accessDeniedUsernameChannelUID, new StringType(user));
                    }

                    if (isLinked(CHANNEL_ACCESS_DENIED_SCORE)) {
                        updateState(accessDeniedScoreChannelUID, new DecimalType(score.get(0)));
                    }

                    if (isLinked(CHANNEL_ACCESS_DENIED_TIME)) {
                        updateState(accessDeniedTimeChannelUID, new DateTimeType());
                    }
                }
            }

            updateStatus(ThingStatus.ONLINE);

        } catch (Exception e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
}
