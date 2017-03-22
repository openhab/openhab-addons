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

package org.openhab.binding.snapcast.internal.types;

import java.util.ArrayList;

/**
 * Generic POJO for Unmashalling Json Response from Snapcast
 */
public class ServerStatus {
	private String host;
	private int id;
	private ArrayList<Client> clients;
	private Server server;
	private ArrayList<Stream> streams;

	public void setStreams(ArrayList<Stream> streams) {
		this.streams = streams;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public ArrayList<Client> getClients() {
		return clients;
	}

	public void setClients(ArrayList<Client> clients) {
		this.clients = clients;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Stream> getStreams() {
		return streams;
	}

	@Override
	public String toString() {
		return "ServerStatus{}";
	}

	public Server getServer() {
		return server;
	}
}
