package org.openhab.persistence.influxdb2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.FilterCriteria;

@NonNullByDefault
/**
 * Creates InfluxDB query sentence given a OpenHab persistence {@link FilterCriteria}
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
public interface FilterCriteriaQueryCreator {
    String createQuery(FilterCriteria criteria, String bucket);
}
