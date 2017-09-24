<%--
  Created by IntelliJ IDEA.
  User: avezhnovets
  Date: 23.09.2017
  Time: 17:48
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% String id = String.valueOf(request.getAttribute("trxId"));%>
<% String errorString = (String) request.getAttribute("errorString");%>
<html>
<head>
    <title>Trx</title>
    <link href="../resources/style.css" rel="STYLESHEET" type="text/css">
    <script type="text/javascript" src="../resources/trxForm.js"></script>
</head>
<body>
<div class="wrapper">
    <div class="title">Transaction</div>
    <div class="globalText" style="padding: 24px 24px 0; color: red"><%= errorString%></div>
    <div class="content">
        <form action="${operation}" method="post">
        <ul>
            <li>
                <input class="block" id="trxData" type="text" name="data" placeholder="Transaction data" value="${trxData}"/>
            </li>
            <li>
                <input class="block" id="timestamp" type="text" name="timestamp" placeholder="date & time (auto generate)" value="${timestamp}"/>
            </li>
        </ul>
    </div>
    <div class="actions">
        <input type="hidden" name="id" value="<%= id%>">
        <button class="button blue">save</button>
        </form>
        <form action="/trx" method="get">
        <button class="button">cancel</button>
        </form>
    </div>
</div>
</body>
</html>
<%! private String getAction(String id) {
    if (id == null) {
        return "addTrx";
    }
    return "editTrx";
}%>
