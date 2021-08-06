angular.module('main').directive('skillUserMobile',['CharacterSvc', '$uibModal', function(characterSvc, $uibModal){
    return {
        restrict:'E',
        scope:{
            model:'=',
            characterContext:'=',
            elementId: '@',
            useAbility: '&?',
            unuseAbility: '&?',
            hasBuff: '&?',
            removeBuff: '&?',
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
            scope.healPool = 0;
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
                scope.healPool = 0;
                scope.secondaryTargetIndex = 0;
                scope.skillCheckIndex = 0;
                scope.primaryHealAmount = 0;
                scope.seconaryHealAmount = 0;
                scope.lohUseAmount = 1;
                scope.spellCast = false;
                scope.healError = false;
                scope.monster = scope.characterContext.monsters[0];
                scope.magePowers = false;
                scope.element = -1;
                scope.conserve = 0;
                scope.fork = 0;
                scope.intensify = 0;
                scope.sharpen = 0;
                
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
                    if (scope.characterContext.magePower || scope.characterContext.archMagePower)
                        scope.magePowers = true;
                    
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
            
            scope.setLoH10Index = function(index) {
                 scope.loh10Index = index;
            };
            
            scope.setLoH15Index = function(index) {
                 scope.loh15Index = index;
            };
            
            scope.useSkill = function(primaryAmount, seconaryHealAmount, lohAmount, markUse) {
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
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, "PLUS_10_LOH", markUse);
                        } else if (scope.loh15Index === 1) {
                            scope.primaryHealAmount += 15;
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, "PLUS_15_LOH", markUse);
                        } else {
                            if (scope.targetIndex === 1) {
                                selfHeal = scope.primaryHealAmount;
                            }
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, lohAmount, null, markUse);
                        }
                    } else {
                        var totalHeal = scope.characterContext.stats.spellHeal + ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);
                        
                        if (scope.hasBuff()("Spell Surge")) {
                            if(scope.skillCheckIndex === 0)
                                totalHeal += scope.model.maxEffect;
                            else
                               totalHeal += scope.model.minEffect; 
                        }

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
                                
                                var buff = scope.hasBuff()("Spell Surge");
                                if (buff !== null) {
                                    scope.removeBuff()(buff);
                                }
                                
                                scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                            }
                        } else {
                            scope.primaryHealAmount = (scope.model.aoe) ? ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : totalHeal;
                            scope.seconaryHealAmount = 0;
                            var selfHeal = 0;
                            scope.healPool = (scope.model.aoe) ? (scope.hasBuff()("Spell Surge")) ? scope.characterContext.stats.spellHeal + ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : scope.characterContext.stats.spellHeal : 0;

                            if (scope.targetIndex === 1 || scope.model.skillTarget === 'PARTY') {
                                selfHeal = scope.primaryHealAmount;
                            }
                            
                            var buff = scope.hasBuff()("Spell Surge");
                            if (buff !== null) {
                                scope.removeBuff()(buff);
                            }
                    
                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                        }
                    }
                } else if (scope.model.skillType === 'DAMAGE') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = (scope.model.aoe) ? dmg : scope.characterContext.stats.spellDmg + dmg;
                    var madEvoker = false;
                    scope.damagePool = (scope.model.aoe) ? scope.characterContext.stats.spellDmg : 0;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        if (scope.model.aoe)
                            scope.damagePool += dmg;
                        else 
                            totalDamage += dmg;
                    }
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    }
                    
                    scope.damage = totalDamage;
                    
                    scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }

                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
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
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    } else if (scope.sharpen === 1 && scope.hitRollNatural >= 18) {
                        totalDamage = totalDamage*2;
                    }
                    
                    scope.damage = totalDamage;
                    
                    scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if (scope.monster.monsterEffects.includes("INCORPOREAL")) {
                            if (!scope.hasBuff()("Ignore Incorporeal") && (Math.floor(Math.random() * 2) === 0)) {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            }
                        } else if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }
                    
                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
                } else {
                    if (scope.targetIndex === 1) {
                        scope.spellCastSucess(true, 0, false, 0, null, markUse);
                    } else {
                        scope.spellCastSucess(false, 0, false, 0, null, markUse);
                    }
                    scope.closeModal();
                }
            }; 
            
            scope.useSkillAsScroll = function(primaryAmount, seconaryHealAmount, markUse) {
                if (scope.model.skillType === 'HEAL') {
                    var totalHeal = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);
                    
                    if (scope.hasBuff()("Spell Surge")) {
                        if(scope.skillCheckIndex === 0)
                            totalHeal += scope.model.maxEffect;
                        else
                           totalHeal += scope.model.minEffect; 
                    }

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
                            
                            var buff = scope.hasBuff()("Spell Surge");
                            if (buff !== null) {
                                scope.removeBuff()(buff);
                            }

                            scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                        }
                    } else {
                        scope.primaryHealAmount = (scope.model.aoe) ? ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : totalHeal;
                        scope.seconaryHealAmount = 0;
                        var selfHeal = 0;
                        scope.healPool = (scope.model.aoe) ? (scope.hasBuff()("Spell Surge")) ?((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect) : scope.characterContext.stats.spellHeal : 0;

                        if (scope.targetIndex === 1 || scope.model.skillTarget === 'PARTY') {
                            selfHeal = scope.primaryHealAmount;
                        }

                        var buff = scope.hasBuff()("Spell Surge");
                        if (buff !== null) {
                            scope.removeBuff()(buff);
                        }

                        scope.spellCastSucess(selfHeal > 0, selfHeal, false, 0, null, markUse);
                    }
                } else if (scope.model.skillType === 'DAMAGE') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = dmg;
                    var madEvoker = false;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        if (scope.model.name === 'Fireball' || scope.model.name === 'Lightning Storm' || scope.model.name === 'Burning Hands' || scope.model.name === 'Prismatic Spray' || scope.model.name === 'Stone Storm')
                            scope.damagePool = dmg;
                        else 
                            totalDamage += dmg;
                    }
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    }
                    
                    scope.damage = totalDamage;
                    
                    scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }
                    
                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
                } else if (scope.model.skillType === 'DAMAGE_RANGE_AC_15') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = dmg;
                    var madEvoker = false;

                    scope.hitRollNatural = scope.roll()();
                    scope.hitRoll = scope.hitRollNatural + scope.characterContext.stats.rangeHit;
                    
                    if (scope.madEvokerIndex === 1) {
                        madEvoker = true;
                        totalDamage += dmg;
                    }
                    
                    var buff = scope.hasBuff()("Spell Surge");
                    if (buff !== null) {
                        totalDamage += dmg;
                        scope.removeBuff()(buff);
                    } else if (scope.sharpen === 1 && scope.hitRollNatural >= 18) {
                        totalDamage = totalDamage*2;
                    }
                    
                    scope.damage = totalDamage;
                    
                    scope.applyDr();
                    
                    if(scope.monster.monsterEffects) {
                        if (scope.monster.monsterEffects.includes("INCORPOREAL")) {
                            if (!scope.hasBuff()("Ignore Incorporeal") && (Math.floor(Math.random() * 2) === 0)) {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            }
                        } else if(scope.model.skillLevel === 'ZERO' && scope.monster.monsterEffects.includes("PHASING_NORMAL")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE')  && scope.monster.monsterEffects.includes("PHASING_HARDCORE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if((scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO')  && scope.monster.monsterEffects.includes("PHASING_NIGHTMARE")) {
                            scope.damage = 0;
                            scope.damagePool = 0;
                        } else if(scope.monster.monsterEffects.includes("PHASING_EPIC")) {
                            if (scope.model.skillLevel === 'ZERO' || scope.model.skillLevel === 'ONE' || scope.model.skillLevel === 'TWO') {
                                scope.damage = 0;
                                scope.damagePool = 0;
                            } else {
                                scope.damage =  Math.round(scope.damage * .5);
                                scope.damagePool = Math.round(scope.damagePool * .5);
                            }
                        }
                    }
                    
                    scope.spellCastSucess(false, 0, madEvoker, 0, null, markUse);
                } else {
                    if (scope.targetIndex === 1) {
                        scope.spellCastSucess(true, 0, false, 0, null, markUse);
                    } else {
                        scope.spellCastSucess(false, 0, false, 0, null, markUse);
                    }
                    scope.closeModal();
                }
            }; 
            
            scope.applyDr = function() {
                var damageDelta = 0;
                
                if (scope.monster.rangeDr !== 0) {
                    damageDelta += scope.monster.rangeDr;
                } if (scope.monster.universalDr !== 0) {
                    damageDelta += scope.monster.universalDr;
                } 
                
                if (scope.element !== -1) {
                    if (scope.monster.fire !== 0 && scope.element === 1) {
                        damageDelta += scope.monster.fire;
                    } if (scope.monster.cold !== 0 && scope.element === 0) {
                        damageDelta += scope.monster.cold;
                    } if (scope.monster.shock !== 0 && scope.element === 2) {
                        damageDelta += scope.monster.shock;
                    }
                } else {
                    if (scope.monster.fire !== 0 && scope.model.fire) {
                        damageDelta += scope.monster.fire;
                    } if (scope.monster.cold !== 0 && scope.model.cold) {
                        damageDelta += scope.monster.cold;
                    } if (scope.monster.shock !== 0 && scope.model.shock) {
                        damageDelta += scope.monster.shock;
                    } if (scope.monster.sonic !== 0 && scope.model.sonic) {
                        damageDelta += scope.monster.sonic;
                    } if (scope.monster.poison !== 0 && scope.model.poison) {
                        damageDelta += scope.monster.poison;
                    } if (scope.monster.sacred !== 0 && scope.model.sacred) {
                        damageDelta += scope.monster.sacred;
                    } if (scope.monster.darkrift !== 0 && scope.model.darkrift) {
                        damageDelta += scope.monster.darkrift;
                    } if (scope.monster.acid !== 0 && scope.model.acid) {
                        damageDelta += scope.monster.acid;
                    }
                }
                
                if (damageDelta !== 0) {
                    if (scope.damagePool === 0) {
                        if (damageDelta < 0) {
                            if (damageDelta*-1 > scope.damage)
                                scope.damage += scope.damage;
                            else
                                scope.damage += damageDelta*-1;
                        } else 
                            scope.damage -= damageDelta; 
                    } else if (scope.damagePool > 0) {
                        if (damageDelta < 0) {
                            if (damageDelta*-1 > scope.damagePool) {
                                var poolDelta = damageDelta*-1 - scope.damagePool;
                                scope.damagePool += scope.damagePool;
                                if (poolDelta > scope.damage)
                                    scope.damagePool += scope.damage;
                                else
                                    scope.damagePool += poolDelta;
                            } else
                                scope.damagePool += damageDelta*-1;
                        } else
                            scope.damagePool -= damageDelta; 
                    }
                    if (scope.damagePool < 0) {
                        scope.damage -= scope.damagePool; 
                        scope.damagePool = 0;
                    }
                    if (scope.damage < 0) {
                        scope.damage = 0;
                    }
                }
            };
            
            scope.markSkill = function() {
                scope.useAbility()(scope.model.id, false, 0, false, 0, null, true, true);
                scope.closeModal();
            }; 
            
            scope.spellCastSucess = function(selfTarget, healAmount, madEvoker, lohNumber, inGameEffect, markUse) {
                if (scope.conserve === 1)
                    markUse = false;
                scope.useAbility()(scope.model.id, selfTarget, healAmount, madEvoker, lohNumber, inGameEffect, markUse, false);
                scope.spellCast = true;
            };
            
            scope.unuseSkill = function() {
                scope.unuseAbility()(scope.model.id);
                scope.closeModal();
            };  
            
            scope.setElement = function(index) {
                if (scope.element === index)
                    scope.element = -1;
                else
                    scope.element = index;
            };  
            
            scope.setConserve = function(index) {
                scope.conserve = index;
            }; 
            
            scope.setFork = function(index) {
                scope.fork = index;
            };  
            
            scope.setIntensify = function(index) {
                scope.intensify = index;
            };
            
            scope.setSharpen = function(index) {
                scope.sharpen = index;
            };
            
            scope.continueCast = function() {
                scope.magePowers = false;
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
                scope.magePowers = false;
                scope.element = -1;
                scope.conserve = 0;
                scope.fork = 0;
                scope.intensify = 0;
                scope.sharpen = 0;
            };
        },
        templateUrl:'common/skillUser/mobile/skillUserMobileTemplate-@{TDCC_VERSION}.html'
    };
}]);