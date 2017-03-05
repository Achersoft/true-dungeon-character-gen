angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/password/reset/:resetId', {
        templateUrl: 'admin/user/resetPassword.html',
        controller: 'PasswordResetCtrl'
    })
    .when('/users/viewAll', {
        templateUrl: 'admin/user/userList.html',
        controller: 'UserCtrl'
    })
    .when('/users/add', {
        templateUrl: 'admin/user/userDetails.html',
        controller: 'UserCtrl'
    });
}])

.controller('UserCtrl', ['$scope', '$location', '$route', '$routeParams', 'NgTableParams', 'UserSvc', 'UserState', function ($scope, $location, $route, $routeParams, NgTableParams, userSvc, userState) {
    $scope.userContext = userState.get();
    
    $scope.add = function() {
        userState.setUser({}, true, true);
        $location.path("/users/add");
    };
    
    $scope.edit = function(userId) {
        userSvc.getUser(userId).then(function (result) {
            userState.setUser(result.data, true, false);
            $location.path("/users/add");
        });
    }; 
    
    $scope.create = function(isValid) {
        if(isValid) {
            userSvc.createUser($scope.userContext.user).then(function() {
                $location.path("/users/viewAll");
            });
        }
    }; 
    
    $scope.editUser = function(isValid) {
        if(isValid) {
            userSvc.editUser($scope.userContext.user.id, $scope.userContext.user).then(function() {
                $location.path("/users/viewAll");
            });
        }
    }; 
    
    $scope.delete = function(userId) {
        userSvc.deleteUser(userId).then(function() {
            $route.reload();
        });
    }; 
    
    $scope.tableParams = new NgTableParams({
        page: 1,         
        count: 20     
    },
    {   total: 0, 
        counts: [], 
        getData: function ($defer, params) {
            userSvc.getUsers().success(function (result) {
                params.total(result.length);
                $defer.resolve(result.slice((params.page() - 1) * params.count(), params.page() * params.count()));
            }).error(function(error){
                $scope.status = 'Unable to load candidate list for page ' + params.page() + ': ';
            });
        }
    });
}])

.controller('PasswordResetCtrl', ['$scope', '$location', '$route', '$routeParams', 'NgTableParams', 'UserSvc', 'UserState', function ($scope, $location, $route, $routeParams, NgTableParams, userSvc, userState) {
    $scope.userContext = userState.get();
    
    $scope.add = function() {
        userState.setUser({}, true, true);
        $location.path("/users/add");
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

.factory('UserSvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var userSvc={};

    userSvc.getUsers = function(){
        return $http.get(RESOURCES.REST_BASE_URL + '/users/');
    };
    
    userSvc.getUser = function(userId){
        return $http.get(RESOURCES.REST_BASE_URL + '/users/' + userId);
    };
    
    userSvc.createUser = function(user){
        return $http.post(RESOURCES.REST_BASE_URL + "/users/create",
                          user, {silentHttpErrors : true});
    };
    
    userSvc.editUser = function(userId, user){
        return $http.put(RESOURCES.REST_BASE_URL + "/users/" + userId,
                          user, {silentHttpErrors : true});
    };
     
    userSvc.deleteUser = function(id){
        return $http.delete(RESOURCES.REST_BASE_URL + "/users/" + id,
                            {silentHttpErrors : true});
    };
        
    return userSvc;
}]);