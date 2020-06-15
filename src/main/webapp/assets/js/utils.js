function Modal(modalElement) {
    let self = this;
    this.data = null;
    let form = modalElement ? new Form(modalElement.querySelector('.content form')) : null;
    this.content = modalElement.querySelector('.content');

    this.show = function (data) {
        self.data = data;
        let event = new Event('show');
        modalElement.dispatchEvent(event);

        if(event.defaultPrevented)return;

        document.querySelector('.modalbg').classList.remove('hidden');
        modalElement.classList.remove('hidden');
    }

    this.close = function (result) {

        if(modalElement.classList.contains('form-modal') && form && result.toLowerCase() === 'ok')
        {
            if(!form.checkValidity())return;
            form.submit(response =>
            {
                let event = new CustomEvent('close', {
                    detail: {
                        modalResult: result,
                        formResponse: response
                    }
                });
                modalElement.dispatchEvent(event);
                if(event.defaultPrevented)return;
                form.reset();
                document.querySelector('.modalbg').classList.add('hidden');
                modalElement.classList.add('hidden');
            });
        }
        else
        {
            let event = new CustomEvent('close', {
                detail: {
                    modalResult: result
                }
            });
            modalElement.dispatchEvent(event);
            if(event.defaultPrevented)return;
            if(modalElement.classList.contains('form-modal') && form)
            {
                form.reset();
            }
            document.querySelector('.modalbg').classList.add('hidden');
            modalElement.classList.add('hidden');
        }
    }

    this.setFormValue = function (elementId, value) {
        if(form)
        {
            let element = form.formElement.querySelector(`#${elementId}`);
            if(element)element.value = value;
        }
    }

    this.setTitle = function (title) {
        let titleElement = modalElement.querySelector('header > .title');
        if(titleElement)titleElement.innerText = title;
    }

    this.text = function (text) {
        let content = modalElement.querySelector('.content');
        if(text && content)content.innerText = text;
        return content ? content.innerText : undefined;
    }

    this.remove = function () {
        modalElement.remove();
    }

    this.addEventListener = (event, callback) => modalElement.addEventListener(event, callback);

    if(modalElement)
    {
        let closeBtn = modalElement.querySelector('header .close-btn');
        if(closeBtn) closeBtn.addEventListener('click',  () => self.close('CANCEL'));
    }
    if(modalElement) Array.from(modalElement.querySelectorAll('footer > button')).forEach(btn => btn.addEventListener('click',  (e) => self.close(e.target.value)));
}

Modal.createErrorModal = function (title, error) {
    let modalElement = document.createElement('div');
    modalElement.classList.add('modal', 'hidden', 'error-modal');

    let modalHeader = document.createElement('header');
    modalHeader.setAttribute('data-color', 'red');

    let headerIcon = document.createElement('img');
    headerIcon.classList.add('icon');
    headerIcon.src = '/assets/images/error_outline-24px.svg';
    modalHeader.appendChild(headerIcon);

    let modalTitle = document.createElement('span');
    modalTitle.classList.add('title');
    modalTitle.innerText = title ? title : 'Errore';
    modalHeader.appendChild(modalTitle);

    modalElement.appendChild(modalHeader);

    let modalContent = document.createElement('div');
    modalContent.classList.add('content');
    modalContent.innerText = error ? error : 'Si è verificato un errore.'
    modalElement.appendChild(modalContent);

    let modalFooter = document.createElement('footer');

    let okayBtn = document.createElement('button');
    okayBtn.classList.add('okay-btn');
    okayBtn.value = 'OK';
    okayBtn.innerText = 'ok';
    modalFooter.appendChild(okayBtn);

    modalElement.appendChild(modalFooter);

    document.body.appendChild(modalElement);

    return new Modal(modalElement);
}


function Form(form)
{
    this.customValidity = null;
    let self = this;
    let validationBinds = [];
    let onlineValidations = [];

    if(!form)return null;
    this.formElement = form;

    Array.from(form.querySelectorAll('input[type=submit]')).forEach(submitButton =>
    {
        submitButton.addEventListener('click', function (e) {
            e.preventDefault();
            self.checkValidity().then(valid =>{
                if(valid)self.submit();
            })
        });
    })

    this.bindInputValidation = function (id1, id2, inverse) {
        validationBinds.push({
            id1: id1,
            id2: id2
        });
        if(inverse){
            validationBinds.push({
                id1: id2,
                id2: id1
            });
        }
    }

    this.addOnlineValidation = function (inputId, action, callback) {
        onlineValidations.push({
            id: inputId,
            action: action,
            callback: callback
        });
    }

    this.submit = function submitForm(callback)
    {
        let action = form.getAttribute('action');
        let method = form.getAttribute('method');

        let request = new XMLHttpRequest();
        request.open(method, action);

        request.onreadystatechange = function () {
            if(request.readyState === XMLHttpRequest.DONE)
            {
                if(request.status < 400)
                {
                    Array.from(form.querySelectorAll('.form-error-message')).forEach(elem => elem.classList.remove('show'));
                    if(callback)callback(request);
                    if(self.onresponse)self.onresponse(request);
                }
                else
                {
                    self.onerror(request);
                }
            }
        };

        if(method.toLowerCase() === 'get')request.send();
        else request.send(new FormData(form));
    }

    this.onresponse = null;

    this.onerror = function (request) {
        let errorMessage = form.querySelector(`.form-error-message[data-error-code="${request.status}"]`);
        if(errorMessage)errorMessage.classList.add('show');
    }

    this.reset = function () {
        form.reset();
        Array.from(form.querySelectorAll('.form-error-message')).forEach(elem => elem.classList.remove('show'));
    }

    this.checkValidity = async function checkFormValidity()
    {
        let valid = true;
        let inputs = Array.from(form.querySelectorAll('input, textarea, select'));
        for(let inputElement of inputs){
            if(!(await checkElementValidity(inputElement)))valid = false;
        }
        return valid;
    }

    function checkOnlineValidity(inputElement) {
        return new Promise((resolve, reject) =>{
            for(let onlineValidation of onlineValidations){
                if(onlineValidation.id === inputElement.id){
                    let request = new XMLHttpRequest();
                    request.open('GET', `${onlineValidation.action}?${inputElement.name}=${inputElement.value}`);
                    request.onreadystatechange = function () {
                        if(request.readyState === XMLHttpRequest.DONE){
                            if(request.status < 400){
                                let errorMessage = onlineValidation.callback(request.responseText);
                                if(!errorMessage)resolve(true);
                                else {
                                    inputElement.setCustomValidity(errorMessage);
                                    resolve(false);
                                }
                            }
                            else if(request.status >= 400 && request.status < 500){
                                console.error('Error checking online validation. Status code: '+request.status);
                                resolve(false);
                            }
                            else if(request.status >= 500){
                                console.error('Error checking online validation. Status code: '+request.status);
                                resolve(true);
                            }
                        }
                    }
                    request.send();
                    return;
                }
            }
            resolve(true);
        });
    }

    async function checkElementValidity(formElement) {
        formElement.setCustomValidity('');
        let valid = false;
        if(formElement.checkValidity() && (!self.customValidity || self.customValidity(formElement)))
        {
            let isValid = await checkOnlineValidity(formElement);

            if(isValid){
                let container = formElement.closest('.input-container');
                if(container)
                {
                    container.classList.remove('invalid');
                    container.removeAttribute('data-validity-message');
                }
                valid = true;
            }
            else{
                let container = formElement.closest('.input-container');
                if(container)
                {
                    container.classList.add('invalid');
                    container.setAttribute('data-validity-message', formElement.validationMessage);
                }
            }

        }
        else
        {
            let container = formElement.closest('.input-container');
            if(container)
            {
                container.classList.add('invalid');
                container.setAttribute('data-validity-message', formElement.validationMessage);
            }
        }

        for(let validationBind of validationBinds){
            if(validationBind.id1 === formElement.id){
                let element2 = form.querySelector(`#${validationBind.id2}`);
                if(element2) checkElementValidity(element2);
            }
        }

        return valid;
    }

    Array.from(form.querySelectorAll('input, textarea')).forEach(formElement =>
    {
        formElement.addEventListener('keyup', e => checkElementValidity(e.currentTarget));
        formElement.addEventListener('blur', e => checkElementValidity(e.currentTarget));
    });
}

function ContextMenu()
{
    let self = this;
    this.contextElement = null;
    this.contextData = null;

    let menu = document.createElement('ul');
    menu.classList.add('context-menu');
    document.body.appendChild(menu);

    window.addEventListener('click', function () {
        self.close();
    })

    this.addOption = function (option, callback) {
        let menuItem = document.createElement('li');
        menuItem.classList.add('context-menu-item');
        menuItem.innerText = option;
        menuItem.addEventListener('click', function (e) {
            callback(self.contextElement);
        })
        menu.appendChild(menuItem);
    }

    this.show = function (e, data) {
        this.contextElement = e.currentTarget;
        this.contextData = data;
        let mouseX = e.clientX;
        let mouseY = e.clientY;

        menu.style.top = `${mouseY - 4}px`;
        menu.style.left = `${mouseX + 4}px`;
        menu.style.display = 'block';
    }

    this.close = function () {
        this.contextElement = null;
        menu.style.display = 'none';
    }
}