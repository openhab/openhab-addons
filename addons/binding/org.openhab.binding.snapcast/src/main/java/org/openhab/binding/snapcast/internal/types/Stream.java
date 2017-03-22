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

/**
 * Generic POJO for Unmashalling Json Response from Snapcast
 */
public class Stream {
	private String id;
	private String status;
	private StreamUri uri;

	public Stream() {
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setUri(StreamUri uri) {
		this.uri = uri;
	}

	public String getId() {
		return id;
	}

	// @JsonIgnoreProperties({ "host" })
	class StreamUri {

		private String path;
		private String fragment;
		private String raw;
		private String scheme;
		private String host;
		private StreamQuery query;

		public StreamUri() {
		}

		public void setHost(String host) {
			this.host = host;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public void setFragment(String fragment) {
			this.fragment = fragment;
		}

		public void setRaw(String raw) {
			this.raw = raw;
		}

		public void setScheme(String scheme) {
			this.scheme = scheme;
		}

		public void setQuery(StreamQuery query) {
			this.query = query;
		}



	}


}
