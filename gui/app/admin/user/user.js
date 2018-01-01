angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
  .when('/password/requestreset', {
        templateUrl: 'admin/user/requestResetPassword.html',
        controller: 'PasswordResetRequestCtrl'
    })
    .when('/password/reset/:resetId', {
        templateUrl: 'admin/user/resetPassword.html',
        controller: 'PasswordResetCtrl'
    })
    .when('/users/viewAll', {
        templateUrl: 'admin/user/userList.html',
        controller: 'UserListCtrl'
    })
    .when('/users/edit/:id', {
        templateUrl: 'admin/user/userDetails.html',
        controller: 'UserCtrl'
    });
}])

.controller('UserListCtrl', ['$scope', 'UserSvc', '$route', 'ConfirmDialogSvc', function ($scope, userSvc, $route, confirmDialogSvc) {
    $scope.users = {};
    
    userSvc.getUsers().then(function (result) {
        $scope.users = result.data;
    });
    
    $scope.deleteUser = function(userId, userName) {
        confirmDialogSvc.confirm("Are you sure you wish to delete user " + userName +"?", function(){
            userSvc.deleteUser(userId).then(function() {
                $route.reload();
            });
        });
    }; 
}])

.controller('UserCtrl', ['$scope', '$location', '$route', '$routeParams', 'UserSvc', function ($scope, $location, $route, $routeParams, userSvc) {
    $scope.user = {};
    
    userSvc.getUser($routeParams.id).then(function(result) {
        $scope.user = result.data;
    });

    $scope.edit = function() {
        userSvc.editUser($scope.user.id, $scope.user).then(function() {
            $location.path("/users/viewAll");
        });
    }; 
}])

.controller('PasswordResetRequestCtrl', ['$scope', '$location', 'UserSvc', function ($scope, $location, UserSvc) {
    $scope.username = null;
    $scope.email = null;
   
    $scope.requestResetPassword = function() {
        UserSvc.requestResetPassword({"username":$scope.username,"email":$scope.email}).then(function (data) {
           $location.path("/login");
        });
    };
}])

.controller('PasswordResetCtrl', ['$scope', '$location', '$routeParams', 'UserSvc', function ($scope, $location, $routeParams, UserSvc) {
    $scope.password = null;
    $scope.confirmPassword = null;
   
    $scope.resetPassword = function() {
        if($scope.password !== null && ($scope.password === $scope.confirmPassword)) {
            UserSvc.resetPassword({"resetId":$routeParams.resetId,"newPassword":$scope.password}).then(function (data) {
               $location.path("/login");
            });
        } else
            alert("Passwords cannot be empty and must match");
    };
}])

.factory('UserState', [
    function() {                    
        var userState = {
            user: {},
            users: {},
            editMode: false,
            addMode: false
        };

        function setUsers(data) {
            userState.users = data;
        }

        function setUser(data, editMode, addMode) {
            userState.user = data;
            userState.editMode = editMode;
            userState.addMode = addMode;
        }

        function get() {
            return userState;
        }

        return {
            setUsers: setUsers,
            setUser: setUser,
            get: get
        };
    }])

.factory('UserSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {        
    var userSvc={};
    
    userSvc.requestResetPassword = function(resetParam){
        return $http.post(RESOURCES.REST_BASE_URL + '/users/resetpassword', resetParam, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    userSvc.resetPassword = function(resetParam){
        return $http.post(RESOURCES.REST_BASE_URL + '/users/reset/changepassword', resetParam, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };

    userSvc.getUsers = function(){
        return $http.get(RESOURCES.REST_BASE_URL + '/users/').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    userSvc.getUser = function(userId){
        return $http.get(RESOURCES.REST_BASE_URL + '/users/' + userId).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    userSvc.createUser = function(user){
        return $http.post(RESOURCES.REST_BASE_URL + "/users/create", user, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    userSvc.editUser = function(userId, user){
        return $http.put(RESOURCES.REST_BASE_URL + "/users/" + userId, user, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
     
    userSvc.deleteUser = function(id){
        return $http.delete(RESOURCES.REST_BASE_URL + "/users/" + id, {silentHttpErrors : true}).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
        
    return userSvc;
}]);