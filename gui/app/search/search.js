angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/search/advanced', {
        templateUrl: 'search/advanced.html',
        controller: 'AdvancedSearchCtrl'
    })
    .when('/search/results', {
        templateUrl: 'search/searchList.html',
        controller: 'SearchCtrl'
    });
}])

.controller('SearchCtrl', ['$scope', 'NgTableParams', 'SearchState', 'SearchSvc', function ($scope, NgTableParams, searchState, searchSvc) {
    $scope.tableParams = new NgTableParams({
        page: 1,         
        count: 20     
    },
    {   total: 0, 
        counts: [], 
        getData: function ($defer, params) {
            searchState.get().page = params.page();
            searchState.get().limit = params.count();
            searchSvc.search(params.page(), params.count()).success(function (result) {
                params.total(result.count);
                $defer.resolve(result.cards);
            }).error(function(error){
                $scope.status = 'Unable to load list for page ' + params.page() + ': ' + error;
            });
        }
    });
}])

.controller('AdvancedSearchCtrl', ['$scope', 'SearchState', 'SearchSvc', '$location', '$route', function ($scope, searchState, searchSvc, $location, $route) {
    $scope.search = searchState.reset();
    $scope.cardTypes = ["CREATURE", "ARTIFACT", "SORCERY", "INSTANT", "ENCHANTMENT", "PLANESWALKER", "LAND"];
    $scope.cardColors = ["BLUE", "BLACK", "GREEN", "RED", "WHITE", "COLORLESS"];
    $scope.cardRarities = ["COMMON", "UNCOMMON", "RARE", "MYTHIC", "SPECIAL", "BASIC"];
    $scope.cardLanguages = ["English", "Russian", "Japanese", "Korean", "Spanish", "German", "Chinese", "Portuguese", "French", "Italian"];

    $scope.typeSelected =  function(type){
        return $.inArray(type, $scope.search.types) > -1;
    };
    
    $scope.toggleTypeSelected =  function(type){
        if($scope.typeSelected(type)) 
            $scope.search.types.splice($scope.search.types.indexOf(type), 1);
        else
            $scope.search.types.push(type);
    };
    
    $scope.colorSelected =  function(type){
        return $.inArray(type, $scope.search.colors) > -1;
    };
    
    $scope.toggleColorSelected =  function(type){
        if($scope.colorSelected(type)) 
            $scope.search.colors.splice($scope.search.colors.indexOf(type), 1);
        else
            $scope.search.colors.push(type);
    };
    
    $scope.raritySelected =  function(type){
        return $.inArray(type, $scope.search.rarities) > -1;
    };
    
    $scope.toggleRaritySelected =  function(type){
        if($scope.raritySelected(type)) 
            $scope.search.rarities.splice($scope.search.rarities.indexOf(type), 1);
        else
            $scope.search.rarities.push(type);
    };
    
    $scope.languageSelected =  function(type){
        return $.inArray(type, $scope.search.languages) > -1;
    };
    
    $scope.toggleLanguageSelected =  function(type){
        if($scope.languageSelected(type)) 
            $scope.search.languages.splice($scope.search.languages.indexOf(type), 1);
        else
            $scope.search.languages.push(type);
    };
    
    $scope.doSearch = function(){
        $location.path("/search/results");
        $route.reload();
    };
}])

.factory('SearchState', [
    function() {                    
        var searchState = {
            name: null,
            like: null,
            colors: [],
            types: [],
            rarities: [],
            languages: ["English"],
            cmc: null,
            priceMin: null,
            priceMax: null,
            page: 1,
            limit: 20,
            quick: false,
            inStock: false
        };
        
        function setCardName(cardName) {
            searchState = {name: cardName, page: 1, limit: 20};
        }
        
        function setLikeName(cardName) {
            searchState = {like: cardName, page: 1, limit: 10, quick: true};
        }

        function setContext(data) {
            searchState = data;
        }
        
        function reset() {
            searchState = {
                    name: null,
                    like: null,
                    colors: [],
                    types: [],
                    rarities: [],
                    languages: ["English"],
                    cmc: null,
                    priceMin: null,
                    priceMax: null,
                    page: 1,
                    limit: 20,
                    quick: false,
                    inStock: false
                };
            return searchState;   
        }

        function get() {
            return searchState;
        }

        return {
            setCardName: setCardName,
            setLikeName: setLikeName,
            setContext: setContext,
            reset: reset,
            get: get
        };
    }])

.factory('SearchSvc',['$http', 'RESOURCES', 'SearchState', function($http, RESOURCES, searchState) {    
    var searchSvc={};

    searchSvc.search = function() {
        return $http.put(RESOURCES.REST_BASE_URL + '/cards/search/', searchState.get());
    };
    
    return searchSvc;
}]);