<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	SearchData data = new SearchData(application, request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=WebappResources.getString("SearchResults", request)%></title>

<style type="text/css">
<%@ include file="list.css"%>
</style>


<base target="ContentViewFrame">
<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">		

function refresh() 
{ 
	window.location.replace("search_results.jsp?<%=request.getQueryString()%>");
}
</script>


</head>

<body >

<%
if (!data.isSearchRequest()) {
	out.write(WebappResources.getString("doSearch", request));
} else if (data.isProgressRequest()) {
%>

<CENTER>
<TABLE BORDER='0'>
	<TR><TD><%=WebappResources.getString("Indexing", request)%></TD></TR>
	<TR><TD ALIGN='LEFT'>
		<DIV STYLE='width:100px;height:16px;border:1px solid WindowText;'>
			<DIV ID='divProgress' STYLE='width:<%=data.getIndexedPercentage()%>px;height:100%;background-color:Highlight'></DIV>
		</DIV>
	</TD></TR>
	<TR><TD><%=data.getIndexedPercentage()%>% <%=WebappResources.getString("complete", request)%></TD></TR>
	<TR><TD><br><%=WebappResources.getString("IndexingPleaseWait", request)%></TD></TR>
</TABLE>
</CENTER>
<script language='JavaScript'>
setTimeout('refresh()', 2000);
</script>
</body>
</html>

<%
	return;
} else if (data.getHits().length == 0){
	out.write(WebappResources.getString("Nothing_found", request));
} else {
		
	Hit[] hits = data.getHits();
%>

<table id='list'  cellspacing='0' >

<%
	for (int i = 0; i < hits.length; i++) 
	{
%>

<tr class='list' id='r<%=i%>'>
	<td class='score' align='right'><%=hits[i].getScore()%></td>
	<td align='left' class='label' nowrap>
		<a id='a<%=i%>' 
		   href='<%=hits[i].getHref()%>' 
		   onclick='parent.parent.setToolbarTitle("<%=UrlUtil.JavaScriptEncode(hits[i].getTocLabel())%>")' 
		   title="<%=UrlUtil.htmlEncode(hits[i].getTocLabel())%>">
		   <%=UrlUtil.htmlEncode(hits[i].getLabel())%>
		 </a>
	</td>
</tr>

<%
	}
%>

</table>

<%

}

%>

<script language="JavaScript">
	selectTopicById('<%=data.getSelectedTopicId()%>');
</script>

</body>
</html>

