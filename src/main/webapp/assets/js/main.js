let currentDraggedType;
let currentDragParent;
let errorModal;
let newDirModal;

window.onload = function () {
    let folderContextMenu = new ContextMenu();

    folderContextMenu.addOption('Nuova cartella', function () {
        console.log("ciao");
    });

    window.addEventListener('click', function () {
        folderContextMenu.close();
    });

    let confirmDeleteModal = new Modal(document.getElementById('confirm-delete-modal'));
    errorModal = new Modal(document.getElementById('error-modal'));
    newDirModal = new Modal(document.getElementById('new-folder-dialog'));

    confirmDeleteModal.addEventListener('close', function (e) {
        if(e.detail.modalResult === 'OK') {
            let request = new XMLHttpRequest();
            request.open('POST', '/delete');
            request.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

            request.onreadystatechange = function () {
                if(request.readyState === XMLHttpRequest.DONE) {
                    let type = confirmDeleteModal.data.type;
                    if(request.status === 200) {
                        let id = confirmDeleteModal.data.id;
                        let parent = confirmDeleteModal.data.parent;
                        let deleted = document.getElementById(type === 'doc' ? `document-${id}` : `folder-${id}`);

                        let list = deleted.closest('.list');
                        deleted.remove();
                        if(parent > 0 && Array.from(list.querySelectorAll('.item:not(.empty-folder-item)')).length === 0)
                                list.querySelector('.empty-folder-item').classList.remove('hidden-item');
                    }
                    else{
                        let suffix = type === 'dir' ? 'della cartella' : 'del documento';
                        errorModal.text(`Si è verificato un errore durante l'eliminazione ${suffix}.`);
                        errorModal.show();
                    }
                }
            }
            request.send(`type=${confirmDeleteModal.data.type}&id=${confirmDeleteModal.data.id}`);
        }
    });

    newDirModal.addEventListener('close', function (e) {
        if(e.detail.modalResult === 'OK')
        {
            if(e.detail.formResponse.status === 200)
            {
                let dirJson = JSON.parse(e.detail.formResponse.responseText);
                let parentId = dirJson.parentId;
                let dirId = dirJson.id;
                let dirName = dirJson.name;

                let list =  parentId > 0 ? document.getElementById(`folder-${parentId}`).querySelector('.list') : document.getElementById('root-list');
                if(list.childElementCount === 1 && parentId > 0)list.querySelector('.empty-folder-item').classList.add('hidden-item');
                list.appendChild(createDirectory(dirId, parentId, dirName));
            }
        }
    });

    Array.from(document.querySelectorAll('.new-folder-button')).forEach(btn =>
        btn.addEventListener('click', e => newFolderButtonClickHandler(e)));

    Array.from(document.querySelectorAll('[data-folder]')).forEach(folder =>
    {
        addFolderDragStart(folder);

        folder.addEventListener('contextmenu', function (e) {
            e.preventDefault();
            folderContextMenu.show(e);
        })

        folder.addEventListener('dblclick', function (e) {
            e.target.closest('.item').querySelector('.open-folder-btn').click();
        })
    });

    Array.from(document.querySelectorAll('[data-sub-folder]')).forEach(folder =>
    {
        addFolderDragStart(folder);
        addFolderDropEvent(folder);

        folder.addEventListener('dblclick', function (e) {
            e.target.closest('.item').querySelector('.open-folder-btn').click();
        })
    });


    let trashcan = document.getElementById('trashcan');
    trashcan.addEventListener('dragover', function (e) {
        e.preventDefault();
        e.target.classList.add('red');
    });

    trashcan.addEventListener('dragleave', function (e) {
        e.target.classList.remove('red');
    });

    trashcan.addEventListener('dragenter', function (e) {
        e.preventDefault();
    });

    trashcan.addEventListener('drop', function (e) {
        e.target.classList.remove('red');
        let data = JSON.parse(e.dataTransfer.getData('text/plain'));
        confirmDeleteModal.show(data);
    });


    Array.from(document.getElementsByClassName('open-folder-btn')).forEach(button =>
        button.addEventListener('click', e => openFolderOnClickHandler(e)));

    Array.from(document.getElementsByClassName('close-folder-btn')).forEach(button =>
        button.addEventListener('click', e => closeFolderOnClickHandler(e)));
}

function openFolderOnClickHandler(e)
{
    let button = e.currentTarget;

    let folder = button.closest('.item');

    if(!folder.hasAttribute('data-id') || folder.getAttribute('data-doc-loaded') === 'true')
    {
        folder.querySelector('.list').classList.remove('hidden');
        button.style.display = "none";
        button.nextElementSibling.removeAttribute('style');
        folder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder_open-24px.svg')
    }
    else
    {
        button.style.display = "none";
        let loader = button.closest('.right-buttons').querySelector(".loader");
        loader.classList.remove('hidden');

        let folderId = folder.getAttribute('data-id');
        let documentList = folder.querySelector('.document-list');
        fetchDocuments(folderId, function (documents) {

            if(Object.keys(documents).length === 0) documentList.querySelector('.empty-folder-item').classList.remove('hidden-item');
            documents.forEach(documentJson =>
            {
                //CHECK IF THE DOCUMENT IS ALREADY PRESENT
                //THE DOCUMENT CAN BE ALREADY PRESENT IF IS MOVED FROM ANOTHER FOLDER BEFORE THE DOCUMENTS FETCH
                let docElement = documentList.querySelector(`#document-${documentJson.id}`);
                if(!docElement)documentList.appendChild(createDocumentElement(documentJson));
            });
            folder.setAttribute('data-doc-loaded', 'true');

            folder.querySelector('.list').classList.remove('hidden');
            loader.classList.add('hidden');
            button.nextElementSibling.removeAttribute('style');
            folder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder_open-24px.svg')

        }, function () {
            errorModal.text(`Si è verificato un errore durante il download dei documenti.`);
            errorModal.show();
        });
    }
}

function closeFolderOnClickHandler(e)
{
    let button = e.currentTarget;

    let folder = button.closest('.item');
    let list = folder.querySelector('.list');
    list.classList.add('hidden');
    Array.from(list.querySelectorAll('.list')).forEach(subList =>
    {
        let subFolder = subList.closest('.item');
        Array.from(subFolder.querySelectorAll('.close-folder-btn')).forEach(openBtn => openBtn.style.display = 'none');
        Array.from(subFolder.querySelectorAll('.open-folder-btn')).forEach(openBtn => openBtn.style.display = 'block');
        subList.classList.add('hidden');
        subFolder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder-24px.svg');
    });
    button.style.display = "none";
    button.previousElementSibling.removeAttribute('style');
    folder.querySelector('.item-content .item-icon').setAttribute('src', '/assets/images/folder-24px.svg');
}

function newFolderButtonClickHandler(e) {
    let button = e.currentTarget;
    let parentId;
    if(button.classList.contains('root'))parentId = 0;
    else parentId = button.closest('.item').getAttribute('data-folder-id');
    newDirModal.setFormValue('parent-id-input', parentId);
    newDirModal.show();
}

function createDirectory(id, parentId, name) {
    let item = document.createElement('li');
    item.classList.add('item');
    item.setAttribute('data-folder-id', id);
    item.setAttribute('data-parent-id', parentId);
    item.setAttribute('id', `folder-${id}`);

    if(parentId > 0)
    {
        item.setAttribute('data-id', id);
        item.setAttribute('data-doc-loaded', 'false');
    }

    let itemContent = document.createElement('div');
    itemContent.classList.add('item-content');
    if(parentId > 0)itemContent.classList.add('second-level');
    itemContent.setAttribute('draggable', 'true');
    itemContent.setAttribute(parentId > 0 ? 'data-sub-folder' : 'data-folder', '');

    let itemIcon = document.createElement('img');
    itemIcon.classList.add('item-icon');
    itemIcon.setAttribute('src', '/assets/images/folder-24px.svg');

    let itemText = document.createElement('span');
    itemText.classList.add('item-text');
    itemText.innerText = name;

    let rightButtons = document.createElement('div');
    rightButtons.classList.add('right-buttons');

    let openFolderButton = document.createElement('button');
    openFolderButton.classList.add('button', 'open-folder-btn');
    openFolderButton.innerText = 'apri';
    openFolderButton.addEventListener('click', e => openFolderOnClickHandler(e));

    let closeFolderButton = document.createElement('button');
    closeFolderButton.classList.add('button', 'close-folder-btn');
    closeFolderButton.innerText = 'chiudi';
    closeFolderButton.style.display = 'none';
    closeFolderButton.addEventListener('click', e => closeFolderOnClickHandler(e));

    let loader = document.createElement('img');
    loader.classList.add('loader', 'hidden');
    loader.setAttribute('src', '/assets/images/loader-24px.svg');

    let newButton = document.createElement('button');
    newButton.classList.add('icon-button');
    if(parentId === 0)newButton.classList.add('new-folder-button');
    newButton.setAttribute('title', parentId > 0 ? 'Carica documento' : 'Crea nuova cartella');

    if(parentId > 0)
    {
        //TODO
    }
    else
    {
        newButton.addEventListener('click', e => newFolderButtonClickHandler(e));
    }

    let newButtonIcon = document.createElement('img');
    newButtonIcon.classList.add('btn-icon');
    newButtonIcon.setAttribute('src', parentId > 0 ? '/assets/images/cloud_upload-24px.svg' : '/assets/images/create_new_folder-24px.svg');

    newButton.appendChild(newButtonIcon);
    rightButtons.appendChild(openFolderButton);
    rightButtons.appendChild(closeFolderButton);
    rightButtons.appendChild(loader);
    rightButtons.appendChild(newButton);

    itemContent.appendChild(itemIcon);
    itemContent.appendChild(itemText);
    itemContent.appendChild(rightButtons);

    let list = document.createElement('ul');
    list.classList.add('list', 'hidden');
    if(parentId > 0)list.classList.add('document-list');

    let emptyFolderItem = document.createElement('li');
    emptyFolderItem.classList.add('item', 'empty-folder-item');

    let emptyFolderItemContent = document.createElement('div');
    emptyFolderItemContent.classList.add('item-content', parentId > 0 ? 'third-level' : 'second-level');

    let emptyFolderText = document.createElement('a');
    emptyFolderText.classList.add('item-text');
    emptyFolderText.innerText = 'Cartella vuota';

    emptyFolderItemContent.appendChild(emptyFolderText);
    emptyFolderItem.appendChild(emptyFolderItemContent);
    list.appendChild(emptyFolderItem);

    item.appendChild(itemContent);
    item.appendChild(list);

    addFolderDragStart(itemContent);
    if(parentId > 0)addFolderDropEvent(itemContent);

    return item;
}

function addFolderDragStart(folder) {
    let folderItem = folder.closest(".item");
    let folderId = folderItem.getAttribute("data-folder-id");
    let parentId = folderItem.getAttribute("data-parent-id");

    folder.addEventListener('dragstart', function (e) {
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.dropEffect = 'move';
        currentDraggedType = 'dir';
        currentDragParent = parentId;

        e.dataTransfer.setData('text/plain', JSON.stringify({
            type: 'dir',
            id: folderId,
            parent: parentId
        }));
    });
}

function addFolderDropEvent(folder) {
    let folderItem = folder.closest(".item");
    let targetFolderId = folderItem.getAttribute('data-folder-id');

    folderItem.addEventListener('dragover', function (e) {
        e.preventDefault();
        if(currentDraggedType === 'doc' && currentDragParent !== targetFolderId)folder.classList.add('green-border');
        else folder.classList.add('red-border');
    });
    folderItem.addEventListener('dragenter', function (e) {
        e.preventDefault();
    });
    folderItem.addEventListener('dragleave', function (e) {
        folder.classList.remove('green-border', 'red-border');
    });
    folderItem.addEventListener('drop', function (e) {
        folder.classList.remove('green-border', 'red-border');
        let data = JSON.parse(e.dataTransfer.getData('text/plain'));
        if(data.type !== 'doc') return;

        if(targetFolderId === `${data.parent}`) return;
        let request = new XMLHttpRequest();
        request.open("GET", `/move?dir=${targetFolderId}&doc=${data.id}`);
        request.onreadystatechange = function () {
            if(request.readyState === XMLHttpRequest.DONE)
            {
                if(request.status === 200)
                {
                    let targetList = folderItem.querySelector('.list');
                    let documentElement = document.getElementById(`document-${data.id}`);
                    let sourceList = documentElement.closest('.list');
                    documentElement.setAttribute('data-parent-id', targetFolderId);
                    if(targetList.childElementCount === 1)
                        targetList.querySelector('.empty-folder-item').classList.add('hidden-item');
                    targetList.appendChild(documentElement);
                    if(sourceList.childElementCount === 1)
                        sourceList.querySelector('.empty-folder-item').classList.remove('hidden-item');
                }
                else{
                    errorModal.text(`Si è verificato un errore durante lo spostamento del documento.`);
                    errorModal.show();
                }
            }
        };
        request.send();
    });
}

function createDocumentElement(documentJson)
{
    let documentElement = document.createElement('li');
    documentElement.classList.add('item');
    documentElement.setAttribute('id', `document-${documentJson.id}`)
    documentElement.setAttribute('data-parent-id', documentJson.parentId);
    documentElement.setAttribute('draggable', 'true');

    let documentContent = document.createElement('div');
    documentContent.classList.add('item-content', 'third-level');

    let icon = document.createElement('img');
    icon.classList.add('item-icon');
    icon.setAttribute('src', '/assets/images/document-24px.svg')

    let itemText = document.createElement('a');
    itemText.classList.add('item-text');
    itemText.innerText = documentJson.name;

    documentContent.appendChild(icon);
    documentContent.appendChild(itemText);
    documentElement.appendChild(documentContent);

    let dragIcon = new Image();
    dragIcon.src = '/assets/images/document-24px.svg';

    documentElement.addEventListener('dragstart', function (e) {
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.dropEffect = 'move';
        let parent = documentElement.getAttribute('data-parent-id');

        currentDraggedType = 'doc';
        currentDragParent = parent;

        e.dataTransfer.setData('text/plain', JSON.stringify({
            type: 'doc',
            id: documentJson.id,
            parent: parent
        }))

        //e.dataTransfer.setDragImage(dragIcon, 0, 0);
    });

    return documentElement;
}

function fetchDocuments(folderId, callback, errorCallback) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if(this.readyState === XMLHttpRequest.DONE)
        {
            if(this.status < 400) callback(JSON.parse(this.responseText));
            else errorCallback();
        }
    };
    request.open('GET', `/documents?dir=${folderId}`);
    request.send();
}

function Modal(modalElement) {
    let self = this;
    this.data = null;
    let form = modalElement ? modalElement.querySelector('.content form') : null;

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
            submitForm(form, response =>
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
                Array.from(form.querySelectorAll('.form-error-message')).forEach(elem => elem.classList.remove('show'));
                document.querySelector('.modalbg').classList.add('hidden');
                modalElement.classList.add('hidden');
            }, errorResponse =>
            {

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
                Array.from(form.querySelectorAll('.form-error-message')).forEach(elem => elem.classList.remove('show'));
            }
            document.querySelector('.modalbg').classList.add('hidden');
            modalElement.classList.add('hidden');
        }
    }

    this.setFormValue = function (elementId, value) {
        if(form)
        {
            let element = form.querySelector(`#${elementId}`);
            if(element)element.value = value;
        }
    }

    this.text = function (text) {
        let content = modalElement.querySelector('.content');
        if(text && content)content.innerText = text;
        return content ? content.innerText : undefined;
    }

    this.addEventListener = (event, callback) => modalElement.addEventListener(event, callback);

    if(modalElement)
    {
        let closeBtn = modalElement.querySelector('header .close-btn');
        if(closeBtn) closeBtn.addEventListener('click',  () => self.close('CANCEL'));
    }
    if(modalElement) Array.from(modalElement.querySelectorAll('footer > button')).forEach(btn => btn.addEventListener('click',  (e) => self.close(e.target.value)));
}

function submitForm(form, callback, errorCallback)
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
                callback(request);
            }
            else
            {
                let errorMessage = form.querySelector(`.form-error-message[data-error-code="${request.status}"]`);
                if(errorMessage)errorMessage.classList.add('show');
                errorCallback(request);
            }
        }
    };

    if(method.toLowerCase() === 'get')request.send();
    else request.send(new FormData(form));
}

function ContextMenu()
{
    let menu = document.createElement('ul');
    menu.classList.add('context-menu');
    document.body.appendChild(menu);

    this.addOption = function (option, callback) {
        let menuItem = document.createElement('li');
        menuItem.classList.add('context-menu-item');
        menuItem.innerText = option;
        menuItem.addEventListener('onclick', function (e) {
            callback(e.target);
        })
        menu.appendChild(menuItem);
    }

    this.show = function (e) {
        let mouseX = e.clientX;
        let mouseY = e.clientY;

        menu.style.top = `${mouseY - 4}px`;
        menu.style.left = `${mouseX + 4}px`;
        menu.style.display = 'block';
    }

    this.close = function () {
        menu.style.display = 'none';
    }
}