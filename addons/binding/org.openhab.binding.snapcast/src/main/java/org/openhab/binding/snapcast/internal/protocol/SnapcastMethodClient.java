/*
 * Copyright 2017 Steffen Folman Sørensen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.snapcast.internal.protocol;

import java.util.Map;

import org.openhab.binding.snapcast.internal.rpc.JsonRpcNotificationHandler;
import org.openhab.binding.snapcast.internal.types.Client;
import org.openhab.binding.snapcast.internal.types.ClientNotification;

/**
 * Created by wayland on 1/18/17.
 */
abstract public class SnapcastMethodClient extends JsonRpcNotificationHandler<ClientNotification> {
	private final Map<String, Client> clientMap;
	private final SnapcastController controller;

	public SnapcastMethodClient(final String method,
	                            final Map<String,Client> clientMap,
	                            final SnapcastController controller) {
		super(method, ClientNotification.class);
		this.clientMap = clientMap;
		this.controller = controller;
	}

	@Override
	public void handleNotification(ClientNotification notification) {
		final Client client = notification.getClient();
		this.clientMap.put(client.getHost().getMac(), client);
		this.controller.getClient(client.getHost().getMac()).notifyUpdateListeners();
	}
}
