angular.module('SmartHomeManagerApp.controllers', []).controller('BodyController', function($scope, inboxService) {
    $scope.getSchemeClass = function() {
        var theme = localStorage.getItem('theme');
        if (theme) {
            return 'theme-' + theme;
        } else {
            return 'theme-openhab';
        }
    }
    $scope.isEshTheme = function() {
        return $scope.getSchemeClass() === 'theme-white';
    }

    var numberOfInboxEntries = -1;
    setInterval(function() {
        inboxService.getAll(function(inboxEntries) {
            if (numberOfInboxEntries != -1) {
                if (inboxEntries.length > numberOfInboxEntries) {
                    var newEntry = inboxEntries[inboxEntries.length - 1];
                    $scope.showDefaultToast('New Inbox Entry: ' + newEntry.label, 'Show Inbox', 'inbox');
                }
            }
            numberOfInboxEntries = inboxEntries.length;
        });
    }, 5000);

}).controller('ControlPageController', function($scope, $routeParams, $location, $timeout, itemService) {
    $scope.items = [];
    $scope.currentTab = $routeParams.tab ? $routeParams.tab : 'All';
    $scope.tabs = [ 'All' ];
    $scope.navigateTo = function(path) {
        $location.path('control/' + path);
    }

    itemService.getAll(function(items) {
        $scope.items['All'] = items;
        for (var int = 0; int < items.length; int++) {
            var item = items[int];
            if (item.type === 'GroupItem') {
                if(item.tags.indexOf("room") > -1) {
                    $scope.tabs.push(item.name);
                }
                $scope.items[item.name] = item.members;
            }
            $('paper-tabs').prop('selected', $scope.tabs.indexOf($scope.currentTab));
        }
        $scope.$watch('items', function(value) {
            var val = value || null;
            if (val) {
                $timeout(function() {
                    new Masonry('.items', {});
                }, 0, false);
            }
        });
    });

}).controller('ConfigurationPageController', function($scope, itemService, $routeParams, $location) {
    $scope.currentTab = $routeParams.tab ? $routeParams.tab : 'bindings';
    $scope.action = $routeParams.action;
    $scope.actionArg = $routeParams.actionArg;
    $scope.tabs = [ 'bindings', 'items', 'things' ];
    $scope.navigateTo = function(path) {
        $location.path('configuration/' + path);
    }
}).controller('PreferencesPageController', function($scope) {
    var localStorage = window.localStorage;
    var language = localStorage.getItem('language');
    var theme = localStorage.getItem('theme');

    $scope.language = language ? language : 'english';
    $scope.theme = theme ? theme : 'openhab';

    $scope.save = function(language, theme) {
        localStorage.setItem('language', language);
        localStorage.setItem('theme', theme);
        $scope.showSuccessToast('Preferences saved successfully. Please reload the page.');
    }

    $scope.getSelected = function(property) {
        return $('select#' + property + ' option:selected').val();
    }
}).controller('NavController', function($scope, $location) {
    $scope.isActive = function(viewLocation) {
        var active = (viewLocation === $location.path().split('/')[1]);
        return active;
    }
    $scope.$on('$routeChangeSuccess', function() {
        $('body').removeClass('sml-open');
        $('.mask').remove();
    });
}).controller('ControlController', function($scope, $timeout, itemService) {

}).controller('ItemController', function($scope, $timeout, itemService) {
    $scope.itemList = [];

    $scope.getAll = function() {
        itemService.getAll(function(response) {
            $scope.itemList = response;
        });
    };
    $scope.getAll();
}).controller('ViewItemController', function($scope, $timeout, itemService, thingService, linkService) {
    $scope.item = undefined;
    $scope.items = [];
    $scope.channels = [];
    $scope.boundChannels = [];
    $scope.systemTags = ['room'];
    $scope.tagInput = undefined;
    
    $scope.getItem = function() {
        itemService.getByName({
            itemName : $scope.actionArg
        }, function(response) {
            $scope.item = response;
            $scope.boundChannels = [];
            linkService.getAll(function(response) {
                $.each(response, function(i, link) {
                    if (link.itemName === $scope.item.name) {
                        $scope.boundChannels.push(link.channelUID);
                    }
                });
            });
            $timeout(function() {
                $scope.tagInput = $('.tags input');
                
                // reinit
                $scope.tagInput.tagsinput('destroy');
                $scope.tagInput.off();
                
                $scope.tagInput.tagsinput();
                $scope.tagInput.on('itemAdded', function(event) {
                   itemService.addTag({
                       itemName: $scope.item.name, 
                       tag: event.item
                   });
                });
                $scope.tagInput.on('itemRemoved', function(event) {
                    itemService.removeTag({
                        itemName: $scope.item.name, 
                        tag: event.item
                    });
                 });
            }, 0, false)
        });
    };
    
    itemService.getAll(function(response) {
        $scope.items = response;
    });

    thingService.getAll(function(response) {
        $.each(response, function(i, thing) {
            $.each(thing.channels, function(i, channel) {
                $scope.channels.push(thing.UID + ':' + channel.id);
            });
        });
    });

    $scope.remove = function(itemName) {
        itemService.remove({
            itemName : itemName
        }, function() {
            $scope.navigateTo('items');
            $scope.showSuccessToast('Item removed.');
        });
    };

    $scope.addMember = function(itemName, memberItemName) {
        itemService.addMember({
            itemName : itemName,
            memberItemName : memberItemName
        }, function() {
            $scope.getItem();
        });
    };

    $scope.removeMember = function(itemName, memberItemName) {
        itemService.removeMember({
            itemName : itemName,
            memberItemName : memberItemName
        }, function() {
            $scope.getItem();
        });
    };

    $scope.link = function(itemName, channelUID) {
        linkService.link({
            itemName : itemName,
            channelUID : channelUID
        }, function() {
            $scope.getItem();
        });
    }

    $scope.unlink = function(itemName, channelUID) {
        linkService.unlink({
            itemName : itemName,
            channelUID : channelUID
        }, function() {
            $scope.getItem();
        });
    }
    
    $scope.addSystemTag = function(systemTag) {
        $scope.tagInput.tagsinput('add', systemTag);
    }

    $scope.getItem();
}).controller('AddItemController', function($scope, $timeout, itemService) {
    $scope.add = function() {
        var itemName = $('paper-input#itemName').val();
        var itemType = $('paper-input#itemType').val();
        itemService.create({
            'itemName' : itemName
        }, itemType, function(response) {
            $scope.navigateTo('items');
            $scope.showSuccessToast('Item added.');
        });
    };
}).controller('DefaultItemController', function($scope, itemService) {

    $scope.sendCommand = function(state) {
        itemService.sendCommand({
            itemName : $scope.item.name
        }, state);
    }

}).controller('SwitchItemController', function($scope, $timeout, itemService) {

    $scope.toggleSwitch = function(e) {
        var newState = $(e.target).attr('name') === 'ON' ? 'ON' : 'OFF';
        itemService.sendCommand({
            itemName : $scope.item.name
        }, newState);
    }

    $scope.isOn = function(item) {
        $scope.item.state === 'ON';
    }

}).controller('DimmerItemController', function($scope, $timeout, itemService) {

    $scope.toggleSwitch = function(e) {
        var newState = $(e.target).attr('name') === 'ON' ? 'ON' : 'OFF';
        itemService.sendCommand({
            itemName : $scope.item.name
        }, newState);
    }

    $scope.isOn = function() {
        return $scope.item.state !== '0';
    }

    $scope.getDimValue = function() {
        return $scope.item.state;
    }

    $scope.setDimValue = function(e) {
        var dimValue = e.target.immediateValue === 0 ? '0' : e.target.immediateValue;
        $scope.item.state = dimValue;
        itemService.sendCommand({
            itemName : $scope.item.name
        }, dimValue);
    }

}).controller('ColorItemController', function($scope, $timeout, $element, itemService) {

    $scope.toggleSwitch = function(e) {
        var newState = $(e.target).attr('name') === 'ON' ? 'ON' : 'OFF';
        itemService.sendCommand({
            itemName : $scope.item.name
        }, newState);
    }

    $scope.isOn = function() {
        return $scope.toTinyColor($scope.item.state).toHsv().v > 0;
    }

    $scope.getDimValue = function() {
        return $scope.toTinyColor($scope.item.state).toHsv().v * 100;
    }

    $scope.setDimValue = function(e) {
        var dimValue = e.target.immediateValue === 0 ? '0' : e.target.immediateValue;
        itemService.sendCommand({
            itemName : $scope.item.name
        }, dimValue);
    }
    
    $scope.setHueValue = function(e) {
        var hueValue = e.target.immediateValue === 0 ? '0' : e.target.immediateValue;
        var color = $scope.toTinyColor($scope.item.state).toHsv();
        color.h = hueValue;
        
        if(!color.s) {
            color.s = 1;
        }
        if(!color.v) {
            color.v = 1;
        }
        itemService.sendCommand({
            itemName : $scope.item.name
        }, $scope.toColorState(color));
    }

    $scope.toTinyColor = function(state) {
        var colorParts = state.split(",");
        return tinycolor({
            h : colorParts[0],
            s : colorParts[1] / 100,
            v : colorParts[2] / 100
        });
    }

    $scope.getHexColor = function() {
        var hsv = $scope.toTinyColor($scope.item.state);
        return hsv.toHexString();
    }

    $scope.toColorState = function(hsv) {
        return Math.ceil(hsv.h) + ',' + Math.ceil(hsv.s * 100) + ',' + Math.ceil(hsv.v * 100);
    }
    
    var hue = $scope.toTinyColor($scope.item.state).toHsv().h;
    var brightness = $scope.toTinyColor($scope.item.state).toHsv().v * 100;
    $scope.hue = hue ? hue : 0;
    $scope.brightness = brightness ? brightness : 0;

}).controller('BindingController', function($scope, $timeout, bindingService) {
    $scope.bindings = [];

    bindingService.getAll(function(response) {
        $scope.bindings = response;
    });
}).controller('InboxPageController', function($scope, $routeParams, $location) {
    $scope.currentTab = $routeParams.tab ? $routeParams.tab : 'inbox';
    $scope.tabs = [ 'inbox', 'discovery' ];
    $scope.navigateTo = function(path) {
        $location.path('inbox/' + path);
    }
}).controller('InboxController', function($scope, $timeout, inboxService) {
    $scope.discoveryResults = [];

    $scope.approve = function(thingUID) {
        inboxService.approve({
            'thingUID' : thingUID
        }, function() {
            $scope.getAll();
            $scope.showDefaultToast('Thing added.', 'Show Thing', 'configuration/things/view/' + thingUID);
        });
    };
    $scope.ignore = function(thingUID) {
        inboxService.ignore({
            'thingUID' : thingUID
        }, function() {
            $scope.getAll();
        });
    };
    $scope.remove = function(thingUID) {
        inboxService.remove({
            'thingUID' : thingUID
        }, function() {
            $scope.getAll();
            $scope.showSuccessToast('Inbox entry removed.');
        });
    };

    $scope.getAll = function() {
        inboxService.getAll(function(response) {
            $scope.discoveryResults = response;
        });
    };
    $scope.getAll();

}).controller('DiscoveryController', function($scope, $timeout, discoveryService) {
    $scope.supportedBindings = [];

    $scope.scan = function(bindingId) {
        discoveryService.scan({
            'bindingId' : bindingId
        }, function() {

        });
        var progressBar = $('paper-progress[data-binding-id=' + bindingId + ']');
        progressBar.prop('value', 0);
        var progress = setInterval(function() {
            var progress = progressBar.prop('value');
            if (progress >= 100) {
                clearInterval(progress);
            } else {
                progressBar.prop('value', progress + 1);
            }
        }, 50);
    };

    discoveryService.getAll(function(response) {
        $scope.supportedBindings = response;
    });

}).controller('ThingController', function($scope, $timeout, thingService, thingTypeService, bindingService) {
    $scope.data.things = [];
    $scope.data.thingTypes = [];

    $scope.unlink = function(thingUID, channelId) {
        thingService.unlink({
            'thingUID' : thingUID,
            'channelId' : channelId
        }, function() {
            $scope.getAll();
        });
    };

    $scope.getAll = function() {
        thingService.getAll(function(response) {
            $scope.data.things = response;
        });
    }
    $scope.getThingTypes = function() {
        thingTypeService.getAll(function(response) {
            $scope.data.thingTypes = response;
        });
    }

    bindingService.getAll(function(response) {
        $scope.data.bindings = response;
    });

    $scope.getThingType = function(thingUID) {
        var segments = thingUID.split(':');
        var thingTypeUID = segments[0] + ':' + segments[1];
        if (!$scope.data.thingTypes) {
            return;
        }
        return $.grep($scope.data.thingTypes, function(thingType, i) {
            return thingTypeUID == thingType.UID;
        })[0];
    };

    $scope.getThingTypes();
    $scope.getAll();
}).controller('AddThingController', function($scope, $timeout, thingService, thingTypeService) {
    $scope.thingType = undefined;
    thingTypeService.getByUid({
        'thingTypeUID' : $scope.actionArg
    }, function(response) {
        $scope.thingType = response;
    });
    $scope.add = function() {
        var thingUID = $scope.thingType.UID + ':' + $('paper-input#id').val();
        var thing = {
            UID : thingUID,
            configuration : {}
        };
        $.each($scope.thingType.configParameters, function(index, parameter) {
            thing.configuration[parameter.name] = $('paper-input#' + parameter.name).val();
        });
        thingService.add({
            'thingUID' : thingUID
        }, thing, function(response) {
            $scope.navigateTo('things');
            $scope.showSuccessToast('Thing added.');
        });
    };
}).controller('EditThingController', function($scope, $timeout, thingService, thingTypeService) {
    $scope.thingType = undefined;
    $scope.thing = undefined;

    $scope.getThing = function() {
        thingService.getByUid({
            'thingUID' : $scope.actionArg
        }, function(response) {

            $scope.thing = response;
            var uidSegments = $scope.thing.UID.split(':');
            var thingTypeUID = uidSegments[0] + ':' + uidSegments[1];

            thingTypeService.getByUid({
                'thingTypeUID' : thingTypeUID
            }, function(response) {
                $scope.thingType = response;
            });
        });
    }

    $scope.getConfigValue = function(name) {
        return $scope.thing.configuration[name];
    };

    $scope.update = function() {
        $.each($scope.thingType.configParameters, function(index, parameter) {
            $scope.thing.configuration[parameter.name] = $('paper-input#' + parameter.name).val();
        });
        thingService.update({
            'thingUID' : $scope.thing.UID
        }, $scope.thing, function(response) {
            $scope.navigateTo('things/view/' + $scope.thing.UID);
            $scope.showSuccessToast('Configuration saved.');
        });
    };
    $scope.remove = function(thingUID) {
        thingService.remove({
            'thingUID' : thingUID
        }, function() {
            $scope.navigateTo('things');
            $scope.showSuccessToast('Thing removed.');
        });
    };

    var firstToUpperCase = function(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }

    $scope.link = function(thingUid, channelId) {
        var segments = thingUid.split(':');
        var itemName = firstToUpperCase(segments[1]) + firstToUpperCase(segments[2]) + firstToUpperCase(channelId);
        thingService.link({
            'thingUID' : thingUid,
            'channelId' : channelId
        }, itemName, function() {
            $scope.getThing();
        });
        $scope.dialog = undefined;
    };

    $scope.getChannelById = function(channelId) {
        if (!$scope.thingType) {
            return;
        }
        return $.grep($scope.thingType.channels, function(channel, i) {
            return channelId == channel.id;
        })[0];
    };

    $scope.getThing();
});