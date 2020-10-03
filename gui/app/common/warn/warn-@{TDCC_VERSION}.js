angular.module('main').factory('WarnDialogSvc',['$uibModal', function($uibModal) {    
    var warnDialogSvc={};

    warnDialogSvc.showMessage = function(title, text) {
        $uibModal.open({
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            controller: 'WarnModalInstanceCtrl',
            controllerAs: '$ctrl',
            templateUrl: 'common/warn/warnModalTemplate-@{TDCC_VERSION}.html',
            resolve: {
              title: function () {
                return title;
              },  
              text: function () {
                return text;
              }
            }
        });
    };

    return warnDialogSvc;
}])
.controller('WarnModalInstanceCtrl',function ($uibModalStack, title, text) {
    var $ctrl = this;
    $ctrl.title = title;
    $ctrl.text = text;
    
    $ctrl.close = function () {
        $uibModalStack.dismissAll();
    };
});