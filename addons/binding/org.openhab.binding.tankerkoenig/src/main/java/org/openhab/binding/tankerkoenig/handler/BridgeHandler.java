package org.openhab.binding.tankerkoenig.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigListResult;
import org.openhab.binding.tankerkoenig.internal.data.TankerkoenigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    String apiKey;
    int refreshInterval;
    boolean setupMode;

    private ArrayList<Thing> tankstellenThingList;
    private HashMap<String, LittleStation> tankstellenList;

    TankerkoenigListResult tankerkoenigListResult;

    private ScheduledFuture<?> pollingJob;
    private TankerkoenigService tankerkoenigService;

    private boolean isValidated;

    public BridgeHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
        this.tankstellenThingList = new ArrayList<Thing>();
        tankstellenList = new HashMap<String, LittleStation>();
        tankerkoenigService = new TankerkoenigService();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        // logger.debug("About to initialize bridge " + BindingConstants.BRIDGE_FRITZBOX);
        @SuppressWarnings("unused")
        Bridge bridge = this.getThing();

        // logger.debug("discovered fritzaha bridge initialized: " + config.toString());

        Configuration config = getThing().getConfiguration();
        this.setApiKey((String) config.get("apikey"));
        this.setRefreshInterval(((BigDecimal) config.get("refresh")).intValue());
        this.setSetupMode((boolean) config.get("setupmode"));
        updateStatus(ThingStatus.ONLINE);

        int pollingPeriod = this.getRefreshInterval();
        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("Try to refresh data");
                // Just update data if setupMode is false(off)
                if (!isSetupMode()) {
                    UpdateTankstellenData();

                    UpdateTankstellenThings();

                }
            }
        }, 15, pollingPeriod * 60, TimeUnit.SECONDS);

        logger.debug("Refresh job scheduled to run every {} min. for '{}'", pollingPeriod, getThing().getUID());

    }

    /***
     *
     * @param tankstelle
     * @return
     */
    public boolean RegisterTankstelleThing(Thing tankstelle) {

        if (this.tankstellenThingList.size() == 10) {
            return false;
        }

        logger.info("Tankstelle " + tankstelle.getUID().toString() + "was registered to config "
                + this.getThing().getUID().toString());

        this.tankstellenThingList.add(tankstelle);

        return true;

    }

    public void UnregisterTankstelleThing(Thing tankstelle) {
        logger.info("Tankstelle " + tankstelle.getUID().toString() + "was unregistered from config "
                + this.getThing().getUID().toString());
        this.tankstellenThingList.remove(tankstelle);

    }

    /***
     * Updates the data from tankerkoenig api (no update on things)
     */
    public void UpdateTankstellenData() {
        // Get data
        String locationIDsString = GenerateLocationIDsString();

        if (locationIDsString.length() < 1) {
            logger.info("No tankstellen id's found. Nothing to update");
            return;
        }

        TankerkoenigService service = new TankerkoenigService();
        TankerkoenigListResult result = service.GetTankstellenListData(this.getApiKey(), locationIDsString);

        this.setTankerkoenigListResult(result);

        this.tankstellenList.clear();
        for (LittleStation station : result.getPrices().getStations()) {
            this.tankstellenList.put(station.getID(), station);
        }

    }

    /***
     * Updates all registered Tankstellen with new data
     */
    public void UpdateTankstellenThings() {

        for (Thing thing : tankstellenThingList) {
            TankerkoenigHandler tkh = (TankerkoenigHandler) thing.getHandler();
            LittleStation s = this.tankstellenList.get(tkh.getLocationID());
            if (s == null) {
                logger.info("Could not find tankstelle with id " + tkh.getLocationID());
                continue;
            }

            tkh.updateData(s);
        }

    }

    /***
     * Generates a comma separated string with all tankstellen id's
     *
     * @return
     */
    private String GenerateLocationIDsString() {
        StringBuilder sb = new StringBuilder();
        for (Thing thing : tankstellenThingList) {
            TankerkoenigHandler tkh = (TankerkoenigHandler) thing.getHandler();
            if (sb.toString().equals("")) {
                sb.append(tkh.getLocationID());
            } else {
                sb.append("," + tkh.getLocationID());
            }
        }

        return sb.toString();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public TankerkoenigListResult getTankerkoenigListResult() {
        return tankerkoenigListResult;
    }

    public void setTankerkoenigListResult(TankerkoenigListResult tankerkoenigListResult) {
        this.tankerkoenigListResult = tankerkoenigListResult;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }

    public boolean isSetupMode() {
        return setupMode;
    }

    public void setSetupMode(boolean setupMode) {
        this.setupMode = setupMode;
    }

}
