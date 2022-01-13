angular.module('main')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/admin/tokens/set/management', {
        templateUrl: 'admin/tokens/addSet-@{TDCC_VERSION}.html',
        controller: 'SetManagementCtrl'
    })
    .when('/admin/tokens/management', {
        templateUrl: 'admin/tokens/addToken-@{TDCC_VERSION}.html',
        controller: 'TokenManagementCtrl'
    })
    .when('/admin/tokens/edit', {
        templateUrl: 'admin/tokens/editToken-@{TDCC_VERSION}.html',
        controller: 'TokenEditCtrl'
    })
    .when('/admin/reset', {
        templateUrl: 'admin/tokens/reset-@{TDCC_VERSION}.html',
        controller: 'ResetManagementCtrl'
    });
}])

.controller('ResetManagementCtrl', ['$scope', 'TokenAdminSvc', '$location', '$route', function ($scope, tokenAdminSvc, $location, $route) {

    $scope.resetCharacterStats = function(){
        tokenAdminSvc.resetCharacterStats().then(function() {});
    };
}])

.controller('SetManagementCtrl', ['$scope', 'SetAdminState', 'TokenAdminSvc', '$location', '$route', function ($scope, setAdminState, tokenAdminSvc, $location, $route) {
    $scope.search = setAdminState.reset();

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
    
    $scope.addSetBonus = function(){
        $scope.search.bonus.push({
                    pieceCount: 0,
                    str: 0,
                    dex: 0,
                    con: 0,
                    intel: 0,
                    wis: 0,
                    cha: 0,
                    mFire: 0,
                    mCold: 0,
                    mShock: 0,
                    mSonic: 0,
                    mEldritch: 0,
                    mPoison: 0,
                    mDarkrift: 0,
                    mSacred: 0,
                    mForce: 0,
                    mAcid: 0,
                    rFire: 0,
                    rCold: 0,
                    rShock: 0,
                    rSonic: 0,
                    rEldritch: 0,
                    rPoison: 0,
                    rDarkrift: 0,
                    rSacred: 0,
                    rForce: 0,
                    rAcid: 0,
                    health: 0,
                    regen: 0,
                    meleeHit: 0,
                    meleeDmg: 0,
                    meleePolyHit: 0,
                    meleePolyDmg: 0,
                    meleeFire: false,
                    meleeCold: false,
                    meleeShock: false,
                    meleeSonic: false,
                    meleeEldritch: false,
                    meleePoison: false,
                    meleeDarkrift: false,
                    meleeSacred: false,
                    meleeAC: 0,
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
                    possession: false,
                    addLevel: false,
                    wonderEffect: false,
                    setRingsThree: false,
                    noRings: false,
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
                    drForce: 0,
                    headSlots: 0,
                    backSlots: 0,
                    stoneSlots: 0,
                    charmSlots: 0,
                    alwaysInEffect: false,
                    oncePerRound: false,
                    oncePerRoom: false,
                    oncePerGame: false,
                    specialText: null
                });
    };
    
    $scope.addSet = function(){
        tokenAdminSvc.addToken($scope.search).then(function() {
            $scope.search = setAdminState.reset();
        });
    };
}])

.controller('TokenManagementCtrl', ['$scope', 'TokenAdminState', 'TokenAdminSvc', '$location', '$route', function ($scope, tokenAdminState, tokenAdminSvc, $location, $route) {
    $scope.search = tokenAdminState.reset();
    $scope.tokenUsability = ["ALL", "BARBARIAN", "BARD", "CLERIC", "DRUID", "DWARF_FIGHTER", "ELF_WIZARD", "FIGHTER", "WIZARD", "MONK", "PALADIN", "RANGER", "ROGUE"];
    $scope.tokenSlots = ["BACK", "BEAD", "CHARM", "EAR", "EYES", "FEET", "FIGURINE", "FINGER", "HANDS", "HEAD", "INSTRUMENT", "IOUNSTONE", "LEGS", "MAINHAND", 
        "NECK", "OFFHAND", "POLYMORPH","RUNESTONE", "SHINS", "SHIRT", "SLOTLESS", "TORSO", "WAIST", "WRIST"];
    $scope.tokenRarities = ["COMMON", "UNCOMMON", "RARE", "ULTRARARE", "ENHANCED", "EXALTED", "RELIC", "LEGENDARY", "ELDRITCH", "PREMIUM", "ARTIFACT", "PLAYER_REWARD"];
    $scope.tokenConditionals = ["NONE", "DEXTERITY_18", "DEXTERITY_20", "INTELLECT_20", "WISDOM_20", "WEAPON_2H", "WEAPON_1H", "WEAPON_RANGED", "WEAPON_RANGED_2H", "MAY_NOT_USE_SHIELDS", "STRENGTH_24", "NOT_WITH_COA", "NO_OTHER_TREASURE", 
        "NOT_WITH_PRO_SCROLL", "NOT_WITH_ROSP", "IRON_WEAPON", "SLING", "CROSSBOW", "BOW", "THRALL_WEAPON", "NOT_WITH_COS_COA", "ONE_OTHER_UR_TREASURE", "DIRK_WEAPON", "COMMON_WEAPON", "RARE_WEAPON", "NO_OTHER_IOUN_STONE", "MISSILE_ATTACK", "NOT_RARE_PLUS_TORSO", 
        "NOT_UR_PLUS_RING", "GOBLIN_WEAPON", "DWARF_WEAPON", "NOT_WITH_OTHER_WONDER", "PLUS_1_AC_GOBLIN_WEAPON", "UNCOMMON_OR_BELOW_WEAPON", "LESS_THAN_10_HIT_MELEE", "UNCOMMON_OR_BELOW_WEAPON_RANGE", "NO_OTHER_HAVOC", "NOT_WITH_SOC", "NOT_WITH_ROSS"];
        
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
    $scope.tokenSlots = ["BACK", "BEAD", "CHARM", "EAR", "EYES", "FEET", "FIGURINE", "FINGER", "HANDS", "HEAD", "INSTRUMENT", "IOUNSTONE", "LEGS", "MAINHAND", 
        "NECK", "OFFHAND", "POLYMORPH", "RUNESTONE", "SHINS", "SHIRT", "SLOTLESS", "TORSO", "WAIST", "WRIST"];
    $scope.tokenRarities = ["COMMON", "UNCOMMON", "RARE", "ULTRARARE", "ENHANCED", "EXALTED", "RELIC", "LEGENDARY", "ELDRITCH", "PREMIUM", "ARTIFACT", "PLAYER_REWARD"];
    $scope.tokenConditionals = ["NONE", "DEXTERITY_18", "DEXTERITY_20", "INTELLECT_20", "WISDOM_20", "WEAPON_2H", "WEAPON_1H", "WEAPON_RANGED", "WEAPON_RANGED_2H", "MAY_NOT_USE_SHIELDS", "STRENGTH_24", "NOT_WITH_COA", "NO_OTHER_TREASURE", 
        "NOT_WITH_PRO_SCROLL", "NOT_WITH_ROSP", "IRON_WEAPON", "SLING", "CROSSBOW", "BOW", "THRALL_WEAPON", "NOT_WITH_COS_COA", "ONE_OTHER_UR_TREASURE", "DIRK_WEAPON", "COMMON_WEAPON", "RARE_WEAPON", "NO_OTHER_IOUN_STONE", "MISSILE_ATTACK", "NOT_RARE_PLUS_TORSO", 
        "NOT_UR_PLUS_RING", "GOBLIN_WEAPON", "DWARF_WEAPON", "NOT_WITH_OTHER_WONDER", "PLUS_1_AC_GOBLIN_WEAPON", "UNCOMMON_OR_BELOW_WEAPON", "LESS_THAN_10_HIT_MELEE", "UNCOMMON_OR_BELOW_WEAPON_RANGE", "NO_OTHER_HAVOC", "NOT_WITH_SOC", "NOT_WITH_ROSS"];
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
        if ($scope.search.damageRange !== null && $scope.search.damageRange === '')
            $scope.search.damageRange = null;
        if ($scope.search.damageExplodeRange !== null && $scope.search.damageExplodeRange === '')
            $scope.search.damageExplodeRange = null;
        if ($scope.search.weaponExplodeCondition !== null && $scope.search.weaponExplodeCondition === '')
            $scope.search.weaponExplodeCondition = null;
        if ($scope.search.weaponExplodeText !== null && $scope.search.weaponExplodeText === '')
            $scope.search.weaponExplodeText = null;

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
                damageRange: null,
                damageExplodeRange: null,
                weaponExplodeCondition: null,
                weaponExplodeText: null,
                critMin: 20,
                usableBy: [],
                str: 0,
                dex: 0,
                con: 0,
                intel: 0,
                wis: 0,
                cha: 0,
                mFire: 0,
                mCold: 0,
                mShock: 0,
                mSonic: 0,
                mEldritch: 0,
                mPoison: 0,
                mDarkrift: 0,
                mSacred: 0,
                mForce: 0,
                mAcid: 0,
                rFire: 0,
                rCold: 0,
                rShock: 0,
                rSonic: 0,
                rEldritch: 0,
                rPoison: 0,
                rDarkrift: 0,
                rSacred: 0,
                rForce: 0,
                rAcid: 0,
                health: 0,
                regen: 0,
                oneHanded: false,
                twoHanded: false,
                thrown: false,
                rangerOffhand: false,
                instrument: false,
                monkOffhand: false,
                shuriken: false,
                bracerWeapon: false,
                shield: false,
                mug: false,
                buckler: false,
                holyWeapon: false,
                meleeHit: 0,
                meleeDmg: 0,
                meleePolyHit: 0,
                meleePolyDmg: 0,
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
                possession: false,
                addLevel: false,
                wonderEffect: false,
                setRingsThree: false,
                noRings: false,
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
                drForce: 0,
                headSlots: 0,
                backSlots: 0,
                stoneSlots: 0,
                charmSlots: 0,
                eyeSlots: 0,
                figurineSlots: 0,
                rareEyeSlots: 0,
                rareFeetSlots: 0,
                rareLegSlots: 0,
                rareShirtSlots: 0,
                rareWaistSlots: 0,
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

.factory('SetAdminState', [
    function() {                    
        var setAdminState = {};
        
        function setContext(data) {
            setAdminState = data;
        }
        
        function reset() {
            setAdminState = {
                id: null,
                name: null,
                bonus: [{
                    pieceCount: 0,
                    str: 0,
                    dex: 0,
                    con: 0,
                    intel: 0,
                    wis: 0,
                    cha: 0,
                    mFire: 0,
                    mCold: 0,
                    mShock: 0,
                    mSonic: 0,
                    mEldritch: 0,
                    mPoison: 0,
                    mDarkrift: 0,
                    mSacred: 0,
                    mForce: 0,
                    mAcid: 0,
                    rFire: 0,
                    rCold: 0,
                    rShock: 0,
                    rSonic: 0,
                    rEldritch: 0,
                    rPoison: 0,
                    rDarkrift: 0,
                    rSacred: 0,
                    rForce: 0,
                    rAcid: 0,
                    health: 0,
                    regen: 0,
                    meleeHit: 0,
                    meleeDmg: 0,
                    meleePolyHit: 0,
                    meleePolyDmg: 0,
                    meleeFire: false,
                    meleeCold: false,
                    meleeShock: false,
                    meleeSonic: false,
                    meleeEldritch: false,
                    meleePoison: false,
                    meleeDarkrift: false,
                    meleeSacred: false,
                    meleeAC: 0,
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
                    possession: false,
                    addLevel: false,
                    wonderEffect: false,
                    setRingsThree: false,
                    noRings: false,
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
                    drForce: 0,
                    headSlots: 0,
                    backSlots: 0,
                    stoneSlots: 0,
                    charmSlots: 0,
                    alwaysInEffect: false,
                    oncePerRound: false,
                    oncePerRoom: false,
                    oncePerGame: false,
                    specialText: null
                }]
            };
            return setAdminState;   
        }

        function get() {
            return setAdminState;
        }

        function addBonus() {
            setAdminState.bonus.push({
                    pieceCount: 0,
                    str: 0,
                    dex: 0,
                    con: 0,
                    intel: 0,
                    wis: 0,
                    cha: 0,
                    mFire: 0,
                    mCold: 0,
                    mShock: 0,
                    mSonic: 0,
                    mEldritch: 0,
                    mPoison: 0,
                    mDarkrift: 0,
                    mSacred: 0,
                    mForce: 0,
                    mAcid: 0,
                    rFire: 0,
                    rCold: 0,
                    rShock: 0,
                    rSonic: 0,
                    rEldritch: 0,
                    rPoison: 0,
                    rDarkrift: 0,
                    rSacred: 0,
                    rForce: 0,
                    rAcid: 0,
                    health: 0,
                    regen: 0,
                    meleeHit: 0,
                    meleeDmg: 0,
                    meleePolyHit: 0,
                    meleePolyDmg: 0,
                    meleeFire: false,
                    meleeCold: false,
                    meleeShock: false,
                    meleeSonic: false,
                    meleeEldritch: false,
                    meleePoison: false,
                    meleeDarkrift: false,
                    meleeSacred: false,
                    meleeAC: 0,
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
                    possession: false,
                    addLevel: false,
                    wonderEffect: false,
                    setRingsThree: false,
                    noRings: false,
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
                    drForce: 0,
                    headSlots: 0,
                    backSlots: 0,
                    stoneSlots: 0,
                    charmSlots: 0,
                    alwaysInEffect: false,
                    oncePerRound: false,
                    oncePerRoom: false,
                    oncePerGame: false,
                    specialText: null
                });
            return setAdminState;
        }
        
        function removeBonus() {
            if (setAdminState.bonus.length > 1)
                setAdminState.bonus.pop();
            return setAdminState;
        }
        
        return {
            setContext: setContext,
            reset: reset,
            get: get,
            addBonus: addBonus,
            removeBonus: removeBonus
        };
    }])

.factory('TokenAdminSvc',['$http', 'RESOURCES', 'ErrorDialogSvc', '$q', function($http, RESOURCES, errorDialogSvc, $q) {
    var tokenAdminSvc={};

    tokenAdminSvc.addToken = function(token) {
        return $http.put(RESOURCES.REST_BASE_URL + '/token/admin', token).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.editToken = function(token) {
        return $http.post(RESOURCES.REST_BASE_URL + '/token/admin', token).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.search = function(name) {
        return $http.get(RESOURCES.REST_BASE_URL + '/token/search/?name=' + name).catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    tokenAdminSvc.resetCharacterStats = function() {
        return $http.get(RESOURCES.REST_BASE_URL + '/character/resetAll').catch(function(response) {
            errorDialogSvc.showError(response);
            return($q.reject(response));
        });
    };
    
    return tokenAdminSvc;
}]);