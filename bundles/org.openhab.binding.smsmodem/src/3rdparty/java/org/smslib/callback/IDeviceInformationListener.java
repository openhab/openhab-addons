package org.smslib.callback;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IDeviceInformationListener} will receive informations
 * and statistics
 * Extracted from SMSLib
 */
@NonNullByDefault
public interface IDeviceInformationListener {

    void setManufacturer(String manufacturer);

    void setModel(String string);

    void setSwVersion(String swVersion);

    void setSerialNo(String serialNo);

    void setImsi(String imsi);

    void setRssi(String rssi);

    void setMode(String mode);

    void setTotalSent(String totalSent);

    void setTotalFailed(String totalFailed);

    void setTotalReceived(String totalReceived);

    void setTotalFailures(String totalFailure);
}
