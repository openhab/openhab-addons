;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('tabControl', {
        bindings : {
            tab : '<',
            onSelectionUpdate : '&'
        },
        templateUrl : 'partials/control/component.control.tab.html',
        controller : TabControlController

    });

    TabControlController.$inject = [ '$location', 'channelTypeRepository', 'thingTypeRepository', 'thingConfigService' ]

    function TabControlController($location, channelTypeRepository, thingTypeRepository, thingConfigService) {
        var ctrl = this;

        this.things = [];

        this.navigateTo = navigateTo;
        this.onTabSelect = onTabSelect;

        this.$onInit = activate;

        function activate() {
            renderThings();
        }

        function navigateTo(path) {
            $location.path(path);
        }

        function onTabSelect() {
            ctrl.onSelectionUpdate({
                $event : {
                    tabName : ctrl.tab.name
                }
            });
        }

        function renderThings() {
            var renderedThings = [];
            channelTypeRepository.getAll().then(function(channelTypes) {
                angular.forEach(ctrl.tab.things, function(thing) {
                    thingTypeRepository.getOne(function(thingType) {
                        return thingType.UID === thing.thingTypeUID
                    }, function(thingType) {
                        var renderedThing = renderThing(thing, thingType, channelTypes);
                        if (renderedThing) {
                            renderedThings.push(renderedThing);
                            renderedThings = renderedThings.sort(function(a, b) {
                                return a.label < b.label ? -1 : a.label > b.label ? 1 : 0
                            })
                            ctrl.things = renderedThings;
                        }
                    }, false)
                })
            });
        }

        function renderThing(thing, thingType, channelTypes) {
            thing.thingChannels = thingConfigService.getThingChannels(thing, thingType, channelTypes, true);
            angular.forEach(thing.thingChannels, function(thingChannel) {
                thingChannel.channels = thingChannel.channels.filter(function(channel) {
                    return channel.linkedItems.length > 0;
                });
            });

            var hasChannels = false;
            angular.forEach(thing.thingChannels, function(channelGroup) {
                angular.forEach(channelGroup.channels, function(channel) {
                    if (channel.linkedItems && channel.linkedItems.length > 0) {
                        hasChannels = true;
                    }
                })
            })

            if (hasChannels) {
                return thing;
            }
        }
    }

})()
