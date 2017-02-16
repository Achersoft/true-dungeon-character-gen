angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/admin/tokens/management', {
        templateUrl: 'admin/tokens/addToken.html',
        controller: 'TokenManagementCtrl'
    })
    .when('/admin/tokens/edit', {
        templateUrl: 'admin/tokens/editToken.html',
        controller: 'TokenEditCtrl'
    });
}])

.controller('TokenManagementCtrl', ['$scope', 'TokenAdminState', 'TokenAdminSvc', '$location', '$route', function ($scope, tokenAdminState, tokenAdminSvc, $location, $route) {
    $scope.search = tokenAdminState.reset();
    $scope.tokenUsability = ["ALL", "BARBARIAN", "BARD", "CLERIC", "DRUID", "DWARF_FIGHTER", "ELF_WIZARD", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];
    $scope.tokenSlots = ["BACK", "CHARM", "EAR", "EYES", "FEET", "FIGURINE", "FINGER", "HANDS", "HEAD", "INSTRUMENT", "IOUNSTONE", "LEGS", "MAINHAND", 
        "NECK", "OFFHAND", "RUNESTONE", "SHIRT", "SLOTLESS", "TORSO", "WAIST", "WRIST"];
    $scope.tokenRarities = ["COMMON", "UNCOMMON", "RARE", "ULTRARARE", "ENHANCED", "EXALTED", "RELIC", "LEGENDARY", "ELDRITCH", "PREMIUM", "ARTIFACT"];
    $scope.tokenConditionals = ["NONE", "DEXTERITY_18", "DEXTERITY_20", "INTELLECT_20", "WISDOM_20", "WEAPON_2H", "WEAPON_1H", "WEAPON_RANGED", "MAY_NOT_USE_SHIELDS", "STRENGTH_24"];
    
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
    
    $scope.conditionalSelected =  function(conditionalUse){
        return conditionalUse === $scope.search.conditionalUse;
    };
    
    $scope.toggleConditionalSelected =  function(conditionalUse){
        $scope.search.conditionalUse = conditionalUse;
    };
    
    $scope.addToken = function(){
        tokenAdminSvc.addToken($scope.search).then(function() {
            $scope.search = tokenAdminState.reset();
        });
    };
}])

.controller('TokenEditCtrl', ['$scope', 'TokenAdminState', 'TokenAdminSvc', '$location', '$route', function ($scope, tokenAdminState, tokenAdminSvc, $location, $route) {
    $scope.search = tokenAdminState.reset();
    $scope.tokenUsability = ["ALL", "BARBARIAN", "BARD", "CLERIC", "DRUID", "DWARF_FIGHTER", "ELF_WIZARD", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];
    $scope.tokenSlots = ["BACK", "CHARM", "EAR", "EYES", "FEET", "FIGURINE", "FINGER", "HANDS", "HEAD", "INSTRUMENT", "IOUNSTONE", "LEGS", "MAINHAND", 
        "NECK", "OFFHAND", "RUNESTONE", "SHIRT", "SLOTLESS", "TORSO", "WAIST", "WRIST"];
    $scope.tokenRarities = ["COMMON", "UNCOMMON", "RARE", "ULTRARARE", "ENHANCED", "EXALTED", "RELIC", "LEGENDARY", "ELDRITCH", "PREMIUM", "ARTIFACT"];
    $scope.tokenConditionals = ["NONE", "DEXTERITY_18", "DEXTERITY_20", "INTELLECT_20", "WISDOM_20", "WEAPON_2H", "WEAPON_1H", "WEAPON_RANGED", "MAY_NOT_USE_SHIELDS", "STRENGTH_24"];
    
    $scope.selectedToken = '';
            
    $scope.onSelect = function($item){
        tokenAdminState.setContext($item);
        $scope.search = tokenAdminState.get();
        $scope.selectedToken = '';
    };

    $scope.searchForToken = function(viewValue) {
        return tokenAdminSvc.search(viewValue).then(function(response) {
            console.log(response.data);
            return response.data;
        });
    };
    
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
    
    $scope.conditionalSelected =  function(conditionalUse){
        return conditionalUse === $scope.search.conditionalUse;
    };
    
    $scope.toggleConditionalSelected =  function(conditionalUse){
        $scope.search.conditionalUse = conditionalUse;
    };
    
    $scope.editToken = function(){
        tokenAdminSvc.editToken($scope.search).then(function() {
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
                id: null,
                name: null,
                text: null,
                rarity: null,
                slot: null,
                usableBy: [],
                str: 0,
                dex: 0,
                con: 0,
                intel: 0,
                wis: 0,
                cha: 0,
                health: 0,
                regen: 0,
                oneHanded: false,
                twoHanded: false,
                thrown: false,
                rangerOffhand: false,
                instrument: false,
                shield: false,
                meleeHit: 0,
                meleeDmg: 0,
                meleeFire: false,
                meleeCold: false,
                meleeShock: false,
                meleeSonic: false,
                meleeEldritch: false,
                meleePoison: false,
                meleeDarkrift: false,
                meleeSacred: false,
                meleeAC: 0,
                rangedWeapon: false,
                rangeHit: 0,
                rangeDmg: 0,
                rangeFire: false,
                rangeCold: false,
                rangeShock: false,
                rangeSonic: false,
                rangeEldritch: false,
                rangePoison: false,
                rangeDarkrift: false,
                rangeSacred: false,
                rangeAC: 0,
                rangeMissileAC: 0,
                fort: 0,
                reflex: 0,
                will: 0,
                retDmg: 0,
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
                spellDmg: 0,
                spellHeal: 0,
                spellResist: 0,
                initiative: 0,
                treasureMin: 0,
                treasureMax: 0, 
                drMelee: 0,
                drRange: 0,
                drSpell: 0,
                drFire: 0,
                drCold: 0,
                drShock: 0,
                drSonic: 0,
                drEldritch: 0,
                drPoison: 0,
                drDarkrift: 0,
                drSacred: 0,
                conditionalUse: 'NONE',
                alwaysInEffect: false,
                oncePerRound: false,
                oncePerRoom: false,
                oncePerGame: false,
                specialText: null
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
        return $http.put(RESOURCES.REST_BASE_URL + '/token/admin', token);
    };
    
    tokenAdminSvc.editToken = function(token) {
        return $http.post(RESOURCES.REST_BASE_URL + '/token/admin', token);
    };
    
    tokenAdminSvc.search = function(name) {
        return $http.get(RESOURCES.REST_BASE_URL + '/token/search/?name=' + name);
    };
    
    return tokenAdminSvc;
}]);