/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Stream} is a data structure for the api communication.
 * It represents a snapcast stream.
 *
 * @author Steffen Brandemann - Initial contribution
 */
public class Stream implements Identifiable {

    @SerializedName("id")
    private String id;

    @SerializedName("status")
    private String status;

    @SerializedName("uri")
    private StreamUri uri;

    public Stream() {
    }

    /**
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the uri
     */
    public StreamUri getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(StreamUri uri) {
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Stream other = (Stream) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }

    /**
     * The {@link StreamUri} is a data structure for the api communication.
     * It contains information about the stream uri.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class StreamUri {

        @SerializedName("scheme")
        private String scheme;

        @SerializedName("path")
        private String path;

        @SerializedName("query")
        private StreamQuery query;

        @SerializedName("raw")
        private String raw;

        public StreamUri() {
        }

        /**
         * @return the scheme
         */
        public String getScheme() {
            return scheme;
        }

        /**
         * @param scheme the scheme to set
         */
        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        /**
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * @param path the path to set
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * @return the query
         */
        public StreamQuery getQuery() {
            return query;
        }

        /**
         * @param query the query to set
         */
        public void setQuery(StreamQuery query) {
            this.query = query;
        }

        /**
         * @return the raw
         */
        public String getRaw() {
            return raw;
        }

        /**
         * @param raw the raw to set
         */
        public void setRaw(String raw) {
            this.raw = raw;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            result = prime * result + ((query == null) ? 0 : query.hashCode());
            result = prime * result + ((raw == null) ? 0 : raw.hashCode());
            result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            StreamUri other = (StreamUri) obj;
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            if (query == null) {
                if (other.query != null) {
                    return false;
                }
            } else if (!query.equals(other.query)) {
                return false;
            }
            if (raw == null) {
                if (other.raw != null) {
                    return false;
                }
            } else if (!raw.equals(other.raw)) {
                return false;
            }
            if (scheme == null) {
                if (other.scheme != null) {
                    return false;
                }
            } else if (!scheme.equals(other.scheme)) {
                return false;
            }
            return true;
        }

    }

    /**
     * The {@link StreamQuery} is a data structure for the api communication.
     * It contains information about the stream uri.
     *
     * @author Steffen Brandemann - Initial contribution
     */
    public static class StreamQuery {

        @SerializedName("name")
        private String name;

        @SerializedName("buffer_ms")
        private Integer buffer;

        @SerializedName("codec")
        private String codec;

        @SerializedName("sampleformat")
        private String sampleformat;

        public StreamQuery() {
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the buffer
         */
        public Integer getBuffer() {
            return buffer;
        }

        /**
         * @param buffer the buffer to set
         */
        public void setBuffer(Integer buffer) {
            this.buffer = buffer;
        }

        /**
         * @return the codec
         */
        public String getCodec() {
            return codec;
        }

        /**
         * @param codec the codec to set
         */
        public void setCodec(String codec) {
            this.codec = codec;
        }

        /**
         * @return the sampleformat
         */
        public String getSampleformat() {
            return sampleformat;
        }

        /**
         * @param sampleformat the sampleformat to set
         */
        public void setSampleformat(String sampleformat) {
            this.sampleformat = sampleformat;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((buffer == null) ? 0 : buffer.hashCode());
            result = prime * result + ((codec == null) ? 0 : codec.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((sampleformat == null) ? 0 : sampleformat.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            StreamQuery other = (StreamQuery) obj;
            if (buffer == null) {
                if (other.buffer != null) {
                    return false;
                }
            } else if (!buffer.equals(other.buffer)) {
                return false;
            }
            if (codec == null) {
                if (other.codec != null) {
                    return false;
                }
            } else if (!codec.equals(other.codec)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (sampleformat == null) {
                if (other.sampleformat != null) {
                    return false;
                }
            } else if (!sampleformat.equals(other.sampleformat)) {
                return false;
            }
            return true;
        }

    }

}
