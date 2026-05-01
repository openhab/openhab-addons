function app() {
    const getAuthHeaders = () => {
        const token = localStorage.getItem('openhab.homeconnectdirect:token');
        return token ? {'Authorization': 'Bearer ' + token} : {};
    };

    const getHeaders = () => {
        return {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...getAuthHeaders()
        };
    };

    const handleAuthError = (res) => {
        if (res.status === 401) {
            globalThis.location.href = globalThis.servletPath + '/auth/login';
            return true;
        }
        return false;
    };

    const escapeHtml = (unsafe) => {
        if (!unsafe) return "";
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    };

    const syntaxHighlight = (json) => {
        if (!json) return '';
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
            let cls = 'json-number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'json-key';
                } else {
                    cls = 'json-string';
                }
            } else if (/true|false/.test(match)) {
                cls = 'json-boolean';
            } else if (/null/.test(match)) {
                cls = 'json-null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
    };

    const toLocalISOString = (date) => {
        const offset = date.getTimezoneOffset();
        const localDate = new Date(date.getTime() - (offset * 60 * 1000));
        return localDate.toISOString().slice(0, 16);
    };

    const jsonToYaml = (data, indent = 0) => {
        let yaml = '';
        const spaces = ' '.repeat(indent);

        if (typeof data !== 'object' || data === null) {
            return String(data);
        }

        for (const key in data) {
            const value = data[key];
            if (Array.isArray(value)) {
                yaml += `${spaces}${key}:\n`;
                value.forEach(item => {
                    yaml += `${spaces}  - ${item}\n`;
                });
            } else if (typeof value === 'object' && value !== null) {
                if (Object.keys(value).length > 0) {
                    yaml += `${spaces}${key}:\n${jsonToYaml(value, indent + 2)}`;
                }
            } else {
                yaml += `${spaces}${key}: ${String(value)}\n`;
            }
        }
        return yaml;
    };

    const buildFilterParams = (filter, allActions) => {
        const filterToSend = {};
        if (filter.start) filterToSend.start = new Date(filter.start).toISOString();
        if (filter.end) filterToSend.end = new Date(filter.end).toISOString();
        if (filter.type && filter.type.length === 1) {
            filterToSend.type = filter.type[0];
        }
        if (filter.valueKeys && filter.valueKeys.length > 0) filterToSend.valueKeys = filter.valueKeys;
        if (filter.descriptionChangeKeys && filter.descriptionChangeKeys.length > 0) filterToSend.descriptionChangeKeys = filter.descriptionChangeKeys;
        if (filter.resources && filter.resources.length > 0) filterToSend.resources = filter.resources;

        if (filter.actions && filter.actions.length > 0) {
            const actions = Array.isArray(filter.actions)
                ? filter.actions
                : filter.actions.split(',').map(s => s.trim()).filter(s => s);
            if (actions.length < allActions.length) {
                filterToSend.actions = actions;
            }
        }

        if (Object.keys(filterToSend).length > 0) return 'filter=' + encodeURIComponent(JSON.stringify(filterToSend));
        return null;
    };

    const yamlSyntaxHighlight = (yaml) => {
        if (!yaml) return '';

        return yaml.split('\n').map(line => {
            if (!line.trim()) return line;
            if (line.trim().startsWith('#')) return `<span class="yaml-comment">${escapeHtml(line)}</span>`;

            const keyValMatch = line.match(/^(\s*)([\w\-\.]+)(:\s*)(.*)$/);
            if (keyValMatch) {
                const indent = keyValMatch[1];
                const key = keyValMatch[2];
                const sep = keyValMatch[3];
                let val = keyValMatch[4];
                let valHtml = escapeHtml(val);

                if (val) {
                    if (/^'.*'$/.test(val) || /^".*"$/.test(val)) {
                        valHtml = `<span class="yaml-string">${escapeHtml(val)}</span>`;
                    } else if (/^-?\d+(\.\d+)?$/.test(val)) {
                        valHtml = `<span class="yaml-number">${escapeHtml(val)}</span>`;
                    } else if (/^(true|false|yes|no|on|off)$/i.test(val)) {
                        valHtml = `<span class="yaml-boolean">${escapeHtml(val)}</span>`;
                    } else {
                        valHtml = `<span class="yaml-string">${escapeHtml(val)}</span>`;
                    }
                }
                return `${indent}<span class="yaml-key">${escapeHtml(key)}</span>${sep}${valHtml}`;
            }

            const keyMatch = line.match(/^(\s*)([\w\-\.]+)(:)$/);
            if (keyMatch) {
                return `${keyMatch[1]}<span class="yaml-key">${escapeHtml(keyMatch[2])}</span>${keyMatch[3]}`;
            }

            const arrayMatch = line.match(/^(\s*-\s+)(.*)$/);
            if (arrayMatch) {
                const prefix = arrayMatch[1];
                let val = arrayMatch[2];
                let valHtml = escapeHtml(val);
                if (val) {
                    if (/^'.*'$/.test(val) || /^".*"$/.test(val)) {
                        valHtml = `<span class="yaml-string">${escapeHtml(val)}</span>`;
                    } else if (/^-?\d+(\.\d+)?$/.test(val)) {
                        valHtml = `<span class="yaml-number">${escapeHtml(val)}</span>`;
                    } else if (/^(true|false|yes|no|on|off)$/i.test(val)) {
                        valHtml = `<span class="yaml-boolean">${escapeHtml(val)}</span>`;
                    } else {
                        valHtml = `<span class="yaml-string">${escapeHtml(val)}</span>`;
                    }
                }
                return `${escapeHtml(prefix)}${valHtml}`;
            }

            return escapeHtml(line);
        }).join('\n');
    };

    const dslSyntaxHighlight = (dsl) => {
        if (!dsl) return '';
        return dsl.split('\n').map(line => {
            if (!line.trim()) return line;
            if (line.trim().startsWith('//')) return `<span class="dsl-comment">${escapeHtml(line)}</span>`;

            let html = escapeHtml(line);
            html = html.replace(/\b(Thing|Type|Channels)\b/g, '<span class="dsl-keyword">$1</span>');
            html = html.replace(/(&quot;.*?&quot;)/g, '<span class="dsl-string">$1</span>');
            html = html.replace(/(=)(\d+)/g, '$1<span class="dsl-number">$2</span>');
            return html;
        }).join('\n');
    };

    return {
        view: 'appliances', // 'appliances', 'appliance', 'profiles', 'logs', 'log-details'
        appliances: [],
        profiles: [],
        logs: [],
        currentAppliance: null,
        messages: [],
        availableResources: [],
        availableValueKeys: [],
        availableDescriptionChangeKeys: [],
        inputValueKey: '',
        inputResource: '',
        inputDescriptionChangeKey: '',
        valueKeySuggestions: [],
        resourceSuggestions: [],
        descriptionChangeKeySuggestions: [],
        showValueKeySuggestions: false,
        showResourceSuggestions: false,
        showDescriptionChangeKeySuggestions: false,
        showUploadModal: false,
        toasts: [],
        showDeleteModal: false,
        showDeleteLogModal: false,
        logToDelete: null,
        showImportLogModal: false,
        importLogError: null,
        importingLog: false,
        showImportProxyLogModal: false,
        importProxyLogError: null,
        importingProxyLog: false,
        showSendMessageModal: false,
        sendMessageForm: {
            action: 'GET',
            resource: '',
            data: '',
            version: null
        },
        sendMessageError: null,
        sendingMessage: false,
        showConfigurationModal: false,
        currentConfigurationCode: '',
        configurationKey: '',
        configurationAttribute: null,
        showDescriptionAttributeModal: false,
        selectedDescriptionKey: '',
        descriptionAttributes: [],
        configMode: 'YAML',
        applianceYaml: '',
        applianceDsl: '',
        configurationChannelId: '',
        configurationLabel: '',
        configurationType: 'String',
        configurationUnit: '',
        configurationEnumValues: [],
        configurationOnValues: [],
        currentLog: null,
        showDeviceDescriptionModal: false,
        currentDeviceDescription: null,
        currentDeviceDescriptionMeta: null,
        activeDeviceDescriptionTab: '',
        deviceDescriptionSearch: '',
        currentSearchMatchIndex: 0,
        totalSearchMatches: 0,
        currentEnumerationType: null,
        showValueModal: false,
        currentValueMeta: null,
        showSourceModal: false,
        currentMessage: null,
        showSidebar: false,
        profileToDelete: null,
        uploading: false,
        uploadError: null,
        socket: null,
        connected: false,
        pingInterval: null,
        servletPath: globalThis.servletPath,
        loginEnabled: globalThis.loginEnabled,
        bindingDeviceId: globalThis.bindingDeviceId,
        allActions: ['NOTIFY', 'GET', 'POST', 'RESPONSE'],
        specialResources: ['/ci/registeredDevices', '/ci/tzInfo', '/ci/info', '/ni/config', '/ci/services', '/ni/info', '/ei/initialValues'],
        locale: 'en',
        allTypes: ['INCOMING', 'OUTGOING'],
        filter: {
            start: '',
            end: '',
            valueKeys: [],
            descriptionChangeKeys: [],
            resources: [],
            actions: ['NOTIFY', 'GET', 'POST', 'RESPONSE'],
            type: ['INCOMING', 'OUTGOING']
        },
        currentPage: 1,
        pageSize: 50,
        messageQueueSize: globalThis.messageQueueSize || 200,
        hideUnchangedDescriptions: false,
        inputHideUnchangedDescriptions: false,

        router: {
            _component: null,

            init(component) {
                this._component = component;
                this.parseUrlAndSetState();
                globalThis.addEventListener('popstate', () => this.parseUrlAndSetState());
            },

            parseUrlAndSetState() {
                const path = globalThis.location.pathname;
                const basePath = this._component.servletPath;

                let relativePath = path.startsWith(basePath) ? path.substring(basePath.length) : path;
                if (relativePath.startsWith('/')) relativePath = relativePath.substring(1);

                // Disconnect WebSocket if changing appliance or view
                if (this._component.socket) {
                    this._component.socket.close();
                    this._component.socket = null; // Clear socket reference
                    this._component.connected = false;
                }
                if (this._component.pingInterval) {
                    clearInterval(this._component.pingInterval);
                    this._component.pingInterval = null;
                }

                if (relativePath.startsWith('appliances/')) {
                    const uid = relativePath.substring('appliances/'.length);
                    this._component.view = 'appliance';
                    let appliance = this._component.appliances.find(a => a.uid === uid);

                    if (appliance) {
                        this._component.currentAppliance = appliance;
                        this._component.connectWs(appliance.uid);
                        this._component.availableResources = [];
                        this._component.availableValueKeys = [];
                        this._component.availableDescriptionChangeKeys = [];
                    } else if (this._component.appliances.length === 0) {
                        // appliances not yet loaded, watch for them
                        let executed = false;
                        let unwatch = this._component.$watch('appliances', () => {
                            if (executed) return;
                            executed = true;

                            appliance = this._component.appliances.find(a => a.uid === uid);
                            if (appliance) {
                                this._component.currentAppliance = appliance;
                                this._component.connectWs(appliance.uid);
                                this._component.availableResources = [];
                                this._component.availableValueKeys = [];
                                this._component.availableDescriptionChangeKeys = [];
                            } else {
                                // appliance not found after load, redirect to dashboard
                                this.replace('/appliances');
                            }
                            if (typeof unwatch === 'function') unwatch(); // stop watching after first load
                        });
                    } else {
                        // appliances loaded but specific UID not found, redirect to dashboard
                        this.replace('/appliances');
                    }
                } else if (relativePath === 'profiles') {
                    this._component.fetchProfiles();
                    this._component.view = 'profiles';
                    this._component.currentAppliance = null;
                } else if (relativePath === 'logs') {
                    this._component.fetchLogs();
                    this._component.view = 'logs';
                    this._component.currentAppliance = null;
                    this._component.resetFilter();
                } else if (relativePath.startsWith('logs/')) {
                    const fileId = relativePath.substring('logs/'.length);
                    this._component.currentLog = {id: fileId};
                    this._component.fetchLogs();
                    this._component.fetchMessages(fileId);
                    this._component.view = 'log-details';
                    this._component.currentAppliance = null;
                } else if (relativePath === 'appliances') {
                    this._component.fetchAppliances();
                    this._component.view = 'appliances';
                    this._component.currentAppliance = null;
                    this._component.resetFilter();
                } else {
                    // default view if path is unknown or empty (e.g., / or /homeconnectdirect)
                    this._component.fetchAppliances();
                    this._component.view = 'appliances';
                    this._component.currentAppliance = null;
                    this.replace('/appliances');
                }
            },

            // navigates to a new path and adds it to history
            push(path, state = {}) {
                const fullPath = this._component.servletPath + path;
                if (globalThis.location.pathname !== fullPath) {
                    history.pushState(state, '', fullPath);
                }
                this.parseUrlAndSetState();
            },

            // navigates to a new path and replaces current history entry
            replace(path, state = {}) {
                const fullPath = this._component.servletPath + path;
                history.replaceState(state, '', fullPath);
                this.parseUrlAndSetState();
            },

            // go back/forward in browser history
            go(delta) {
                history.go(delta);
            }
        },

        init() {
            this.fetchAppliances();
            this.fetchProfiles();
            this.router.init(this);
            setInterval(() => this.updateAppliances(), 10000);
            this.$watch('filter.actions', (value) => {
                if (value.length === 0) {
                    this.filter.actions = [...this.allActions];
                }
            });
            this.$watch('filter.type', (value) => {
                if (value.length === 0) {
                    this.filter.type = [...this.allTypes];
                }
            });
            this.$watch('availableValueKeys', () => this.updateSuggestions('valueKeys', this.inputValueKey));
            this.$watch('availableDescriptionChangeKeys', () => this.updateSuggestions('descriptionChangeKeys', this.inputDescriptionChangeKey));
            this.$watch('availableResources', () => this.updateSuggestions('resources', this.inputResource));
            this.$watch('currentPage', () => {
                const container = document.querySelector('.value-table-container');
                if (container) container.scrollTop = 0;
            });
            this.$watch('deviceDescriptionSearch', () => setTimeout(() => this.updateSearchMatches(), 100));
            this.$watch('activeDeviceDescriptionTab', () => setTimeout(() => this.updateSearchMatches(), 100));
        },

        // --- Navigation ---

        selectAppliance(app) {
            this.router.push(`/appliances/${app.uid}`);
        },

        openLog(file) {
            this.router.push(`/logs/${file.id}`);
        },

        navigateToProfiles() {
            this.router.push('/profiles');
        },

        navigateToLogs() {
            this.router.push('/logs');
        },

        navigateToAppliances() {
            this.router.push('/appliances');
        },

        // --- API & Data Fetching ---

        async fetchAppliances() {
            try {
                const res = await fetch(this.servletPath + globalThis.apiAppliancesPath, {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();

                    if (this.currentAppliance) {
                        const updatedIndex = data.data.findIndex(a => a.uid === this.currentAppliance.uid);
                        if (updatedIndex !== -1) {
                            Object.assign(this.currentAppliance, data.data[updatedIndex]);
                            data.data[updatedIndex] = this.currentAppliance;
                        }
                    }
                    this.appliances = data.data;
                }
            } catch (e) {
                console.error(e);
            }
        },

        async updateAppliances() {
            try {
                const res = await fetch(this.servletPath + globalThis.apiAppliancesPath, {
                    headers: getHeaders()
                });
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();
                    const newAppliances = data.data;

                    // Update existing appliances and add new ones
                    newAppliances.forEach(newApp => {
                        const existingApp = this.appliances.find(a => a.uid === newApp.uid);
                        if (existingApp) {
                            const wasOffline = existingApp.status !== 'ONLINE';
                            Object.assign(existingApp, newApp);

                            if (this.currentAppliance && this.currentAppliance.uid === existingApp.uid &&
                                wasOffline && existingApp.status === 'ONLINE') {
                                this.connectWs(existingApp.uid);
                            }
                        } else {
                            this.appliances.push(newApp);
                        }
                    });

                    // Remove appliances that are no longer present
                    for (let i = this.appliances.length - 1; i >= 0; i--) {
                        if (!newAppliances.find(na => na.uid === this.appliances[i].uid)) {
                            this.appliances.splice(i, 1);
                        }
                    }
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchProfiles() {
            try {
                const res = await fetch(globalThis.servletPath + globalThis.apiProfilesPath, {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();
                    this.profiles = data.data;
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchLogs() {
            try {
                const res = await fetch(this.servletPath + globalThis.apiLogsPath, {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();
                    this.logs = data.data;
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchMessages(fileId) {
            try {
                let url = this.servletPath + globalThis.apiLogsPath + '/' + encodeURIComponent(fileId) + '/messages';
                const filterParams = buildFilterParams(this.filter, this.allActions);
                if (filterParams) {
                    url += '?' + filterParams;
                }
                const res = await fetch(url, {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();
                    this.messages = data.data.map(m => {
                        m.id = m.id || (Date.now() + Math.random());
                        m.isNew = false;
                        return m;
                    });
                    this.currentPage = 1;
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchResources(uidOrFileId) {
            const baseUrl = this.view === 'log-details' ? (globalThis.apiLogsPath + '/') : (globalThis.apiMessagesPath + '/');
            const postfix = '/resources';
            try {
                const res = await fetch(this.servletPath + baseUrl + encodeURIComponent(uidOrFileId) + postfix,
                    {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();
                    this.availableResources = data.data;
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchValueKeys(uidOrFileId) {
            const baseUrl = this.view === 'log-details' ? (globalThis.apiLogsPath + '/') : (globalThis.apiMessagesPath + '/');
            const postfix = '/value-keys';
            try {
                const res = await fetch(this.servletPath + baseUrl + encodeURIComponent(uidOrFileId) + postfix,
                    {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();
                    this.availableValueKeys = data.data;
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchDescriptionChangeKeys(uidOrFileId) {
            const baseUrl = this.view === 'log-details' ? (globalThis.apiLogsPath + '/') : (globalThis.apiMessagesPath + '/');
            const postfix = '/description-change-keys';
            try {
                const res = await fetch(this.servletPath + baseUrl + encodeURIComponent(uidOrFileId) + postfix,
                    {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const data = await res.json();
                    this.availableDescriptionChangeKeys = data.data;
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchFullDeviceDescription() {
            let url;
            if (this.view === 'appliance' && this.currentAppliance) {
                url = `${this.servletPath}${globalThis.apiAppliancesPath}/${encodeURIComponent(this.currentAppliance.uid)}/device-description`;
            } else if (this.view === 'log-details' && this.currentLog) {
                url = `${this.servletPath}${globalThis.apiLogsPath}/${encodeURIComponent(this.currentLog.id)}/device-description`;
            } else {
                return;
            }

            try {
                const res = await fetch(url, {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    this.currentDeviceDescription = await res.json();
                    this.currentDeviceDescriptionMeta = null;
                    this.currentEnumerationType = null;
                    this.deviceDescriptionSearch = '';
                    this.currentSearchMatchIndex = 0;
                    this.totalSearchMatches = 0;

                    const keys = ['statusList', 'settingList', 'eventList', 'commandList', 'optionList', 'programGroup', 'activeProgram', 'selectedProgram', 'protectionPort', 'enumerationTypeList'];
                    this.activeDeviceDescriptionTab = keys.find(k => this.currentDeviceDescription[k]) || 'statusList';

                    this.showDeviceDescriptionModal = true;
                } else {
                    const error = await res.json();
                    this.addToast({message: error.message || 'Failed to load device description', type: 'error'});
                }
            } catch (e) {
                console.error(e);
                this.addToast({message: 'Error loading device description', type: 'error'});
            }
        },

        async openDeviceDescriptionModal(description) {
            this.currentDeviceDescriptionMeta = description;
            this.currentDeviceDescription = description.object;
            this.deviceDescriptionSearch = '';
            this.currentSearchMatchIndex = 0;
            this.totalSearchMatches = 0;
            this.currentEnumerationType = null;
            this.showDeviceDescriptionModal = true;

            if (this.currentDeviceDescription && this.currentDeviceDescription.enumerationType !== undefined && this.currentDeviceDescription.enumerationType !== null) {
                await this.fetchEnumerationType(this.currentDeviceDescription.enumerationType);
            }
        },

        async fetchEnumerationType(enumId) {
            let url;
            if (this.view === 'appliance' && this.currentAppliance) {
                url = `${this.servletPath}${globalThis.apiAppliancesPath}/${encodeURIComponent(this.currentAppliance.uid)}/device-description?uid=${enumId}&type=ENUMERATION_TYPE`;
            } else if (this.view === 'log-details' && this.currentLog) {
                url = `${this.servletPath}${globalThis.apiLogsPath}/${encodeURIComponent(this.currentLog.id)}/device-description?uid=${enumId}&type=ENUMERATION_TYPE`;
            } else {
                return;
            }

            try {
                const res = await fetch(url, {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    this.currentEnumerationType = await res.json();
                }
            } catch (e) {
                console.error(e);
            }
        },

        openValueModal(value) {
            this.currentValueMeta = value;
            this.showValueModal = true;
        },

        openSendMessageModal() {
            this.sendMessageForm = {
                action: 'GET',
                resource: '',
                data: '',
                version: null
            };
            this.sendMessageError = null;
            this.showSendMessageModal = true;
            if (this.currentAppliance) {
                this.fetchResources(this.currentAppliance.uid);
            }
        },

        async sendMessage() {
            this.sendMessageError = null;
            let data = null;
            if (this.sendMessageForm.data && this.sendMessageForm.data.trim() !== '') {
                try {
                    data = JSON.parse(this.sendMessageForm.data);
                } catch (e) {
                    this.sendMessageError = 'Invalid JSON data';
                    return;
                }
            }

            this.sendingMessage = true;
            try {
                const payload = {
                    action: this.sendMessageForm.action,
                    resource: this.sendMessageForm.resource,
                    data: data,
                    version: this.sendMessageForm.version
                };

                const res = await fetch(this.servletPath + globalThis.apiMessagesPath + '/' + encodeURIComponent(this.currentAppliance.uid), {
                    method: 'POST',
                    body: JSON.stringify(payload),
                    headers: getHeaders()
                });

                if (handleAuthError(res)) return;

                if (res.ok) {
                    this.showSendMessageModal = false;
                    this.addToast({message: 'Message sent successfully', type: 'success'});
                } else {
                    let errorMsg = 'Failed to send message';
                    try {
                        const body = await res.json();
                        if (body.error) errorMsg = body.error;
                    } catch (e) {}
                    this.sendMessageError = errorMsg;
                }
            } catch (e) {
                this.sendMessageError = 'Error: ' + e;
            } finally {
                this.sendingMessage = false;
            }
        },

        // --- WebSocket ---

        connectWs(uid) {
            if (this.socket) {
                this.socket.close();
                this.socket = null;
            }
            if (this.pingInterval) {
                clearInterval(this.pingInterval);
                this.pingInterval = null;
            }
            this.messages = [];
            this.currentPage = 1;
            this.connected = false;

            const appliance = this.appliances.find(a => a.uid === uid);
            if (!appliance) {
                return;
            }

            const protocol = globalThis.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const host = globalThis.location.host;
            let wsUrl = `${protocol}//${host}${this.servletPath}/ws/${encodeURIComponent(uid)}`;
            const params = new URLSearchParams();

            const token = localStorage.getItem('openhab.homeconnectdirect:token');
            if (token) {
                params.append('token', token);
            }

            const filterParams = buildFilterParams(this.filter, this.allActions);
            if (filterParams) wsUrl += `?${filterParams}`;
            const tokenStr = params.toString();
            if (tokenStr) wsUrl += (filterParams ? '&' : '?') + tokenStr;

            console.log("Connecting to WS:", wsUrl);

            this.socket = new WebSocket(wsUrl);

            this.socket.onmessage = (e) => {
                if (e.data === 'PONG') return;
                try {
                    const msg = JSON.parse(e.data);
                    msg.id = msg.id || (Date.now() + Math.random());
                    msg.isNew = true;
                    this.messages.unshift(msg);
                    if (this.messages.length > this.messageQueueSize) this.messages.pop();
                    setTimeout(() => {
                        msg.isNew = false;
                    }, 3000);
                } catch (err) {
                    console.error("Error parsing WS message", err);
                }
            };

            this.socket.onopen = () => {
                console.log("WS Connected (%s)", uid);
                this.connected = true;
                this.pingInterval = setInterval(() => {
                    if (this.socket && this.socket.readyState === WebSocket.OPEN) this.socket.send('PING');
                }, 30000);
            };

            this.socket.onclose = () => {
                console.log("WS Closed (%s)", uid);
                this.connected = false;
            };
        },

        // --- Pagination ---

        getPaginatedMessages() {
            const start = (this.currentPage - 1) * this.pageSize;
            const end = start + this.pageSize;
            return this.messages.slice(start, end);
        },

        totalPages() {
            return Math.ceil(this.messages.length / this.pageSize);
        },

        nextPage() { if (this.currentPage < this.totalPages()) this.currentPage++; },
        prevPage() { if (this.currentPage > 1) this.currentPage--; },

        // --- Filtering & Search ---

        applyFilter() {
            this.hideUnchangedDescriptions = this.inputHideUnchangedDescriptions;
            if (this.view === 'appliance' && this.currentAppliance) {
                this.connectWs(this.currentAppliance.uid);
            } else if (this.view === 'log-details' && this.currentLog) {
                this.fetchMessages(this.currentLog.id);
            }
        },

        setFilterRange(minutes) {
            const end = new Date();
            const start = new Date(end.getTime() - minutes * 60 * 1000);
            this.filter.end = toLocalISOString(end);
            this.filter.start = toLocalISOString(start);
        },

        resetFilterRange() {
            this.filter.end = null;
            this.filter.start = null;
        },

        resetFilter() {
            this.filter.start = '';
            this.filter.end = '';
            this.filter.valueKeys = [];
            this.filter.descriptionChangeKeys = [];
            this.filter.resources = [];
            this.filter.actions = [...this.allActions];
            this.filter.type = [...this.allTypes];
            this.inputValueKey = '';
            this.inputResource = '';
            this.inputDescriptionChangeKey = '';
        },

        updateSuggestions(type, inputValue) {
            let allOptions;
            let targetProp;
            if (type === 'valueKeys') {
                allOptions = this.availableValueKeys;
                targetProp = 'valueKeySuggestions';
            } else if (type === 'descriptionChangeKeys') {
                allOptions = this.availableDescriptionChangeKeys;
                targetProp = 'descriptionChangeKeySuggestions';
            } else {
                allOptions = this.availableResources;
                targetProp = 'resourceSuggestions';
            }
            const selectedValues = this.filter[type];

            if (!allOptions) {
                this[targetProp] = [];
                return;
            }

            const search = inputValue ? inputValue.trim().toLowerCase() : '';

            this[targetProp] = allOptions.filter(opt => {
                if (selectedValues.includes(opt)) return false;
                if (search === '') return true;
                return opt.toLowerCase().includes(search);
            });
        },

        selectSuggestion(type, suggestion) {
            this.addTag(type, suggestion);
            if (type === 'valueKeys') {
                this.showValueKeySuggestions = false;
            } else if (type === 'descriptionChangeKeys') {
                this.showDescriptionChangeKeySuggestions = false;
            } else {
                this.showResourceSuggestions = false;
            }
        },

        addTag(type, value, applyImmediately = false) {
            const val = value ? value.trim() : '';
            if (val && !this.filter[type].includes(val)) {
                this.filter[type].push(val);
            }
            if (type === 'valueKeys') {
                this.inputValueKey = '';
            } else if (type === 'descriptionChangeKeys') {
                this.inputDescriptionChangeKey = '';
            } else this.inputResource = '';

            this.updateSuggestions(type, '');

            if (applyImmediately) {
                this.applyFilter();
            }
        },

        removeTag(type, index) {
            this.filter[type].splice(index, 1);
            this.updateSuggestions(type, '');
        },

        removeTagByValue(type, value, applyImmediately = false) {
            const index = this.filter[type].indexOf(value);
            if (index > -1) {
                this.filter[type].splice(index, 1);
            }
            this.updateSuggestions(type, '');

            if (applyImmediately) {
                this.applyFilter();
            }
        },

        removeLastTag(type) {
            let inputVal;
            if (type === 'valueKeys') {
                inputVal = this.inputValueKey;
            } else if (type === 'descriptionChangeKeys') {
                inputVal = this.inputDescriptionChangeKey;
            } else {
                inputVal = this.inputResource;
            }
            if (inputVal === '' && this.filter[type].length > 0) {
                this.filter[type].pop();
            }
        },

        yamlSyntaxHighlight,
        dslSyntaxHighlight,

        // --- UI Helpers & Formatting ---

        formatDateTime(timestamp) {
            return new Intl.DateTimeFormat(this.locale, {
                dateStyle: 'medium',
                timeStyle: 'medium'
            }).format(new Date(timestamp))
        },

        truncateStart(text, maxLength = 50) {
            if (text === null || text === undefined) return '';
            const str = String(text);
            if (str.length <= maxLength) return str;
            return '...' + str.substring(str.length - maxLength);
        },

        formatFileSize(bytes) {
            if (bytes === 0) return '0 B';
            const k = 1024;
            const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
        },

        statusClass(status) {
            if (status === 'ONLINE') return 'bg-success';
            if (status === 'OFFLINE') return 'bg-danger';
            return 'bg-secondary';
        },

        isOnline(appliance) {
            return appliance && appliance.status === 'ONLINE';
        },

        actionClass(action) {
            if (action === 'NOTIFY') return 'bg-warning';
            if (action === 'GET' || action === 'POST') return 'bg-success';
            return 'bg-secondary';
        },

        sessionColor(sessionId) {
            if (!sessionId) return '';
            let hash = 0;
            const str = String(sessionId);
            for (let i = 0; i < str.length; i++) {
                hash = str.charCodeAt(i) + ((hash << 5) - hash);
            }
            const colors = ['#F0F0FB', '#FBEDF4', '#FDF1B4'];
            const color = colors[Math.abs(hash) % colors.length];
            return `background-color: ${color}; color: gray;`;
        },

        typeIcon(type) {
            return type === 'INCOMING' ? '←' : '→';
        },

        formatCamelCase(text) {
            if (!text) return '';
            return text.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase()).trim();
        },

        highlightMatch(text, type) {
            if (!text) return '';
            let safeText = escapeHtml(text);
            const filters = this.filter[type];

            if (!filters || filters.length === 0) return safeText;

            const escapeRegExp = (string) => string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
            const pattern = filters.map(escapeRegExp).join('|');

            try {
                const regex = new RegExp(`(${pattern})`, 'gi');
                return safeText.replace(regex, '<mark>$1</mark>');
            } catch (e) {
                return safeText;
            }
        },

        formatDescriptionDetails(description, asHtml) {
            if (!description) return '';
            const excludedKeys = ['key', 'uid', 'parentUid', 'type', 'changes', 'object'];
            let parts = Object.entries(description)
                .filter(([key, value]) => !excludedKeys.includes(key) && value !== null && value !== undefined)
                .map(([key, value]) => {
                    if (asHtml) {
                        return `<span class="description-inline-key">${key}</span>: ${value}`;
                    }
                    return `${key}: ${value}`;
                });

            return parts.join(', ');
        },

        getFilteredDescriptionEntries(description) {
            if (!description) return [];
            const excludedKeys = ['key', 'uid', 'parentUid', 'parentKey', 'parentType', 'changes', 'type', 'object', 'value'];
            return Object.entries(description)
                .filter(([key, value]) => !excludedKeys.includes(key) && value !== null && value !== undefined);
        },

        copyToClipboard(text) {
            if (!text) return;
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(text)
                    .then(() => this.addToast({message: 'Copied to clipboard', type: 'success', duration: 2000}))
                    .catch(err => {
                        console.error('Failed to copy:', err);
                        this.addToast({message: 'Failed to copy', type: 'error'});
                    });
            } else {
                try {
                    const textArea = document.createElement("textarea");
                    textArea.value = text;
                    textArea.style.position = "fixed";
                    textArea.style.left = "-9999px";
                    document.body.appendChild(textArea);
                    textArea.focus();
                    textArea.select();
                    document.execCommand('copy');
                    document.body.removeChild(textArea);
                    this.addToast({message: 'Copied to clipboard', type: 'success', duration: 2000});
                } catch (err) {
                    console.error('Fallback copy failed:', err);
                    this.addToast({message: 'Failed to copy', type: 'error'});
                }
            }
        },

        copySource(source) {
            const content = typeof source === 'string' ? source : JSON.stringify(source, null, 2);
            this.copyToClipboard(content);
        },

        openDescriptionAttributeSelectionModal(description) {
            this.selectedDescriptionKey = description.key;
            this.descriptionAttributes = [];

            const obj = description.object;
            if (obj) {
                if (obj.available !== undefined && obj.available !== null) this.descriptionAttributes.push('available');
                if (obj.access !== undefined && obj.access !== null) this.descriptionAttributes.push('access');
                if (obj.min !== undefined && obj.min !== null) this.descriptionAttributes.push('min');
                if (obj.max !== undefined && obj.max !== null) this.descriptionAttributes.push('max');
                if (obj.stepSize !== undefined && obj.stepSize !== null) this.descriptionAttributes.push('stepSize');
                if (obj.enumerationType !== undefined && obj.enumerationType !== null) this.descriptionAttributes.push('enumerationType');
                if (obj.enumerationTypeKey !== undefined && obj.enumerationTypeKey !== null) this.descriptionAttributes.push('enumerationTypeKey');
            } else {
                this.descriptionAttributes = ['available', 'access'];
            }

            this.showDescriptionAttributeModal = true;
        },

        selectDescriptionAttribute(attribute) {
            this.showDescriptionAttributeModal = false;
            this.openDescriptionChangeConfigurationModal(this.selectedDescriptionKey, attribute);
        },

        async openConfigurationModal(key, value, contentType, type, uid, enumerationType) {
            this.configurationKey = key;
            this.configurationAttribute = null;
            this.configurationEnumValues = [];
            this.configurationOnValues = [];
            const lastPart = key.includes('.') ? key.split('.').pop() : key;

            this.configurationLabel = lastPart.replace(/([a-z0-9])([A-Z])/g, '$1 $2').trim();
            this.configurationChannelId = lastPart
                .replace(/([a-z0-9])([A-Z])/g, '$1-$2') // CamelCase to kebab-case
                .replace(/_/g, '-') // Underscores to hyphens
                .toLowerCase()
                .replace(/[^a-z0-9-]/g, ''); // Remove invalid chars

            this.configurationType = type === 'EVENT' ? 'trigger' : 'string';
            this.configurationUnit = '';

            if (this.configurationType !== 'trigger') {
                if (contentType) {
                    switch (contentType) {
                        case 'BOOLEAN':
                        case 'boolean':
                            this.configurationType = 'switch';
                            break;
                        case 'ENUMERATION':
                            // Will be resolved after loading enum values
                            this.configurationType = 'string';
                            break;
                        case 'INTEGER':
                        case 'FLOAT':
                        case 'BIG_INTEGER':
                        case 'BYTE_LENGTH':
                        case 'DBM':
                        case 'LIQUID_VOLUME':
                        case 'WATER_HARDNESS':
                        case 'RPM':
                        case 'FLOW_RATE':
                        case 'LENGTH':
                        case 'AREA':
                        case 'POWER':
                        case 'ENERGY':
                        case 'SPEED':
                            this.configurationType = 'number';
                            break;
                        case 'PERCENT':
                            this.configurationType = 'number';
                            this.configurationUnit = '%%';
                            break;
                        case 'WEIGHT':
                            this.configurationType = 'number';
                            this.configurationUnit = 'g';
                            break;
                        case 'TEMPERATURE_CELSIUS':
                            this.configurationType = 'number';
                            this.configurationUnit = '°C';
                            break;
                        case 'TEMPERATURE_FAHRENHEIT':
                            this.configurationType = 'number';
                            this.configurationUnit = '°F';
                            break;
                        case 'TIME_SPAN':
                            this.configurationType = 'number';
                            this.configurationUnit = 's';
                            break;
                    }
                } else if (value !== null && value !== undefined) {
                    if (typeof value === 'number') {
                        this.configurationType = 'number';
                        if (key.includes('Time') || key.includes('Duration')) {
                            this.configurationUnit = 's';
                        } else if (key.includes('Progress')) {
                            this.configurationUnit = '%%';
                        }
                    } else if (typeof value === 'boolean') this.configurationType = 'switch';
                }
            }

            // Load enum values for ENUMERATION contentType
            if (contentType === 'ENUMERATION' && enumerationType && this.currentAppliance) {
                await this.loadEnumValuesForConfiguration(enumerationType);
            }

            this.updateConfigurationCode();
            this.showConfigurationModal = true;
            await this.fetchApplianceConfiguration();
        },

        async openDescriptionChangeConfigurationModal(key, attribute) {
            this.configurationKey = key;
            this.configurationAttribute = attribute;

            const lastPart = key.includes('.') ? key.split('.').pop() : key;
            const attrLabel = attribute.charAt(0).toUpperCase() + attribute.slice(1);

            this.configurationLabel = (lastPart.replace(/([a-z0-9])([A-Z])/g, '$1 $2').trim() + ' ' + attrLabel).trim();
            this.configurationChannelId = (lastPart + '-' + attribute)
                .replace(/([a-z0-9])([A-Z])/g, '$1-$2') // CamelCase to kebab-case
                .replace(/_/g, '-') // Underscores to hyphens
                .toLowerCase()
                .replace(/[^a-z0-9-]/g, ''); // Remove invalid chars

            this.configurationUnit = '';

            switch (attribute) {
                case 'min':
                case 'max':
                case 'stepSize':
                case 'enumerationType':
                    this.configurationType = 'number';
                    break;
                case 'available':
                    this.configurationType = 'switch';
                    break;
                case 'access':
                case 'enumerationTypeKey':
                    this.configurationType = 'string';
                    break;
                default:
                    this.configurationType = 'string';
            }

            this.updateConfigurationCode();
            this.showConfigurationModal = true;
            await this.fetchApplianceConfiguration();
        },

        async fetchApplianceConfiguration() {
            this.applianceYaml = '';
            if (this.currentAppliance) {
                this.applianceYaml = 'Loading...';
                try {
                    const res = await fetch(this.servletPath + globalThis.apiAppliancesPath + '/' + encodeURIComponent(this.currentAppliance.uid) + '/yaml', {
                        headers: getHeaders()
                    });
                    if (handleAuthError(res)) return;
                    if (res.ok) {
                        const json = await res.json();
                        this.applianceYaml = jsonToYaml(json);
                    } else {
                        this.applianceYaml = '# Failed to load configuration';
                    }

                    const resDsl = await fetch(this.servletPath + globalThis.apiAppliancesPath + '/' + encodeURIComponent(this.currentAppliance.uid) + '/dsl', {
                        headers: getHeaders()
                    });
                    if (handleAuthError(resDsl)) return;
                    if (resDsl.ok) {
                        const json = await resDsl.json();
                        this.applianceDsl = json.code;
                    } else {
                        this.applianceDsl = '// Failed to load configuration';
                    }
                } catch (e) {
                    console.error(e);
                    this.applianceYaml = '# Error loading configuration';
                }
            }
        },

        async loadEnumValuesForConfiguration(enumerationTypeId) {
            const ON_MAPPINGS = ['On', 'Present', 'Confirmed', 'Open'];
            try {
                const url = `${this.servletPath}${globalThis.apiAppliancesPath}/${encodeURIComponent(this.currentAppliance.uid)}/device-description?uid=${enumerationTypeId}&type=ENUMERATION_TYPE`;
                const res = await fetch(url, {headers: getHeaders()});
                if (handleAuthError(res)) return;
                if (res.ok) {
                    const enumType = await res.json();
                    if (enumType && enumType.enumerations) {
                        this.configurationEnumValues = enumType.enumerations.map(e => e.valueKey);

                        const hasOnMapping = this.configurationEnumValues.some(v => ON_MAPPINGS.includes(v));
                        if (hasOnMapping) {
                            this.configurationType = 'enum-switch';
                            this.configurationOnValues = this.configurationEnumValues.filter(v => ON_MAPPINGS.includes(v));
                        }
                    }
                }
            } catch (e) {
                console.error('Failed to load enumeration type for configuration', e);
            }
        },

        toggleEnumOnValue(enumVal) {
            const index = this.configurationOnValues.indexOf(enumVal);
            if (index >= 0 && this.configurationOnValues.length > 1) {
                this.configurationOnValues.splice(index, 1);
            } else if (index < 0) {
                this.configurationOnValues.push(enumVal);
            }
            this.updateConfigurationCode();
        },

        updateConfigurationCode() {
            let code = '';
            let typeId = this.configurationType;

            // Initialize ON values when switching to enum-switch manually
            if (this.configurationType === 'enum-switch' && this.configurationOnValues.length === 0 && this.configurationEnumValues.length > 0) {
                const ON_MAPPINGS = ['On', 'Present', 'Confirmed', 'Open'];
                const matched = this.configurationEnumValues.filter(v => ON_MAPPINGS.includes(v));
                this.configurationOnValues = matched.length > 0 ? matched : [this.configurationEnumValues[0]];
            }

            if (this.configurationAttribute) {
                typeId = `device-description-${this.configurationType}`;
            }

            if (this.configMode === 'YAML') {
                code = `      ${this.configurationChannelId}:\n`;
                code += `        type: ${typeId}\n`;
                code += `        label: ${this.configurationLabel}\n`;
                code += `        config:\n`;
                if (this.configurationAttribute) {
                    code += `          descriptionKey: ${this.configurationKey}\n`;
                    code += `          attribute: ${this.configurationAttribute}`;
                } else {
                    code += `          valueKey: ${this.configurationKey}`;
                    if (this.configurationType === 'number' && this.configurationUnit) {
                        code += `\n          unit: '${this.configurationUnit}'`;
                    }
                    if (this.configurationType === 'enum-switch' && this.configurationOnValues.length > 0) {
                        code += `\n          onValue: ${this.configurationOnValues.join(',')}`;
                    }
                }
            } else {
                code = `    Type ${typeId} : ${this.configurationChannelId}`;
                if (['number', 'string', 'boolean', 'switch', 'trigger', 'enum-switch'].includes(this.configurationType)) {
                    code += ` "${this.configurationLabel}"`;
                }
                if (this.configurationAttribute) {
                    code += ` [ descriptionKey="${this.configurationKey}", attribute="${this.configurationAttribute}" ]`;
                } else {
                    code += ` [ valueKey="${this.configurationKey}"`;
                    if (this.configurationType === 'number' && this.configurationUnit) {
                        code += `, unit="${this.configurationUnit}"`;
                    }
                    if (this.configurationType === 'enum-switch' && this.configurationOnValues.length > 0) {
                        code += `, onValue="${this.configurationOnValues.join(',')}"`;
                    }
                    code += ` ]`;
                }
            }

            this.currentConfigurationCode = code;
        },

        getFilteredRawPayloadEntries(payload) {
            if (!payload) return [];
            const excludedKeys = ['uid', 'parentUID', 'value'];
            return Object.entries(payload)
                .filter(([key, value]) => !excludedKeys.includes(key) && value !== null && value !== undefined);
        },

        // --- Modals & Toasts ---

        openSourceModal(msg) {
            this.currentMessage = msg;
            this.showSourceModal = true;
        },

        animateModal(el, open) {
            if (!el) return;
            const animationDuration = 200;
            if (el._animationTimeout) clearTimeout(el._animationTimeout);

            if (open) {
                if (!el.open) el.showModal();
                el.classList.remove('modal-is-closing');
                el.classList.add('modal-is-opening');
                el._animationTimeout = setTimeout(() => {
                    el.classList.remove('modal-is-opening');
                }, animationDuration);
            } else {
                if (!el.open) return;
                el.classList.remove('modal-is-opening');
                el.classList.add('modal-is-closing');
                el._animationTimeout = setTimeout(() => {
                    if (el.open) el.close();
                    el.classList.remove('modal-is-closing');
                }, animationDuration);
            }
        },

        getHighlightedSourceContent(source, searchTerm) {
            const content = JSON.stringify(source, null, 2);
            let html = syntaxHighlight(content);
            if (searchTerm && searchTerm.trim().length > 0) {
                const term = escapeHtml(searchTerm.trim());
                // Escape regex characters in the search term
                const escapedTerm = term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
                try {
                    // Match the term only if it's not inside an HTML tag (lookahead for > without <)
                    const regex = new RegExp(`(${escapedTerm})(?![^<]*>)`, 'gi');
                    html = html.replace(regex, '<mark>$1</mark>');
                } catch (e) { /* ignore regex errors */ }
            }
            return html;
        },

        updateSearchMatches() {
            const modal = document.querySelector('.full-width-modal');
            if (!modal) return;
            const marks = modal.querySelectorAll('mark');
            this.totalSearchMatches = marks.length;
            if (this.totalSearchMatches > 0) {
                this.currentSearchMatchIndex = 1;
                this.scrollToMatch(0);
            } else {
                this.currentSearchMatchIndex = 0;
            }
        },

        nextSearchMatch() {
            if (this.totalSearchMatches === 0) return;
            if (this.currentSearchMatchIndex < this.totalSearchMatches) {
                this.currentSearchMatchIndex++;
            } else {
                this.currentSearchMatchIndex = 1;
            }
            this.scrollToMatch(this.currentSearchMatchIndex - 1);
        },

        prevSearchMatch() {
            if (this.totalSearchMatches === 0) return;
            if (this.currentSearchMatchIndex > 1) {
                this.currentSearchMatchIndex--;
            } else {
                this.currentSearchMatchIndex = this.totalSearchMatches;
            }
            this.scrollToMatch(this.currentSearchMatchIndex - 1);
        },

        scrollToMatch(index) {
            const modal = document.querySelector('.full-width-modal');
            if (!modal) return;
            const marks = modal.querySelectorAll('mark');
            if (marks[index]) {
                marks.forEach(m => m.classList.remove('active-match'));
                marks[index].classList.add('active-match');
                marks[index].scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        },

        addToast(toast) {
            toast.id = Date.now() + Math.random();
            this.toasts.push(toast);
            setTimeout(() => this.removeToast(toast.id), toast.duration || 4000);
        },

        removeToast(id) {
            const index = this.toasts.findIndex(t => t.id === id);
            if (index > -1) {
                this.toasts.splice(index, 1);
            }
        },

        // --- Profile Operations ---

        getProfile(haId) {
            if (!this.profiles) return null;
            return this.profiles.find(p => p.haId === haId);
        },

        askDeleteProfile(haId) {
            this.profileToDelete = haId;
            this.showDeleteModal = true;
        },

        async deleteProfile() {
            if (!this.profileToDelete) return;

            const res = await fetch(this.servletPath + globalThis.apiProfilesPath + '/' + encodeURIComponent(this.profileToDelete), {
                method: 'DELETE',
                headers: getHeaders()
            });
            if (handleAuthError(res)) return;
            if (res.ok) {
                this.fetchProfiles();
                this.showDeleteModal = false;
                this.profileToDelete = null;
            } else {
                alert('Failed to delete profile');
            }
        },

        async uploadProfile() {
            this.uploadError = null;
            const file = this.$refs.fileInput.files[0];
            if (!file) return;

            this.uploading = true;
            const formData = new FormData();
            formData.append('zipFile', file);

            try {
                const res = await fetch(this.servletPath + globalThis.apiProfilesPath, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Accept': 'application/json',
                        ...getAuthHeaders()
                    }
                });
                if (handleAuthError(res)) return;
                if (res.ok) {
                    this.showUploadModal = false;
                    this.fetchProfiles();
                    this.$refs.fileInput.value = '';
                    this.addToast({message: 'Profile uploaded successfully', type: 'success'});
                } else {
                    let errorMsg = 'Upload failed';
                    try {
                        const data = await res.json();
                        if (data.error) errorMsg = data.error;
                    } catch (e) { /* ignore */ }
                    this.uploadError = errorMsg;
                }
            } catch (e) {
                this.uploadError = 'Upload error: ' + e;
            } finally {
                this.uploading = false;
            }
        },

        async downloadProfile(haId) {
            try {
                const res = await fetch(this.servletPath + globalThis.apiProfilesPath + '/' + encodeURIComponent(haId), {
                    headers: {
                        'Accept': 'application/zip',
                        ...getAuthHeaders()
                    }
                });

                if (handleAuthError(res)) return;
                if (res.ok) {
                    const blob = await res.blob();
                    const url = globalThis.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;

                    const contentDisposition = res.headers.get('Content-Disposition');
                    let fileName = 'profile-' + haId + '.zip';
                    if (contentDisposition) {
                        const match = contentDisposition.match(/filename="?([^"]+)"?/);
                        if (match && match[1]) fileName = match[1];
                    }

                    a.download = fileName;
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    globalThis.URL.revokeObjectURL(url);
                } else {
                    alert('Download failed');
                }
            } catch (e) {
                console.error(e);
                alert('Download error');
            }
        },

        // --- Log Operations ---

        async downloadLogs(fileId) {
            try {
                const res = await fetch(this.servletPath + globalThis.apiLogsPath + '/' + encodeURIComponent(fileId), {
                    headers: {
                        'Accept': 'application/zip',
                        ...getAuthHeaders()
                    }
                });

                if (handleAuthError(res)) return;
                if (res.ok) {
                    const blob = await res.blob();
                    const url = globalThis.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;

                    const contentDisposition = res.headers.get('Content-Disposition');
                    let fileName = 'logs-' + fileId + '.zip';
                    if (contentDisposition) {
                        const match = contentDisposition.match(/filename="?([^"]+)"?/);
                        if (match && match[1]) fileName = match[1];
                    }

                    a.download = fileName;
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    globalThis.URL.revokeObjectURL(url);
                } else {
                    this.addToast({message: 'Download failed', type: 'error'});
                }
            } catch (e) {
                console.error(e);
                this.addToast({message: 'Download error', type: 'error'});
            }
        },

        askDeleteLog(fileId) {
            this.logToDelete = fileId;
            this.showDeleteLogModal = true;
        },

        async deleteLog() {
            if (!this.logToDelete) return;

            try {
                const res = await fetch(this.servletPath + globalThis.apiLogsPath + '/' + encodeURIComponent(this.logToDelete), {
                    method: 'DELETE',
                    headers: getHeaders()
                });

                if (handleAuthError(res)) return;

                if (res.ok) {
                    this.fetchLogs();
                    this.showDeleteLogModal = false;
                    this.logToDelete = null;
                    this.addToast({message: 'Log file deleted successfully', type: 'success'});
                } else {
                    this.addToast({message: 'Failed to delete log file', type: 'error'});
                }
            } catch (e) {
                console.error(e);
                this.addToast({message: 'Error deleting log file', type: 'error'});
            }
        },

        async importLog() {
            this.importLogError = null;
            const file = this.$refs.logFileInput.files[0];
            if (!file) return;

            this.importingLog = true;
            const formData = new FormData();
            formData.append('zipFile', file);

            try {
                const res = await fetch(this.servletPath + globalThis.apiLogsPath, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Accept': 'application/json',
                        ...getAuthHeaders()
                    }
                });
                if (handleAuthError(res)) return;
                if (res.ok) {
                    this.showImportLogModal = false;
                    this.fetchLogs();
                    this.$refs.logFileInput.value = '';
                    this.addToast({message: 'Log imported successfully', type: 'success'});
                } else {
                    let errorMsg = 'Import failed';
                    try {
                        const data = await res.json();
                        if (data.error) errorMsg = data.error;
                    } catch (e) { /* ignore */ }
                    this.importLogError = errorMsg;
                }
            } catch (e) {
                this.importLogError = 'Import error: ' + e;
            } finally {
                this.importingLog = false;
            }
        },

        async importProxyLog() {
            this.importProxyLogError = null;
            const jsonFile = this.$refs.proxyJsonFile.files[0];
            const deviceDescFile = this.$refs.proxyDeviceDescFile.files[0];
            const featureMapFile = this.$refs.proxyFeatureMapFile.files[0];

            if (!jsonFile || !deviceDescFile || !featureMapFile) {
                this.importProxyLogError = 'Please select all 3 required files.';
                return;
            }

            // Basic validation of file types
            if (!jsonFile.name.endsWith('.json')) {
                this.importProxyLogError = 'Invalid JSON file selected.';
                return;
            }
            if (!deviceDescFile.name.endsWith('_DeviceDescription.xml')) {
                this.importProxyLogError = 'Invalid Device Description file selected (must end with _DeviceDescription.xml).';
                return;
            }
            if (!featureMapFile.name.endsWith('_FeatureMapping.xml')) {
                this.importProxyLogError = 'Invalid Feature Mapping file selected (must end with _FeatureMapping.xml).';
                return;
            }

            this.importingProxyLog = true;
            const formData = new FormData();
            formData.append('proxyFiles', jsonFile);
            formData.append('proxyFiles', deviceDescFile);
            formData.append('proxyFiles', featureMapFile);

            try {
                const res = await fetch(this.servletPath + globalThis.apiLogsPath, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Accept': 'application/json',
                        ...getAuthHeaders()
                    }
                });
                if (handleAuthError(res)) return;
                if (res.ok) {
                    this.showImportProxyLogModal = false;
                    this.fetchLogs();
                    this.$refs.proxyJsonFile.value = '';
                    this.$refs.proxyDeviceDescFile.value = '';
                    this.$refs.proxyFeatureMapFile.value = '';
                    this.addToast({message: 'Proxy log imported successfully', type: 'success'});
                } else {
                    let errorMsg = 'Import failed';
                    try {
                        const data = await res.json();
                        if (data.error) errorMsg = data.error;
                    } catch (e) { /* ignore */ }
                    this.importProxyLogError = errorMsg;
                }
            } catch (e) {
                this.importProxyLogError = 'Import error: ' + e;
            } finally {
                this.importingProxyLog = false;
            }
        },

        async downloadLog(uid, persist = false) {
            try {
                const headers = {
                    'Accept': 'application/zip',
                    ...getAuthHeaders()
                };

                if (persist) {
                    headers['Prefer'] = 'handling=persist';
                }

                const res = await fetch(this.servletPath + globalThis.apiMessagesPath + '/' + encodeURIComponent(uid), {
                    headers: headers
                });

                if (handleAuthError(res)) return;

                if (res.ok) {
                    if (persist) {
                        this.addToast({message: 'Messages persisted successfully.', type: 'success'});
                    } else {
                        const blob = await res.blob();
                        const url = globalThis.URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = url;

                        const contentDisposition = res.headers.get('Content-Disposition');
                        let fileName = 'log-' + uid + '.zip.log';
                        if (contentDisposition) {
                            const match = contentDisposition.match(/filename="?([^"]+)"?/);
                            if (match && match[1]) fileName = match[1];
                        }

                        a.download = fileName;
                        document.body.appendChild(a);
                        a.click();
                        a.remove();
                        globalThis.URL.revokeObjectURL(url);
                    }
                } else {
                    this.addToast({message: 'Action failed: ' + res.status, type: 'error', duration: 5000});
                }
            } catch (e) {
                console.error(e);
                this.addToast({message: 'An error occurred during the action.', type: 'error', duration: 5000});
            }
        },

        // --- Auth ---

        async logout() {
            const token = localStorage.getItem('openhab.homeconnectdirect:token');
            if (token) {
                try {
                    await fetch(this.servletPath + '/auth/logout', {
                        method: 'POST',
                        headers: getHeaders()
                    });
                } catch (e) {
                    console.error(e);
                }
            }
            localStorage.removeItem('openhab.homeconnectdirect:token');
            globalThis.location.href = this.servletPath + '/auth/login';
        }
    }
}

async function login(e) {
    e.preventDefault();
    const password = document.getElementById('password').value;
    const csrfTokenInput = document.getElementById('csrf_token');
    const errorMsg = document.getElementById('error-msg');

    try {
        const res = await fetch(globalThis.servletPath + '/auth/token', {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
            body: JSON.stringify({
                password: password,
                _csrf: csrfTokenInput.value
            })
        });

        if (res.ok) {
            const data = await res.json();
            localStorage.setItem('openhab.homeconnectdirect:token', data.token);
            globalThis.location.href = globalThis.servletPath;
        } else {
            const data = await res.json();
            if (data.newCsrfToken) {
                csrfTokenInput.value = data.newCsrfToken;
            }

            if (res.status === 401) {
                errorMsg.textContent = 'Invalid password. Please try again.';
            } else {
                errorMsg.textContent = 'Login expired. Please try again.';
            }
            errorMsg.style.display = 'block';
        }
    } catch (err) {
        console.error(err);
        errorMsg.textContent = 'Error during login. Please try again.';
        errorMsg.style.display = 'block';
    }
}
