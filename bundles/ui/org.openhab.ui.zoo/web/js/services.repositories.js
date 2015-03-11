var DataCache = function($q, $rootScope, remoteService, dataType) {
	var self = this;
	var cacheEnabled = false;
	var dirty = false;
	this.setDirty = function() {
		this.dirty = true;
	}
	this.getAll = function(callback, refresh) {
		var deferred = $q.defer();
		remoteService.getAll(function(data) {
			if((!cacheEnabled || (data.length != $rootScope.data[dataType].length) || self.dirty || refresh)) {
				$rootScope.data[dataType] = data;
				deferred.resolve(data);
			} else {
				deferred.resolve('No update');
			}
		});
        deferred.promise.then(function(res) {
        	if(callback && res !== 'No update') {
        		return callback(res);
        	} else {
        		return;
        	}
        }, function(res) {
        	return;
        }, function(res) {
        	if(callback) {
        		return callback(res);
        	} else {
        		return;
        	}
        });
        if(cacheEnabled) {
        	deferred.notify($rootScope.data[dataType]);
        }
        return deferred.promise;
	};
	this.getOne = function(condition, callback, refresh) {
		var element = self.find(condition);
		if(element != null && !this.dirty && !refresh) {
			callback(element);
		} else {
			self.getAll(null, true).then(function(res) {
				if(callback) {
					callback(self.find(condition));
	        		return;
	        	} else {
	        		return;
	        	}
	        }, function(res) {
	        	callback(null);
	        	return;
	        }, function(res) {
	        	return;
	        });
		}
	};
	this.find = function(condition) {
		for (var i = 0; i < $rootScope.data[dataType].length; i++) {
			var element = $rootScope.data[dataType][i];
			if(condition(element)) {
				if(condition(element)) {
					return element;
				}
			}
		}
		return null;
	};
	this.add = function(element) {
		$rootScope.data[dataType].push(element);
	};
	this.remove = function(element) {
		// TODO: implement
	};
}

angular.module('SmartHomeManagerApp.services.repositories', []).factory('bindingRepository', 
		function($q, $rootScope, bindingService) {
	$rootScope.data.bindings = [];
	return new DataCache($q, $rootScope, bindingService, 'bindings');
}).factory('thingTypeRepository', 
		function($q, $rootScope, thingTypeService) {
	$rootScope.data.thingTypes = [];
	return new DataCache($q, $rootScope, thingTypeService, 'thingTypes');
}).factory('discoveryResultRepository', 
		function($q, $rootScope, inboxService, eventService) {
	var dataCache = new DataCache($q, $rootScope, inboxService, 'discoveryResults')
	$rootScope.data.discoveryResults = [];
	eventService.onEvent('smarthome/inbox/added/*', function(topic, discoveryResult) {
		dataCache.add(discoveryResult);
	});
	return dataCache;
}).factory('thingRepository', 
		function($q, $rootScope, thingSetupService) {
	var dataCache = new DataCache($q, $rootScope, thingSetupService, 'things')
	$rootScope.data.things = [];
	return dataCache;
}).factory('homeGroupRepository', 
		function($q, $rootScope, groupSetupService) {
	var dataCache = new DataCache($q, $rootScope, groupSetupService, 'homeGroups')
	$rootScope.data.homeGroups = [];
	return dataCache;
}).factory('itemRepository', 
		function($q, $rootScope, itemService) {
	var dataCache = new DataCache($q, $rootScope, itemService, 'items')
	$rootScope.data.items = [];
	return dataCache;
});