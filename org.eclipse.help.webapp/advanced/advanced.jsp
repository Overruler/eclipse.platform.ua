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
<title><%=WebappResources.getString("Advanced", request)%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<style type="text/css">

/* need this one for Mozilla */
HTML { 
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
 }
 
BODY {
	font: icon;
	background:ButtonFace;
	border:1px solid WindowText;
	padding:0px;
	margin:0px;
}

TABLE {
	font:icon;
	background:ButtonFace;
}

TD, TR {
	margin:0px;
	padding:0px;
	border:0px;
}

FORM {
	margin:0;
	padding:0px;
	border:0px;
	height:100%;
}


#searchTable {
	background:transparent; 
	margin:10px 0px 20px 0px;
}

#searchWord {
	border:1px solid WindowText;
	width:100%;
	font:icon;
}

#booksContainer {
	background:Window;
	border:1px solid ThreeDShadow;
	margin:0px 10px;
	overflow:auto;
}

.book {
	margin:0xp;
	border:0px;
	padding:0px;
}

.button {
	font:icon;
	border:1px solid #ffffff;
	margin:0px;
	padding:0px;
}

<%
if (data.isMozilla()) {
%>
input[type="checkbox"] {
	border:2px solid WindowText; 
	margin:0xp; 
	padding:0px;	
	height:12px;
	width:12px;
}
<%
}
%>
</style>

<script language="JavaScript">

function doAdvancedSearch()
{
	try
	{
		var form = document.forms["searchForm"];
		var searchWord = form.searchWord.value;
		var maxHits = form.maxHits.value;
		if (!searchWord || searchWord == "")
			return;
	
		var scope = "";
		var buttons = document.getElementsByTagName("INPUT");
		for (var i=0; i<buttons.length; i++)
		{
			if (buttons[i].type != "checkbox") continue;
			if (buttons[i].checked == false) continue;
			scope += "&<%=data.getScopeParamName()%>="+escape(buttons[i].name);
		}
		
		// persist selection
		window.opener.saveSelectedBooks(getSelectedBooks());
		
		window.opener.document.forms["searchForm"].searchWord.value = searchWord;
		var query = "<%=data.getSearchWordParamName()%>="+escape(searchWord)+"&maxHits="+maxHits + "&scopedSearch=true" + scope;
		window.opener.doSearch(query);
		window.opener.focus();
		window.close();
	} catch(ex) {}
}

function restoreSelectedBooks()
{
	var selectedBooks = window.opener.selectedBooks;
	var inputs = document.body.getElementsByTagName("INPUT");
	for (var i=0; i<inputs.length; i++) {
		if (inputs[i].type == "checkbox" && isSelected(inputs[i].name, selectedBooks))
			inputs[i].checked = true;
	}
}


function getSelectedBooks()
{
	var selectedBooks = new Array();
	var inputs = document.body.getElementsByTagName("INPUT");
	for (var i=0; i<inputs.length; i++) {
		if (inputs[i].type == "checkbox"  && inputs[i].checked)
			selectedBooks[selectedBooks.length] = inputs[i].name;
	}
	return selectedBooks;
}

function isSelected(book, selectedBooks)
{
	// the first time select all
	if (!selectedBooks)
		return true;
		
	for (var i=0; i<selectedBooks.length; i++)
		if (book == selectedBooks[i])
			return true;
	return false;
}

function onloadHandler()
{
	// select the books from previous run, or all otherwise
	restoreSelectedBooks();
}

</script>

</head>

<body onload="onloadHandler()">

<form name="searchForm" onsubmit="doAdvancedSearch()">
<div style="overflow:auto;height:250px;">
	<table id="searchTable" width="100%" cellspacing=0 cellpading=0 border=0 align=center >
		<tr><td style="padding:0px 10px;"><%=WebappResources.getString("SearchExpression", request)%>
		</td></tr>
		<tr><td style="padding:0px 10px;"><input type="text" id="searchWord" name="searchWord" value='<%=data.getSearchWord()%>' maxlength=256 alt='<%=WebappResources.getString("SearchExpression", request)%>'>
          	  	<input type="hidden" name="maxHits" value="500" >
        </td></tr>
        <tr><td style="padding:0px 10px;"><%=WebappResources.getString("expression_label", request)%>
        </td></tr>
    </table>
  
  	<table id="filterTable" width="100%" cellspacing=0 cellpading=0 border=0 align=center  style="background:transparent;">
		<tr><td><div id="selectBook" style="margin-left:10px;"><%=WebappResources.getString("Select", request)%></div>
		</td></tr>
		<tr><td>
			<div id="booksContainer">
<% 
IToc[] tocs = data.getTocs();
for (int i=0; i<tocs.length; i++)
{
	String label = UrlUtil.htmlEncode(tocs[i].getLabel());
%>
				<div class="book"><input class="checkbox" type="checkbox" name='<%=tocs[i].getHref()%>' alt="<%=label%>"><%=label%></div>
<%
}		
%>
			</div>
		</td></tr>
	</table>
</div>
<div style="height:50px;">
	<table valign="bottom" align="right">
		<tr id="buttonsTable" valign="bottom"><td valign="bottom" align="right">
  			<table cellspacing=10 cellpading=0 border=0 align=right  style="background:transparent;">
				<tr>
					<td style="border:1px solid WindowText; padding:0px; margin:0px;">
						<input id="searchButton" class='button'  type="button" onclick="doAdvancedSearch()" value='<%=WebappResources.getString("Search", request)%>'  id="go" alt='<%=WebappResources.getString("Search", request)%>'>
					</td>
					<td style="border:1px solid WindowText; padding:0px; margin:0px;">
					  	<input class='button' type="button" onclick="window.close()"  type="button"  value='<%=WebappResources.getString("Cancel", request)%>'  id="cancel" alt='<%=WebappResources.getString("Cancel", request)%>'>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
</div>
 </form>

</body>
</html>
