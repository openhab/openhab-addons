angular.module('PaperUI.things') //
.controller('ViewThingController', function($scope, $mdDialog, $location, toastService, thingTypeService, thingRepository, thingService, linkService, channelTypeRepository, configService, thingConfigService, util, itemRepository, channelTypeService, configDescriptionService, profileTypeService) {
    $scope.setSubtitle([ 'Things' ]);

    var thingUID = $scope.path[4];
    $scope.thingTypeUID = null;
    $scope.advancedMode;
    $scope.thing;
    $scope.thingType;
    $scope.thingChannels = [];
    $scope.showAdvanced = false;
    $scope.channelTypes;
    $scope.items;

    $scope.navigateTo = function(path) {
        if (path.startsWith("/")) {
            $location.path(path);
        } else {
            $location.path('configuration/things/' + path);
        }
    }

    channelTypeRepository.getAll(function(channels) {
        $scope.channelTypes = channels;
        $scope.refreshChannels(false);
    });

    itemRepository.getAll(function(items) {
        $scope.items = items;
    });

    $scope.remove = function(thing, event) {
        event.stopImmediatePropagation();
        $mdDialog.show({
            controller : 'RemoveThingDialogController',
            templateUrl : 'partials/things/dialog.removething.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                thing : thing
            }
        }).then(function() {
            $scope.navigateTo('');
        });
    }

    $scope.enableChannel = function(thingUID, channelID, event, longPress) {
        var channel = $scope.getChannelById(channelID);
        event.stopImmediatePropagation();
        if ($scope.advancedMode || channel.kind === 'TRIGGER') {
            if (channel.linkedItems && channel.linkedItems.length > 0) {
                $scope.getLinkedItems(channel, event);
            } else {
                $scope.linkChannel(channelID, event, longPress);
            }
        } else if (channel.linkedItems.length == 0) {
            linkService.link({
                itemName : $scope.thing.UID.replace(/[^a-zA-Z0-9_]/g, "_") + '_' + channelID.replace(/[^a-zA-Z0-9_]/g, "_"),
                channelUID : $scope.thing.UID + ':' + channelID
            }, function(newItem) {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel linked');
            });
        }
    };

    $scope.disableChannel = function(thingUID, channelID, itemName, event) {
        var channel = $scope.getChannelById(channelID);
        event.stopImmediatePropagation();
        var linkedItem = channel.linkedItems[0];
        if (!itemName || itemName === '') {
            itemName = linkedItem;
        }
        if ($scope.advancedMode || channel.kind === 'TRIGGER') {
            $scope.unlinkChannel(channelID, itemName, event);
        } else {
            linkService.unlink({
                itemName : $scope.thing.UID.replace(/[^a-zA-Z0-9_]/g, "_") + '_' + channelID.replace(/[^a-zA-Z0-9_]/g, "_"),
                channelUID : $scope.thing.UID + ':' + channelID
            }, function() {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel unlinked');
            });
        }
    };

    $scope.editChannel = function(thingUID, channelID, itemName, event) {
        var channel = $scope.getChannelById(channelID);
        event.stopImmediatePropagation();

        linkService.getLink({
            itemName : itemName,
            channelUID : channel.uid
        }).$promise.then(function(link) {
            configDescriptionService.getByUri({
                uri : "link:" + itemName + " -> " + channel.uid
            }).$promise.then(function(linkConfigDescription) {
                var oldConfig = link.configuration;

                $mdDialog.show({
                    controller : 'ProfileEditDialogController',
                    controllerAs : '$ctrl',
                    templateUrl : 'partials/things/dialog.link-edit.html',
                    targetEvent : event,
                    hasBackdrop : true,
                    locals : {
                        linkConfigDescription : linkConfigDescription,
                        link : link,
                        channel : channel
                    }
                }).then(function(success) {
                    // store link
                    if (!success) {
                        return;
                    }
                    var eq = JSON.stringify(link.configuration) === JSON.stringify(oldConfig);
                    if (!eq) {
                        if (link.configuration && link.configuration['profile'] == "system:default") {
                            // do not store default profile, since this is chosen by the framework
                            delete link.configuration['profile'];
                        }
                        linkService.link({
                            itemName : link.itemName,
                            channelUID : link.channelUID
                        }, link, function(newItem) {
                            toastService.showDefaultToast('Link configuration updated');
                        }, function() {
                            toastService.showDefaultToast('Could not update link configuration.');
                        });
                    }
                });
            })
        });
    };

    $scope.linkChannel = function(channelID, event, preSelect) {
        var channel = $scope.getChannelById(channelID);
        var channelType = $scope.getChannelTypeByUID(channel.channelTypeUID);

        link = {
            channelUID : channel.uid,
            configuration : {
                profile : undefined
            },
            itemName : undefined
        };

        var params = {
            linkedItems : channel.linkedItems && channel.linkedItems.length > 0 ? channel.linkedItems : '',
            acceptedItemTypes : channel.acceptedItemTypes,
            category : channelType && channelType.category ? channelType.category : '',
            suggestedName : getItemNameSuggestion(channelID, channel.label),
            suggestedLabel : channel.label,
            suggestedCategory : channelType && channelType.category ? channelType.category : '',
            preSelectCreate : preSelect,
            // allow "Create new Item" in advanced mode only, disable for
            // normalMode
            allowNewItemCreation : $scope.advancedMode,
            link : link,
            channel : channel
        }

        $mdDialog.show({
            controller : 'LinkChannelDialogController',
            templateUrl : 'partials/things/dialog.linkchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            params : params
        }).then(function(newItem) {
            if (newItem) {
                link.itemName = newItem.itemName;

                var profileUid = link.configuration['profile'];
                if (profileUid) {
                    configDescriptionService.getByUri({
                        uri : "profile:" + profileUid
                    }).$promise.then(function(profileConfigDescription) {
                        // show profile config dialog and then store link
                        $mdDialog.show({
                            controller : 'ProfileEditDialogController',
                            controllerAs : '$ctrl',
                            templateUrl : 'partials/things/dialog.profile-edit.html',
                            targetEvent : event,
                            hasBackdrop : true,
                            locals : {
                                linkConfigDescription : undefined,
                                link : link,
                                channel : channel
                            }
                        }).then(function(success) {
                            if (success) {
                                storeLink(link, channel, newItem);
                            } else {
                                var emptyConfig = {
                                    profile : link.configuration['profile']
                                };
                                link.configuration = emptyConfig;
                                storeLink(link, channel, newItem);
                            }
                        });
                    }, function() {
                        // no config description for this profile -> store link directly
                        storeLink(link, channel, newItem);
                    })
                }
            }
        });
    }

    function storeLink(link, channel, newItem) {
        linkService.link({
            itemName : link.itemName,
            channelUID : link.channelUID
        }, link, function() {
            $scope.getThing(true);
            var item = $.grep($scope.items, function(item) {
                return item.name == link.itemName;
            });
            channel.items = channel.items ? channel.items : [];
            if (item.length > 0) {
                channel.items.push(item[0]);
            } else {
                channel.items.push({
                    name : newItem.itemName,
                    label : newItem.label
                });
            }
            toastService.showDefaultToast('Channel linked');
        });
    }

    function getItemNameSuggestion(channelID, label) {
        var itemName = getInCamelCase($scope.thing.label);
        if (channelID) {
            var id = channelID.split('#');
            if (id.length > 1 && id[0].length > 0) {
                itemName += ('_' + getInCamelCase(id[0]));
            }
            itemName += ('_' + getInCamelCase(label));
        }
        return itemName;
    }

    function getInCamelCase(str) {
        var camelStr = "";
        if (str) {
            var arr = str.split(/[^a-zA-Z0-9_]/g);
            for (var i = 0; i < arr.length; i++) {
                if (arr[i] && arr[i].length > 0) {
                    camelStr += (arr[i][0].toUpperCase() + (arr[i].length > 1 ? arr[i].substring(1, arr[i].length) : ''));
                }
            }
        }
        return camelStr;
    }

    $scope.getKeysArray = function(object) {
        return Object.keys(object);
    }

    $scope.unlinkChannel = function(channelID, itemName, event) {
        var channel = $scope.getChannelById(channelID);
        $mdDialog.show({
            controller : 'UnlinkChannelDialogController',
            templateUrl : 'partials/things/dialog.unlinkchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                itemName : itemName
            }
        }).then(function() {
            if (itemName) {
                linkService.unlink({
                    itemName : itemName,
                    channelUID : $scope.thing.UID + ':' + channelID
                }, function() {
                    $scope.getThing(true);
                    var item = $.grep(channel.items, function(item) {
                        return item.name == itemName;
                    });
                    if (item.length > 0) {
                        channel.items.splice(channel.items.indexOf(item[0]), 1);
                    }
                    toastService.showDefaultToast('Channel unlinked');
                });
            }
        });
    }
    $scope.getChannelById = function(channelId) {
        if (!$scope.thing) {
            return;
        }
        return $.grep($scope.thing.channels, function(channel, i) {
            return channelId == channel.id;
        })[0];
    }

    $scope.getChannelTypeByUID = function(channelUID) {
        return thingConfigService.getChannelTypeByUID($scope.thingType, $scope.channelTypes, channelUID);
    };

    $scope.getChannelFromChannelTypes = function(channelUID) {
        if (!$scope.channelTypes) {
            return;
        }
        return thingConfigService.getChannelFromChannelTypes($scope.channelTypes, channelUID);
    };

    var getChannels = function(advanced) {
        if (!$scope.thingType || !$scope.thing || !$scope.channelTypes) {
            return;
        }
        $scope.isAdvanced = checkAdvance($scope.thing.channels);
        var thingChannels = thingConfigService.getThingChannels($scope.thing, $scope.thingType, $scope.channelTypes, advanced);

        // set the linkable item types for each channel
        var channelType2ItemTypeCache = [];
        angular.forEach(thingChannels, function(channelGroup) {
            angular.forEach(channelGroup.channels, function(channel) {
                if (channel.kind !== 'TRIGGER') {
                    channel.acceptedItemTypes = [ channel.itemType ];
                    return true;
                }
                if (channelType2ItemTypeCache[channel.channelTypeUID]) {
                    channel.acceptedItemTypes = channelType2ItemTypeCache[channel.channelTypeUID];
                    return true;
                }

                var acceptedItemTypes = [];
                channelTypeService.getLinkableItemTypes({
                    channelTypeUID : channel.channelTypeUID
                }, function(linkableItemTypes) {
                    if (linkableItemTypes && linkableItemTypes.length > 0) {
                        angular.forEach(linkableItemTypes, function(itemType) {
                            acceptedItemTypes.push(itemType);
                        });
                    } else if (channel.itemType) {
                        acceptedItemTypes.push(channel.itemType);
                    }
                });
                channelType2ItemTypeCache[channel.channelTypeUID] = acceptedItemTypes;
                channel.acceptedItemTypes = acceptedItemTypes;
            });
        });

        return thingChannels;
    };

    $scope.refreshChannels = function(showAdvanced) {
        $scope.thingChannels = getChannels(showAdvanced);
    }

    function checkAdvance(channels) {
        var advancedChannels = channels.filter(function(channel) {
            var channelType = $scope.getChannelTypeByUID(channel.channelTypeUID);
            return channelType && channelType.advanced
        })

        return advancedChannels.length > 0
    }

    $scope.getThing = function(refresh) {
        thingRepository.getOne(function(thing) {
            return thing.UID === thingUID;
        }, function(thing) {
            angular.forEach(thing.channels, function(value, i) {
                value.showItems = $scope.thing ? $scope.thing.channels[i].showItems : false;
                value.items = $scope.thing ? $scope.thing.channels[i].items : null;
            });
            $scope.thing = thing;
            checkThingProperties(thing);
            $scope.thingTypeUID = thing.thingTypeUID;
            getThingType();
            $scope.setSubtitle([ 'Things', thing.label ]);
        }, refresh);
    }

    function checkThingProperties(thing) {
        if (thing.properties) {
            var hasFirmwareVersion = thing.properties['firmwareVersion'];
            if ((Object.keys(thing.properties).length > 0 && !hasFirmwareVersion) || (Object.keys(thing.properties).length > 1 && hasFirmwareVersion)) {
                $scope.thing.hasProperties = true;
            } else {
                $scope.thing.hasProperties = false;
            }
        } else {
            $scope.thing.hasProperties = false;
        }
    }
    $scope.getThing(true);

    function getThingType() {
        thingTypeService.getByUid({
            thingTypeUID : $scope.thingTypeUID
        }, function(thingType) {
            $scope.thingType = thingType;
            if (thingType) {
                $scope.thingTypeChannels = thingType.channels && thingType.channels.length > 0 ? thingType.channels : thingType.channelGroups;
                $scope.setHeaderText(thingType.description);
            }
            $scope.refreshChannels($scope.showAdvanced);
        });
    }

    $scope.configChannel = function(channel, thing, event) {
        configDescriptionService.getByUri({
            uri : 'channel:' + channel.uid
        }, function(configDescription) {
            $mdDialog.show({
                controller : 'ChannelConfigController',
                templateUrl : 'partials/things/dialog.channelconfig.html',
                targetEvent : event,
                hasBackdrop : true,
                locals : {
                    channelType : configDescription,
                    channel : channel,
                    thing : thing
                }
            });
        });

    };

    $scope.getLinkedItems = function(channel) {
        channel.showItems = !channel.showItems;
        if (channel.showItems && channel.items === null || channel.items === undefined) {
            channel.items = $.grep($scope.items, function(item) {
                return $.grep(channel.linkedItems, function(linkedItemName) {
                    return linkedItemName == item.name;
                }).length > 0;
            });
        }
    }

    $scope.showDescription = function(channel, channelType) {
        var description = channel.description ? channel.description : channel.channelType ? channel.channelType.description : null;
        if (description) {
            popup = $mdDialog.alert({
                title : channel.label ? channel.label : channel.channelType ? channel.channelType.label : channel.id,
                textContent : description,
                ok : 'Close'
            });
            $mdDialog.show(popup);
        }
    }

    $scope.$watch('thing.channels', function() {
        $scope.refreshChannels($scope.showAdvanced);
    });

    $scope.isExtensible = function() {
        var thingType = $scope.thingType
        return thingType && thingType.extensibleChannelTypeIds && thingType.extensibleChannelTypeIds.length > 0
    }

    $scope.isExtensibleChannel = function(channelTypeUID) {
        if (!channelTypeUID || !$scope.isExtensible()) {
            return false
        }

        var channelTypeId = channelTypeUID.split(':')[1]
        return $scope.thingType.extensibleChannelTypeIds.indexOf(channelTypeId) >= 0
    }

    $scope.addChannel = function(event) {
        $mdDialog.show({
            controller : 'AddChannelController',
            templateUrl : 'partials/things/dialog.addchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                thing : $scope.thing,
                thingType : $scope.thingType,
            }
        });
    }

    $scope.removeChannel = function(channel, event) {
        event.stopImmediatePropagation();
        $mdDialog.show({
            controller : 'RemoveChannelDialogController',
            templateUrl : 'partials/things/dialog.removechannel.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                thing : $scope.thing,
                channel : channel
            }
        }).then(function() {
            $scope.getThing(false);
        });
    }
}).controller('AddChannelController', function($scope, $mdDialog, toastService, channelTypeRepository, configService, thingService, thingType, thing) {
    $scope.channelTypes = []
    $scope.parameters = undefined

    // values filled form the dialog
    $scope.channelType = undefined
    $scope.channelId = undefined
    $scope.channelLabel = undefined
    $scope.configuration = {}

    var refreshChannelTypes = function() {
        angular.forEach(thingType.extensibleChannelTypeIds, function(channelTypeId) {
            var channelTypeUID = thing.UID.split(":")[0] + ':' + channelTypeId;
            channelTypeRepository.getOne(function filter(element) {
                return element.UID === channelTypeUID
            }, function callback(channelType) {
                $scope.channelTypes.push(channelType)
            })
        })
    }

    $scope.$watch('channelType', function() {
        if ($scope.channelType) {
            $scope.parameters = configService.getRenderingModel($scope.channelType.parameters, $scope.channelType.parameterGroups);
        }
    })

    $scope.close = function() {
        $mdDialog.cancel();
    }

    $scope.save = function() {
        var channel = {
            uid : thing.UID + ':' + $scope.channelId,
            id : $scope.channelId,
            channelTypeUID : $scope.channelType.UID,
            itemType : $scope.channelType.itemType,
            label : $scope.channelLabel,
            configuration : $scope.configuration,
            defaultTags : $scope.channelType.tags,
            linkedItems : []
        }

        thing.channels.push(channel);

        thingService.update({
            thingUID : thing.UID
        }, thing, function() {
            $mdDialog.hide();
            toastService.showSuccessToast('Channel added.');
        }, function() {
            $mdDialog.hide();
            toastService.showErrorToast('Error adding channel.');
        });
    }

    refreshChannelTypes();
}).controller('RemoveChannelDialogController', function($scope, $mdDialog, toastService, thingService, thing, channel) {
    $scope.channel = channel;

    $scope.close = function() {
        $mdDialog.cancel();
    }

    $scope.remove = function() {
        var index = thing.channels.indexOf(channel);
        thing.channels.splice(index, 1);
        thingService.update({
            thingUID : thing.UID
        }, thing, function() {
            $mdDialog.hide();
            toastService.showSuccessToast('Channel removed.');
        }, function() {
            $mdDialog.hide();
            toastService.showErrorToast('Error removing channel.');
        });
    }
});
