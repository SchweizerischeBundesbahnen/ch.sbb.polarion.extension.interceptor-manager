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
    <script type="module" src="../js/modules/settings.js?bundle=<%= bundleTimestamp %>"></script>
</head>

<body>

<div class="standard-admin-page">
    <h1 id="hooks-settings-header">Interceptor Manager: Hooks settings</h1>

    <div id="hooks-reload-container">
        <button id="hooks-reload-button" type="submit" title="Reload jar files from the hooks containing folder">Reload hooks list</button>
    </div>

    <div id="hooks-choose-container">
    </div>

    <div class="input-container">
        <div class="input-block wide">

            <jsp:include page='/common/jsp/notifications.jsp' />

            <div id="no-hooks-registered" class="alert alert-warning" style="display: none">
                <img src="/polarion/ria/images/icon-indicatorWarning16.png?bundle=<%= bundleTimestamp %>" class="gwt-Image">
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

</body>
</html>
