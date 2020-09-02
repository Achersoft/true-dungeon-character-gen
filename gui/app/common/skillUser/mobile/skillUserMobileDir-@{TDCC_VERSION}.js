angular.module('main').directive('skillUserMobile',['CharacterSvc', '$uibModal', function(characterSvc, $uibModal){
    return {
        restrict:'E',
        scope:{
            model:'=',
            characterContext:'=',
            elementId: '@',
            useAbility: '&?',
            unuseAbility: '&?',
            roll: '&?'
        },
        link: function(scope) {
            scope.itemSelection = {};
            scope.targetIndex = 0;
            scope.madEvokerIndex = 0;
            scope.secondaryTargetIndex = 0;
            scope.skillCheckIndex = 0;
            scope.hitRoll = 0;
            scope.hitRollNatural = 0;
            scope.damage = 0;
            scope.damagePool = 0;
            scope.primaryHealAmount = 0;
            scope.seconaryHealAmount = 0;
            scope.spellCast = false;
            scope.healError = false;
            scope.modalInstance = null;
            
            scope.openModal = function(isChecked) {
                if (isChecked) {
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'mobile-modal-dialog',
                        openedClass: 'mobile-modal-content',
                        templateUrl: 'common/skillUser/mobile/skillUserUnsetMobileModalTemplate-@{TDCC_VERSION}.html'
                    });
                } else {
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'mobile-modal-dialog',
                        openedClass: 'mobile-modal-content',
                        templateUrl: 'common/skillUser/mobile/skillUserMobileModalTemplate-@{TDCC_VERSION}.html'
                    });
                }
            };
            
            scope.setSkillCheckIndex = function(index) {
                 scope.skillCheckIndex = index;
            };
            
            scope.setTargetIndex = function(index) {
                 scope.targetIndex = index;
            };
            
            scope.setSecondaryTargetIndex = function(index) {
                 scope.secondaryTargetIndex = index;
            };
            
            scope.setMadEvokerIndex = function(index) {
                 scope.madEvokerIndex = index;
            };
            
            scope.useSkill = function(primaryAmount, seconaryHealAmount) {
                if (scope.model.skillType === 'HEAL') {
                    var totalHeal = scope.characterContext.stats.spellHeal + ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);
                        
                    if (scope.secondaryTargetIndex !== 0) {
                        scope.primaryHealAmount = primaryAmount;
                        scope.seconaryHealAmount = seconaryHealAmount;
                   
                        if ((+scope.primaryHealAmount + +scope.seconaryHealAmount) !== totalHeal) {
                            scope.healError = true;
                        } else {
                            var selfHeal = 0;
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            if (scope.secondaryTargetIndex === 2) {
                                selfHeal = scope.seconaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false);
                        }
                    } else {
                        scope.primaryHealAmount = totalHeal;
                        scope.seconaryHealAmount = 0;
                        var selfHeal = 0;
                        
                        if (scope.targetIndex === 1) {
                            selfHeal = scope.primaryHealAmount;
                        }
                        scope.spellCastSucess(selfHeal > 0, selfHeal, false);
                    }
                } else if (scope.model.skillType === 'DAMAGE') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = scope.characterContext.stats.spellDmg + dmg;
                    var madEvoker = false;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        if (scope.model.name === 'Fireball' || scope.model.name === 'Lightning Storm' || scope.model.name === 'Burning Hands')
                            scope.damagePool = dmg;
                        else 
                            totalDamage += dmg;
                    }
                    
                    scope.damage = totalDamage;
                    scope.spellCastSucess(false, 0, madEvoker);
                } else if (scope.model.skillType === 'DAMAGE_RANGE_AC_15') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = scope.characterContext.stats.spellDmg + dmg;
                    var madEvoker = false;

                    scope.hitRollNatural = scope.roll()();
                    scope.hitRoll = scope.hitRollNatural + scope.characterContext.stats.rangeHit;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        totalDamage += dmg;
                    }
                    
                    scope.damage = totalDamage;
                    scope.spellCastSucess(false, 0, madEvoker);
                } else {
                    if (scope.targetIndex === 1) {
                        scope.spellCastSucess(true, 0, false);
                    } else {
                        scope.spellCastSucess(false, 0, false);
                    }
                    scope.closeModal();
                }
            }; 
            
            scope.spellCastSucess = function(selfTarget, healAmount, madEvoker) {
                scope.useAbility()(scope.model.id, selfTarget, healAmount, madEvoker);
                scope.spellCast = true;
            };
            
            scope.unuseSkill = function() {
                scope.unuseAbility()(scope.model.id);
                scope.closeModal();
            };  
            
            scope.closeModal = function() {
                scope.modalInstance.close();
                scope.targetIndex = 0;
                scope.madEvokerIndex = 0;
                scope.hitRoll = 0;
                scope.hitRollNatural = 0;
                scope.damage = 0;
                scope.damagePool = 0;
                scope.secondaryTargetIndex = 0;
                scope.skillCheckIndex = 0;
                scope.primaryHealAmount = 0;
                scope.seconaryHealAmount = 0;
                scope.spellCast = false;
                scope.healError = false;
            };
        },
        templateUrl:'common/skillUser/mobile/skillUserMobileTemplate-@{TDCC_VERSION}.html'
    };
}]);