<%@ page import="ch.sbb.polarion.extension.interceptor.rest.model.Version" %>
<%@ page import="ch.sbb.polarion.extension.interceptor.util.ExtensionInfo" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<%! Version version = ExtensionInfo.getInstance().getVersion();%>

<head>
    <title></title>
    <link rel="stylesheet" href="../css/common.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
    <link rel="stylesheet" href="../css/about.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
</head>

<body>
<div class="standard-admin-page about-page">
    <h1>About</h1>

    <div class="about-page-text">
        <h3>Version</h3>

        <table>
            <tbody>
            <tr>
                <td>Bundle-Name</td>
                <td id="version-bundle-name">
                    <%= version.getBundleName() %>
                </td>
            </tr>
            <tr>
                <td>Bundle-Vendor</td>
                <td id="version-bundle-vendor">
                    <%= version.getBundleVendor() %>
                </td>
            </tr>
            <tr>
                <td>Automatic-Module-Name</td>
                <td id="version-automatic-module-name">
                    <%= version.getAutomaticModuleName() %>
                </td>
            </tr>
            <tr>
                <td>Bundle-Version</td>
                <td id="version-bundle-version">
                    <%= version.getBundleVersion() %>
                </td>
            </tr>
            <tr>
                <td>Bundle-Build-Timestamp</td>
                <td id="version-bundle-build-timestamp">
                    <%= version.getBundleBuildTimestamp() %>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>