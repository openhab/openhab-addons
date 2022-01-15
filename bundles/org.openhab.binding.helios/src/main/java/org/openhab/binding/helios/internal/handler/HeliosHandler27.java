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
package org.openhab.binding.helios.internal.handler;

import static org.openhab.binding.helios.internal.HeliosBindingConstants.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.wsn.wsdl.WSNWSDLLocator;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.openhab.binding.helios.internal.ws.soap.SOAPActionHandler;
import org.openhab.binding.helios.internal.ws.soap.SOAPCallStateChanged;
import org.openhab.binding.helios.internal.ws.soap.SOAPCardEntered;
import org.openhab.binding.helios.internal.ws.soap.SOAPCodeEntered;
import org.openhab.binding.helios.internal.ws.soap.SOAPDeviceState;
import org.openhab.binding.helios.internal.ws.soap.SOAPEvent;
import org.openhab.binding.helios.internal.ws.soap.SOAPKeyPressed;
import org.openhab.binding.helios.internal.ws.soap.SOAPObjectFactory;
import org.openhab.binding.helios.internal.ws.soap.SOAPSubscriptionActionHandler;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link HeliosHandler27} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
@WebService(endpointInterface = "org.oasis_open.docs.wsn.bw_2.NotificationConsumer")
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class HeliosHandler27 extends BaseThingHandler implements NotificationConsumer {

    // List of Configuration constants
    public static final String IP_ADDRESS = "ipAddress";
    public static final String OPENHAB_IP_ADDRESS = "openHABipAddress";
    public static final String OPENHAB_PORT_NUMBER = "openHABportNumber";

    private final Logger logger = LoggerFactory.getLogger(HeliosHandler27.class);

    private static final String SUBSCRIPTION_PERIOD = "PT1H";

    private JAXBContext context = null;
    private Endpoint endpoint = null;
    private NotificationProducer notificationProducer = null;
    private PausableSubscriptionManager subscription = null;
    private GregorianCalendar currentTime;
    private GregorianCalendar terminationTime;
    private W3CEndpointReference subscriptionReference;
    private String subscriptionID;
    private SOAPEvent previousEvent = null;

    public static final String HELIOS_URI = "http://www.2n.cz/2013/event";
    public static final String DIALECT_URI = "http://www.2n.cz/2013/TopicExpression/Multiple";
    public static final String WSN_URI = "http://docs.oasis-open.org/wsn/b-2";

    public static final QName TOPIC_EXPRESSION = new QName(WSN_URI, "TopicExpression");
    public static final QName INITIAL_TERMINATION_TIME = new QName(WSN_URI, "InitialTerminationTime");
    public static final QName MAXIMUM_NUMBER = new QName(HELIOS_URI, "MaximumNumber");
    public static final QName SIMPLE_MESSAGES = new QName(HELIOS_URI, "SimpleMessages");
    public static final QName START_TIME_STAMP = new QName(HELIOS_URI, "StartTimestamp");
    public static final QName START_RECORD_ID = new QName(HELIOS_URI, "StartRecordId");

    public HeliosHandler27(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            // 2N.cz has not yet released the automation part of the Helios IP HTTP/SOAP based API. Only the
            // notification part has been documented, so for now there is nothing to do
            // here
            logger.debug("The Helios IP is a read-only device and can not handle commands");
        }
    }

    public String getSubscriptionID() {
        return subscriptionID;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void initialize() {
        logger.debug("Initializing Helios IP handler.");
        List<Handler> handlerChain = new ArrayList<>();
        handlerChain.add(new SOAPActionHandler());

        try {
            context = JAXBContext.newInstance(SOAPObjectFactory.class, SOAPEvent.class, SOAPKeyPressed.class,
                    SOAPCallStateChanged.class, SOAPCodeEntered.class, SOAPCardEntered.class, SOAPDeviceState.class);
        } catch (JAXBException e) {
            logger.error("An exception occurred while setting up the JAXB Context factory: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            throw new RuntimeException();
        }

        try {
            if (endpoint == null || (endpoint != null && !endpoint.isPublished())) {
                String address = "http://" + (String) getConfig().get(OPENHAB_IP_ADDRESS) + ":"
                        + ((BigDecimal) getConfig().get(OPENHAB_PORT_NUMBER)).toString() + "/notification"
                        + System.currentTimeMillis();
                logger.debug("Publishing the notification consumer webservice on '{}", address);
                endpoint = Endpoint.publish(address, this);
                ((javax.xml.ws.soap.SOAPBinding) endpoint.getBinding()).setHandlerChain(handlerChain);
            }
        } catch (WebServiceException e1) {
            logger.debug("An exception occurred while setting up the notification consumer webservice: {}",
                    e1.getMessage(), e1);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.getMessage());
            return;
        }

        try {
            String heliosAddress = "http://" + (String) getConfig().get(IP_ADDRESS) + "/notification";
            Service notificationProducerService = Service.create(WSNWSDLLocator.getWSDLUrl(),
                    new QName("http://cxf.apache.org/wsn/jaxws", "NotificationProducerService"));
            notificationProducer = notificationProducerService.getPort(
                    new W3CEndpointReferenceBuilder().address(heliosAddress).build(), NotificationProducer.class);
            ((BindingProvider) notificationProducer).getBinding().setHandlerChain(handlerChain);
        } catch (WebServiceException e1) {
            logger.debug("An exception occurred while setting up the notification webservice client: {}",
                    e1.getMessage(), e1);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.getMessage());
            return;
        }

        try {
            // set up the access to the subscription manager on the Helios IP
            // Vario so that we can renew in the future
            String heliosAddress = "http://" + (String) getConfig().get(IP_ADDRESS) + "/notification";
            Service subscriptionService = Service.create(WSNWSDLLocator.getWSDLUrl(),
                    new QName("http://cxf.apache.org/wsn/jaxws", "PausableSubscriptionManagerService"));
            subscription = subscriptionService.getPort(new W3CEndpointReferenceBuilder().address(heliosAddress).build(),
                    PausableSubscriptionManager.class);

            handlerChain = new ArrayList<>();
            handlerChain.add(new SOAPSubscriptionActionHandler(this));
            ((BindingProvider) subscription).getBinding().setHandlerChain(handlerChain);
        } catch (WebServiceException e1) {
            logger.debug("An exception occurred while setting up the subscription manager client: {}", e1.getMessage(),
                    e1);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.getMessage());
            return;
        }

        try {
            subscribe(endpoint.getEndpointReference(W3CEndpointReference.class));
        } catch (WebServiceException | TopicNotSupportedFault | InvalidFilterFault | TopicExpressionDialectUnknownFault
                | UnacceptableInitialTerminationTimeFault | SubscribeCreationFailedFault
                | InvalidMessageContentExpressionFault | InvalidTopicExpressionFault | UnrecognizedPolicyRequestFault
                | UnsupportedPolicyRequestFault | ResourceUnknownFault | NotifyMessageNotSupportedFault
                | InvalidProducerPropertiesExpressionFault e) {
            logger.debug("An exception occurred while subscribing to the notifications for thing '{}': {}",
                    getThing().getUID(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Helios IP handler.");

        try {
            unsubscribe();
            if (endpoint != null) {
                endpoint.stop();
            }
            endpoint = null;
            terminationTime = null;
            context = null;
            notificationProducer = null;
            subscription = null;
        } catch (Exception e) {
            logger.error("An exception occurred while disposing the Helios Thing Handler : {}", e.getMessage(), e);
        }
    }

    @Override
    public void notify(
            @WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {
        for (Object object : notify.getAny()) {
            try {
                this.processNotification(context.createUnmarshaller().unmarshal((Element) object, SOAPEvent.class));
            } catch (JAXBException e) {
                logger.error("An exception occurred while processing a notification message : {}", e.getMessage(), e);
            }
        }
    }

    public void processNotification(JAXBElement<SOAPEvent> message) {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            SOAPEvent event = message.getValue();
            // WS-Notification does not provide a mechanism to query existing
            // subscriptions established before, so these keep lingering on the
            // remote device. Therefore, when restarting the OH runtime, we
            // might receive events more than one time, we need to filter these
            // out
            if (previousEvent == null || !previousEvent.equals(event)) {
                previousEvent = event;

                Object data = event.getData();

                if (data instanceof SOAPKeyPressed) {
                    StringType valueType = new StringType(((SOAPKeyPressed) data).getKeyCode());
                    updateState(new ChannelUID(getThing().getUID(), KEY_PRESSED), valueType);

                    DateTimeType stampType = new DateTimeType(event.getTimestamp());
                    updateState(new ChannelUID(getThing().getUID(), KEY_PRESSED_STAMP), stampType);
                }

                if (data instanceof SOAPCallStateChanged) {
                    StringType valueType = new StringType(((SOAPCallStateChanged) data).getState());
                    updateState(new ChannelUID(getThing().getUID(), CALL_STATE), valueType);

                    valueType = new StringType(((SOAPCallStateChanged) data).getDirection());
                    updateState(new ChannelUID(getThing().getUID(), CALL_DIRECTION), valueType);

                    DateTimeType stampType = new DateTimeType(event.getTimestamp());
                    updateState(new ChannelUID(getThing().getUID(), CALL_STATE_STAMP), stampType);
                }

                if (data instanceof SOAPCardEntered) {
                    StringType valueType = new StringType(((SOAPCardEntered) data).getCard());
                    updateState(new ChannelUID(getThing().getUID(), CARD), valueType);

                    valueType = new StringType(((SOAPCardEntered) data).getValid());
                    updateState(new ChannelUID(getThing().getUID(), CARD_VALID), valueType);

                    DateTimeType stampType = new DateTimeType(event.getTimestamp());
                    updateState(new ChannelUID(getThing().getUID(), CARD_STAMP), stampType);
                }

                if (data instanceof SOAPCodeEntered) {
                    StringType valueType = new StringType(((SOAPCodeEntered) data).getCode());
                    updateState(new ChannelUID(getThing().getUID(), CODE), valueType);

                    valueType = new StringType(((SOAPCodeEntered) data).getValid());
                    updateState(new ChannelUID(getThing().getUID(), CODE_VALID), valueType);

                    DateTimeType stampType = new DateTimeType(event.getTimestamp());
                    updateState(new ChannelUID(getThing().getUID(), CODE_STAMP), stampType);
                }

                if (data instanceof SOAPDeviceState) {
                    StringType valueType = new StringType(((SOAPDeviceState) data).getState());
                    updateState(new ChannelUID(getThing().getUID(), DEVICE_STATE), valueType);

                    DateTimeType stampType = new DateTimeType(event.getTimestamp());
                    updateState(new ChannelUID(getThing().getUID(), DEVICE_STATE_STAMP), stampType);
                }
            } else {
                logger.warn("Duplicate event received due to lingering subscriptions: '{}':'{}'", event.getEventName(),
                        event.getTimestamp());
            }
        }
    }

    public void renew(String newTerminationTime) throws ResourceUnknownFault, UnacceptableTerminationTimeFault {
        if (subscription != null) {
            Renew renew = new Renew();
            renew.setTerminationTime(newTerminationTime);

            RenewResponse response = subscription.renew(renew);
            currentTime = response.getCurrentTime().toGregorianCalendar();
            terminationTime = response.getTerminationTime().toGregorianCalendar();

            SimpleDateFormat pFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            logger.debug("Renewed the subscription with ID '{}' for '{}' from {} until {}",
                    new Object[] { getSubscriptionID(), getThing().getUID(), pFormatter.format(currentTime.getTime()),
                            pFormatter.format(terminationTime.getTime()) });
        }
    }

    public void unsubscribe() {
        if (subscription != null) {
            try {
                subscription.unsubscribe(new Unsubscribe());
                logger.debug("Unsubscribing the subscription with ID '{}' for '{}' ", getSubscriptionID(),
                        getThing().getUID());
            } catch (UnableToDestroySubscriptionFault | ResourceUnknownFault e) {
                logger.error("An exception occurred while unsubscribing from the subscription : {}", e.getMessage(), e);
            }
        }
    }

    public void subscribe(W3CEndpointReference epr)
            throws TopicNotSupportedFault, InvalidFilterFault, TopicExpressionDialectUnknownFault,
            UnacceptableInitialTerminationTimeFault, SubscribeCreationFailedFault, InvalidMessageContentExpressionFault,
            InvalidTopicExpressionFault, UnrecognizedPolicyRequestFault, UnsupportedPolicyRequestFault,
            ResourceUnknownFault, NotifyMessageNotSupportedFault, InvalidProducerPropertiesExpressionFault {
        if (notificationProducer != null) {
            Subscribe subscribeRequest = new Subscribe();
            subscribeRequest.setConsumerReference(epr);
            subscribeRequest.setFilter(new FilterType());

            TopicExpressionType topicExp = new TopicExpressionType();
            topicExp.getContent().add("");
            topicExp.setDialect(DIALECT_URI);
            subscribeRequest.getFilter().getAny()
                    .add(new JAXBElement<>(TOPIC_EXPRESSION, TopicExpressionType.class, topicExp));

            subscribeRequest.setInitialTerminationTime(
                    new JAXBElement<>(INITIAL_TERMINATION_TIME, String.class, SUBSCRIPTION_PERIOD));

            subscribeRequest.setSubscriptionPolicy(new Subscribe.SubscriptionPolicy());
            subscribeRequest.getSubscriptionPolicy().getAny().add(new JAXBElement<>(MAXIMUM_NUMBER, Integer.class, 1));
            subscribeRequest.getSubscriptionPolicy().getAny().add(new JAXBElement<>(SIMPLE_MESSAGES, Integer.class, 1));
            GregorianCalendar now = new GregorianCalendar();
            SimpleDateFormat pFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            subscribeRequest.getSubscriptionPolicy().getAny()
                    .add(new JAXBElement<>(START_TIME_STAMP, String.class, pFormatter.format(now.getTime())));

            SubscribeResponse response = notificationProducer.subscribe(subscribeRequest);

            if (response != null) {
                currentTime = response.getCurrentTime().toGregorianCalendar();
                terminationTime = response.getTerminationTime().toGregorianCalendar();
                subscriptionReference = response.getSubscriptionReference();

                Element element = DOMUtils.createDocument().createElement("elem");
                subscriptionReference.writeTo(new DOMResult(element));
                NodeList nl = element.getElementsByTagNameNS("http://www.2n.cz/2013/event", "SubscriptionId");
                if (nl != null && nl.getLength() > 0) {
                    Element e = (Element) nl.item(0);
                    subscriptionID = DOMUtils.getContent(e).trim();
                }
                logger.debug("Established a subscription with ID '{}' for '{}' as from {} until {}",
                        new Object[] { subscriptionID, getThing().getUID(), pFormatter.format(currentTime.getTime()),
                                pFormatter.format(terminationTime.getTime()) });

                java.util.Calendar triggerTime = terminationTime;
                triggerTime.add(Calendar.MINUTE, -1);

                logger.debug("Scheduling a renewal of the subscription with ID '{}' for '{}' to happen on {}",
                        new Object[] { subscriptionID, getThing().getUID(), pFormatter.format(triggerTime.getTime()) });
                try {
                    scheduler.schedule(renewRunnable, triggerTime.getTimeInMillis() - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException e) {
                    logger.error("An exception occurred while scheduling a renewal : '{}'", e.getMessage(), e);
                }
            }
        }
    }

    protected Runnable renewRunnable = new Runnable() {

        @Override
        public void run() {
            SimpleDateFormat pFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            boolean result = false;

            try {
                ((HeliosHandler27) getThing().getHandler()).renew(SUBSCRIPTION_PERIOD);
                result = true;
            } catch (Exception e) {
                logger.error("An exception occurred while renewing the subscription : {}", e.getMessage(), e);
                ((HeliosHandler27) getThing().getHandler()).dispose();
                ((HeliosHandler27) getThing().getHandler()).initialize();
                return;
            }

            if (result) {
                java.util.Calendar triggerTime = terminationTime;
                triggerTime.add(Calendar.MINUTE, -1);

                logger.debug("Scheduling a renewal of the subscription with ID '{}' for '{}' to happen on {}",
                        new Object[] { subscriptionID, getThing().getUID(), pFormatter.format(triggerTime.getTime()) });
                try {
                    scheduler.schedule(renewRunnable, triggerTime.getTimeInMillis() - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException e) {
                    logger.error("An exception occurred while scheduling a renewal : '{}'", e.getMessage(), e);
                }
            }
        }
    };
}
