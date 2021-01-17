package org.openhab.binding.awattar.internal.handler;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.awattar.internal.aWATTarBindingConstants.BINDING_ID;
import static org.openhab.binding.awattar.internal.aWATTarUtil.*;

import java.util.*;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.awattar.internal.aWATTarBridgeConfiguration;
import org.openhab.binding.awattar.internal.aWATTarPrice;
import org.openhab.binding.awattar.internal.connection.aWATTarConnectionException;
import org.openhab.binding.awattar.internal.dto.AwattarApiData;
import org.openhab.binding.awattar.internal.dto.Datum;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link aWATTarBridgeHandler} is responsible for retrieving data from the aWATTar API.
 *
 * The API provides hourly prices for the current day and, starting from 14:00, hourly prices for the next day.
 * Check the documentation at https://www.awattar.de/services/api
 *
 *
 *
 * @author Wolfgang Klimt - Initial contribution
 */
public class aWATTarBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(aWATTarBridgeHandler.class);
    private HttpClient httpClient;
    private @Nullable ScheduledFuture<?> dataRefresher;

    private String URL = "https://api.awattar.de/v1/marketdata";

    // This cache stores price data for up to two days
    private SortedMap<Long, aWATTarPrice> priceMap = null;
    private int dataRefreshInterval = 60;
    private @Nullable aWATTarBridgeConfiguration config = null;
    private double vatFactor = 0;

    private long lastUpdated = 0;

    private double basePrice = 0;
    private long minTimestamp = 0;
    private long maxTimestamp = 0;

    public aWATTarBridgeHandler(Bridge thing, HttpClient httpClient) {
        super(thing);
        logger.trace("Creating aWATTarBridgeHandler instance {}", this);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.trace("Initializing aWATTar bridge {}", this);
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(aWATTarBridgeConfiguration.class);
        vatFactor = 1 + (config.vatPercent / 100);
        basePrice = config.basePrice;

        logger.trace("Start Data refresh job at interval {} seconds.", dataRefreshInterval);
        dataRefresher = scheduler.scheduleAtFixedRate(this::refreshIfNeeded,
                getMillisToNextMinute(dataRefreshInterval / 60), dataRefreshInterval * 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        logger.trace("Disposing aWATTar bridge {}", this);
        dataRefresher.cancel(true);
        dataRefresher = null;
    }

    public void refreshIfNeeded() {
        logger.trace("Refresh if needed called");
        if (needRefresh()) {
            refresh();
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private void getPrices() {
        try {
            GregorianCalendar cal = new GregorianCalendar();

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long start = cal.getTimeInMillis();
            cal.add(Calendar.DATE, 2);
            long end = cal.getTimeInMillis();

            StringBuilder request = new StringBuilder(URL);
            request.append("?start=").append(start).append("&end=").append(end);

            logger.trace("aWATTar API request: = '{}'", request);
            ContentResponse contentResponse = httpClient.newRequest(request.toString()).method(GET)
                    .timeout(10, TimeUnit.SECONDS).send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.trace("aWATTar API response: status = {}, content = '{}'", httpStatus, content);

            switch (httpStatus) {
                case OK_200:
                    Gson gson = new Gson();
                    SortedMap<Long, aWATTarPrice> result = new TreeMap<Long, aWATTarPrice>();
                    minTimestamp = 0;
                    maxTimestamp = 0;
                    AwattarApiData apiData = gson.fromJson(content, AwattarApiData.class);
                    for (Datum d : apiData.data) {
                        result.put(d.startTimestamp,
                                new aWATTarPrice(d.marketprice / 10.0, d.startTimestamp, d.endTimestamp));
                        updateMin(d.startTimestamp);
                        updateMax(d.endTimestamp);
                    }
                    priceMap = result;
                    updateStatus(ThingStatus.ONLINE);
                    break;

                default:
                    logger.warn("aWATTar server responded with status code {}: {}", httpStatus, content);
                    // TODO more infos
                    updateStatus(ThingStatus.OFFLINE);
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            throw new aWATTarConnectionException(errorMessage, e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            logger.info("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new aWATTarConnectionException(e.getLocalizedMessage(), e.getCause());
        }
    }

    private boolean needRefresh() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return true;
        }
        if (priceMap == null) {
            return true;
        }
        if (priceMap.lastKey() < new Date().getTime() + 9 * 3600 * 1000) {
            return true;
        }
        return false;
    }

    private void refresh() {
        logger.trace("Refreshing aWATTar data ...");
        try {
            getPrices();
            lastUpdated = new Date().getTime();
            updateStatus(ThingStatus.ONLINE);
        } catch (aWATTarConnectionException e) {
            logger.error(e.getMessage());
            updateStatus(ThingStatus.OFFLINE);
        } catch (Exception e) {
            logger.error("Unexpected Problem occured: {}", e.getMessage());
            logger.error("Exception: ", e);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    public double getVatFactor() {
        return vatFactor;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public SortedMap<Long, aWATTarPrice> getPriceMap() {
        if (priceMap == null) {
            synchronized (this) {
                refresh();
            }
        }
        return priceMap;
    }

    public aWATTarPrice getPriceFor(long timestamp) {
        SortedMap<Long, aWATTarPrice> priceMap = getPriceMap();
        if (priceMap == null) {
            return null;
        }
        if (!containsPriceFor(timestamp)) {
            return null;
        }
        for (aWATTarPrice price : priceMap.values()) {
            if (timestamp >= price.getStartTimestamp() && timestamp < price.getEndTimestamp()) {
                return price;
            }
        }
        return null;
    }

    public boolean containsPriceFor(long timestamp) {
        return minTimestamp <= timestamp && maxTimestamp >= timestamp;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command {} for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            refresh();
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }

    private void updateMin(long ts) {
        minTimestamp = (minTimestamp == 0) ? ts : Math.min(minTimestamp, ts);
    }

    private void updateMax(long ts) {
        maxTimestamp = (maxTimestamp == 0) ? ts : Math.max(ts, maxTimestamp);
    }
}
