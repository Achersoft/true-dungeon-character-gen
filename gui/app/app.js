'use strict';

// Declare app level module which depends on views, and components
angular.module('main', [
  'ui.bootstrap', 
  'ngAnimate',
  'ngRoute',
  'angular-storage',
  'angular-jwt',
  'http-auth-interceptor',
  'toggle-switch'
])
.constant('RESOURCES', (function() {
    'use strict';
    var restBaseUrl = 'http://localhost:8080/TD-Character-Gen';
    var uriBaseUrl = 'http://localhost:8080';
    return {
        // Service URLs
        IMG_BASE_URL    : uriBaseUrl,
        REST_BASE_URL   : restBaseUrl,
        ENUMS_BASE_URL  : restBaseUrl + '/enum',
        LOGIN_URL       : restBaseUrl + '/login',
        LOGOUT_URL      : restBaseUrl + '/logout'
    };
})())  
.filter('capitalize', function() {
  return function(input) {
    if (input===null || input===undefined)
        return input;
    input = input.toLowerCase();
    return input.substring(0,1).toUpperCase()+input.substring(1);
  };
})
.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/character/mine'});
}])
.run(['$rootScope', '$location', 'RESOURCES', function($rootScope, $location, RESOURCES) {
    $rootScope.loginRequired = false;

    // If angular-http-auth inteceptor fires "requires login" event, we need to display login "view"
    $rootScope.$on('event:auth-loginRequired', function() {
        $rootScope.loginRequired = true;
        $location.path("/login");
    });
    
    $rootScope.$on('event:auth-loginConfirmed', function() {
        $rootScope.loginRequired = false;
    });
    
    $rootScope.$on('event:auth-forbidden', function(event, rejection) {
        $rootScope.loginRequired = true;
        $location.path("/login");
    });

    // Handles HTTP errors that are not specifically handled by the application services/controllers
    $rootScope.$on(RESOURCES.ESI_EVENT_HTTP_ERROR, function(event, rejection) {
        // 401 - Not Auth is handled separately - causing app to display login "page" (area)
        // 403 - is Forbidden and is handled by event above - which is broadcast by
        // http-auth-inteceptor
        if ( rejection.status !== 401 && rejection.status !== 403 ) {
            var errstr;
            if ( rejection.status === -1 ) {
                errstr = 'Unable to communicate with server.';
            } else {
                errstr = 'An unexpected error occurred while comunicating with the server.';
            }
        }
    });
}]);
