// The Project panel: the bridge (call 2), its keyring (call 7) with an upload (call 19), its project (call 8) with an
// upload (call 17) and Remove (call 18), and the check of channels against the project (call 16). The page changes no
// bridge setting: after a keyring upload it shows the value for keyringFile to copy. A password goes from its field
// into the one call's header and the field is emptied when the call returns.

import { add, attr, clear, el } from '../dom.js';
import { dateText, parseTime } from '../rows.js';
import { errorText, hasText, text } from '../texts.js';
import { copyText } from './objects.js';

const PROJECT_STATES = Object.freeze({
    NONE: 'project.none',
    UNREADABLE: 'project.unreadable',
    NEWER_VERSION: 'project.newer',
});

const INTERFACE_COLUMNS = ['address', 'type', 'host', 'user', 'secured', 'own'];
const CHECK_COLUMNS = ['address', 'kind', 'label', 'configured', 'project'];

function present(value) {
    return value !== undefined && value !== null && value !== '';
}

function named(group, value) {
    return hasText(`${group}.${value}`) ? text(`${group}.${value}`) : String(value ?? '');
}

function count(value) {
    return Number.isFinite(value) ? value : 0;
}

function timeText(value) {
    const date = parseTime(value);
    return date ? dateText(date) : String(value ?? '');
}

/** @returns {{close: () => void}} */
export function show(context) {
    const { document, element, api, rows, bridge, confirm, clipboard } = context;
    element.classList.add('sections');
    add(document, element, el(document, 'h2', {}, text('view.project')));

    const handled = bridge.present === true;
    let closed = false;

    function part(name, titleKey) {
        const body = el(document, 'div', { class: 'body' }, el(document, 'p', { class: 'note' }, text('app.loading')));
        add(document, element, el(document, 'section', { class: `part ${name}` }, el(document, 'h3', {}, text(titleKey)), body));
        return body;
    }

    function note(key, ...values) {
        return el(document, 'p', { class: 'note' }, text(key, ...values));
    }

    function failure(error) {
        return el(document, 'p', { class: 'note' }, errorText(error?.code, error?.text));
    }

    function facts(entries) {
        return el(
            document,
            'dl',
            { class: 'facts' },
            entries
                .filter(([, value]) => present(value))
                .map(([key, value]) => [el(document, 'dt', {}, text(key)), el(document, 'dd', {}, String(value))]),
        );
    }

    function passwordField(name) {
        return el(document, 'input', { type: 'password', name, autocomplete: 'off', placeholder: text('setup.password'), 'aria-label': text('setup.password') });
    }

    function fileField(name, labelKey) {
        return el(document, 'input', { type: 'file', name, 'aria-label': text(labelKey) });
    }

    function chosenFile(field) {
        const files = field.files;
        return files && files.length > 0 ? files[0] : null;
    }

    async function read(call) {
        try {
            return { value: await call() };
        } catch (error) {
            return { error };
        }
    }

    // Bridge

    const bridgeBody = handled ? part('bridge', 'setup.bridge') : null;

    async function loadBridge() {
        const { value, error } = await read(() => api.bridge(bridge.uid));
        if (closed) {
            return;
        }
        clear(bridgeBody);
        if (error) {
            add(document, bridgeBody, failure(error));
            return;
        }
        const buffer = Number.isInteger(value?.bufferSize) ? (value.bufferSize === 0 ? text('setup.bufferOff') : String(value.bufferSize)) : null;
        add(
            document,
            bridgeBody,
            facts([
                ['setup.uid', value?.uid],
                ['setup.label', value?.label],
                ['setup.status', value?.status],
                ['setup.connection', present(value?.connection) ? named('connection', value.connection) : null],
                ['setup.ownAddress', value?.ownAddress],
                ['setup.bufferSize', buffer],
            ]),
        );
    }

    // Keyring

    const keyringBody = handled ? part('keyring', 'setup.keyring') : null;
    const keyringFile = fileField('keyringFile', 'setup.keyringFile');
    const keyringPassword = passwordField('keyringPassword');
    const keyringButton = el(document, 'button', { type: 'button', class: 'upload' }, text('setup.upload'));
    const keyringResult = el(document, 'div', { class: 'result', 'aria-live': 'polite' });
    const keyringNameField = el(document, 'input', { type: 'text', name: 'keyringFileName', readonly: true, 'aria-label': text('setup.keyringFileName') });
    const keyringCopy = el(document, 'button', { type: 'button', class: 'copy' }, text('device.copy'));
    const keyringCopyState = el(document, 'span', { class: 'copied', 'aria-live': 'polite' });
    const keyringView = el(document, 'div', { class: 'state' });
    if (keyringBody) {
        clear(keyringBody);
        add(
            document,
            keyringBody,
            keyringView,
            el(document, 'h4', {}, text('setup.keyringUpload')),
            el(document, 'div', { class: 'tools' }, keyringFile, keyringPassword, keyringButton),
            keyringResult,
        );
        add(document, keyringView, note('app.loading'));
    }

    function signatureText(valid) {
        return valid === true ? text('setup.signatureValid') : valid === false ? text('setup.signatureInvalid') : text('setup.signatureUnchecked');
    }

    function interfacesTable(interfaces) {
        return el(
            document,
            'table',
            { class: 'interfaces' },
            el(document, 'thead', {}, el(document, 'tr', {}, INTERFACE_COLUMNS.map((name) => el(document, 'th', {}, text(`setup.interface.${name}`))))),
            el(
                document,
                'tbody',
                {},
                interfaces.map((entry) =>
                    el(
                        document,
                        'tr',
                        { class: entry.own === true ? 'own' : null },
                        el(document, 'td', {}, el(document, 'span', { class: 'address' }, String(entry.address ?? ''))),
                        el(document, 'td', {}, String(entry.type ?? '')),
                        el(document, 'td', {}, String(entry.host ?? '')),
                        el(document, 'td', {}, Number.isInteger(entry.userId) ? String(entry.userId) : ''),
                        el(document, 'td', {}, String(count(entry.linkedSecuredGroupAddresses))),
                        el(document, 'td', {}, entry.own === true ? el(document, 'span', { class: 'mark' }, text('setup.own')) : ''),
                    ),
                ),
            ),
        );
    }

    function drawKeyring(keyring) {
        clear(keyringView);
        if (keyring?.loaded !== true) {
            add(document, keyringView, note('setup.keyringNone'));
            return;
        }
        const interfaces = Array.isArray(keyring.interfaces) ? keyring.interfaces.filter((entry) => typeof entry === 'object' && entry !== null) : [];
        add(
            document,
            keyringView,
            facts([
                ['setup.keyringFileName', keyring.fileName],
                ['setup.signature', signatureText(keyring.signatureValid)],
                ['setup.backbone', keyring.backboneKeyPresent === true ? text('setup.yes') : keyring.backboneKeyPresent === false ? text('setup.no') : null],
                ['setup.securedAddresses', Number.isInteger(keyring.securedGroupAddresses) ? String(keyring.securedGroupAddresses) : null],
            ]),
            interfaces.length === 0 ? note('setup.noInterfaces') : interfacesTable(interfaces),
        );
    }

    async function loadKeyring() {
        const { value, error } = await read(() => api.keyring(bridge.uid));
        if (closed) {
            return;
        }
        if (error) {
            clear(keyringView);
            add(document, keyringView, failure(error));
            return;
        }
        drawKeyring(value);
    }

    function drawKeyringAnswer(answer) {
        clear(keyringResult);
        const fileName = present(answer?.fileName) ? String(answer.fileName) : null;
        const parts = [
            el(document, 'p', {}, answer?.stored === false || fileName === null ? text('setup.keyringNotStored') : text('setup.keyringStored', fileName)),
            answer?.signatureValid === true || answer?.signatureValid === false
                ? el(document, 'p', {}, answer.signatureValid ? text('setup.uploadSignatureValid') : text('setup.uploadSignatureInvalid'))
                : null,
            answer?.shadowedByConfigFolder === true ? el(document, 'p', { class: 'warn' }, text('setup.shadowed')) : null,
            answer?.keyring?.loaded === true
                ? el(
                      document,
                      'p',
                      { class: 'note' },
                      text(
                          'setup.keyringHolds',
                          count(answer.keyring.securedGroupAddresses),
                          Array.isArray(answer.keyring.interfaces) ? answer.keyring.interfaces.length : 0,
                      ),
                  )
                : null,
        ];
        add(document, keyringResult, parts);
        if (fileName !== null) {
            keyringNameField.value = fileName;
            clear(keyringCopyState);
            add(
                document,
                keyringResult,
                el(document, 'p', { class: 'set-keyring' }, text('setup.setKeyringFile', fileName)),
                el(document, 'div', { class: 'tools' }, keyringNameField, keyringCopy, keyringCopyState),
            );
        }
    }

    async function uploadKeyring() {
        const file = chosenFile(keyringFile);
        const password = keyringPassword.value;
        clear(keyringResult);
        if (file === null) {
            keyringPassword.value = '';
            add(document, keyringResult, el(document, 'p', {}, text('setup.chooseFile')));
            return;
        }
        attr(keyringButton, 'disabled', true);
        add(document, keyringResult, el(document, 'p', { class: 'note' }, text('setup.uploading')));
        let answer;
        try {
            answer = await api.uploadKeyring(bridge.uid, file, { password });
        } catch (error) {
            if (!closed) {
                clear(keyringResult);
                add(document, keyringResult, el(document, 'p', { class: 'warn' }, errorText(error?.code, error?.text)));
            }
            return;
        } finally {
            keyringPassword.value = '';
            attr(keyringButton, 'disabled', false);
        }
        if (closed) {
            return;
        }
        drawKeyringAnswer(answer);
        loadKeyring();
    }

    async function copyKeyringFile() {
        clear(keyringCopyState);
        const copied = await copyText(document, clipboard, keyringNameField);
        if (!closed) {
            add(document, keyringCopyState, text(copied ? 'device.copied' : 'device.pressCopy'));
        }
    }

    // Project

    const projectBody = part('project', 'setup.project');
    const projectView = el(document, 'div', { class: 'state' });
    const projectFile = fileField('projectFile', 'setup.projectFile');
    const projectPassword = passwordField('projectPassword');
    const languageField = el(document, 'input', {
        type: 'text',
        name: 'language',
        maxlength: 10,
        autocomplete: 'off',
        placeholder: text('setup.language'),
        'aria-label': text('setup.language'),
    });
    const projectButton = el(document, 'button', { type: 'button', class: 'upload' }, text('setup.upload'));
    const projectResult = el(document, 'div', { class: 'result', 'aria-live': 'polite' });
    const removeButton = el(document, 'button', { type: 'button', class: 'remove' }, text('setup.remove'));
    const removeResult = el(document, 'div', { class: 'removal', 'aria-live': 'polite' });
    clear(projectBody);
    add(document, projectBody, projectView, removeResult);
    add(document, projectView, note('app.loading'));
    if (handled) {
        add(
            document,
            projectBody,
            el(document, 'h4', {}, text('setup.projectUpload')),
            el(document, 'div', { class: 'tools' }, projectFile, projectPassword, languageField, projectButton),
            projectResult,
        );
    }

    function drawProject(project) {
        clear(projectView);
        const state = project?.state;
        if (state !== 'LOADED') {
            add(document, projectView, Object.hasOwn(PROJECT_STATES, state) ? note(PROJECT_STATES[state]) : note('setup.projectState', String(state ?? '')));
        } else {
            const secured = Number.isInteger(project.securedGroupAddresses) ? project.securedGroupAddresses : 0;
            add(
                document,
                projectView,
                facts([
                    ['setup.projectName', project.name],
                    ['setup.installation', project.installation],
                    ['setup.sourceName', project.sourceName],
                    ['setup.importedAt', present(project.importedAt) ? timeText(project.importedAt) : null],
                    ['setup.projectLanguage', project.language],
                    ['setup.addressStyle', project.groupAddressStyle],
                    ['setup.toolVersion', project.toolVersion],
                    ['setup.lastModified', present(project.lastModified) ? timeText(project.lastModified) : null],
                    ['setup.groupAddresses', Number.isInteger(project.groupAddresses) ? text('setup.addressCount', project.groupAddresses, secured) : null],
                    ['setup.devices', Number.isInteger(project.devices) ? String(project.devices) : null],
                    ['setup.groupObjects', Number.isInteger(project.groupObjects) ? String(project.groupObjects) : null],
                    ['setup.ignored', count(project.ignoredInstallations) > 0 ? String(project.ignoredInstallations) : null],
                ]),
            );
        }
        if (state !== 'NONE' && present(state)) {
            add(document, projectView, el(document, 'div', { class: 'tools' }, removeButton));
        }
    }

    async function loadProject() {
        const { value, error } = await read(() => api.project(bridge.uid));
        if (closed) {
            return;
        }
        if (error) {
            clear(projectView);
            add(document, projectView, failure(error));
            return;
        }
        drawProject(value);
    }

    function projectChanged() {
        rows?.forgetProject?.();
        if (handled) {
            loadCheck();
        }
    }

    async function uploadProject() {
        const file = chosenFile(projectFile);
        const password = projectPassword.value;
        const language = languageField.value;
        clear(projectResult);
        if (file === null) {
            projectPassword.value = '';
            add(document, projectResult, el(document, 'p', {}, text('setup.chooseFile')));
            return;
        }
        attr(projectButton, 'disabled', true);
        add(document, projectResult, el(document, 'p', { class: 'note' }, text('setup.reading')));
        let answer;
        try {
            answer = await api.uploadProject(bridge.uid, file, { password, language });
        } catch (error) {
            if (!closed) {
                clear(projectResult);
                add(document, projectResult, el(document, 'p', { class: 'warn' }, errorText(error?.code, error?.text)));
                if (error?.code === 'password-required') {
                    projectPassword.focus();
                }
            }
            return;
        } finally {
            projectPassword.value = '';
            attr(projectButton, 'disabled', false);
        }
        if (closed) {
            return;
        }
        clear(projectResult);
        add(document, projectResult, el(document, 'p', {}, text('setup.projectStored')));
        drawProject(answer);
        projectChanged();
    }

    async function removeProject() {
        clear(removeResult);
        if (!confirm(text('setup.removeConfirm'))) {
            return;
        }
        attr(removeButton, 'disabled', true);
        const { error } = await read(() => api.removeProject(bridge.uid));
        attr(removeButton, 'disabled', false);
        if (closed) {
            return;
        }
        if (error && error.code !== 'no-project') {
            add(document, removeResult, el(document, 'p', { class: 'warn' }, errorText(error.code, error.text)));
            return;
        }
        add(document, removeResult, el(document, 'p', {}, text('setup.removed')));
        projectChanged();
        loadProject();
    }

    // Check

    const checkBody = handled ? part('check', 'setup.check') : null;

    function differencesTable(differences) {
        return el(
            document,
            'table',
            { class: 'differences' },
            el(document, 'thead', {}, el(document, 'tr', {}, CHECK_COLUMNS.map((name) => el(document, 'th', {}, text(`setup.difference.${name}`))))),
            el(
                document,
                'tbody',
                {},
                differences.map((difference) =>
                    el(
                        document,
                        'tr',
                        { class: difference.noteOnly === true ? 'note-only' : null },
                        el(document, 'td', {}, el(document, 'span', { class: 'address' }, String(difference.address ?? ''))),
                        el(
                            document,
                            'td',
                            {},
                            named('check', difference.kind),
                            difference.noteOnly === true ? el(document, 'span', { class: 'mark' }, text('setup.noteOnly')) : null,
                        ),
                        el(document, 'td', {}, String(difference.label ?? '')),
                        el(document, 'td', { class: 'type' }, String(difference.configuredDataType ?? '')),
                        el(document, 'td', { class: 'type' }, String(difference.projectDataType ?? '')),
                    ),
                ),
            ),
        );
    }

    async function loadCheck() {
        const { value, error } = await read(() => api.check(bridge.uid));
        if (closed) {
            return;
        }
        clear(checkBody);
        if (error) {
            add(document, checkBody, error.code === 'no-project' ? note('setup.checkNeedsProject') : failure(error));
            return;
        }
        const differences = Array.isArray(value?.differences) ? value.differences.filter((entry) => typeof entry === 'object' && entry !== null) : [];
        add(document, checkBody, differences.length === 0 ? note('setup.noDifferences') : differencesTable(differences));
        if (count(value?.thingsWithoutHandler) > 0) {
            add(document, checkBody, note('setup.thingsWithoutHandler', value.thingsWithoutHandler));
        }
    }

    if (!handled) {
        element.insertBefore(note('setup.removedBridge'), element.childNodes[1] ?? null);
    }

    const onClick = (event) => {
        const target = event.target;
        if (target === keyringButton) {
            uploadKeyring();
        } else if (target === keyringCopy) {
            copyKeyringFile();
        } else if (target === projectButton) {
            uploadProject();
        } else if (target === removeButton) {
            removeProject();
        }
    };
    element.addEventListener('click', onClick);

    if (handled) {
        loadBridge();
        loadKeyring();
        loadCheck();
    }
    loadProject();

    return {
        close() {
            closed = true;
            keyringPassword.value = '';
            projectPassword.value = '';
            element.removeEventListener('click', onClick);
        },
    };
}
