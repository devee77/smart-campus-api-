<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Redirect the webapp root to the API discovery endpoint.
    response.sendRedirect(request.getContextPath() + "/api/v1/");
%>
