angular.module('main').factory('ErrorDialogSvc',['$uibModal', function($uibModal) {    
    var errorDialogSvc={};

    errorDialogSvc.showError = function(response) {
        if(response.data.message)
            var obj = response.data;
        else {
            var decodedString = String.fromCharCode.apply(null, new Uint8Array(response.data));
            var obj = JSON.parse(decodedString);
        }
        
        $uibModal.open({
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            controller: 'ModalInstanceCtrl',
            controllerAs: '$ctrl',
            templateUrl: 'common/error/errorModalTemplate-@{TDCC_VERSION}.html',
            resolve: {
              text: function () {
                return obj.message;
              }
            }
        });
    };

    return errorDialogSvc;
}])
.controller('ModalInstanceCtrl',function ($uibModalStack, text) {
    var $ctrl = this;
    $ctrl.text = text;
    
    $ctrl.close = function () {
        $uibModalStack.dismissAll();
    };
});