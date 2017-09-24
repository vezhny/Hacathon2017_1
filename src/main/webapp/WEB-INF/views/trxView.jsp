<%@ page import="java.util.List" %>
<%@ page import="server.persistence.entity.Trx" %>
<%@ page import="server.util.Tools" %><%--
  Created by IntelliJ IDEA.
  User: avezhnovets
  Date: 23.09.2017
  Time: 15:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% List<Trx> trxList = (List<Trx>) request.getAttribute("trxList"); %>
<html>
<head>
    <title>Trx View</title>
    <link href="../resources/style.css" rel="STYLESHEET" type="text/css">
</head>
<body>
<h2 class="globalText" align="center">Transactions</h2>
    <table class="trxTable" align="center" cellspacing="5">
        <tbody class="trxTBody">
        <tr class="trxLine">
            <th class="trxHeader" align="center" style="background-color: #848484">Version</th>
            <th class="trxHeader" align="center" style="background-color: #848484">Data</th>
            <th class="trxHeader" align="center" style="background-color: #848484">Date & Time</th>
            <th class="trxHeader" align="center" style="background-color: #848484"></th>
            <th class="trxHeader" align="center" style="background-color: #848484"></th>
        </tr>
        <% String lineColor = ""; %>
        <% for (Trx trx : trxList) {%>
        <% lineColor = getLineColor(lineColor); %>
            <tr class="trxLine">
                <td class="trxCell" align="center" style="background-color: <%= lineColor %>; width: 50px"><%= trx.getVersion()%></td>
                <td class="trxCell" align="center" style="width: 600px; background-color: <%= lineColor %>;"><%= trx.getData()%></td>
                <td class="trxCell" align="center" style="background-color: <%= lineColor %>; width: 150px"><%= Tools.getDateByPattern(trx.getTimestamp(), "dd MM yyyy HH:mm:ss")%></td>
                <td align="center" style="background-color: <%= lineColor %>; width: 50px"><a href="/editTrx?id=<%= trx.getId()%>">Edit</a></td>
                <td align="center" style="background-color: <%= lineColor %>; width: 50px"><a href="/deleteTrx?id=<%= trx.getId()%>">Delete</a></td>
            </tr>
        <%}%>
        </tbody>
    </table>
    <br>
<form action="/newTrx" method="get">
    <center><button class="flareButton">New Transaction</button></center>
</form>
</body>
</html>
<%! private String getLineColor(String color) {
    if (color.equals("white")) {
        return "#AFEEEE";
    }
    return "white";
}%>