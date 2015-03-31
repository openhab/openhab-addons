'use strict';

angular.module('SmartHome.services.repositories', ['SmartHome.services.datacache'])
	.factory('bindingRepository', function (DataCache, $rootScope, bindingService) {
		//$rootScope.data.bindings = [];
		return DataCache.init(bindingService);
	})
	.factory('thingTypeRepository', function (DataCache, $rootScope, thingTypeService) {
		//$rootScope.data.thingTypes = [];
		return DataCache.init(thingTypeService);
	})
	.factory('discoveryResultRepository', function (DataCache, $rootScope, inboxService, eventService) {
		//$rootScope.data.discoveryResults = [];
		DataCache.init(inboxService);

		eventService.onEvent('smarthome/inbox/added/*', function (topic, discoveryResult) {
			DataCache.add(discoveryResult);
		});
		return DataCache;
	})
	.factory('thingRepository', function (DataCache, $rootScope, thingSetupService) {
		//$rootScope.data.things = [];
		return DataCache.init(thingSetupService);
	})
	.factory('homeGroupRepository', function (DataCache, $rootScope, groupSetupService) {
		//$rootScope.data.homeGroups = [];
		return DataCache.init(groupSetupService);
	})
	.factory('itemRepository', function (DataCache, $rootScope, itemService) {
		//$rootScope.data.items = [];
		return DataCache.init(itemService);
	});