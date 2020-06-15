(function () {

    window.onload = function () {

        let errorModal = Modal.createErrorModal();

        let formElement = document.getElementById('register-form');
        let form = new Form(formElement);

        let inputPassword = document.getElementById('input-password');

        form.bindInputValidation('input-password', 'input-password-confirm');
        form.addOnlineValidation('input-username', '/check-username', function (responseText) {
            if(responseText === 'unavailable')return 'Nome utente già in uso.';
            if(responseText === 'invalid') return 'Nome utente non valido.';
            return null;
        });

        form.addOnlineValidation('input-email', '/check-email', function (responseText) {
            if(responseText === 'unavailable')return 'Email non disponibile.';
            if(responseText === 'invalid') return 'Email non valida.';
            return null;
        });

        form.onresponse = function (response){
            window.location.href = '/';
        }

        form.onerror = function(response){
            errorModal.show();
        }


        form.customValidity = function (inputElement) {
            if(inputElement.id === 'input-password-confirm'){
                if(inputElement.value !== inputPassword.value){
                    inputElement.setCustomValidity('La password non coincide.');
                    return false;
                }
            }
            return true;
        }
    }

})();