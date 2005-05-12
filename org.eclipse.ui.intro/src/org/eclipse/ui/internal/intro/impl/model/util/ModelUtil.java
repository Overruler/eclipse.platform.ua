/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model.util;

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.intro.impl.model.url.IntroURLParser;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.eclipse.ui.internal.intro.impl.util.StringUtil;
import org.eclipse.ui.internal.intro.impl.util.Util;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Util class for model. Has methods for resolving model attributes, and methods
 * for manipulating XHTML DOM.
 */
public class ModelUtil {

    private static String TAG_BODY = "body"; //$NON-NLS-1$
    private static String TAG_HEAD = "head"; //$NON-NLS-1$
    private static String TAG_BASE = "base"; //$NON-NLS-1$
    private static String TAG_PARAM = "param"; //$NON-NLS-1$
    private static String ATT_SRC = "src"; //$NON-NLS-1$
    private static String ATT_HREF = "href"; //$NON-NLS-1$
    private static String ATT_CITE = "cite"; //$NON-NLS-1$
    private static String ATT_LONGDESC = "longdesc"; //$NON-NLS-1$
    private static String ATT_DATA = "data"; //$NON-NLS-1$
    private static String ATT_CODEBASE = "codebase"; //$NON-NLS-1$
    private static String ATT_VALUE = "value"; //$NON-NLS-1$
    private static String ATT_VALUE_TYPE = "valuetype"; //$NON-NLS-1$


    /*
     * ********* Model util methods ************************************
     */

    /**
     * Checks to see if the passed string is a valid URL (has a protocol), if
     * yes, returns it as is. If no, treats it as a resource relative to the
     * declaring plugin. Return the plugin relative location, fully qualified.
     * Retruns null if the passed string itself is null.
     * 
     * @param resource
     * @param pluginDesc
     * @return returns the URL as is if it had a protocol.
     */
    public static String resolveURL(String url, String pluginId) {
        Bundle bundle = null;
        if (pluginId != null)
            // if pluginId is not null, use it.
            bundle = Platform.getBundle(pluginId);
        return resolveURL("", url, bundle); //$NON-NLS-1$
    }



    /**
     * Checks to see if the passed string is a valid URL (has a protocol), if
     * yes, returns it as is. If no, treats it as a resource relative to the
     * declaring plugin. Return the plugin relative location, fully qualified.
     * Retruns null if the passed string itself is null.
     * 
     * @param resource
     * @param pluginDesc
     * @return returns the URL as is if it had a protocol.
     */
    public static String resolveURL(String url, IConfigurationElement element) {
        Bundle bundle = BundleUtil.getBundleFromConfigurationElement(element);
        return resolveURL("", url, bundle); //$NON-NLS-1$
    }



    /**
     * @see resolveURL(String url, IConfigurationElement element)
     */
    public static String resolveURL(String base, String url, Bundle bundle) {
        // quick exit
        if (url == null)
            return null;
        IntroURLParser parser = new IntroURLParser(url);
        if (parser.hasProtocol())
            return url;
        // make plugin relative url. Only now we need the bundle.
        return BundleUtil.getResolvedResourceLocation(base, url, bundle);
    }



    /**
     * Util method to support jarring. Used to extract parent folder of intro
     * xml content files. And to extract parent folder of invalidPage.xhtml
     * 
     * @param resource
     */
    public static void extractParentFolder(Bundle bundle, String contentFile) {
        try {
            long start = 0;
            if (Log.logPerformance)
                start = System.currentTimeMillis();
            IPath parentFolder = ModelUtil.getParentFolderPath(contentFile);
            URL parentFolderURL = Platform.find(bundle, parentFolder);
            if (parentFolderURL == null) {
                // should never be here.
                Log.error("Could not find folder to extract: " + contentFile,
                    null);
                return;
            }

            Platform.asLocalURL(parentFolderURL);
            if (Log.logPerformance) {
                String msg = StringUtil.concat(
                    "extracting content folder ", contentFile, " (", //$NON-NLS-1$ //$NON-NLS-2$
                    bundle.getSymbolicName(), ")", " took: ").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                Util.logPerformanceTime(msg, start);
            }

        } catch (Exception e) {
            if (contentFile != null)
                Log.error("Failed to extract Intro content folder for: " //$NON-NLS-1$
                        + contentFile, e);
        }
    }


    /**
     * Returns the path to the parent folder containing the passed content xml
     * file. It is assumed that the path is a local url representing a content
     * file.
     */
    public static String getParentFolderToString(String contentFilePath) {
        IPath path = getParentFolderPath(contentFilePath);
        return path.toString();
    }


    /*
     * 
     * ******** XHTML DOM util methods *********************************
     */

    /**
     * Returns the path to the parent folder containing the passed content xml
     * file. It is assumed that the path is a local url representing a content
     * file.
     */
    public static String getParentFolderOSString(String contentFilePath) {
        IPath path = getParentFolderPath(contentFilePath);
        return path.toOSString();
    }

    /**
     * Returns the parent folder of the given path.
     */
    public static IPath getParentFolderPath(String contentFilePath) {
        IPath path = new Path(contentFilePath);
        path = path.removeLastSegments(1).addTrailingSeparator();
        return path;
    }




    public static void insertBase(Document dom, String baseURL) {
        // there should only be one head and one base element dom.
        NodeList headList = dom.getElementsByTagName(TAG_HEAD);
        Element head = (Element) headList.item(0);
        NodeList baseList = head.getElementsByTagName(TAG_BASE);
        if (baseList.getLength() == 0) {
            // insert a base element, since one is not defined already.
            Element base = dom.createElement(TAG_BASE);
            base.setAttribute(ATT_HREF, baseURL);
            head.insertBefore(base, head.getFirstChild());
        }
    }


    /**
     * Returns a reference to the body of the DOM.
     * 
     * @param dom
     * @return
     */
    public static Element getBodyElement(Document dom) {
        // there should only be one body element dom.
        NodeList bodyList = dom.getElementsByTagName(TAG_BODY);
        Element body = (Element) bodyList.item(0);
        return body;
    }



    public static Element createElement(Document dom, String elementName,
            Properties attributes) {

        Element element = dom.createElement(elementName);
        if (attributes != null) {
            Enumeration e = attributes.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                element.setAttribute(key, attributes.getProperty(key));
            }
        }
        return element;
    }

    public static Element createAndAppendChild(Element parentElement,
            String elementName, Properties attributes) {

        Element element = createElement(parentElement.getOwnerDocument(),
            elementName, attributes);
        parentElement.appendChild(element);
        return element;
    }



    /**
     * Returns an Element array of all first level descendant Elements with a
     * given tag name, in the order in which they are encountered in the DOM.
     * Unlike the JAXP apis, which returns preorder traversal of this Element
     * tree, this method filters out children deeper than first level child
     * nodes.
     */
    public static Element[] getElementsByTagName(Element parent, String tagName) {
        NodeList allChildElements = parent.getElementsByTagName(tagName);
        Vector vector = new Vector();
        for (int i = 0; i < allChildElements.getLength(); i++) {
            // we know that the nodelist is of elements.
            Element aElement = (Element) allChildElements.item(i);
            if (aElement.getParentNode().equals(parent))
                // first level child element. add it.
                vector.add(aElement);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);
        return filteredElements;
    }

    /**
     * Same as getElementsByTagName(Element parent, String tagName) but the
     * parent element is assumed to be the root of the document.
     * 
     * @see getElementsByTagName(Element parent, String tagName)
     */
    public static Element[] getElementsByTagName(Document dom, String tagName) {
        NodeList allChildElements = dom.getElementsByTagName(tagName);
        Vector vector = new Vector();
        for (int i = 0; i < allChildElements.getLength(); i++) {
            // we know that the nodelist is of elements.
            Element aElement = (Element) allChildElements.item(i);
            if (aElement.getParentNode().equals(dom.getDocumentElement()))
                // first level child element. add it. Cant use getParent
                // here.
                vector.add(aElement);
        }
        Element[] filteredElements = new Element[vector.size()];
        vector.copyInto(filteredElements);
        return filteredElements;
    }


    /*
     * Util method similar to DOM getElementById() method, but it works without
     * an id attribute being specified. Deep searches all children in this
     * container's DOM for the first child with the given id. The element
     * retrieved must have the passed local name. Note that in an XHTML file
     * (aka DOM) elements should have a unique id within the scope of a
     * document. We use local name because this allows for finding intro
     * anchors, includes and dynamic content element regardless of whether or
     * not an xmlns was used in the xml.
     */
    public static Element getElementById(Document dom, String id,
            String localElementName) {
        NodeList children = dom.getElementsByTagNameNS("*", "*");
        for (int i = 0; i < children.getLength(); i++) {
            Element element = (Element) children.item(i);
            if (element.getAttribute("id").equals(id)) //$NON-NLS-1$
                return element;
        }
        // non found.
        return null;

    }




    /**
     * Updates all the resource attributes of the passed element to point to a
     * local resolved url.
     * 
     * @param element
     * @param extensionContent
     */
    public static void updateResourceAttributes(Element element,
            String localContentFilePath) {
        String folderLocalPath = getParentFolderOSString(localContentFilePath);
        doUpdateResourceAttributes(element, folderLocalPath);
        NodeList children = element.getElementsByTagName("*"); //$NON-NLS-1$
        for (int i = 0; i < children.getLength(); i++) {
            Element child = (Element) children.item(i);
            doUpdateResourceAttributes(child, folderLocalPath);
        }
    }

    private static void doUpdateResourceAttributes(Element element,
            String folderLocalPath) {
        qualifyAttribute(element, ATT_SRC, folderLocalPath);
        qualifyAttribute(element, ATT_HREF, folderLocalPath);
        qualifyAttribute(element, ATT_CITE, folderLocalPath);
        qualifyAttribute(element, ATT_LONGDESC, folderLocalPath);
        qualifyAttribute(element, ATT_CODEBASE, folderLocalPath);
        qualifyAttribute(element, ATT_DATA, folderLocalPath);
        qualifyValueAttribute(element, folderLocalPath);
    }

    private static void qualifyAttribute(Element element, String attributeName,
            String folderLocalPath) {
        if (element.hasAttribute(attributeName)) {
            String attributeValue = element.getAttribute(attributeName);
            if (new IntroURLParser(attributeValue).hasProtocol())
                return;
            IPath localSrcPath = new Path(folderLocalPath)
                .append(attributeValue);
            element.setAttribute(attributeName, localSrcPath.toOSString());
        }
    }

    private static void qualifyValueAttribute(Element element,
            String folderLocalPath) {
        if (element.hasAttribute(ATT_VALUE)
                && element.hasAttribute(ATT_VALUE_TYPE)
                && element.getAttribute(ATT_VALUE_TYPE).equals("ref") //$NON-NLS-1$
                && element.getLocalName().equals(TAG_PARAM)) {
            String value = element.getAttribute(ATT_VALUE);
            if (new IntroURLParser(value).hasProtocol())
                return;
            IPath localSrcPath = new Path(folderLocalPath).append(value);
            element.setAttribute(ATT_VALUE, localSrcPath.toOSString());
        }
    }


    /**
     * Returns an array version of the passed NodeList. Used to work around DOM
     * design issues.
     */
    public static Node[] getArray(NodeList nodeList) {
        Node[] nodes = new Node[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++)
            nodes[i] = nodeList.item(i);
        return nodes;
    }


    /**
     * Remove all instances of the element from the DOM.
     * 
     */
    public static void removeAllElements(Document dom, String elementLocalName) {
        // get all elements in DOM and remove them.
        NodeList elements = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
            elementLocalName);
        // get the array version of the nodelist to work around DOM api design.
        Node[] elementsArray = ModelUtil.getArray(elements);
        for (int i = 0; i < elementsArray.length; i++) {
            Node element = elementsArray[i];
            element.getParentNode().removeChild(element);
        }

    }



}