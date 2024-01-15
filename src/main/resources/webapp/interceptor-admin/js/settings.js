const Hooks = {
    list: [],
    selectedHook: undefined
};
InterceptorContext.init({
    initCodeInput: true,
    propertiesHighlighting: true
});

function saveSettings() {
    InterceptorContext.hideActionAlerts();

    InterceptorContext.callAsync({
        method: 'POST',
        url: `/polarion/interceptor/rest/internal/settings/${Hooks.selectedHook.name}`,
        contentType: 'application/json',
        body: JSON.stringify({
            'enabled': InterceptorContext.getCheckboxValueById('enable-hook'),
            'properties': InterceptorContext.getValueById('properties-input')
        }),
        onOk: () => {
            InterceptorContext.showSaveSuccessAlert();
            InterceptorContext.setNewerVersionNotificationVisible(false);
            readAndFillRevisions();
        },
        onError: () => InterceptorContext.showSaveErrorAlert()
    });
}

function getItemTypeName(itemType) {
    switch (itemType) {
        case 'WORKITEM': return 'Work Item';
        case 'TESTRUN': return 'Test Run';
        case 'MODULE': return 'Module';
        case 'PLAN': return 'Plan';
    }
    return itemType;
}

function getInterceptorTypeName(hookType) {
    switch (hookType) {
        case 'SAVE': return 'pre-save';
        case 'DELETE': return 'pre-delete';
    }
    return hookType;
}

function readSelectedHook() {
    InterceptorContext.setLoadingErrorNotificationVisible(false);

    InterceptorContext.callAsync({
        method: 'GET',
        url: `/polarion/interceptor/rest/internal/settings/${Hooks.selectedHook.name}`,
        contentType: 'application/json',
        onOk: (responseText) => {
            document.getElementById('hook-description-container').innerHTML =
                "Affected item type: <b>" + getItemTypeName(Hooks.selectedHook.itemType) + "</b><br>" +
                "Interceptor action type: <b>" + getInterceptorTypeName(Hooks.selectedHook.actionType) + "</b><br>" +
                Hooks.selectedHook.description;
            parseAndSetSettings(responseText, true);
            readAndFillRevisions();
        },
        onError: () => InterceptorContext.setLoadingErrorNotificationVisible(true)
    });
}

function readHooksList(reload) {
    InterceptorContext.setLoadingErrorNotificationVisible(false);

    InterceptorContext.callAsync({
        method: 'GET',
        url: `/polarion/interceptor/rest/internal/hooks?reload=${reload}`,
        contentType: 'application/json',
        onOk: (responseText) => {
            const hooks = JSON.parse(responseText);
            Hooks.list = hooks;

            const container = document.getElementById('hooks-choose-container');

            let noHooks = Hooks.list.length === 0;
            InterceptorContext.setNoHooksNotificationVisible(noHooks);
            let displayStyle = noHooks ? 'none' : 'block';
            container.style.display = displayStyle;
            document.getElementById('hook-settings-container').style.display = displayStyle;
            document.getElementById('actions-container').style.display = displayStyle;
            if (noHooks) {
                return;
            }

            container.replaceChildren(); //remove div content

            for (const interceptor of hooks) {
                let div = document.createElement("div");
                let label = document.createElement("label");
                label.innerHTML = interceptor.name;

                let radio = document.createElement("input");
                radio.type = "radio";
                radio.name = "hook-name";
                radio.id = interceptor.name;
                radio.addEventListener('change', () => {
                    Hooks.selectedHook = interceptor;
                    readSelectedHook();
                });

                label.appendChild(radio);
                div.appendChild(label);
                container.appendChild(div);
            }

            if (Hooks.selectedHook === undefined || reload) { //currently selected hook may disappear after reload
                Hooks.selectedHook = hooks[0];
            }
            if (reload) {
                alert("Hooks list reloaded successfully. Total hooks: " + hooks.length);
            }
            document.getElementById(Hooks.selectedHook.name).checked = true;
            readSelectedHook();
        },
        onError: () => InterceptorContext.setLoadingErrorNotificationVisible(true)
    });
}

function cancelEdit() {
    if (confirm("Are you sure you want to cancel editing and revert all changes made?")) {
        readSelectedHook();
    }
}

function revertToDefault() {
    if (confirm("Are you sure you want to return the default values?")) {
        InterceptorContext.setLoadingErrorNotificationVisible(false);
        InterceptorContext.hideActionAlerts();

        InterceptorContext.callAsync({
            method: 'GET',
            url: `/polarion/interceptor/rest/internal/settings/${Hooks.selectedHook.name}/default`,
            contentType: 'application/json',
            onOk: (responseText) => {
                parseAndSetSettings(responseText);
                InterceptorContext.showRevertedToDefaultAlert();
            },
            onError: () => InterceptorContext.setLoadingErrorNotificationVisible(true)
        });
    }
}

function parseAndSetSettings(text, checkNewerVersion) {
    const settings = JSON.parse(text);
    InterceptorContext.setCheckboxValueById('enable-hook', settings.enabled);
    InterceptorContext.setValueById('properties-input', settings.properties);
    if (checkNewerVersion && settings.hookVersion !== Hooks.selectedHook.version) {
        InterceptorContext.setNewerVersionNotificationVisible(true);
    }
}

function readAndFillRevisions() {
    InterceptorContext.readAndFillRevisions({
        setting: Hooks.selectedHook.name,
        revertToRevisionCallback: (responseText) => parseAndSetSettings(responseText)
    });
}

readHooksList(false);
