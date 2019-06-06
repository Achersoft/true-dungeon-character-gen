angular.module('main').directive('navBar',[ '$location', '$rootScope', 'AuthorizationSvc',  function($location, $rootScope, AuthorizationSvc){
    return{
        restrict:'E',
        templateUrl:'common/topNav/navBar-@{TDCC_VERSION}.html',
        link: function(scope) {
            scope.logout = function() {
                AuthorizationSvc.release().then(function() {
                    $rootScope.loginRequired = true;
                    $location.path("/login");
                });
            };
        } 
    };
}]);