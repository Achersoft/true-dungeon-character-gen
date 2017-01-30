angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/admin/tokens/management', {
        templateUrl: 'admin/tokens/tokenManagement.html',
        controller: 'TokenManagementCtrl'
    });
}])

.controller('TokenManagementCtrl', ['$scope', 'SearchState', 'SearchSvc', '$location', '$route', function ($scope, searchState, searchSvc, $location, $route) {
    $scope.search = searchState.reset();
    $scope.tokenSlots = ["BACK", "CHARM", "EAR", "EYES", "FEET", "FIGURINE", "FINGER", "HANDS", "HEAD", "IOUN_STONE", "LEGS", "MAINHAND", 
        "NECK", "OFFHAND", "RUNESTONE", "SHIRT", "SLOTLESS", "TORSO", "WAIST", "WRIST"];
    $scope.tokenRarities = ["COMMON", "UNCOMMON", "RARE", "ULRARARE", "ENHANCED", "EXALTED", "RELIC", "LEGENDARY", "ELDRITCH", "PREMIUM", "ARTIFACT"];

    $scope.slotSelected =  function(slot){
        return $.inArray(slot, $scope.search.slot) > -1;
    };
    
    $scope.toggleSlotSelected =  function(slot){
        if($scope.slotSelected(slot)) 
            $scope.search.slot.splice($scope.search.slot.indexOf(slot), 1);
        else
            $scope.search.slot.push(slot);
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
        
        function setCardName(tokenName) {
            searchState = {name: tokenName, page: 1, limit: 20};
        }
        
        function setLikeName(tokenName) {
            searchState = {like: tokenName, page: 1, limit: 10, quick: true};
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
        return $http.put(RESOURCES.REST_BASE_URL + '/tokens/search/', searchState.get());
    };
    
    return searchSvc;
}]);