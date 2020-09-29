angular.module('main').directive('skillUserDesktop',['VtdSvc', '$uibModal', function(vtdSvc, $uibModal){
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
            scope.loh10Index = 0;
            scope.loh15Index = 0;
            scope.secondaryTargetIndex = 0;
            scope.skillCheckIndex = 0;
            scope.hitRoll = 0;
            scope.hitRollNatural = 0;
            scope.damage = 0;
            scope.damagePool = 0;
            scope.primaryHealAmount = 0;
            scope.seconaryHealAmount = 0;
            scope.lohUseAmount = 1;
            scope.spellCast = false;
            scope.healError = false;
            scope.modalInstance = null;
            
            scope.openModal = function(isChecked) {
                scope.targetIndex = 0;
                scope.madEvokerIndex = 0;
                scope.loh10Index = 0;
                scope.loh15Index = 0;
                scope.hitRoll = 0;
                scope.hitRollNatural = 0;
                scope.damage = 0;
                scope.damagePool = 0;
                scope.secondaryTargetIndex = 0;
                scope.skillCheckIndex = 0;
                scope.primaryHealAmount = 0;
                scope.seconaryHealAmount = 0;
                scope.lohUseAmount = 1;
                scope.spellCast = false;
                scope.healError = false;
                
                if (isChecked) {
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'desktop-vtd-dialog',
                        openedClass: 'desktop-modal-content',
                        templateUrl: 'common/skillUser/desktop/skillUserUnsetDesktopModalTemplate-@{TDCC_VERSION}.html'
                    });
                } else {
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'desktop-vtd-dialog',
                        openedClass: 'desktop-modal-content',
                        templateUrl: 'common/skillUser/desktop/skillUserDesktopModalTemplate-@{TDCC_VERSION}.html'
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
            
            scope.setLoH10Index = function(index) {
                 scope.loh10Index = index;
            };
            
            scope.setLoH15Index = function(index) {
                 scope.loh15Index = index;
            };
            
            scope.useSkill = function(primaryAmount, seconaryHealAmount, lohAmount) {
                if (scope.model.skillType === 'HEAL') {
                    if (scope.model.name === 'Lay on Hands') {
                        if (lohAmount < 1)
                            lohAmount = 1;
                        else if (lohAmount > (scope.model.usableNumber - scope.model.usedNumber))
                            lohAmount = (scope.model.usableNumber - scope.model.usedNumber);
                        
                        scope.primaryHealAmount = lohAmount;
                        var selfHeal = 0;
                       
                        if (scope.loh10Index === 1) {
                            scope.primaryHealAmount += 10;
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, "PLUS_10_LOH");
                        } else if (scope.loh15Index === 1) {
                            scope.primaryHealAmount += 15;
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, "PLUS_15_LOH");
                        } else {
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, null);
                        }
                    } else {
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
                                scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null);
                            }
                        } else {
                            scope.primaryHealAmount = totalHeal;
                            scope.seconaryHealAmount = 0;
                            var selfHeal = 0;

                            if (scope.targetIndex === 1 || scope.model.skillTarget === 'PARTY') {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null);
                        }
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
                    scope.spellCastSucess(false, 0, madEvoker, 0, null);
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
                    scope.spellCastSucess(false, 0, madEvoker, 0, null);
                } else {
                    if (scope.targetIndex === 1) {
                        scope.spellCastSucess(true, 0, false, 0, null);
                    } else {
                        scope.spellCastSucess(false, 0, false, 0, null);
                    }
                    scope.closeModal();
                }
            }; 
            
            scope.spellCastSucess = function(selfTarget, healAmount, madEvoker, lohNumber, inGameEffect) {
                scope.useAbility()(scope.model.id, selfTarget, healAmount, madEvoker, lohNumber, inGameEffect);
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
                scope.loh10Index = 0;
                scope.loh15Index = 0;
                scope.hitRoll = 0;
                scope.hitRollNatural = 0;
                scope.damage = 0;
                scope.damagePool = 0;
                scope.secondaryTargetIndex = 0;
                scope.skillCheckIndex = 0;
                scope.primaryHealAmount = 0;
                scope.seconaryHealAmount = 0;
                scope.lohUseAmount = 1;
                scope.spellCast = false;
                scope.healError = false;
            };
        },
        templateUrl:'common/skillUser/desktop/skillUserDesktopTemplate-@{TDCC_VERSION}.html'
    };
}]);