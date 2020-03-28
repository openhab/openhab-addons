package org.openhab.persistence.influxdb2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.FilterCriteria;

@NonNullByDefault
public interface FilterCriteriaQueryCreator {
    String createQuery(FilterCriteria criteria, String bucket);
}
