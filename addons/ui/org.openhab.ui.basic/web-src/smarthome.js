/**
 * Eclipse SmartHome BasicUI javascript
 *
 * @author Vlad Ivanov — initial version
 */

/*eslint-env browser */
/*eslint no-undef:2*/
/*eslint no-new:0 */
/*eslint no-underscore-dangle:0*/

/*global componentHandler */

(function(o) {
	"use strict";

	var
		smarthome = {
			dataModel: {}
		};

	var
		featureSupport = {
			eventLayerXY: (function() {
				var
					event;

				if (document.createEvent === undefined) {
					event = new MouseEvent(null);
				} else {
					event = document.createEvent("MouseEvent");
				}

				return (event.layerX !== undefined);
			})(),
			pointerEvents: (document.createElement("div").style.pointerEvents !== undefined),
			customEvent: (function() {
				var
					supported = true;

				try {
					new CustomEvent("event", {});
				} catch (e) {
					supported = false;
				}

				return supported;
			})(),
			elementRemove: Element.prototype.remove !== undefined,
			flexbox: (document.createElement("div").style.flexBasis !== undefined),
			flexboxLegacy: (function() {
				var
					e = document.createElement("div");

				return (
					(e.style.boxDirection !== undefined) ||
					(e.style.webkitBoxDirection !== undefined)
				);
			})(),
			eventSource: ("EventSource" in window)
		},
		createEvent;

	// Add polyfills for unsupported features
	(function() {
		// CustomEvent
		if (featureSupport.customEvent) {
			createEvent = function(name, data) {
				return new CustomEvent(
					name, {
						detail: data
					}
				);
			};
		} else {
			createEvent = function(name, data) {
				var
					event = document.createEvent("CustomEvent");
				event.initCustomEvent(name, true, false, data);
				return event;
			};
		}

		// Element.prototype.remove
		if (!featureSupport.elementRemove) {
			Element.prototype.remove = function() {
				this.parentNode.removeChild(this);
			};
		}

		// Legacy flexbox
		if (
			!featureSupport.flexbox &&
			featureSupport.flexboxLegacy
		) {
			document.documentElement.classList.add("flexbox-legacy");
		}
	})();

	function createDOM(text) {
		var
			e = document.createElement("div");

		e.innerHTML = text;
		return e.childNodes;
	}

	function append(node, child) {
		if (child instanceof NodeList) {
			[].slice.call(child).forEach(function(e) {
				node.appendChild(e);
			});
		} else {
			node.appendChild(child);
		}
	}

	function ajax(params) {
		var
			p = params,
			type = typeof p.type !== "undefined" ? p.type : "GET",
			data = typeof p.data !== "undefined" ? p.data : "",
			headers = typeof p.headers !== "undefined" ? p.headers : {},
			request = new XMLHttpRequest();

		request.open(type, p.url, true);

		for (var h in headers) {
			request.setRequestHeader(h, headers[h]);
		}

		request.onload = function() {
			if (request.status < 200 || request.status > 400) {
				if (typeof p.error === "function") {
					p.error(request);
				}
				return;
			}
			if (typeof p.callback === "function") {
				p.callback(request);
			}
		};
		request.onerror = function() {
			if (typeof p.error === "function") {
				p.error(request);
			}
		};
		request.send(data);

		return request;
	}

	function renderTemplate(id, contents) {
		var
			text = document.getElementById(id).innerHTML;

		return text.replace(/\{([\w]+)\}/, function(match, capture) {
			//jshint unused:false
			if (typeof contents[capture] !== "undefined") {
				return contents[capture];
			} else {
				return "";
			}
		});
	}

	function EventMapper() {
		var
			_t = this;

		function processEvents(callable, table) {
			table.forEach(function(entry) {
				var
					element = entry[0],
					event = entry[1],
					handler = entry[2];

				callable.call(element, event, handler);
			});
		}

		_t.map = processEvents.bind(null, Node.prototype.addEventListener);
		_t.unmap = processEvents.bind(null, Node.prototype.removeEventListener);
	}

	function HistoryStack() {
		var
			_t = this;

		_t.level = 0;

		_t.push = function(page) {
			_t.level++;
			history.pushState({page: page}, document.title.textContent);
		};

		_t.replace = function(page, url) {
			history.replaceState({page: page}, document.title.textContent, url);
		};

		window.addEventListener("popstate", function(e) {
			_t.level--;
			smarthome.UI.navigate(e.state.page, false);
		}, false);
	}

	function Modal(text) {
		var
			_t = this;

		_t.templateId = "template-modal";
		_t.text = renderTemplate(_t.templateId, {
			content: text
		});

		function onkeydown(event) {
			event = event || window.event;
			if (event.keyCode === 27) {
				_t.hide();
			}
		}

		function init() {
			document.addEventListener("keydown", onkeydown);
		}

		function destroy() {
			if (_t.onHide !== undefined) {
				_t.onHide();
			}

			document.removeEventListener("keydown", onkeydown);
		}

		_t.show = function() {
			append(document.body, createDOM(_t.text));

			_t.bg = document.querySelector(o.modal);
			_t.container = _t.bg.querySelector(o.modalContainer);

			smarthome.UI.currentModal = _t;
			_t.bg.addEventListener("click", function() {
				_t.hide();
			});

			_t.container.addEventListener("click", function(event) {
				event.stopPropagation();
			});

			init();
		};

		_t.hide = function() {
			document.body.querySelector(o.modal).remove();
			delete smarthome.UI.currentModal;
			destroy();
		};
	}

	function DebounceProxy(callback, callInterval) {
		var
			_t = this,
			finished = false,
			args;

		_t.lock = false;
		_t.call = function() {
			args = arguments;
			if (!_t.lock) {
				finished = false;
				callback.apply(null, args);
				_t.lock = true;
				setTimeout(function() {
					if (!finished) {
						callback.apply(null, args);
					}
					_t.lock = false;
				}, callInterval);
			}
		};

		_t.finish = function() {
			finished = true;
		};
	}

	function VisibilityChangeProxy(delay, maxEvents) {
		var
			_t = this;

		function processEvent(event) {
			event.widget.setVisible(event.visibility);
		}

		_t.queue = [];
		_t.timeout = null;

		_t.processEvents = function() {
			_t.timeout = null;

			while (_t.queue.length !== 0) {
				processEvent(_t.queue[0]);
				_t.queue = _t.queue.slice(1);
			}
		};

		_t.push = function(event) {
			if (_t.queue.length > maxEvents) {
				return;
			}

			_t.queue.push(event);

			if (_t.timeout === null) {
				_t.timeout = setTimeout(_t.processEvents, delay);
			} else {
				clearTimeout(_t.timeout);
				_t.timeout = setTimeout(_t.processEvents, delay);
			}
		};
	}

	/* class Control */
	function Control(parentNode) {
		var
			_t = this,
			suppress = false,
			noneImageSrc = "/icon/none.png";

		_t.parentNode = parentNode;
		_t.formRow = parentNode.parentNode;
		_t.item = _t.parentNode.getAttribute(o.itemAttribute);
		_t.id = _t.parentNode.getAttribute(o.idAttribute);
		_t.icon = _t.parentNode.parentNode.querySelector(o.formIcon);
		_t.visible = !_t.formRow.classList.contains(o.formRowHidden);
		_t.label = _t.parentNode.parentNode.querySelector(o.formLabel);

		function replaceImageWithNone() {
			this.removeEventListener("error", replaceImageWithNone);
			this.src = noneImageSrc;
		}

		if (_t.icon !== null) {
			_t.iconName = _t.icon.getAttribute(o.iconAttribute);
			if (_t.icon.src !== noneImageSrc) {
				_t.icon.addEventListener("error", replaceImageWithNone);
			}
		}

		_t.reloadIcon = function(state) {
			// Some widgets don't have icons
			if (_t.icon !== null) {
				_t.icon.addEventListener("error", replaceImageWithNone);
				_t.icon.setAttribute("src",
					"/icon/" +
					_t.iconName +
					"?state=" +
					encodeURIComponent(state) +
					"&format=" +
					smarthome.UI.iconType
				);
			}
		};

		_t.setVisible = function(state) {
			if (state) {
				_t.formRow.classList.remove(o.formRowHidden);
			} else {
				_t.formRow.classList.add(o.formRowHidden);
			}

			_t.visible = state;
		};

		_t.setValue = function(value, itemState, visible) {
			_t.reloadIcon(itemState);
			if (suppress) {
				suppress = false;
			} else {
				_t.setValuePrivate(value, itemState, visible);
			}
		};

		_t.setValuePrivate = function() {};

		_t.setLabel = function() {};

		_t.suppressUpdate = function() {
			suppress = true;
		};

		_t.setLabelColor = function(color) {
			if (_t.label !== null) {
				_t.label.style.color = color;
			}
		};

		_t.setValueColor = function(color) {
			_t.parentNode.style.color = color;
		};

		_t.destroy = function() {
			if (_t.icon !== null) {
				_t.icon.removeEventListener("error", replaceImageWithNone);
			}

			[].forEach.call(_t.parentNode.querySelectorAll("video, audio"), function(media) {
				if (media instanceof HTMLMediaElement) {
					media.pause();
				}
			});
		};
	}

	/* class Frame */
	/* Mimics Control interface, only setVisible method is used */
	function Frame(parentNode) {
		var
			_t = this;

		_t.parentNode = parentNode;
		_t.id = _t.parentNode.getAttribute(o.idAttribute);
		_t.visible = !_t.parentNode.classList.contains(o.formHidden);
		_t.title = _t.parentNode.querySelector(o.formTitle);

		_t.setVisible = function(state) {
			if (state) {
				_t.parentNode.classList.remove(o.formHidden);
			} else {
				_t.parentNode.classList.add(o.formHidden);
			}

			_t.visible = state;
		};

		_t.setLabel = function(label) {
			_t.title.innerHTML = label;
		};

		_t.setValue = function() {};
		_t.setLabelColor = function() {};
		_t.setValueColor = function() {};
		_t.suppressUpdate = function() {};
		_t.destroy = function() {};
	}

	/* class ControlImage */
	function ControlImage(parentNode, callSuper) {
		// Some controls combine Image functionality with
		// other classes, so calling Control is conditional
		if (callSuper) {
			Control.call(this, parentNode);
		} else {
			this.parentNode = parentNode;
			this.id = this.parentNode.getAttribute(o.idAttribute);
		}

		var
			_t = this,
			interval = null,
			urlNoneIcon = "images/none.png";

		_t.image = parentNode.querySelector("img");
		_t.updateInterval = parseInt(parentNode.getAttribute("data-update-interval"), 10);

		_t.url = parentNode.getAttribute("data-proxied-url");
		_t.validUrl = parentNode.getAttribute("data-valid-url") === "true";
		_t.ignoreRefresh = parentNode.getAttribute("data-ignore-refresh") === "true";

		_t.setValuePrivate = function(value, itemState, visible) {
			if (!visible) {
				_t.ignoreRefresh = true;
				_t.image.setAttribute("src", urlNoneIcon);
			} else if (itemState.startsWith("data:")) {
				// Image element associated to an item of type ImageItem
				_t.ignoreRefresh = true;
				_t.image.setAttribute("src", itemState);
			} else if ((itemState !== "UNDEF") || (_t.validUrl)) {
				// Image element associated to an item of type StringItem (URL)
				// Or no associated item but url is set and valid in the image element
				_t.ignoreRefresh = false;
				_t.image.setAttribute("src", _t.url + "&t=" + Date.now());
			} else {
				// No associated item and url is not set or not valid in the image element
				_t.ignoreRefresh = true;
				_t.image.setAttribute("src", urlNoneIcon);
			}
		};

		_t.setVisible = function(state) {
			if (state) {
				_t.formRow.classList.remove(o.formRowHidden);
				_t.activateRefresh();
			} else {
				_t.formRow.classList.add(o.formRowHidden);
				_t.deactivateRefresh();
			}

			_t.visible = state;
		};

		_t.deactivateRefresh = function() {
			if (interval !== null) {
				clearInterval(interval);
				interval = null;
			}
		};

		_t.activateRefresh = function() {
			_t.deactivateRefresh();

			if (_t.updateInterval === 0 || _t.ignoreRefresh) {
				return;
			}
			// Limit the refresh interval to 100 ms
			if (_t.updateInterval < 100) {
				_t.updateInterval = 100;
			}

			interval = setInterval(function() {
				if (_t.image.clientWidth === 0) {
					clearInterval(interval);
					return;
				}
				_t.image.setAttribute("src", _t.url + "&t=" + Date.now());
			}, _t.updateInterval);
		};

		_t.destroy = function() {
			var
				imageParent = _t.image.parentNode;

			_t.image.setAttribute("src", urlNoneIcon);
			imageParent.removeChild(_t.image);
		};

		if (_t.visible) {
			_t.activateRefresh();
		}
	}

	/* class ControlText extends Control */
	function ControlText(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.hasValue = _t.parentNode.getAttribute("data-has-value") === "true";

		_t.setValuePrivate = function(value) {
			if (_t.hasValue) {
				parentNode.innerHTML = value;
			}
		};
	}

	/* class ControlSelection extends Control */
	function ControlSelection(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.hasValue = _t.parentNode.getAttribute("data-has-value") === "true";
		_t.value = _t.parentNode.parentNode.querySelector(o.formValue);
		_t.count = _t.parentNode.getAttribute("data-count") * 1;
		_t.suppressUpdateButtons = false;
		_t.reset = function() {
			_t.buttons.forEach(function(button) {
				button.classList.remove(o.buttonActiveClass);
			});
		};

		_t.onclick = function() {
			/* HTMLButtonElement this */
			var
				value = this.getAttribute("data-value") + "";

			if (_t.count !== 1) {
				_t.reset();
				this.classList.add(o.buttonActiveClass);
			}

			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: value
			}));
			_t.suppressUpdateButtons = true;
		};
		_t.valueMap = {};
		_t.buttons = [].slice.call(_t.parentNode.querySelectorAll(o.controlButton));
		_t.setValuePrivate = function(value, itemState) {
			if (_t.hasValue) {
				_t.value.innerHTML = value;
			}

			if (_t.count === 1) {
				return;
			}

			if (_t.suppressUpdateButtons) {
				_t.suppressUpdateButtons = false;
				return;
			}

			_t.reset();
			if (
				(_t.valueMap !== undefined) &&
				(_t.valueMap[itemState] !== undefined)
			) {
				_t.valueMap[itemState].classList.add(o.buttonActiveClass);
			}
		};

		_t.setValueColor = function(color) {
			_t.value.style.color = color;
		};

		_t.buttons.forEach.call(_t.buttons, function(button) {
			var
				value = button.getAttribute("data-value") + "";

			_t.valueMap[value] = button;
			button.addEventListener("click", _t.onclick);
		});

		_t.destroy = function() {
			_t.buttons.forEach(function(button) {
				button.removeEventListener("click", _t.onclick);
			});
			componentHandler.downgradeElements(_t.buttons);
		};
	}

	/* class ControlRadio extends Control */
	function ControlRadio(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this,
			valueMapString = parentNode.getAttribute("data-value-map");

		_t.value = null;
		_t.valueNode = parentNode.parentNode.querySelector(o.formValue);

		if (valueMapString !== null) {
			_t.valueMap = JSON.parse(valueMapString);
		} else {
			_t.valueMap = {};
		}

		function onRadioChange(event) {
			event.stopPropagation();

			if (event.target.tagName.toLowerCase() !== "input") {
				return;
			}

			var
				value = event.target.getAttribute("value");

			_t.parentNode.dispatchEvent(createEvent("control-change", {
				item: _t.item,
				value: value
			}));

			setTimeout(function() {
				_t.modal.hide();
			}, 300);
		}

		_t.showModal = function() {
			var
				content = _t.parentNode.querySelector(o.selectionRows).innerHTML;

			_t.modal = new Modal(content);
			_t.modal.show();
			_t.modal.onHide = function() {
				var
					items = [].slice.call(_t.modal.container.querySelectorAll(o.formRadio));

				componentHandler.downgradeElements(items);
				items.forEach(function(control) {
					control.removeEventListener("click", onRadioChange);
				});

				_t.modal = null;
			};

			var
				controls = [].slice.call(_t.modal.container.querySelectorAll(o.formRadio));

			if (_t.value !== null) {
				var
					items = [].slice.call(_t.modal.container.querySelectorAll("input[type=radio]"));

				items.forEach(function(radioItem) {
					if (radioItem.value === _t.value) {
						radioItem.checked = true;
					} else {
						radioItem.checked = false;
					}
				});
			}

			controls.forEach(function(control) {
				componentHandler.upgradeElement(control, "MaterialRadio");
				control.addEventListener("click", onRadioChange);
			});
		};

		_t.setValuePrivate = function(value, itemState) {
			_t.value = "" + itemState;
			if (_t.valueMap[itemState] !== undefined) {
				_t.valueNode.innerHTML = smarthome.UI.escapeHtml(_t.valueMap[itemState]);
			} else {
				_t.valueNode.innerHTML = "";
			}
		};

		_t.setValueColor = function(color) {
			_t.valueNode.style.color = color;
		};

		_t.destroy = function() {
			_t.parentNode.parentNode.removeEventListener("click", _t.showModal);
		};

		_t.parentNode.parentNode.addEventListener("click", _t.showModal);
	}

	/* class ControlRollerblinds extends Control */
	function ControlRollerblinds(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this,
			longpressTimeout = 300,
			longPress,
			timeout,
			pressed = false;

		_t.buttonUp = _t.parentNode.querySelector(o.rollerblind.up);
		_t.buttonDown = _t.parentNode.querySelector(o.rollerblind.down);
		_t.buttonStop = _t.parentNode.querySelector(o.rollerblind.stop);

		_t.hasValue = _t.parentNode.getAttribute("data-has-value") === "true";
		_t.valueNode = _t.parentNode.parentNode.querySelector(o.formValue);
		_t.setValuePrivate = function(value) {
			if (!_t.hasValue) {
				return;
			}
			if (value === "DOWN") {
				value = "100";
			} else if (value === "UP") {
				value = "0";
			}
			_t.valueNode.innerHTML = value;
		};

		function emitEvent(value) {
			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: value
			}));
		}

		function onMouseDown(command, event) {
			longPress = false;
			pressed = true;

			timeout = setTimeout(function() {
				longPress = true;
				emitEvent(command);
			}, longpressTimeout);

			event.stopPropagation();
			event.preventDefault();
		}

		function onMouseUp(command, event) {
			clearTimeout(timeout);
			if (!pressed) {
				return;
			}
			pressed = false;

			if (longPress) {
				emitEvent("STOP");
			} else {
				emitEvent(command);
			}

			event.stopPropagation();
			event.preventDefault();
		}

		function onStop(event) {
			emitEvent("STOP");

			event.stopPropagation();
			event.preventDefault();
		}

		var
			upButtonMouseUp = onMouseUp.bind(null, "UP"),
			upButtonMouseDown = onMouseDown.bind(null, "UP"),

			downButtonMouseUp = onMouseUp.bind(null, "DOWN"),
			downButtonMouseDown = onMouseDown.bind(null, "DOWN");

		var
			eventMap = [
				// Up button
				[ _t.buttonUp,   "touchstart", upButtonMouseDown ],
				[ _t.buttonUp,   "mousedown",  upButtonMouseDown ],
				[ _t.buttonUp,   "touchend",   upButtonMouseUp   ],
				[ _t.buttonUp,   "mouseup",    upButtonMouseUp   ],
				[ _t.buttonUp,   "mouseleave", upButtonMouseUp   ],
				// Down button
				[ _t.buttonDown, "touchstart", downButtonMouseDown ],
				[ _t.buttonDown, "mousedown",  downButtonMouseDown ],
				[ _t.buttonDown, "touchend",   downButtonMouseUp   ],
				[ _t.buttonDown, "mouseup",    downButtonMouseUp   ],
				[ _t.buttonDown, "mouseleave", downButtonMouseUp   ],
				// Stop button
				[ _t.buttonStop, "mousedown",  onStop ],
				[ _t.buttonStop, "touchstart", onStop ]
			];

		_t.destroy = function() {
			componentHandler.downgradeElements([
				_t.buttonUp,
				_t.buttonDown,
				_t.buttonStop
			]);

			smarthome.eventMapper.unmap(eventMap);
		};

		smarthome.eventMapper.map(eventMap);
	}

	/* class ControlSetpoint extends Control */
	function ControlSetpoint(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.up = _t.parentNode.querySelector(o.setpoint.up);
		_t.down = _t.parentNode.querySelector(o.setpoint.down);

		_t.value = _t.parentNode.getAttribute("data-value");
		_t.max = parseFloat(_t.parentNode.getAttribute("data-max"));
		_t.min = parseFloat(_t.parentNode.getAttribute("data-min"));
		_t.step = parseFloat(_t.parentNode.getAttribute("data-step"));

		_t.value = isNaN(parseFloat(_t.value)) ? 0 : parseFloat(_t.value);
		_t.valueNode = _t.parentNode.parentNode.querySelector(o.formValue);

		_t.unit = _t.parentNode.getAttribute("data-unit");

		_t.setValuePrivate = function(value, itemState) {
			if (itemState.indexOf(" ") > 0) {
				var stateAndUnit = itemState.split(" ");
				_t.value = stateAndUnit[0] * 1;
				_t.unit = stateAndUnit[1];
				} else {
					_t.value = itemState * 1;
					}
			_t.valueNode.innerHTML = value;
		};

		function onMouseDown(up, event) {
			var
				value = _t.value + ((up === true) ? _t.step : -_t.step );

			value = value > _t.max ? _t.max : value;
			value = value < _t.min ? _t.min : value;

			var command = value;
			if (_t.unit) {
				command = value + " " + _t.unit;
			}

			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: command
			}));

			_t.value = value;

			event.stopPropagation();
			event.preventDefault();
		}

		var
			increaseHandler = onMouseDown.bind(null, true),
			decreaseHandler = onMouseDown.bind(null, false);

		var
			eventMap = [
				[ _t.up,   "mousedown",  increaseHandler ],
				[ _t.up,   "touchstart", increaseHandler ],
				[ _t.down, "mousedown",  decreaseHandler ],
				[ _t.down, "touchstart", decreaseHandler ]
			];

		_t.destroy = function() {
			componentHandler.downgradeElements([
				_t.up,
				_t.down
			]);

			smarthome.eventMapper.unmap(eventMap);
		};

		smarthome.eventMapper.map(eventMap);
	}
	/* class Colorpicker */
	function Colorpicker(parentNode, color, callback) {
		var
			_t = this;

		/* rgb2hsv and hsv2rgb are modified versions from http://axonflux.com/handy-rgb-to-hsl-and-rgb-to-hsv-color-model-c */
		function rgb2hsv(rgbColor) {
			var
				r = rgbColor.r,
				g = rgbColor.g,
				b = rgbColor.b;

			r = r / 255;
			g = g / 255;
			b = b / 255;

			var
				max = Math.max(r, g, b),
				min = Math.min(r, g, b);

			var
				h,
				s,
				v = max;

			var
				d = max - min;

			s = max === 0 ? 0 : d / max;

			if (max === min) {
				h = 0; // achromatic
			} else {
				switch (max) {
					case r:
						h = (g - b) / d + (g < b ? 6 : 0);
						break;
					case g:
						h = (b - r) / d + 2;
						break;
					case b:
						h = (r - g) / d + 4;
						break;
				}
				h /= 6;
			}

			return {
				h: h,
				s: s,
				v: v
			};
		}

		function hsv2hsl(hsvColor) {
			var
				hue = hsvColor.h,
				sat = hsvColor.s,
				val = hsvColor.v;

			var
				d = ((2 - sat) * val),
				c = d < 1 ? d : 2 - d;

			return {
				h: hue,
				s: c === 0 ? 0 : sat * val / c,
				l: d / 2
			};
		}

		_t.container = parentNode;
		_t.value = color;
		_t.hsvValue = rgb2hsv(color);
		_t.interval = null;
		_t.isBeingChanged = false;

		_t.colorpicker = _t.container.querySelector(o.colorpicker.colorpicker);
		_t.image = _t.container.querySelector(o.colorpicker.image);
		_t.background = _t.container.querySelector(o.colorpicker.background);
		_t.handle = _t.container.querySelector(o.colorpicker.handle);
		_t.slider = _t.container.querySelector(o.colorpicker.slider);
		_t.button = _t.container.querySelector(o.controlButton);

		componentHandler.upgradeElement(_t.button, "MaterialButton");
		componentHandler.upgradeElement(_t.button, "MaterialRipple");

		function updateValue(event) {
			var
				pos;

			if (event.touches !== undefined) {
				pos = {
					x: event.touches[0].pageX - _t.colorpicker.offsetLeft,
					y: event.touches[0].pageY - _t.colorpicker.offsetTop
				};
			} else {
				pos = {
					x: event.pageX - _t.colorpicker.offsetLeft,
					y: event.pageY - _t.colorpicker.offsetTop
				};
			}
			var
				maxR = _t.image.clientWidth / 2,
				offsetX = pos.x - maxR,
				offsetY = pos.y - maxR,
				r = (offsetY * offsetY) + (offsetX * offsetX);

			if (r > (maxR * maxR)) {
				var
					ratio = 1 - Math.abs(maxR / Math.sqrt(r));

				pos.x -= (offsetX * ratio);
				pos.y -= (offsetY * ratio);
			}

			_t.handle.style.left = (pos.x / _t.image.clientWidth) * 100 + "%";
			_t.handle.style.top = (pos.y / _t.image.clientWidth) * 100 + "%";

			var
				angle = offsetX >= 0 ?
						(Math.PI * 2 - Math.atan(offsetY / offsetX) + Math.PI / 2) / (Math.PI * 2) :
						(Math.PI * 2 - Math.atan(offsetY / offsetX) - Math.PI / 2) / (Math.PI * 2),
				hsv = {
					h: isNaN(angle) ? 0 : angle,
					s: Math.sqrt(r) / maxR,
					v: 1
				},
				hsl = hsv2hsl(hsv);

			_t.hsvValue = {
				h: hsv.h,
				s: hsv.s,
				v: _t.slider.value / 100
			};

			hsl.l = hsl.l < 0.5 ? 0.5 : hsl.l;
			_t.background.style.background = "hsl(" + hsl.h * 360 + ", 100%, " + (Math.round(hsl.l * 100)) + "%)";
		}

		function setColor(c) {
			var
				hsv = rgb2hsv(c);

			_t.slider.value = hsv.v * 100;

			var
				x = 50 + Math.round(hsv.s * Math.cos(2 * Math.PI * hsv.h) * 50),
				y = 50 + Math.round(hsv.s * Math.sin(2 * Math.PI * hsv.h) * 50);

			_t.handle.style.top = x + "%";
			_t.handle.style.left = y + "%";

			hsv.v = 1;

			var
				correctedrgb = Colorpicker.hsv2rgb(hsv);

			_t.background.style.background =
				"rgb(" +
					Math.round(correctedrgb.r) + "," +
					Math.round(correctedrgb.g) + "," +
					Math.round(correctedrgb.b) + ")";
		}

		function onWindowMouseup() {
			if (_t.interval !== null) {
				clearInterval(_t.interval);
				_t.interval = null;
			}

			_t.isBeingChanged = false;
			window.removeEventListener("mouseup", onWindowMouseup);
		}

		function onMouseDown(event) {
			_t.interval = setInterval(function() {
				callback(_t.hsvValue);
			}, 300);

			window.addEventListener("mouseup", onWindowMouseup);

			updateValue(event);
			callback(_t.hsvValue);
			_t.isBeingChanged = true;

			event.stopPropagation();
		}

		function onMove(event) {
			if (
				(event.touches === undefined) &&
				(!(event.buttons & 0x01))
			) {
				return;
			}

			updateValue(event);

			event.stopPropagation();
			event.preventDefault();
		}

		function onMouseUp(event) {
			if (_t.interval !== null) {
				clearInterval(_t.interval);
				_t.interval = null;
			}

			window.removeEventListener("mouseup", onWindowMouseup);
			_t.isBeingChanged = false;
			event.stopPropagation();
		}

		_t.debounceProxy = new DebounceProxy(function() {
			callback(_t.hsvValue);
		}, 200);

		_t.updateColor = function(c) {
			if (_t.isBeingChanged) {
				return;
			}

			setColor(c);
		};

		// Some browsers fire onchange while the slider handle is being moved.
		// This is incorrect, according to the specs, but it's impossible to detect,
		// so DebounceProxy is used
		function onSliderChange() {
			_t.hsvValue.v = _t.slider.value / 100;
			_t.debounceProxy.call();
		}

		function onSliderFinish() {
			_t.hsvValue.v = _t.slider.value / 100;

			_t.debounceProxy.call();
			_t.debounceProxy.finish();
		}

		var
			eventMap = [
				[ _t.slider, "change",     onSliderChange ],
				[ _t.slider, "touchend",   onSliderFinish ],
				[ _t.slider, "mouseup",    onSliderFinish ],
				[ _t.image,  "mousedown",  onMove ],
				[ _t.image,  "mousemove",  onMove ],
				[ _t.image,  "touchmove",  onMove ],
				[ _t.image,  "touchstart", onMove ],
				[ _t.image,  "touchend",   onMouseUp ],
				[ _t.image,  "mouseup",    onMouseUp ],
				[ _t.image,  "mousedown",  onMouseDown ],
				[ _t.image,  "touchstart", onMouseDown ]
			];

		_t.destroy = function() {
			smarthome.eventMapper.unmap(eventMap);
			componentHandler.downgradeElements([ _t.button ]);
		};

		smarthome.eventMapper.map(eventMap);
		setColor(color);
	}

	Colorpicker.hsv2rgb = function(hsvColor) {
		var
			h = hsvColor.h,
			s = hsvColor.s,
			v = hsvColor.v,
			r,
			g,
			b;

		var
			i = Math.floor(h * 6),
			f = h * 6 - i,
			p = v * (1 - s),
			q = v * (1 - f * s),
			t = v * (1 - (1 - f) * s);

		switch (i % 6) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			case 5:
				r = v;
				g = p;
				b = q;
				break;
		}

		return {
			r: r * 255,
			g: g * 255,
			b: b * 255
		};
	};

	/* class ControlColorpicker extends Control */
	function ControlColorpicker(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this,
			repeatInterval = 300,
			interval;

		function hex2rgb(hex) {
			return {
				r: parseInt(hex.substr(1, 2), 16),
				g: parseInt(hex.substr(3, 2), 16),
				b: parseInt(hex.substr(5, 2), 16)
			};
		}

		_t.value = hex2rgb(_t.parentNode.getAttribute("data-value"));
		_t.modalControl = null;
		_t.buttonUp = _t.parentNode.querySelector(o.colorpicker.up);
		_t.buttonDown = _t.parentNode.querySelector(o.colorpicker.down);
		_t.buttonPick = _t.parentNode.querySelector(o.colorpicker.pick);
		_t.longPress = false;
		_t.pressed = false;

		_t.setValue = function(value) {
			var
				t = value.split(","),
				hsv = {
					h: t[0] / 360,
					s: t[1] / 100,
					v: t[2] / 100
				};
			_t.value = Colorpicker.hsv2rgb(hsv);

			if (_t.modalControl !== null) {
				_t.modalControl.updateColor(_t.value);
			}
		};

		function emitEvent(value) {
			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: value
			}));
		}

		function onMouseDown(command) {
			_t.pressed = true;
			_t.longPress = false;

			interval = setInterval(function() {
				_t.longPress = true;
				emitEvent(command);
			}, repeatInterval);
		}

		function onMouseUp(command) {
			if (!_t.pressed) {
				return;
			}

			if (!_t.longPress) {
				emitEvent(command);
			}

			_t.pressed = false;
			_t.longPress = false;

			clearInterval(interval);
		}

		function onPick() {
			var
				button;

			function onClick() {
				_t.modal.hide();
			}

			_t.modal = new Modal(renderTemplate("template-colorpicker"));
			_t.modal.show();
			_t.modal.container.classList.add(o.colorpicker.modalClass);
			_t.modal.onHide = function() {
				button.removeEventListener("click", onClick);
				_t.modalControl.destroy();
				_t.modalControl = null;
				_t.modal = null;
			};

			_t.modalControl = new Colorpicker(_t.modal.container, _t.value, function(color) {
				_t.value = Colorpicker.hsv2rgb(color);
				emitEvent(
					Math.round((color.h * 360) % 360) + "," +
					Math.round((color.s * 100) % 100) + "," +
					Math.round(color.v * 100)
				);
			});

			button = _t.modal.container.querySelector(o.colorpicker.button);
			button.addEventListener("click", onClick);
		}

		var
			upButtonMouseDown = onMouseDown.bind(null, "INCREASE"),
			downButtonMouseDown = onMouseDown.bind(null, "DECREASE"),
			upButtonMouseUp = onMouseUp.bind(null, "ON"),
			downButtonMouseUp = onMouseUp.bind(null, "OFF");

		var
			eventMap = [
				[ _t.buttonUp,   "touchstart", upButtonMouseDown ],
				[ _t.buttonUp,   "mousedown",  upButtonMouseDown ],
				[ _t.buttonUp,   "mouseleave", upButtonMouseUp ],
				[ _t.buttonUp,   "touchend",   upButtonMouseUp ],
				[ _t.buttonUp,   "mouseup",    upButtonMouseUp ],
				[ _t.buttonDown, "touchstart", downButtonMouseDown ],
				[ _t.buttonDown, "mousedown",  downButtonMouseDown ],
				[ _t.buttonDown, "touchend",   downButtonMouseUp ],
				[ _t.buttonDown, "mouseup",    downButtonMouseUp ],
				[ _t.buttonDown, "mouseleave", downButtonMouseUp ],
				[ _t.buttonPick, "click",      onPick ]
			];

		_t.destroy = function() {
			smarthome.eventMapper.unmap(eventMap);
			componentHandler.downgradeElements([
				_t.buttonUp,
				_t.buttonDown,
				_t.buttonPick
			]);
		};

		smarthome.eventMapper.map(eventMap);
	}
	/* class ControlSwitch extends Control */
	function ControlSwitch(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.input = _t.parentNode.querySelector("input[type=checkbox]");

		_t.hasValue = _t.parentNode.getAttribute("data-has-value") === "true";
		_t.valueNode = _t.parentNode.parentNode.querySelector(o.formValue);

		function onChange() {
			_t.parentNode.dispatchEvent(createEvent("control-change", {
				item: _t.item,
				value: _t.input.checked ? "ON" : "OFF"
			}));
		}

		_t.setValuePrivate = function(value, itemState) {
			var
				val = itemState === "ON";

			if (_t.input.checked !== val) {
				_t.input.checked = val;
				if (val) {
					_t.parentNode.MaterialSwitch.on();
				} else {
					_t.parentNode.MaterialSwitch.off();
				}
			}

			if (_t.hasValue) {
				_t.valueNode.innerHTML = value;
			}
		};

		_t.setValueColor = function(color) {
			_t.valueNode.style.color = color;
		};

		_t.destroy = function() {
			_t.input.removeEventListener("change", onChange);
			componentHandler.downgradeElements([ _t.parentNode ]);
		};

		_t.input.addEventListener("change", onChange);
	}

	/* class ControlSlider extends Control */
	function ControlSlider(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.input = _t.parentNode.querySelector("input[type=range]");
		_t.hasValue = _t.parentNode.getAttribute("data-has-value") === "true";
		_t.valueNode = _t.parentNode.parentNode.querySelector(o.formValue);
		_t.locked = false;

		_t.unit = _t.parentNode.getAttribute("data-unit");

		(function() {
			var
				value = parseInt(_t.input.getAttribute("data-state"), 10);

			if (isNaN(value)) {
				_t.input.value = 0;
			} else {
				_t.input.value = value;
			}

			if (_t.input.MaterialSlider) {
				_t.input.MaterialSlider.change();
			}
		})();

		function emitEvent() {
			var command = _t.input.value;
			if (_t.unit) {
				command = command + " " + _t.unit;
			}
			_t.parentNode.dispatchEvent(createEvent("control-change", {
				item: _t.item,
				value: command
			}));
		}

		_t.debounceProxy = new DebounceProxy(function() {
			emitEvent();
		}, 200);

		_t.setValuePrivate = function(value, itemState) {
			if (_t.hasValue) {
				_t.valueNode.innerHTML = value;
			}
			if (_t.locked) {
				_t.reloadIcon(itemState);
				return;
			}
			_t.input.value = itemState;
			_t.input.MaterialSlider.change();
		};

		_t.setValueColor = function(color) {
			_t.valueNode.style.color = color;
		};

		var
			unlockTimeout = null;

		function onChange() {
			_t.debounceProxy.call();
		}

		function onChangeStart() {
			if (unlockTimeout !== null) {
				clearTimeout(unlockTimeout);
			}
			_t.locked = true;
			smarthome.changeListener.pause();
		}

		function onChangeEnd() {
			// mouseUp is fired earlier than the value is changed
			// quite a dirty hack, but I don't see any other way
			_t.debounceProxy.call();
			setTimeout(function() {
				smarthome.changeListener.resume();
			}, 5);
			unlockTimeout = setTimeout(function() {
				_t.locked = false;
			}, 300);
			_t.debounceProxy.finish();
		}

		var
			eventMap = [
				[ _t.input, "touchstart", onChangeStart ],
				[ _t.input, "mousedown",  onChangeStart ],
				[ _t.input, "touchend",   onChangeEnd ],
				[ _t.input, "mouseup",    onChangeEnd ],
				[ _t.input, "change",     onChange ]
			];

		_t.destroy = function() {
			smarthome.eventMapper.unmap(eventMap);
			componentHandler.downgradeElements([ _t.input ]);
		};

		smarthome.eventMapper.map(eventMap);
	}

	/* class ControlLink */
	function ControlLink(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.target = parentNode.getAttribute("data-target");
		_t.hasValue = _t.parentNode.getAttribute("data-has-value") === "true";
		_t.container = parentNode.parentNode.querySelector(o.formValue);

		_t.setValuePrivate = function(value) {
			if (_t.hasValue) {
				_t.container.innerHTML = value;
			}
		};

		_t.setValueColor = function(color) {
			_t.container.style.color = color;
		};

		function onClick() {
			smarthome.UI.navigate(_t.target, true);
		}

		_t.destroy = function() {
			parentNode.parentNode.removeEventListener("click", onClick);
		};

		parentNode.parentNode.addEventListener("click", onClick);
	}

	function controlChangeHandler(event) {
		ajax({
			type: "POST",
			url: "/rest/items/" + event.detail.item,
			data: event.detail.value,
			headers: {"Content-Type": "text/plain"}
		});
	}

	function UI(root) {
		/* const */
		var
			NavigationState = {
				Loading: 1,
				Idle: 2
			};

		var
			_t = this,
			state = NavigationState.Idle,
			historyStack = new HistoryStack();

		_t.page = document.body.getAttribute("data-page-id");
		_t.sitemap = document.body.getAttribute("data-sitemap");
		_t.destination = null;
		_t.root = root;
		_t.loading = _t.root.querySelector(o.uiLoadingBar);
		_t.layoutTitle = document.querySelector(o.layoutTitle);
		_t.iconType = document.body.getAttribute(o.iconTypeAttribute);
		_t.notification = document.querySelector(o.notify);

		_t.escapeHtml = function(text) {
			var
				escapedText = text,
				escapeTable = [
					[ /&/g, "&amp;" ],
					[ /</g, "&lt;"  ],
					[ />/g, "&gt;"  ]
				];

			for (var i = 0; i < escapeTable.length; i++) {
				escapedText = escapedText.replace(escapeTable[i][0], escapeTable[i][1]);
			}

			return escapedText;
		};

		_t.setTitle = function(title) {
			document.querySelector("title").innerHTML = title;
			_t.layoutTitle.innerHTML = title;
		};

		function replaceContent(xmlResponse) {
			var
				page = xmlResponse.documentElement,
				nodeArray = [];

			if (page.tagName !== "page") {
				return;
			}

			[].forEach.call(page.childNodes, function(node) {
				if (!(node instanceof Text)) {
					nodeArray.push(node);
				}
			});

			// HTML entities are already escaped on server
			_t.setTitle(nodeArray[0].textContent);

			var
				contentElement = document.querySelector(".page-content");

			while (contentElement.firstChild) {
				contentElement.removeChild(contentElement.firstChild);
			}

			contentElement.insertAdjacentHTML("beforeend", nodeArray[1].textContent);
		}

		_t.upgradeComponents = function() {
			var
				upgrade = componentHandler.upgradeElement;

			[].slice.call(document.querySelectorAll(o.formControls)).forEach(function(e) {
				switch (e.getAttribute("data-control-type")) {
				case "setpoint":
				case "rollerblind":
				case "colorpicker":
				case "buttons":
					[].slice.call(e.querySelectorAll("button")).forEach(function(button) {
						upgrade(button, "MaterialButton");
						upgrade(button, "MaterialRipple");
					});
					break;
				case "checkbox":
					upgrade(e, "MaterialSwitch");
					break;
				case "slider":
					upgrade(e.querySelector("input[type=range]"), "MaterialSlider");
					break;
				default:
					break;
				}
			});
		};

		_t.showLoadingBar = function() {
			_t.loading.style.display = "block";
		};

		_t.hideLoadingBar = function() {
			_t.loading.style.display = "";
		};

		_t.navigateCallback = function(request) {
			state = NavigationState.Idle;

			if (_t.pushState) {
				historyStack.push(_t.page);
			}

			[].forEach.call(document.querySelectorAll(o.formControls), function(e) {
				e.removeEventListener("control-change", controlChangeHandler);
			});

			for (var id in smarthome.dataModel) {
				smarthome.dataModel[id].destroy();
			}

			replaceContent(request.responseXML);

			if (_t.pushState) {
				historyStack.replace(_t.newPage, _t.destination);
			}

			_t.page = _t.newPage;
			_t.upgradeComponents();
			_t.initControls();

			_t.hideLoadingBar();

			if (_t.sitemap !== _t.page) {
				_t.header.classList.remove("navigation-home");
				_t.header.classList.add("navigation-page");
			} else {
				_t.header.classList.add("navigation-home");
				_t.header.classList.remove("navigation-page");
			}

			smarthome.changeListener.navigate(_t.page);
		};

		_t.navigate = function(page, pushState) {
			if (state !== NavigationState.Idle) {
				return;
			}

			state = NavigationState.Loading;
			_t.pushState =
				((pushState === undefined) ||
				(pushState === true));
			_t.newPage = page;

			_t.showLoadingBar();
			_t.destination =
				"/basicui/app?w=" + page +
				"&sitemap=" + smarthome.UI.sitemap;

			ajax({
				url: _t.destination +
					"&subscriptionId=" + smarthome.subscriptionId +
					"&__async=true",
				callback: _t.navigateCallback
			});

			if (smarthome.UI.currentModal !== undefined) {
				smarthome.UI.currentModal.hide();
			}
		};

		_t.initControls = function() {
			smarthome.dataModel = {};

			function appendControl(control) {
				if (control.id.length > 0) {
					smarthome.dataModel[control.id] = control;
				}
			}

			[].forEach.call(document.querySelectorAll(o.formControls), function(e) {
				/*eslint no-fallthrough:0*/
				switch (e.getAttribute("data-control-type")) {
				case "setpoint":
					appendControl(new ControlSetpoint(e));
					break;
				case "rollerblind":
					appendControl(new ControlRollerblinds(e));
					break;
				case "buttons":
					appendControl(new ControlSelection(e));
					break;
				case "selection":
					appendControl(new ControlRadio(e));
					break;
				case "checkbox":
					appendControl(new ControlSwitch(e));
					break;
				case "slider":
					appendControl(new ControlSlider(e));
					break;
				case "chart":
				case "image":
					appendControl(new ControlImage(e, true));
					break;
				case "image-link":
					appendControl(new ControlImage(e, false));
				case "text-link":
				case "group":
					appendControl(new ControlLink(e));
					break;
				case "text":
					appendControl(new ControlText(e));
					break;
				case "colorpicker":
					appendControl(new ControlColorpicker(e));
					break;
				case "video":
				case "webview":
				case "mapview":
					appendControl(new Control(e));
					break;
				default:
					break;
				}
				/*eslint no-fallthrough:0*/
				e.addEventListener("control-change", controlChangeHandler);
			});

			[].forEach.call(document.querySelectorAll(o.form), function(e) {
				appendControl(new Frame(e));
			});
		};

		_t.showNotification = function(text) {
			_t.notification.innerHTML = text;
			_t.notification.classList.remove(o.notifyHidden);
		};

		_t.hideNotification = function() {
			_t.notification.classList.add(o.notifyHidden);
		};

		historyStack.replace(_t.page, document.location.href);

		(function() {
			_t.header = document.querySelector(o.layoutHeader);
			if (_t.sitemap !== _t.page) {
				_t.header.classList.remove("navigation-home");
				_t.header.classList.add("navigation-page");
			}

			document.querySelector(o.backButton).addEventListener("click", function() {
				if (historyStack.level > 0) {
					history.back();
				} else {
					location.href = location.origin + "/basicui/app?sitemap=" + smarthome.UI.sitemap;
				}
			});
		})();
	}

	function AbstractChangeListener() {
		this.paused = false;

		this.pause = function() {
			this.paused = true;
		};

		this.resume = function() {
			this.paused = false;
		};

		this.extractValueFromLabel = function(label) {
			var
				value = null;

			if (
				(typeof(label) === "string") &&
				(label.indexOf("[") !== -1) &&
				(label.indexOf("]") !== -1)
			) {
				var
					pos = label.indexOf("[") + 1;

				value = label.substr(
					pos,
					label.lastIndexOf("]") - pos
				);
			}

			return value;
		};

		this.getTitleFromLabel = function(label) {
			var
				value = this.extractValueFromLabel(label),
				title = null;

			if (value  !== null) {
				title = label.substr(0, label.indexOf("[")) + value;
			}

			return title;
		};

		this.updateWidget = function(widget, update) {
			var
				value = this.extractValueFromLabel(update.label),
				makeVisible = false;

			if (widget.visible !== update.visibility) {
				makeVisible = update.visibility;
				smarthome.UI.layoutChangeProxy.push({
					widget: widget,
					visibility: update.visibility
				});
			}

			if (makeVisible || update.state !== "NULL") {
				if (value === null) {
					value = update.state;
				}
				widget.setValue(smarthome.UI.escapeHtml(value), update.state, update.visibility);
			}

			[{
				apply: widget.setLabel,
				data: update.label,
				fallback: null
			}, {
				apply: widget.setLabelColor,
				data: update.labelcolor,
				fallback: ""
			}, {
				apply: widget.setValueColor,
				data: update.valuecolor,
				fallback: ""
			}].forEach(function(e) {
				if (e.data !== undefined) {
					e.apply(e.data);
				} else if (e.fallback !== null) {
					e.apply(e.fallback);
				}
			});
		};
	}

	function ChangeListenerEventsource(subscribeLocation) {
		AbstractChangeListener.call(this);

		var
			_t = this;

		_t.navigate = function(){};
		_t.source = new EventSource(subscribeLocation);

		_t.source.addEventListener("event", function(payload) {
			if (_t.paused) {
				return;
			}

			var
				data = JSON.parse(payload.data),
				state,
				title;

			if (data.TYPE === "ALIVE") {
				return;
			}

			if (data.TYPE === "SITEMAP_CHANGED") {
				var oldLocation = window.location.href;
				var parts = oldLocation.split("?");
				if (parts.length > 1) {
					window.location.href = parts[0] + "?sitemap=" + data.sitemapName;
				} else {
					window.location.reload(true);
				}
				_t.pause();
				return;
			}

			if (
				!(data.widgetId in smarthome.dataModel) &&
				(data.widgetId !== smarthome.UI.page)
			) {
				return;
			}

			if (data.state === undefined) {
				state = data.item.state;
			} else {
				state = data.state;
			}

			title = _t.getTitleFromLabel(data.label);

			if (
				(data.widgetId === smarthome.UI.page) &&
				(title !== null)
			) {
				smarthome.UI.setTitle(smarthome.UI.escapeHtml(title));
			} else if (smarthome.dataModel[data.widgetId] !== undefined) {
				var update = {
					visibility: data.visibility,
					state: state,
					label: data.label,
					labelcolor: data.labelcolor,
					valuecolor: data.valuecolor
				};
				_t.updateWidget(smarthome.dataModel[data.widgetId], update);
			}
		});

		_t.source.onerror = function() {
			_t.source.close();
			_t.connectionError();
		};
	}

	function ChangeListenerLongpolling() {
		AbstractChangeListener.call(this);

		var
			_t = this;

		_t.sitemap = document.body.getAttribute("data-sitemap");
		_t.page = document.body.getAttribute("data-page-id");

		function applyChanges(response) {
			var
				title,
				id;

			try {
				response = JSON.parse(response);
			} catch (e) {
				return;
			}

			title = _t.getTitleFromLabel(response.title);
			if (title !== null) {
				smarthome.UI.setTitle(smarthome.UI.escapeHtml(title));
			}

			function walkWidgets(widgets) {
				widgets.forEach(function(widget) {
					if (
						widget.widgetId === undefined ||
						smarthome.dataModel[widget.widgetId] === undefined
					) {
						return;
					}

					var
						w = smarthome.dataModel[widget.widgetId],
						state = "NULL",
						update;

					if (widget.item !== undefined) {
						if (widget.state === undefined) {
							state = widget.item.state;
						} else {
							state = widget.state;
						}
					}
					if (!w.visible || widget.item !== undefined) {
						update = {
							visibility: true,
							state: state,
							label: widget.label,
							labelcolor: widget.labelcolor,
							valuecolor: widget.valuecolor
						};
						_t.updateWidget(w, update);
					}
					w.handled = true;
				});
			}

			for (id in smarthome.dataModel) {
				smarthome.dataModel[id].handled = false;
			}

			walkWidgets(response.widgets);
			response.widgets.forEach(function(frameWidget) {
				// Handle widgets in frame
				walkWidgets(frameWidget.widgets);
			});

			for (id in smarthome.dataModel) {
				var
					w = smarthome.dataModel[id];

				if (w.visible && !w.handled) {
					smarthome.UI.layoutChangeProxy.push({
						widget: w,
						visibility: false
					});
				}
			}
		}

		function start() {
			var
				cacheSupression = Math.random().toString(16).slice(2);

			_t.request = ajax({
				url: "/rest/sitemaps/" + _t.sitemap + "/" + _t.page + "?_=" + cacheSupression,
				headers: {"X-Atmosphere-Transport": "long-polling"},
				callback: function(request) {
					if (!_t.paused) {
						applyChanges(request.responseText);
					}
					setTimeout(function() {
						start();
					}, 1);
				},
				error: function() {
					// Wait 1s and restart long-polling
					setTimeout(function() {
						start();
					}, 1000);
				}
			});
		}

		_t.navigate = function(page) {
			_t.request.abort();
			_t.page = page;
			start();
		};

		_t.pause = function() {
			_t.request.abort();
			_t.paused = true;
		};

		_t.resume = function() {
			_t.paused = false;
			start();
		};

		start();
	}

	function ChangeListener() {
		var
			_t = this;

		_t.subscribeRequestURL = "/rest/sitemaps/events/subscribe";
		_t.reconnectInterval = null;
		_t.subscribeResponse = null;
		_t.suppressErrorsState = false;

		function initSubscription(address) {
			if (featureSupport.eventSource && address !== null) {
				ChangeListenerEventsource.call(_t, address);
			} else {
				ChangeListenerLongpolling.call(_t);
			}
		}

		function connectionRestoredNavigateCallback() {
			// This will override _t.navigate back to
			// its normal state
			_t.startSubscriber(_t.subscribeResponse);
			_t.subscribeResponse = null;
		}

		_t.connectionRestored = function(response) {
			clearInterval(_t.reconnectInterval);

			// Temporarily replace navigation callback
			_t.navigate = connectionRestoredNavigateCallback;

			// Once navigation is completed, this will be used
			// to restart SSE subscription
			_t.subscribeResponse = response;

			smarthome.UI.hideNotification();
			// Reload current page without affecting the history
			smarthome.UI.navigate(smarthome.UI.page, false);
		};

		_t.connectionError = function() {
			if (_t.suppressErrorsState) {
				return;
			}

			var
				notify = renderTemplate(o.notifyTemplateOffline, {});

			smarthome.UI.showNotification(notify);

			_t.reconnectInterval = setInterval(function() {
				ajax({
					url: _t.subscribeRequestURL,
					type: "POST",
					callback: _t.connectionRestored
				});
			}, 10000);
		};

		_t.suppressErrors = function() {
			_t.suppressErrorsState = true;
		};

		_t.startSubscriber = function(response) {
			var
				responseJSON,
				subscribeLocation,
				subscribeLocationArray,
				sitemap,
				subscriptionId,
				page;

			try {
				responseJSON = JSON.parse(response.responseText);
			} catch (e) {
				return;
			}

			if (responseJSON.status !== "CREATED") {
				return;
			}

			try {
				subscribeLocation = responseJSON.context.headers.Location[0];
			} catch (e) {
				return;
			}

			subscribeLocationArray = subscribeLocation.split("/");
			subscriptionId = subscribeLocationArray[subscribeLocationArray.length - 1];

			sitemap = document.body.getAttribute("data-sitemap");
			page = document.body.getAttribute("data-page-id");

			smarthome.subscriptionId = subscriptionId;

			initSubscription(subscribeLocation +
				"?sitemap=" + sitemap +
				"&pageid=" + page);
		};

		_t.subscriberError = function() {
			var
				notify = renderTemplate(o.notifyTemplateLongPollingMode, {});

			// Failback to long polling mode
			smarthome.UI.showNotification(notify);
			initSubscription(null);
		};

		ajax({
			url: _t.subscribeRequestURL,
			type: "POST",
			callback: _t.startSubscriber,
			error: _t.subscriberError
		});
	}

	document.addEventListener("DOMContentLoaded", function() {
		smarthome.UI = new UI(document);
		smarthome.UI.layoutChangeProxy = new VisibilityChangeProxy(100, 50);
		smarthome.eventMapper = new EventMapper();
		smarthome.UI.initControls();
		smarthome.changeListener = new ChangeListener();

		window.addEventListener("beforeunload", function() {
			smarthome.changeListener.suppressErrors();
		});
	});
})({
	itemAttribute: "data-item",
	idAttribute: "data-widget-id",
	iconAttribute: "data-icon",
	iconTypeAttribute: "data-icon-type",
	controlButton: "button",
	buttonActiveClass: "mdl-button--accent",
	modal: ".mdl-modal",
	modalContainer: ".mdl-modal__content",
	selectionRows: ".mdl-form__selection-rows",
	form: ".mdl-form",
	formTitle: ".mdl-form__title",
	formHidden: "mdl-form--hidden",
	formControls: ".mdl-form__control",
	formRowHidden: "mdl-form__row--hidden",
	formValue: ".mdl-form__value",
	formRadio: ".mdl-radio",
	formRadioControl: ".mdl-radio__button",
	formIcon: ".mdl-form__icon img",
	formLabel: ".mdl-form__label",
	uiLoadingBar: ".ui__loading",
	layoutTitle: ".mdl-layout-title",
	layoutHeader: ".mdl-layout__header",
	backButton: ".navigation__button-back",
	rollerblind: {
		up: ".mdl-form__rollerblind--up",
		down: ".mdl-form__rollerblind--down",
		stop: ".mdl-form__rollerblind--stop"
	},
	setpoint: {
		up: ".mdl-form__setpoint--up",
		down: ".mdl-form__setpoint--down"
	},
	colorpicker: {
		up: ".mdl-form__colorpicker--up",
		down: ".mdl-form__colorpicker--down",
		pick: ".mdl-form__colorpicker--pick",
		modalClass: "mdl-modal--colorpicker",
		image: ".colorpicker__image",
		handle: ".colorpicker__handle",
		slider: ".colorpicker__brightness",
		background: ".colorpicker__background",
		colorpicker: ".colorpicker",
		button: ".colorpicker__buttons > button"
	},
	notify: ".mdl-notify__container",
	notifyHidden: "mdl-notify--hidden",
	notifyTemplateOffline: "template-offline-notify",
	notifyTemplateLongPollingMode: "template-long-polling-mode-notify"
});
