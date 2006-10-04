<html>
<head>
    <title>JA-SIG CAS client test - <%=request.getHeader("host") %>
    </title>
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Expires" CONTENT="-1">
</head>


<body>
<hr/>
<%
    //CAS server root
    //String casServerUrl= "https://localhost/cas/";
    String casServerUrl = "https://idp.example.be/cas/";

    //thread-safe httpclient
    org.apache.commons.httpclient.HttpClient casHttpClient = new org.apache.commons.httpclient.HttpClient();

    //name of cas assertion in session
    String casAssName = "_cas_assertion_";

    Object assObject = session.getAttribute("_cas_assertion_");
    if (assObject != null) {
        org.jasig.cas.client.validation.Assertion assertion = (org.jasig.cas.client.validation.Assertion) assObject;
        String principalId = assertion.getPrincipal().getId();
        String pgtId = assertion.getProxyGrantingTicketId();
        out.println("principalID: " + principalId + "<br />");
        out.println("PGT-ID: " + pgtId + "<br />");

        out.println("--<br />");
        org.jasig.cas.client.proxy.Cas20ProxyRetriever ptRet = new org.jasig.cas.client.proxy.Cas20ProxyRetriever(casServerUrl, casHttpClient);

        org.jasig.cas.authentication.principal.SimpleService targetService = new org.jasig.cas.authentication.principal.SimpleService("https://testService");
        String pt = ptRet.getProxyTicketIdFor(pgtId, targetService);
        out.println("retrieved PT: " + pt + "<br />");

    } else {
        out.println("no CAS assertion with name \"" + casAssName + "\" found in session");
    }


%>
<hr/>

<%
    out.print("request.getRemoteUser: " + request.getRemoteUser() + "<br/>");
    out.print("REMOTE_USER: " + request.getHeader("REMOTE_USER") + "<br/>");
    out.print("HTTP_REMOTE_USER: " + request.getHeader("HTTP_REMOTE_USER") + "<br/>");
%>


<hr/>


<u>REQUEST PARAMETERS (GET/POST)</u><br/>
<table>
    <%
        java.util.Enumeration eParameters = request.getParameterNames();
        while (eParameters.hasMoreElements()) {
            String name = (String) eParameters.nextElement();
            Object object = request.getParameter(name);
            String value = object.toString();
            out.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
        }
    %>
</table>


<hr/>

<u>ALL HEADERS</u><br/>
<table>
    <%
        java.util.Enumeration eHeaders = request.getHeaderNames();
        while (eHeaders.hasMoreElements()) {
            String name = (String) eHeaders.nextElement();
            Object object = request.getHeader(name);
            String value = object.toString();
            out.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
        }
    %>
</table>


<hr/>


<u>SESSION</u><br/>
<table>
    <%="SESSION_ID: " + session.getId() + "<br/>"%>

    <%
        java.util.Enumeration eSession = session.getAttributeNames();
        while (eSession.hasMoreElements()) {
            String name = (String) eSession.nextElement();
            Object object = session.getAttribute(name);
            String value = object.toString();
            out.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
        }
    %>
</table>


<hr/>


</body>
</html>
