angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/setSelection/:language', {
        templateUrl: 'sets/setSelection.html',
        controller: 'SetCtrl'
    })
    .when('/setList/:setId/:language', {
        templateUrl: 'sets/setList.html',
        controller: 'SetCtrl'
    });
}])

.controller('SetCtrl', ['$scope', '$routeParams', 'RESOURCES', 'NgTableParams', 'SetSvc', function ($scope, $routeParams, RESOURCES, NgTableParams, setSvc) {
    $scope.sets;
    $scope.imgBaseURL = RESOURCES.IMG_BASE_URL;
    
    setSvc.getSets($routeParams.language).success(function (data) {
        $scope.sets = data;
    });

    $scope.headerLinks = ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"];
    
    $scope.hasAnchor = function (start) {
        for(var set in $scope.sets) {
            
           // console.log($scope.sets[set].name);
           // console.log(start.toLowerCase());
          //  console.log(set.toLowerCase().indexOf(start.toLowerCase()));
            if($scope.sets[set].name.toLowerCase().indexOf(start) === 0)
                return true;
        }
        return false;
    };

    $scope.startsWith = function (actual, expected) {
        var lowerStr = (actual + "").toLowerCase();
        return lowerStr.indexOf(expected.toLowerCase()) === 0;
    };

    $scope.gotoAnchor = function(x) {
        var newHash = 'anchor' + x;
        if ($location.hash() !== newHash) {
          // set the $location.hash to `newHash` and
          // $anchorScroll will automatically scroll to it
          $location.hash('anchor' + x);
        } else {
          // call $anchorScroll() explicitly,
          // since $location.hash hasn't changed
          $anchorScroll();
        }
    };

    $scope.tableParams = new NgTableParams({
        page: 1,         
        count: 20     
    },
    {   total: 0, 
        counts: [], 
        getData: function ($defer, params) {
            setSvc.getCards($routeParams.setId, $routeParams.language).success(function (result) {
                params.total(result.length);
                $defer.resolve(result.slice((params.page() - 1) * params.count(), params.page() * params.count()));
            }).error(function(error){
                $scope.status = 'Unable to load candidate list for page ' + params.page() + ': ';
            });
        }
    });
}])

.factory('SetSvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var setSvc={};

    setSvc.getSets = function(lang){
        return $http.get(RESOURCES.REST_BASE_URL + '/cards/sets?language=' + lang);
    };
    
    setSvc.getCards = function(setId, lang){
        return $http.get(RESOURCES.REST_BASE_URL + '/cards/sets/' + setId + '?language=' + lang);
    };

    return setSvc;
}]);