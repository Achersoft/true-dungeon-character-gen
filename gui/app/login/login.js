angular.module('main')

.config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/login/', {
            templateUrl: 'login/login.html',
            controller: 'LoginCtrl'
        }),
    $routeProvider
        .when('/signup/', {
            templateUrl: 'login/signup.html',
            controller: 'SignupCtrl'
        });
}])
.controller('SignupCtrl',['$scope', '$location', '$rootScope', 'AuthorizationSvc', 'AuthorizationState',
    function($scope, $location, $rootScope, AuthorizationSvc, AuthorizationState){
        $scope.createUserValues = { username : '',
                                    firstName : '',
                                    lastName : '',
                                    email : '',
                                    password : ''};
        
        $scope.createAccount = function(){
            AuthorizationSvc.createNewAccount($scope.createUserValues)
                    .then(function(response) {
                        $location.path("/setSelection/English");
                    },
                      function(response) {
                        // Wipe out password to allow re-entry
                        $scope.createUserValues.username = '';
                        $scope.createUserValues.password = '';
                    })
                    .finally(function() {
                        // Wipe out - if error 
                        $scope.createUserValues.username = '';
                        $scope.createUserValues.password = '';
                        // Reset form - to hide validation errors, etc. for next go round...
                        $scope.createaccountform.$setPristine();
                    });
        };
    }
])
.controller('LoginCtrl',['$scope', '$location', '$rootScope', 'AuthorizationSvc', 'AuthorizationState',
    function($scope, $location, $rootScope, AuthorizationSvc, AuthorizationState){
        $scope.loginPrompt = { username : '',
                               password : '',
                               invalidMsg : ''};
        
        $scope.login = function(){
            AuthorizationSvc.authenticate($scope.loginPrompt.username, $scope.loginPrompt.password)
                    .then(function(response) {
                        $location.path("/character/mine");
                        },
                          function(response) {
                            // Wipe out password to allow re-entry
                            $scope.loginPrompt.username = '';
                            $scope.loginPrompt.password = '';
                            $scope.loginPrompt.invalidMsg = "Login failed. Invalid name and/or password. Please retry.";
                        })
                    .finally(function() {
                        // Wipe out - if error 
                        $scope.loginPrompt.username = '';
                        $scope.loginPrompt.password = '';
                        // Reset form - to hide validation errors, etc. for next go round...
                        $scope.estaffLoginForm.$setPristine();
                    });
        };
        
        $scope.isLoggedIn = function() {
            return AuthorizationState.isAuthorized();
        };
    }
]);


