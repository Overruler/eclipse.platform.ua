/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

// Functions to update the nodes of a dynmamic tree based
// on an XML dom. 

var selectedNode;

function updateTree(xml) { 
    var tocData = xml.documentElement;  
    var treeRoot = document.getElementById("tree_root");
    if (tocData.tagName != "tree_data") {
        return;
    }
    var errorTags = xml.getElementsByTagName ("error");
    
    for (var i = 0; i < errorTags.length; i++) {
         var nextError = errorTags[i];
         var errorChildren = nextError.childNodes;
         // Is the next node a text node
         if (errorChildren.length > 0 && errorChildren[0].nodeType == 3) { 
             var message = errorChildren[0].data;
             alert(message);
         }
    }
    
    var nodes = tocData.childNodes;
    selectedNode = null;
    mergeChildren(treeRoot, nodes);
    if (selectedNode != null) {
        focusOnItem(selectedNode);
    }
 }
 
function mergeChildren(treeItem, nodes) {
    var placeholderDiv = findChild(treeItem, "DIV");
    var childAdded = false;
    if (nodes) {      
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            if (node.tagName == "node") {
                if (placeholderDiv && placeholderDiv.className == "unopened") {
                    treeItem.removeChild(placeholderDiv);
                    placeholderDiv = null;
                }
                var title = node.getAttribute("title");
                var isLeaf = node.getAttribute("is_leaf");
                var href = node.getAttribute("href");
                var image = node.getAttribute("image");
                var id = node.getAttribute("id");
                var childItem = mergeChild(treeItem, id, title, href, image, isLeaf);
                var isSelected = node.getAttribute("is_selected");
                if (!isLeaf) {
                    mergeChildren(childItem, node.childNodes);
                }
                if (isSelected) {
                    selectedNode = childItem;
                }
                childAdded = true;
            }
        }   
     }

     if (childAdded) {
         changeExpanderImage(treeItem, true); 
     } 
     
    // Expand this node if it was collapsed         
    var childClass = getChildClass(treeItem);
    if (childClass == "hidden") {
        toggleExpandState(treeItem);
    }
    
}

// Create a child if one with this if does not exist  
function mergeChild(treeItem, id, name, href, image, isLeaf) {  
    var children = treeItem.childNodes;
    if (children !== null) {
        for (var i = 0; i < children.length; i++) {
            if (children[i].nodeid == id ) {
                return children[i];
            }
        }
    }
        
    var childItem = document.createElement("div");
    // roots should have an id of "tree_root" to prevent indentation
    if (treeItem.id == "tree_root") {
        childItem.className = "root";
    } else {
        childItem.className = "visible"; 
    }
    childItem.nodeid = id;
    treeItem.appendChild(childItem);
    
    // If this is not a leaf create a span to prevent line breaking
    var container;
    if (isLeaf) {
        container = childItem;
    } else {
        container = document.createElement("SPAN");
        container.className = "item";
        childItem.appendChild(container);
    }
   
    var topicImage = document.createElement("IMG");
    setImage(topicImage, image);
    var topicName=document.createTextNode(name);
    
    if (!isLeaf) {
        var plusMinusImage= document.createElement("IMG");
        plusMinusImage.className = "expander";
        setImage(plusMinusImage, "plus");
        container.appendChild(plusMinusImage);
    }
      
    var anchor = document.createElement("a");
    if (href === null) {
        anchor.href = "about:blank";
        // anchor.className = "nolink";
    } else {
        anchor.href = href;
    }
    anchor.title = name;
    
    anchor.appendChild(topicImage);
    anchor.appendChild(topicName);
    container.appendChild(anchor);
    
    if (!isLeaf) {
        var innerDiv = document.createElement("DIV");  
        innerDiv.className = "unopened";
        childItem.appendChild(innerDiv);
    }
    return childItem;
}

function setLoadingMessage(treeItem, message) {   
    var placeholderDiv = findChild(treeItem, "DIV");
    if (placeholderDiv !== null && placeholderDiv.childNodes.length == 0) {      
        var msg = document.createTextNode(message);
        placeholderDiv.appendChild(msg);
    }
}