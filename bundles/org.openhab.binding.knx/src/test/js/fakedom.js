// A small stand-in for the browser's document, for the page's view tests. It models only what the views use:
// elements with tag, attributes, children, textContent, classList, value, checked, custom style properties, listeners
// and a way to fire an event, and querySelector by id, class or tag. It is no browser: no layout, no CSS, no HTML parser.
//
// On purpose it has no innerHTML, outerHTML, insertAdjacentHTML or document.write: each throws, so a view that
// used one fails its test. querySelector throws, as a browser does, on a selector that is not "#name", ".name"
// or "tag" with a name of [A-Za-z][\w-]* (an address such as 31/7/1 used as an id is not a valid selector).

const SELECTOR = /^([#.]?)([A-Za-z][\w-]*)$/;
const TAG = /^[A-Za-z][A-Za-z0-9-]*$/;
const FORM_FIELDS = new Set(['INPUT', 'TEXTAREA', 'SELECT']);

function forbidden(name) {
    throw new Error(`the stand-in document has no ${name}; the page must not use it`);
}

class Node {
    constructor(document, nodeType) {
        this.ownerDocument = document;
        this.nodeType = nodeType;
        this.parentNode = null;
        this.childNodes = [];
        this.listeners = new Map();
    }

    get firstChild() {
        return this.childNodes[0] ?? null;
    }

    get lastChild() {
        return this.childNodes.at(-1) ?? null;
    }

    get children() {
        return this.childNodes.filter((node) => node.nodeType === 1);
    }

    appendChild(node) {
        if (!(node instanceof Node)) {
            throw new TypeError('appendChild takes a node');
        }
        if (node === this || node.contains(this)) {
            throw new Error('a node cannot hold itself');
        }
        node.remove();
        node.parentNode = this;
        this.childNodes.push(node);
        return node;
    }

    append(...nodes) {
        for (const node of nodes) {
            this.appendChild(typeof node === 'string' ? this.ownerDocument.createTextNode(node) : node);
        }
    }

    insertBefore(node, before) {
        if (before == null) {
            return this.appendChild(node);
        }
        const at = this.childNodes.indexOf(before);
        if (at < 0) {
            throw new Error('insertBefore: not a child');
        }
        node.remove();
        node.parentNode = this;
        this.childNodes.splice(this.childNodes.indexOf(before), 0, node);
        return node;
    }

    removeChild(node) {
        const at = this.childNodes.indexOf(node);
        if (at < 0) {
            throw new Error('removeChild: not a child');
        }
        this.childNodes.splice(at, 1);
        node.parentNode = null;
        // as a browser: what leaves the document, also to be put back elsewhere, loses the focus
        const document = this.ownerDocument;
        if (document.activeElement && node.contains(document.activeElement)) {
            document.activeElement = null;
        }
        return node;
    }

    replaceChildren(...nodes) {
        while (this.firstChild) {
            this.removeChild(this.firstChild);
        }
        this.append(...nodes);
    }

    remove() {
        if (this.parentNode) {
            this.parentNode.removeChild(this);
        }
    }

    contains(node) {
        for (let at = node; at; at = at.parentNode) {
            if (at === this) {
                return true;
            }
        }
        return false;
    }

    get textContent() {
        return this.childNodes.map((node) => node.textContent).join('');
    }

    set textContent(value) {
        while (this.firstChild) {
            this.removeChild(this.firstChild);
        }
        const text = value == null ? '' : String(value);
        if (text !== '') {
            this.appendChild(this.ownerDocument.createTextNode(text));
        }
    }

    addEventListener(type, listener) {
        if (typeof listener !== 'function') {
            throw new TypeError('the stand-in takes functions as listeners');
        }
        if (!this.listeners.has(type)) {
            this.listeners.set(type, new Set());
        }
        this.listeners.get(type).add(listener);
    }

    removeEventListener(type, listener) {
        this.listeners.get(type)?.delete(listener);
    }

    /** Fires an event of this type on this node; it bubbles to the parents unless init.bubbles is false. */
    fire(type, init = {}) {
        const event = {
            bubbles: true,
            ...init,
            type,
            target: this,
            currentTarget: null,
            defaultPrevented: false,
            stopped: false,
            preventDefault() {
                this.defaultPrevented = true;
            },
            stopPropagation() {
                this.stopped = true;
            },
        };
        for (let at = this; at; at = event.bubbles ? at.parentNode : null) {
            event.currentTarget = at;
            for (const listener of [...(at.listeners.get(type) ?? [])]) {
                listener(event);
            }
            if (event.stopped) {
                break;
            }
        }
        return event;
    }

    /** {@return the number of listeners on this node alone} */
    ownListenerCount() {
        let count = 0;
        for (const set of this.listeners.values()) {
            count += set.size;
        }
        return count;
    }

    querySelector(selector) {
        return this.querySelectorAll(selector)[0] ?? null;
    }

    querySelectorAll(selector) {
        const match = SELECTOR.exec(String(selector));
        if (!match) {
            throw new SyntaxError(`'${selector}' is not a valid selector`);
        }
        const [, kind, name] = match;
        const found = [];
        const visit = (node) => {
            for (const child of node.children) {
                if (
                    (kind === '#' && child.getAttribute('id') === name) ||
                    (kind === '.' && child.classList.contains(name)) ||
                    (kind === '' && child.tagName === name.toUpperCase())
                ) {
                    found.push(child);
                }
                visit(child);
            }
        };
        visit(this);
        return found;
    }

    get innerHTML() {
        return forbidden('innerHTML');
    }

    set innerHTML(value) {
        forbidden('innerHTML');
    }

    get outerHTML() {
        return forbidden('outerHTML');
    }

    set outerHTML(value) {
        forbidden('outerHTML');
    }

    insertAdjacentHTML() {
        forbidden('insertAdjacentHTML');
    }
}

class Text extends Node {
    constructor(document, data) {
        super(document, 3);
        this.data = String(data);
    }

    get textContent() {
        return this.data;
    }

    set textContent(value) {
        this.data = value == null ? '' : String(value);
    }

    get children() {
        return [];
    }

    appendChild() {
        throw new Error('a text node has no children');
    }
}

class ClassList {
    constructor(element) {
        this.element = element;
    }

    names() {
        return (this.element.getAttribute('class') ?? '').split(/\s+/).filter(Boolean);
    }

    contains(name) {
        return this.names().includes(name);
    }

    add(...names) {
        const all = this.names();
        for (const name of names) {
            if (!all.includes(name)) {
                all.push(name);
            }
        }
        this.element.setAttribute('class', all.join(' '));
    }

    remove(...names) {
        this.element.setAttribute('class', this.names().filter((name) => !names.includes(name)).join(' '));
    }

    toggle(name, force) {
        const on = force === undefined ? !this.contains(name) : Boolean(force);
        if (on) {
            this.add(name);
        } else {
            this.remove(name);
        }
        return on;
    }
}

/** The CSS object model of an element, as far as the page uses it: custom properties set and taken away. */
class Style {
    constructor() {
        this.values = new Map();
    }

    setProperty(name, value) {
        this.values.set(String(name), String(value));
    }

    getPropertyValue(name) {
        return this.values.get(name) ?? '';
    }

    removeProperty(name) {
        this.values.delete(name);
    }
}

class Element extends Node {
    constructor(document, tag) {
        super(document, 1);
        this.tagName = tag.toUpperCase();
        this.namespaceURI = null;
        this.style = new Style();
        this.attributes = new Map();
        this.classList = new ClassList(this);
        this.typedValue = undefined;
        this.typedChecked = undefined;
    }

    isTickBox() {
        return this.tagName === 'INPUT' && ['checkbox', 'radio'].includes(this.getAttribute('type'));
    }

    getAttribute(name) {
        return this.attributes.has(name) ? this.attributes.get(name) : null;
    }

    hasAttribute(name) {
        return this.attributes.has(name);
    }

    setAttribute(name, value) {
        this.attributes.set(String(name), String(value));
    }

    removeAttribute(name) {
        this.attributes.delete(name);
    }

    get id() {
        return this.getAttribute('id') ?? '';
    }

    get className() {
        return this.getAttribute('class') ?? '';
    }

    /** The field's value as typed; until something is typed, its value attribute. */
    get value() {
        if (!FORM_FIELDS.has(this.tagName)) {
            return undefined;
        }
        return this.typedValue ?? this.getAttribute('value') ?? '';
    }

    set value(value) {
        this.typedValue = value == null ? '' : String(value);
    }

    get disabled() {
        return this.hasAttribute('disabled');
    }

    /** A tick box's state; until it is clicked or set, its checked attribute. */
    get checked() {
        if (!this.isTickBox()) {
            return undefined;
        }
        return this.typedChecked ?? this.hasAttribute('checked');
    }

    set checked(value) {
        this.typedChecked = Boolean(value);
    }

    focus() {
        this.ownerDocument.activeElement = this;
    }

    /** Selects the text of a field: the document notes the field as its selection. */
    select() {
        if (!FORM_FIELDS.has(this.tagName)) {
            throw new TypeError('only a field has text to select');
        }
        this.ownerDocument.selection = this;
    }

    /** As a browser: a click on a disabled button or field does nothing; a click on a tick box flips it first. */
    click() {
        if (this.disabled) {
            return null;
        }
        if (!this.isTickBox()) {
            return this.fire('click');
        }
        this.typedChecked = this.getAttribute('type') === 'radio' ? true : !this.checked;
        const event = this.fire('click');
        this.fire('input');
        this.fire('change');
        return event;
    }
}

class Document extends Node {
    constructor() {
        super(null, 9);
        this.ownerDocument = this;
        this.nodes = new Set([this]);
        this.activeElement = null;
        this.selection = null;
        this.commands = [];
        /** What execCommand answers: true, false, or an Error it throws. A test sets it. */
        this.commandResult = false;
    }

    /** As a browser's: records the command and answers commandResult. */
    execCommand(command) {
        this.commands.push({ command, selection: this.selection });
        if (this.commandResult instanceof Error) {
            throw this.commandResult;
        }
        return this.commandResult;
    }

    createElement(tag) {
        if (!TAG.test(String(tag))) {
            throw new Error(`'${tag}' is not a valid tag name`);
        }
        const element = new Element(this, String(tag));
        this.nodes.add(element);
        return element;
    }

    /** An element of another namespace (the page's icons); it is kept like any element. */
    createElementNS(namespace, tag) {
        const element = this.createElement(tag);
        element.namespaceURI = String(namespace);
        return element;
    }

    createTextNode(data) {
        const text = new Text(this, data);
        this.nodes.add(text);
        return text;
    }

    /** {@return the number of listeners on the document and on every node it made, attached or not} */
    listenerCount() {
        let count = 0;
        for (const node of this.nodes) {
            count += node.ownListenerCount();
        }
        return count;
    }

    write() {
        forbidden('document.write');
    }

    writeln() {
        forbidden('document.writeln');
    }
}

/** {@return a new, empty stand-in document} */
export function createDocument() {
    return new Document();
}

/** {@return the texts of the element's text nodes, in document order, one entry per node} */
export function textNodes(node) {
    const texts = [];
    const visit = (at) => {
        for (const child of at.childNodes) {
            if (child.nodeType === 3) {
                texts.push(child.data);
            } else {
                visit(child);
            }
        }
    };
    visit(node);
    return texts;
}

/** {@return every element below the node, in document order} */
export function allElements(node) {
    const found = [];
    const visit = (at) => {
        for (const child of at.children) {
            found.push(child);
            visit(child);
        }
    };
    visit(node);
    return found;
}
