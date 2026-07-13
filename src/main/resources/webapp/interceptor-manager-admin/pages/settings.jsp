<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<%! String bundleTimestamp = ch.sbb.polarion.extension.generic.util.VersionUtils.getVersion().getBundleBuildTimestampDigitsOnly(); %>

<head>
    <title>Interceptor Manager: Hooks settings</title>
    <link rel="stylesheet" href="../ui/generic/css/prism.css?bundle=<%= bundleTimestamp %>">
    <script type="text/javascript" src="../ui/generic/js/prism.js?bundle=<%= bundleTimestamp %>"></script>
    <link rel="stylesheet" href="../ui/generic/css/code-input.min.css?bundle=<%= bundleTimestamp %>">
    <script type="text/javascript" src="../ui/generic/js/code-input.min.js?bundle=<%= bundleTimestamp %>"></script>
    <link rel="stylesheet" href="../ui/generic/css/common.css?bundle=<%= bundleTimestamp %>">
    <link rel="stylesheet" href="../css/interceptor-manager-admin.css?bundle=<%= bundleTimestamp %>">
    <link rel="stylesheet" href="../ui/generic/css/micromodal.css?bundle=<%= bundleTimestamp %>">
    <script type="text/javascript" src="../ui/generic/js/micromodal.min.js?bundle=<%= bundleTimestamp %>"></script>
    <script type="module" src="../js/modules/settings.js?bundle=<%= bundleTimestamp %>"></script>
</head>

<body>

<div class="standard-admin-page">
    <h1 id="hooks-settings-header">Interceptor Manager: Hooks settings</h1>

    <div id="hooks-reload-container">
        <button id="hooks-reload-button" class="sbb-btn--link" type="submit" title="Reload jar files from the hooks containing folder">Reload hooks list</button>
    </div>

    <div id="hooks-choose-container">
    </div>

    <div class="input-container">
        <div class="input-block wide">

            <jsp:include page='/common/jsp/notifications.jsp' />

            <div id="no-hooks-registered" class="alert alert-warning" style="display: none">
                <span>No hooks found. Please refer documentation.</span>
            </div>

            <div id="hook-settings-container">
                <div id="hook-description-container"></div>

                <div id="enable-hook-container">
                    <input type="checkbox" id="enable-hook">
                    <label id="enable-hook-label" for="enable-hook">Enable</label>
                </div>

                <div class="label-block" style="margin-top: 20px"><label>Hook properties</label></div>
                <code-input class="html-input vertical-resizable" id="properties-input" lang="properties" placeholder=""></code-input>
            </div>
        </div>
    </div>

    <input id="scope" type="hidden" value="<%= request.getParameter("scope")%>"/>
</div>

<jsp:include page='/common/jsp/buttons.jsp'/>

<div class="modal micromodal-slide" id="info-modal" aria-hidden="true">
    <div class="modal__overlay" tabindex="-1" data-micromodal-close>
        <div class="modal__container standard-dialog" role="dialog" aria-modal="true" aria-labelledby="info-modal-title">
            <header class="modal__header">
                <h2 class="modal__title" id="info-modal-title">Interceptor Manager</h2>
                <button class="modal__close" aria-label="Close" data-micromodal-close></button>
            </header>
            <main class="modal__content" id="info-modal-content"></main>
            <footer class="modal__footer">
                <button class="modal__btn modal__btn-primary" data-micromodal-close>OK</button>
            </footer>
        </div>
    </div>
</div>

</body>
</html>
