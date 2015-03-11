angular.module('SmartHomeManagerApp.services', []).config(function($httpProvider){
    var language = localStorage.getItem('language');
    if(language) {
        $httpProvider.defaults.headers.common['Accept-Language'] = language;
    }
    var interceptor = function ($rootScope, $q) {

        function success(response) {
            return response;
        }

        function error(response) {
        	$rootScope.showErrorToast('An error occured: ' + response.status + ' (' + response.statusText + ')');
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        }

    };
    // TODO: seems not to work
    $httpProvider.interceptors.push(interceptor);
}).factory('eventService', function($resource) {
	var eventSrc = new EventSource('/rest/events');    
	return new function() {
		this.onEvent = function(topic, callback) {
			var topicRegex = topic.replace('/', '\/').replace('*', '.*');
			eventSrc.addEventListener('message', function (event) {
		        var data = JSON.parse(event.data);
		        if(data.topic.match(topicRegex)) {
		        	callback(data.topic, data.object);
		        }
		    });
		}
	};
}).factory('toastService', function($mdToast, $rootScope) {
	var eventSrc = new EventSource('/rest/events');    
	return new function() {
	    var self = this;
		this.showToast = function(id, text, actionText, actionUrl) {
	    	var toast = $mdToast.simple().content(text);
	        if(actionText) {
	        	toast.action(actionText);
	        	toast.hideDelay(6000);
	        } else {
	        	toast.hideDelay(3000);
	        }
	        toast.position('bottom right');
	        $mdToast.show(toast).then(function() {
				$rootScope.navigateFromRoot(actionUrl);
			});
	    }
	    this.showDefaultToast = function(text, actionText, actionUrl) {
	    	self.showToast('default', text, actionText, actionUrl);
	    }
	    this.showErrorToast = function(text, actionText, actionUrl) {
	    	self.showToast('error', text, actionText, actionUrl);
	    }
	    this.showSuccessToast = function(text, actionText, actionUrl){
	    	self.showToast('success', text, actionText, actionUrl);
	    }
	};
});