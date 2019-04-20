package org.openhab.binding.apcupsd.internal.handler;

public interface NewDataListener {
	void onNewData(String magnitude, String value);
}
