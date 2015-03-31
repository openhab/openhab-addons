angular.module('ZooApp', [
	'ui.router',
	'ngResource',
	'angularSpinner',
	'SmartHome.services',
	'SmartHome.filters',
	'ZooLib.controllers',
	'ZooLib.directives'
]).config(function ($stateProvider, $urlRouterProvider, usSpinnerConfigProvider) {

	//$locationProvider.html5Mode(false).hashPrefix('!');
	$urlRouterProvider.otherwise("/login");
	$urlRouterProvider.when('/rooms', '/rooms/');

	usSpinnerConfigProvider.setDefaults({color: 'blue'});

	$stateProvider
		.state('login', {
			url: '/login',
			templateUrl: 'partials/login.html',
			controller: 'LoginController as ctrl'
		})
		.state('rooms', {
			abstract: true,
			url: '/rooms',
			templateUrl: 'partials/rooms.html',
			controller: 'RoomController as ctrl'
		})
		.state('rooms.room', {
			url: '/:room',
			templateUrl: 'partials/rooms.devices.html'
		})
		.state('access', {
			url:'/access',
			templateUrl: 'partials/access.html'
		})
		.state('alerts', {
			url:'/alerts',
			templateUrl: 'partials/alerts.html'
		})
		.state('cctv', {
			url:'/cctv',
			templateUrl: 'partials/cctv.html'
		})
		.state('cost-settings', {
			url:'/cost-settings',
			templateUrl: 'partials/cost-settings.html'
		})
		.state('energy-center', {
			url:'/energy-center',
			templateUrl: 'partials/cost-settings.html'
		})
		.state('profile', {
			url:'profile',
			templateUrl: 'partials/profile.html'
		})
		.state('alarm', {
			url:'/alarm',
			templateUrl: 'partials/alarm.html'
		})
		.state('intercom', {
			url:'/alarm',
			templateUrl: 'partials/alarm.html'
		})
		.state('settings', {
			templateUrl: 'partials/settings.html',
			controller: 'DiscoverController as ctrl'
		})
		.state('settings.discover', {
			url: '/settings/discover',
			templateUrl: 'partials/settings.discover.html'
		})
		.state('settings.manual', {
			url: '/settings/manual',
			templateUrl: 'partials/settings.manual.html'
		})
		.state('settings.groups', {
			url: '/settings/groups',
			templateUrl: 'partials/settings.groups.html'
		})

}).run(['$location', '$rootScope', '$log', 'itemService', function ($rootScope) {

	// TODO Get rid of this
	$rootScope.data = {};

	$rootScope.leftSidebarOpen = false;
	$rootScope.isBlackout = false;

	$rootScope.toggleSidebar = function () {
		$rootScope.leftSidebarOpen = !$rootScope.leftSidebarOpen;
		$rootScope.isBlackout = !$rootScope.isBlackout;
	};

	//itemService.getByName({itemName:'gRooms'}, function (rooms) {
	//	$rootScope.smarthome = {rooms : rooms.members};
	//});

	// TODO Move to directive
	//itemService.getByName({itemName:'gWeather'}, function(weatherItems) {
	//	console.log(weatherItems);
	//	weatherItems.members.forEach(function (item) {
	//		if (item.type === 'GroupItem') return;
	//		$rootScope.weather = {
	//			locationLabel: item.label,
	//			date: new Date(),
	//			temperature: item.state
	//		};
	//	});
	//});
}]);

angular.module("ZooApp").run(function ($rootScope, $state, $stateParams) {
	$rootScope.$state = $state;
	$rootScope.$stateParams = $stateParams;
});

// TODO Remove this, only for dev purposes
angular.module("ZooApp").run(function ($rootScope) {
	$rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
		console.log('state changed: from "%s" to "%s"', fromState.name, toState.name);
		if (toState.name === 'login') {
			$rootScope.user = {};
		} else {
			$rootScope.user = {name: 'Mr. Johnson', houseId: 1, isAuthenticated: true};
		}
	})
});
