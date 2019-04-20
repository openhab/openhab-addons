package org.openhab.binding.apcupsd.internal.handler;

public interface TcpConnectionListener {
	void onConnectionStatusChange(boolean status);
}
