angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/admin/tokens/management', {
        templateUrl: 'admin/tokens/tokenManagement.html',
        controller: 'TokenManagementCtrl'
    });
}])

.controller('TokenManagementCtrl', ['$scope', 'TokenAdminState', 'TokenAdminSvc', '$location', '$route', function ($scope, tokenAdminState, tokenAdminSvc, $location, $route) {
    $scope.search = tokenAdminState.reset();
    $scope.tokenUsability = ["ALL", "BARBARIAN", "BARD", "CLERIC", "DRUID", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];
    $scope.tokenSlots = ["BACK", "CHARM", "EAR", "EYES", "FEET", "FIGURINE", "FINGER", "HANDS", "HEAD", "IOUNSTONE", "LEGS", "MAINHAND", 
        "NECK", "OFFHAND", "RUNESTONE", "SHIRT", "SLOTLESS", "TORSO", "WAIST", "WRIST"];
    $scope.tokenRarities = ["COMMON", "UNCOMMON", "RARE", "ULRARARE", "ENHANCED", "EXALTED", "RELIC", "LEGENDARY", "ELDRITCH", "PREMIUM", "ARTIFACT"];

    $scope.usabilitySelected =  function(usableBy){
        return $.inArray(usableBy, $scope.search.usableBy) > -1;
    };
    
    $scope.toggleUsabilitySelected =  function(usableBy){
        if($scope.usabilitySelected(usableBy)) 
            $scope.search.usableBy.splice($scope.search.usableBy.indexOf(usableBy), 1);
        else
            $scope.search.usableBy.push(usableBy);
    };
    
    $scope.slotSelected =  function(slot){
        return slot === $scope.search.slot;
    };
    
    $scope.toggleSlotSelected =  function(slot){
        $scope.search.slot = slot;
    };
    
    $scope.raritySelected =  function(rarity){
        return rarity === $scope.search.rarity;
    };
    
    $scope.toggleRaritySelected =  function(rarity){
        $scope.search.rarity = rarity;
    };
    
    $scope.addToken = function(){
        tokenAdminSvc.addToken($scope.search).then(function() {
            $scope.search = tokenAdminState.reset();
        });
    };
}])

.factory('TokenAdminState', [
    function() {                    
        var tokenAdminState = {};
        
        function setContext(data) {
            tokenAdminState = data;
        }
        
        function reset() {
            tokenAdminState = {
                name: null,
                text: null,
                rarity: null,
                slot: null,
                usableBy: [],
                str: null,
                dex: null,
                con: null,
                intel: null,
                wis: null,
                cha: null,
                health: null,
                meleeHit: null,
                meleeDmg: null,
                meleeFire: false,
                meleeCold: false,
                meleeShock: false,
                meleeSonic: false,
                meleeEldritch: false,
                meleePoison: false,
                meleeDarkrift: false,
                meleeSacred: false,
                meleeAC: null,
                rangeHit: null,
                rangeDmg: null,
                rangeFire: false,
                rangeCold: false,
                rangeShock: false,
                rangeSonic: false,
                rangeEldritch: false,
                rangePoison: false,
                rangeDarkrift: false,
                rangeSacred: false,
                rangeAC: null,
                rangeMissileAC: null,
                fort: null,
                reflex: null,
                will: null,
                retDmg: null,
                retFire: false,
                retCold: false,
                retShock: false,
                retSonic: false,
                retEldritch: false,
                retPoison: false,
                retDarkrift: false,
                retSacred: false,
                cannotBeSuprised: false,
                freeMovement: false,
                psychic: false,
                spellDmg: null,
                spellHeal: null,
                spellResist: null,
                treasure: null
            };
            return tokenAdminState;   
        }

        function get() {
            return tokenAdminState;
        }

        return {
            setContext: setContext,
            reset: reset,
            get: get
        };
    }])

.factory('TokenAdminSvc',['$http', 'RESOURCES', function($http, RESOURCES) {    
    var tokenAdminSvc={};

    tokenAdminSvc.addToken = function(token) {
        console.log(token);
        return $http.put(RESOURCES.REST_BASE_URL + '/token/admin', token);
    };
    
    return tokenAdminSvc;
}]);