var underscore = angular.module('underscore', []);
underscore.factory('_', ['$window', function($window) {
    return $window._; // assumes underscore has already been loaded on the page
}]);

angular
.module('neeo', ['ui.bootstrap', 'underscore', 'ngSanitize', 'ui.select'])
.controller('NeeoCtrl', neeoController);

neeoController.$inject = ['$scope', '$http', '$timeout', '$window', 'orderByFilter', '$uibModal', '_'];
function neeoController($scope, $http, $timeout, $window, orderBy, $uibModal, _) {

    var ctrl = this;

    ctrl.deviceTypes = [ 
        { value: "", text: "(Exclude)"}, 
        { value: "ACCESSORY", text: "Accessory"}, 
        { value: "AUDIO", text: "Audio"},
        { value: "AVRECEIVER", text: "AV Receiver"},
        { value: "CLIMA", text: "Climate Control"},
        { value: "DVB", text: "DVB"},
        { value: "DVD", text: "DVD Player"},
        { value: "GAMECONSOLE", text: "Game Console"},
        { value: "HDMISWITCH", text: "HDMI Switch"},
        { value: "LIGHT", text: "Light"}, 
        { value: "MEDIAPLAYER", text: "Media Player"},
        { value: "PROJECTOR", text: "Projector"},
        { value: "SOUNDBAR", text: "Soundbar"},
        { value: "THERMOSTAT", text: "Thermostat"},
        { value: "TUNER", text: "Tuner"},
        { value: "TV", text: "TV"},
        { value: "VOD", text: "Video on Demand"}
        ];

    ctrl.hardButtons = [
        { value:"MUTE TOGGLE", text: "Mute"},
        { value:"HOME", text: "Home"},
        { value:"MENU", text: "Menu"},
        { value:"EXIT", text: "Back"},
        { value:"ENTER", text: "OK"},
        { value:"CURSOR LEFT", text: "Cursor Left"},
        { value:"CURSOR RIGHT", text: "Cursor Right"},
        { value:"CURSOR UP", text: "Cursor Up"},
        { value:"CURSOR DOWN", text: "Cursor Down"},
        { value:"VOLUME UP", text: "Volume Up"},
        { value:"VOLUME DOWN", text: "Volume Down"},
        { value:"CHANNEL UP", text: "Channel Up"},
        { value:"CHANNEL DOWN", text: "Channel Down"}
        ];

    ctrl.controlButtons = [
        { value:"POWER ON", text: "Power On"},
        { value:"POWER OFF", text: "Power Off"},
        { value:"POWER TOGGLE", text: "Power Toggle"},
        { value:"PLAY", text: "Play"},
        { value:"PAUSE", text: "Pause"},
        { value:"STOP", text: "Stop"},
        { value:"SKIP BACKWARD", text: "Skip Backward"},
        { value:"SKIP FORWARD", text: "Skip Forward"},
        { value:"FORWARD", text: "Forward"},
        { value:"PREVIOUS", text: "Previous"},
        { value:"NEXT", text: "Next"},
        { value:"REVERSE", text: "Reverse"},
        { value:"PLAY PAUSE TOGGLE", text: "Play/Pause Toggle"},
        { value:"INFO", text: "Info"},
        { value:"CHANNEL SEARCH", text: "Channel Search"},
        { value:"FAVORITE", text: "Favorite"},
        { value:"DIGIT 0", text: "Digit 0"},
        { value:"DIGIT 1", text: "Digit 1"},
        { value:"DIGIT 2", text: "Digit 2"},
        { value:"DIGIT 3", text: "Digit 3"},
        { value:"DIGIT 4", text: "Digit 4"},
        { value:"DIGIT 5", text: "Digit 5"},
        { value:"DIGIT 6", text: "Digit 6"},
        { value:"DIGIT 7", text: "Digit 7"},
        { value:"DIGIT 8", text: "Digit 8"},
        { value:"DIGIT 9", text: "Digit 9"},
        { value:"DIGIT SEPARARTOR", text: "Digit Separator"},
        { value:"FUNCTION RED", text: "Red"},
        { value:"FUNCTION GREEN", text: "Green"},
        { value:"FUNCTION YELLOW", text: "Yellow"},
        { value:"FUNCTION BLUE", text: "Blue"}
        ];

    ctrl.dualButtons = [
        { value:"POWERONOFF", text: "Power On/Off"},
        { value:"VOLUMES", text: "Volume Up/Down"},
        { value:"CHANNELS", text: "Channel Up/Down"},
        { value:"CURSORUPDOWN", text: "Keypad Up/Down"},
        { value:"CURSORLEFTRIGHT", text: "Keypad Left/Right"}
        ];
    
    ctrl.isSpecialButton = function(channel) {
        if (channel.label == undefined) return false;
        var ulabel = channel.label.toLowerCase();
        return _.find(ctrl.hardButtons, function(l) { return l.value.toLowerCase() === ulabel; }) !== undefined 
        || _.find(ctrl.controlButtons, function(l) { return l.value.toLowerCase() === ulabel; }) !== undefined;
    }

    ctrl.isButton = function(channel) {
        return channel.isReadOnly !== true;
    }

    ctrl.isImageUrl = function(channel) {
        return _.find(channel.acceptedCommandTypes, function(type) {            
            return type === "stringtype";
        }) !== undefined;
    }

    ctrl.isSwitch = function(channel) {

        if (channel.isReadOnly === true) {
            return false;
        }

        return _.find(channel.acceptedCommandTypes, function(type) {            
            return _.contains(["onofftype", "increasedecreasetype", "nextprevioustype","openclosedtype","playpausetype","rewindfastforwardtype","stopmovetype","updowntype"], type);
        }) !== undefined;
    }

    ctrl.isSlider = function(channel) {
        return channel.isReadOnly === true ? false : (channel.acceptedCommandTypes.includes("decimaltype") || channel.acceptedCommandTypes.includes("percenttype"));
    }

    ctrl.isPower = function(channel) {
        return channel.acceptedCommandTypes.length === 1 && channel.acceptedCommandTypes.includes("onofftype");
    }

    ctrl.isSensor = function(channel) {
        return (channel.acceptedCommandTypes.includes("decimaltype") || channel.acceptedCommandTypes.includes("percenttype"));
    }
    
    ctrl.addVirtualDevice = function() {
        $http.get("neeostatus/getvirtualdevice")
        .then(function(response) {
            if (response.data.success === true) {
                var newDevice = response.data.device;
                newDevice.isNewDevice = true;
                ctrl.devices.splice(0,0,newDevice);
            } else {
                $.jGrowl("Error getting new virtual device: " + response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
            }
        }
        , function(response) {
            $.jGrowl("Error getting new virtual device: " + response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
        });
    }
    
    ctrl.deleteDevice = function(device) {
        for(var i = ctrl.devices.length - 1; i>=0;i--) {
            if (ctrl.devices[i] === device) {
                if (!device.isNewDevice) {
                    $http.post("neeostatus/deletedevice", device.uid)
                    .then(function(response) {
                        if (response.data.success === true) {
                            ctrl.devices.splice(i, 1);
                            
                            if (ctrl.selectedDevice === device) {
                                ctrl.selectedDevice = undefined;
                            }
                        } else {
                            $.jGrowl(response.data.message, {theme : 'jgrowl-error'});
                        }
                    }, function(response) {
                        $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error'});
                    })
                } else {
                    ctrl.devices.splice(i, 1);
                    
                    if (ctrl.selectedDevice === device) {
                        ctrl.selectedDevice = undefined;
                    }
                }
                break;
            }
        }
    }
    
    ctrl.exportRules = function(device) {
        $http.post("neeostatus/exportrules", device.uid)
        .then(function(response) {
            if (response.data.success && response.data.success === false) {
                $.jGrowl(response.data.message, {theme : 'jgrowl-error'});
            } else {
                var blob = new Blob([response.data], {type: "text/plain"});
                saveAs(blob, device.name + ".rules");
            }
        }, function(response) {
            $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error'});
        })
    }
    
    var resetDevice = function (device) {
        device.isNewDevice = false;
        device.old = {
           type: device.type || '',
           deviceCapabilities: device.deviceCapabilities || [],
           timing: {
               standbyCommandDelay: device.timing.standbyCommandDelay,
               sourceSwitchDelay: device.timing.sourceSwitchDelay,
               shutdownDelay: device.timing.shutdownDelay
           }
        };
        _.each(device.channels, function(c) {
            c.old = {
              type: c.type || '',
              value: c.value || '',
              label: c.label || ''
            };
        })
        
        device.isNewDevice = false;
        device.hasChanges = false;
        device.hasChannelChanges = false;
        device.hasAdded = false;
        device.hasDeleted = false;
    };
    
    $scope.$watch(function() { return ctrl.devices; }, function(nv, ov) {
        for(var d = ctrl.devices.length - 1; d>=0; d--) {
            var device = ctrl.devices[d];
            device.hasChanges = false;
            if (device.old.type !== (device.type || '') 
                || device.timing.standbyCommandDelay != device.old.timing.standbyCommandDelay 
                || device.timing.sourceSwitchDelay != device.old.timing.sourceSwitchDelay 
                || device.timing.shutdownDelay != device.old.timing.shutdownDelay 
                || !_.isEqual(device.deviceCapabilities, device.old.deviceCapabilities) 
                    
            ) {
                device.hasChanges = true;
            }
        }
    }, true);

    $scope.$watch(function() { return ctrl.selectedDevice; }, function(nv, ov) {
        if (ctrl.selectedDevice !== undefined) {
            ctrl.selectedDevice.hasChannelChanges = false;
            for(var c = ctrl.selectedDevice.channels.length - 1 ; c>=0; c--) {
                var chnl = ctrl.selectedDevice.channels[c];
                if (chnl.old.label !== undefined && chnl.old.label !== (chnl.label  || '')) {
                    ctrl.selectedDevice.hasChannelChanges = true;
                    break;
                } else if (chnl.old.value !== undefined && chnl.old.value !== (chnl.value  || '')) {
                    ctrl.selectedDevice.hasChannelChanges = true;
                    break;
                } else if (chnl.old.type !== undefined && chnl.old.type !== (chnl.type || '')) {
                    ctrl.selectedDevice.hasChannelChanges = true;
                    break;
                }
            }
        }

    }, true);

    ctrl.brainStatuses = [];
    ctrl.selectedBrainId = undefined;

    ctrl.devices = [];
    ctrl.selectedDevice = undefined;

    $scope.$watch(function() { return ctrl.brainStatuses; }, function(nv, ov) {
        var brainIds = [ { value: "all", text: "All Brains" }];
        if (nv !== undefined) {
            for(var i = nv.length -1; i>=0; i--) {
                brainIds.push({ value: nv[i].brainId, text: nv[i].brainId })
            }
        }
    }, true)

    ctrl.isVirtualDevice = function(device) {
        // MUST match NeeoConstants.java
        return device.thingType == "virtual";
    }
    
    ctrl.selectDevice = function(device) {
        if (ctrl.selectedDevice == device) {
            ctrl.selectedDevice = undefined;
        } else {
            ctrl.selectedDevice = device;
        }
    }

    ctrl.sortDevices = function() {
        ctrl.devices = orderBy(ctrl.devices, function(device) {
            var sortString = "";
            
            if (device.isEditing === true) {
                sortString = "0:";
            } else if (device.isNewDevice) {
                sortString = "1:";
            } else if (ctrl.isVirtualDevice(device)) {
                sortString = "2:";
            } else {
                sortString = "3:";
            }
            
            return sortString + device.name;
        });
    }
    

    ctrl.addChannel = function(device, channel) {
        var itemMap = new Map();
        
        for(var x = device.channels.length - 1; x>=0;x--) {
            var itemName = device.channels[x].itemName;
            var channelNbr = device.channels[x].channelNbr;
            if (itemMap.has(itemName)) {
                channelNbr = Math.max(itemMap.get(itemName), channelNbr);
            } 
            
            itemMap.set(itemName, channelNbr);
            
        }
        
        var maxNbr = itemMap.has(channel.itemName) ? itemMap.get(channel.itemName) : 0;
        
        var newChannel = jQuery.extend(true, {}, channel);
        
        newChannel.channelNbr = maxNbr +1;
        device.channels.push(newChannel);
        device.hasAdded = true;
    }
    
    ctrl.addTriggerChannel = function(device) {
        var newChannel = {
                kind: "trigger",
                itemName: "",
                channelNbr: 1,
                type: "button",
                label: "",
                value: ""
        };
        
        // make selected if not
        if (ctrl.selectedDevice !== device) {
            ctrl.selectedDevice = device;
        }
        
        device.channels.push(newChannel);
        device.hasAdded = true;
    }
    
    ctrl.removeChannel = function(device, channel) {
        if (device.thingType !== 'virtual' && channel.kind === 'item' && channel.channelNbr === 1) {
            $.jGrowl("Cannot remove the root channel number (1)", {theme : 'jgrowl-error'});
        } else {
            for(var x = device.channels.length - 1; x>=0;x--) {
                if (device.channels[x] == channel) {
                    device.channels.splice(x, 1);
                    device.hasDeleted = true;
                    break;
                }
            }
            
            // If we had multiple channels and we deleted channel nbr 1,
            // make the earliest channel of the same item label number 1
            if (ctrl.isVirtualDevice(device) && channel.channelNbr === 1) {
                var max = device.channels.length;
                for(var x = 0; x < max; x++) {
                    if (device.channels[x].itemLabel === channel.itemLabel) {
                        device.channels[x].channelNbr = 1;
                        break;
                    }
                }
            }
        }
    }
    
    ctrl.hasChannel = function(device, channelKind) {
        if (device == null || device == undefined) {
            return false;
        }
        
        return device.channels.find((el)=>el.kind === channelKind) !== undefined;
    }
    
    ctrl.getChannels = function(device, channelKind) {
        var channels = device.channels.filter((el)=>el.kind === channelKind);
        return channelKind === "trigger" ? channels : orderBy(channels, ["itemName", "channelNbr"]);
    }
    
    var handleDeviceOverlay = function(device, newDevice) {
        for(var x = ctrl.devices.length - 1; x >= 0; x--) {
            if (ctrl.devices[x] === device) {
                ctrl.devices[x] = newDevice;
                if (ctrl.selectedDevice === device) {
                    ctrl.selectedDevice = newDevice;
                }
                break;
            }
        }        
    }

    ctrl.restoreDevice = function(device) {
        $http.post("neeostatus/restoredevice", device.uid)
        .then(function(response) {
            if (response.data.success === true) {
                handleDeviceOverlay(device, response.data.device);
            } else {
                $.jGrowl(response.data.message, {theme : 'jgrowl-error'});
            }
        }, function(response) {
            $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error'});
        })    
    }
    
    ctrl.refreshDevice = function(device) {
        $http.post("neeostatus/refreshdevice", device.uid)
        .then(function(response) {
            if (response.data.success === true) {
                handleDeviceOverlay(device, response.data.device);
            } else {
                $.jGrowl(response.data.message, {theme : 'jgrowl-error'});
            }
        }, function(response) {
            $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error'});
        })
    }
    
    ctrl.saveDevice = function(device) {
        $("div.jGrowl").jGrowl("close"); // close all messages
        $http.post("neeostatus/updatedevice", device)
        .then(function(response) {
            if (response.data.success === true) {
                resetDevice(device);
                device.old == { timing: {}};
                
                _.each(device.channels, function(c) {
                    c.old = {};
                })
                
                device.isNewDevice = false;
                device.hasChanges = false;
                device.hasChannelChanges = false;
                device.hasAdded = false;
                device.hasDeleted = false;

                if (device.keys !== undefined && device.keys.length > 0) {
                    $.jGrowl("'" + device.name + "' was saved but you'll need to drop and re-add the device on the brain to see changes/");
                }
            } else {
                $.jGrowl(response.data.message, {theme : 'jgrowl-error', sticky: true});
            }
        }, function(response) {
            $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
        })
    }
    
    ctrl.blinkLed = function(brainId) {
        $http.get("neeostatus/blinkled?brainid=" + brainId)
        .then(function(response) {
            if (response.data.success === true) {
                // do nothing
            } else {
                $.jGrowl(response.data.message, {theme : 'jgrowl-error', sticky: true});
            }
        }, function(response) {
            $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
        })
    }

    ctrl.showEui = function(brainUrl) {
        var idx = brainUrl.lastIndexOf(":");
        var baseUrl = brainUrl.substring(0, idx);
        $window.open(baseUrl + ":3200/eui");        
    }
    
    ctrl.showLog = function(brainId) {
        $http.get("neeostatus/getlog?brainid=" + brainId)
        .then(function(response) {
            if (response.data.success === true) {
                var modalInstance = $uibModal.open({
                    ariaLabelledBy: 'showLog-title',
                    ariaDescribedBy: 'showLog-body',
                    templateUrl: 'showlog.html',
                    controller: 'ShowLog',
                    controllerAs: '$ctrl',
                    resolve: {
                      log: function () {
                        return response.data.message;
                      }
                    }
                  });

            } else {
                $.jGrowl(response.data.message, {theme : 'jgrowl-error', sticky: true});
            }
        }, function(response) {
            $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
        })
    }

    ctrl.addBrain = function() {
        var ipAddress = prompt("Please enter the IP Address of the NEEO brain:", "");
        if (ipAddress !== null && ipAddress !== "") {
            ctrl.isRefreshingBrains = true;
            $http.post("neeostatus/addbrain", { brainIp: ipAddress })
            .then(function(response) {
                if (response.data.success === true) {
                    ctrl.getBrainStatuses();
                } else {
                    $.jGrowl(response.data.message, {theme : 'jgrowl-error', sticky: true});
                }
                ctrl.isRefreshingBrains = false;
            }, function(response) {
                $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
                ctrl.isRefreshingBrains = false;
            })            
        }
    }

    ctrl.removeBrain = function(brainId) {
        if (confirm("Are you sure you wish to delete this brain?")) {
            ctrl.isRefreshingBrains = true;
            $http.post("neeostatus/removebrain", { brainId: brainId })
            .then(function(response) {
                if (response.data.success === true) {
                    for(var i = ctrl.brainStatuses.length - 1; i>=0; i--) {
                        if (ctrl.brainStatuses[i].brainId === brainId) {
                            ctrl.brainStatuses.splice(i, 1);
                        }
                    }  
                } else {
                    $.jGrowl(response.data.message, {theme : 'jgrowl-error', sticky: true});
                }
                ctrl.isRefreshingBrains = false;
            }, function(response) {
                $.jGrowl(response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
                ctrl.isRefreshingBrains = false;
            })
        }
    }

    ctrl.isRefreshingDevices = false;
    ctrl.getDevices = function() {
        var pendingChanges = false;
        for(var i = ctrl.devices.length - 1; i>=0; i--) {
            var device = ctrl.devices[i];
            if (device.hasChanges === true || device.hasChannelChanges === true || device.hasAdded === true || device.hasDeleted === true) {
                pendingChanges = true;
                break;
            }
        }
        
        if (pendingChanges && !confirm("You have pending changes - are you sure you want to LOSE those changes and refresh?")) {
            return; 
        }
        
        ctrl.isRefreshingDevices = true;
        ctrl.devices = [];
        
        $http.get("neeostatus/thingstatus")
        .then(function(response) {
            var data = response.data;
            // update insert
            for(var i = data.length - 1; i>=0; i--) {
                
                var device = data[i];                
                resetDevice(device);
                device.channels = orderBy(device.channels, ["itemName", "channelNbr"]);
                ctrl.devices.push(device);
            }
            ctrl.isRefreshingDevices = false;
            ctrl.sortDevices();
        }
        , function(response) {
            ctrl.devices = [];
            ctrl.isRefreshingDevices = false;
        });
        
        ctrl.getItems();
    }

    ctrl.isRefreshingBrains = false;
    ctrl.getBrainStatuses = function() {
        ctrl.isRefreshingBrains = true;
        ctrl.brainStatuses = [];
        $http.get("neeostatus/brainstatus")
        .then(function(response) {
            var data = response.data;
            for(var i = data.length - 1; i>=0; i--) {
                ctrl.brainStatuses.push(data[i]);
            }
            ctrl.isRefreshingBrains = false;
        }
        , function(data) {
            ctrl.brainStatuses = [];
            ctrl.isRefreshingBrains = false;
        });
    }
    
    ctrl.thingSearch = undefined;
    ctrl.clearSearch = function() { ctrl.thingSearch = undefined; }
    
    ctrl.items = [];
    ctrl.selectedItems = [];
    
    ctrl.getItems = function() {
        $http.get("/rest/items")
        .then(function(response) {
            if (response.status === 200) {
                ctrl.items = response.data;
            } else {
                $.jGrowl("Error retrieving items: " + response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
            }
        }
        , function(data) {
            ctrl.items = [];
        });
    }
    
    ctrl.addVirtualItems = function(device) {
        var modalInstance = $uibModal.open({
            ariaLabelledBy: 'addItems-title',
            ariaDescribedBy: 'addItems-body',
            templateUrl: 'additems.html',
            controller: 'AddItemsInstanceCtrl',
            controllerAs: '$ctrl',
            resolve: {
              items: function () {
                return ctrl.items;
              },
              devices: function () {
                  return ctrl.devices;
              }
            },
            windowClass: 'app-modal-virtualitems'
          });
        
        modalInstance.result.then(function (selectedItems) {
            // make selected if not
            if (ctrl.selectedDevice !== device) {
                ctrl.selectedDevice = device;
            }
            
            var len = selectedItems.length;
            for(var x = 0; x < len; x++) {
                var itemName = selectedItems[x];
                $http.get("neeostatus/getchannel?itemname=" + encodeURIComponent(itemName))
                .then(function(response) {
                    var data = response.data;
                    if (response.data.success === true) {
                        var channel = response.data.channel;
                        ctrl.addChannel(device, channel);
                    } else {
                        $.jGrowl("Error getting channel (" + itemName + "): " + response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
                    }
                }
                , function(response) {
                    $.jGrowl("Error getting channel (" + itemName + "): " + response.status + " " + response.statusText, {theme : 'jgrowl-error', sticky: true});
                });
            }
          }, function () {

          });
    }
    
    ctrl.showAdvancedProperties = function(device) {
        var modalInstance = $uibModal.open({
            ariaLabelledBy: 'advProp-title',
            ariaDescribedBy: 'advProp-body',
            templateUrl: 'advancedProperties.html',
            controller: 'AdvanceProperties',
            controllerAs: '$ctrl',
            resolve: {
              device: function () {
                return device;
              }
            },
            windowClass: 'app-modal-advancedProperties'
          });
        
        modalInstance.result.then(function (props) {
            device.old.timing.standbyCommandDelay = device.timing.standbyCommandDelay;
            device.old.timing.sourceSwitchDelay = device.timing.sourceSwitchDelay;
            device.old.timing.shutdownDelay = device.timing.shutdownDelay;
            device.old.deviceCapabilities = device.deviceCapabilities.slice();
            
            device.timing.standbyCommandDelay = props.timing.standbyCommandDelay;
            device.timing.sourceSwitchDelay = props.timing.sourceSwitchDelay;
            device.timing.shutdownDelay = props.timing.shutdownDelay;
            device.deviceCapabilities = props.deviceCapabilities.slice();
            
          }, function () {

          });
    }
    
    $timeout(function() { ctrl.getBrainStatuses(); }, 1000);
    $timeout(function() { ctrl.getDevices(); }, 1000);
};

angular.module('ui.bootstrap').controller('AddItemsInstanceCtrl', function ($scope, $uibModalInstance, items, devices) {
    var $ctrl = this;
    
    $ctrl.selectedDevice = undefined;
    
    $ctrl.newItems = [""];
    
    $ctrl.devices = devices;
    $ctrl.items = items;
    
    $ctrl.isImporting = false;
    
    $ctrl.addNewItem = function() {
        $ctrl.newItems.push("");
    }
    
    $ctrl.deleteNewItem = function(idx) {
        $ctrl.newItems.splice(idx, 1);
    }
    
    $ctrl.deleteAllNewItems = function() {
        if (confirm("Are you sure you wish to delete all items?")) {
            $ctrl.newItems = [""];
        }
    }
    
    $ctrl.addThingItems = function() {
        if ($ctrl.selectedDevice !== undefined) {
            // get rid of default row if only one specified
            if ($ctrl.newItems.length == 1 && $ctrl.newItems[0] == "") {
                $ctrl.newItems = [];
            }
            
            for(var c = 0; c < $ctrl.selectedDevice.channels.length - 1 ; c++) {
                var chnl = $ctrl.selectedDevice.channels[c];
                if (chnl.kind === "item") {
                    var itemName = chnl.itemName;
                    if (itemName != undefined) {
                        $ctrl.newItems.push(itemName);
                    }
                }
            }
        }
    }
    
    $scope.importItems = function(element) {
        var itemsFile = element.files[0];
        var reader = new FileReader();
        $scope.$apply(function() {
            $ctrl.isImporting = true;
        });
        
        reader.onload = function(e) {
            $scope.$apply(function() {
                var data = e.target.result;
                
                // get rid of default row if only one specified
                if ($ctrl.newItems.length == 1 && $ctrl.newItems[0] == "") {
                    $ctrl.newItems = [];
                }
                
                // note: will screwup on something like: itemType itemName "label //stuff"...
                // but we don't care because we only care about the itemName
                var cmnts = /(\/\*[\w\'\s\r\n\*]*\*\/)|(\/\/.*$)|(\<![\-\-\s\w\>\/]*\>)/gm;
                data = data.replace(cmnts, "");
                
                // uuuuuggglllyyy
                var itemRegex = /([A-Za-z0-9_:]+(?:\([A-Za-z, ]*\))*)\s+(\w+)\s*(".*?")?\s*(<.*?>)?\s*(\(.*?\))?\s*(\[.*?\])?\s*({.*?})?/g;
                var match;
                while(match = itemRegex.exec(data)) {
                    var itemName = match[2];
                    if (itemName != undefined) {
                        $ctrl.newItems.push(itemName);
                    }
                }
                $ctrl.isImporting = false;
                
                element.value = "";
            });
        };
        reader.readAsText(itemsFile);
    };
    
    $ctrl.ok = function () {
      var newItems = [];
      var max = $ctrl.newItems.length;
      for(var i = 0; i < max; i++) {
          var newItem = $ctrl.newItems[i];
          if (newItem !== undefined && newItem !== null && newItem.trim() !== "") {
              newItems.push(newItem.trim());
          }
      }
      $uibModalInstance.close(newItems);
    };

    $ctrl.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  });


angular.module('ui.bootstrap').controller('AdvanceProperties', function ($scope, $uibModalInstance, device) {
    var $ctrl = this;
    
    $ctrl.device = device;
    
    $ctrl.props = {
        timing: {
            standbyCommandDelay : device.timing.standbyCommandDelay,
            sourceSwitchDelay : device.timing.sourceSwitchDelay,
            shutdownDelay : device.timing.shutdownDelay
        },
        deviceCapabilities : device.deviceCapabilities.slice()
    };
    
    $ctrl.isAlwaysOn = function() {
        return $ctrl.props.deviceCapabilities.includes("alwaysOn");
    }
    
    $ctrl.toggleAlwaysOn = function() {
        const index = $ctrl.props.deviceCapabilities.indexOf("alwaysOn");
        if (index >= 0) {
            $ctrl.props.deviceCapabilities.splice(index, 1);
        } else {
            $ctrl.props.deviceCapabilities.push("alwaysOn");
        }        
    }
    $ctrl.ok = function () {
      $uibModalInstance.close($ctrl.props);
    };

    $ctrl.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  });


angular.module('ui.bootstrap').controller('ShowLog', function ($scope, $sce, $uibModalInstance, log) {
    var $ctrl = this;
    
    $ctrl.log = $sce.trustAsHtml(log);
    $ctrl.ok = function () {
      $uibModalInstance.close();
    };
  });
