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
package org.eclipse.help.internal.toc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.Node;
import org.eclipse.help.TocContribution;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.dynamic.NodeHandler;
import org.eclipse.help.internal.dynamic.NodeProcessor;

/*
 * Assembles toc contributions (toc fragments) into complete, linked, and
 * assembled books.
 */
public class TocAssembler {

	private static final String ELEMENT_LINK = "link"; //$NON-NLS-1$
	private static final String ELEMENT_ANCHOR = "anchor"; //$NON-NLS-1$
	private static final String ATTRIBUTE_LINK_TO = "link_to"; //$NON-NLS-1$
	private static final String ATTRIBUTE_TOC = "toc"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	
	private NodeProcessor processor;
	private NodeHandler[] handlers;
	
	private List contributions;
	private Map contributionsById;
	private Map contributionsByLinkTo;
	private Set processedContributions;
	
	/*
	 * Assembles the given toc contributions into complete, linked
	 * books. The originals are not modified.
	 */
	public List assemble(List contributions) {
		this.contributions = copy(contributions);
		contributionsById = null;
		contributionsByLinkTo = null;
		processedContributions = null;
		
		List books = getBooks();
		Iterator iter = books.iterator();
		while (iter.hasNext()) {
			TocContribution book = (TocContribution)iter.next();
			process(book);
		}
		return books;
	}
	
	/*
	 * Performs a deep copy of all the contributions in the given list
	 * and returns a new list.
	 */
	private List copy(List contributions) {
		List copies = new ArrayList(contributions.size());
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			Node node = (Node)iter.next();
			copies.add(copy(node));
		}
		return copies;
	}

	/*
	 * Performs a deep copy of the given node and all its children.
	 */
	private Node copy(Node node) {
		// copy node
		Node copy = node instanceof TocContribution ? new TocContribution() : new Node();
		copy.setName(node.getName());
		copy.setValue(node.getValue());
		Iterator iter = node.getAttributes().iterator();
		while (iter.hasNext()) {
			String name = (String)iter.next();
			String value = node.getAttribute(name);
			copy.setAttribute(name, value);
		}
		
		// copy children
		Node[] children = node.getChildren();
		for (int i=0;i<children.length;++i) {
			copy.appendChild(copy(children[i]));
		}
		return copy;
	}
	
	/*
	 * Returns the list of contributions that should appear as root TOCs
	 * (books). Contributions are books if:
	 * 
	 * 1. isPrimary() returns true.
	 * 2. The toc has no "link_to" attribute defined (does not link into
	 *    another toc).
	 * 3. No other toc has a link to this contribution (via "link" element).
	 */
	private List getBooks() {
		Set linkedContributionIds = getLinkedContributionIds(contributions);
		List books = new ArrayList();
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			TocContribution contrib = (TocContribution)iter.next();
			if (contrib.isPrimary() && contrib.getToc().getAttribute(ATTRIBUTE_LINK_TO) == null && !linkedContributionIds.contains(contrib.getId())) {
				books.add(contrib);
			}
		}
		return books;
	}
	
	/*
	 * Returns the set of ids of contributions that are linked to by other
	 * contributions, i.e. at least one other contribution has a link element
	 * pointing to it.
	 */
	private Set getLinkedContributionIds(List contributions) {
		if (processor == null) {
			processor = new NodeProcessor();
		}
		final Set linkedContributionIds = new HashSet();
		NodeHandler[] linkFinder = new NodeHandler[] {
			new NodeHandler() {
				public short handle(Node node, String id) {
					if (ELEMENT_LINK.equals(node.getName())) {
						String toc = node.getAttribute(ATTRIBUTE_TOC);
						if (toc != null) {
							TocContribution srcContribution = getContribution(id);
							linkedContributionIds.add(HrefUtil.normalizeHref(srcContribution.getContributorId(), toc));
						}
					}
					return UNHANDLED;
				}
			}
		};
		processor.setHandlers(linkFinder);
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			TocContribution contrib = (TocContribution)iter.next();
			processor.process(contrib.getToc(), contrib.getId());
		}
		return linkedContributionIds;
	}
	
	/*
	 * Processes the given contribution, if it hasn't been processed yet. This
	 * performs the following operations:
	 * 
	 * 1. Topic hrefs are normalized, e.g. "path/doc.html" ->
	 *    "/my.plugin/path/doc.html"
	 * 2. Links are resolved, link is replaced with target content, extra docs
	 *    are merged.
	 * 3. Anchor contributions are resolved, tocs with link_to's are inserted
	 *    at anchors and extra docs merged.
	 */
	private void process(TocContribution contribution) {
		if (processedContributions == null) {
			processedContributions = new HashSet();
		}
		// don't process the same one twice
		if (!processedContributions.contains(contribution)) {
			if (processor == null) {
				processor = new NodeProcessor();
			}
			if (handlers == null) {
				handlers = new NodeHandler[] {
					new NormalizeHandler(),
					new LinkHandler(),
					new AnchorHandler(),
				};
			}
			processor.setHandlers(handlers);
			processor.process(contribution.getToc(), contribution.getId());
			processedContributions.add(contribution);
		}
	}
	
	/*
	 * Returns the contribution with the given id.
	 */
	private TocContribution getContribution(String id) {
		if (contributionsById == null) {
			contributionsById = new HashMap();
			Iterator iter = contributions.iterator();
			while (iter.hasNext()) {
				TocContribution contribution = (TocContribution)iter.next();
				contributionsById.put(contribution.getId(), contribution);
			}
		}
		return (TocContribution)contributionsById.get(id);
	}
	
	/*
	 * Returns all contributions that define a link_to attribute pointing to
	 * the given anchor path. The path has the form "<contributionId>#<anchorId>",
	 * e.g. "/my.plugin/toc.xml#myAnchor".
	 */
	private TocContribution[] getAnchorContributions(String anchorPath) {
		if (contributionsByLinkTo == null) {
			contributionsByLinkTo = new HashMap();
			Iterator iter = contributions.iterator();
			while (iter.hasNext()) {
				TocContribution srcContribution = (TocContribution)iter.next();
				String linkTo = srcContribution.getToc().getAttribute(ATTRIBUTE_LINK_TO);
				if (linkTo != null) {
					String destAnchorPath = HrefUtil.normalizeHref(srcContribution.getContributorId(), linkTo);
					TocContribution[] array = (TocContribution[])contributionsByLinkTo.get(destAnchorPath);
					if (array == null) {
						array = new TocContribution[] { srcContribution };
					}
					else {
						TocContribution[] temp = new TocContribution[array.length + 1];
						System.arraycopy(array, 0, temp, 0, array.length);
						temp[array.length] = srcContribution;
						array = temp;
					}
					contributionsByLinkTo.put(destAnchorPath, array);
				}
			}
		}
		TocContribution[] contributions = (TocContribution[])contributionsByLinkTo.get(anchorPath);
		if (contributions == null) {
			contributions = new TocContribution[0];
		}
		return contributions;
	}
	
	/*
	 * Handler that resolves link elements (replaces the link element with
	 * the linked-to toc's children.
	 */
	private class LinkHandler extends NodeHandler {
		public short handle(Node node, String id) {
			if (ELEMENT_LINK.equals(node.getName())) {
				Node parent = node.getParent();
				if (parent != null) {
					String toc = node.getAttribute(ATTRIBUTE_TOC);
					if (toc != null) {
						TocContribution destContribution = getContribution(id);
						TocContribution srcContribution = getContribution(HrefUtil.normalizeHref(destContribution.getContributorId(), toc));
						if (srcContribution != null) {
							process(srcContribution);
							Node[] children = srcContribution.getToc().getChildren();
							for (int i=0;i<children.length;++i) {
								parent.insertBefore(copy(children[i]), node);
							}
							
							// combine extra docs
							String[] srcExtraDocuments = srcContribution.getExtraDocuments();
							String[] destExtraDocuments = destContribution.getExtraDocuments();
							if (srcExtraDocuments.length != 0) {
								String[] combinedExtraDocuments;
								if (destExtraDocuments.length == 0) {
									combinedExtraDocuments = destExtraDocuments;
								}
								else {
									combinedExtraDocuments = new String[destExtraDocuments.length + srcExtraDocuments.length];
									System.arraycopy(srcExtraDocuments, 0, combinedExtraDocuments, 0, srcExtraDocuments.length);
									System.arraycopy(destExtraDocuments, 0, combinedExtraDocuments, srcExtraDocuments.length, destExtraDocuments.length);
								}
								destContribution.setExtraDocuments(combinedExtraDocuments);
							}
						}
						parent.removeChild(node);
					}
				}
				return HANDLED_SKIP;
			}
			return UNHANDLED;
		}
	}

	/*
	 * Handles anchor contributions. If any contribution's toc wants to link
	 * into this one at the current anchor, link it in.
	 */
	private class AnchorHandler extends NodeHandler {
		public short handle(Node node, String id) {
			if (ELEMENT_ANCHOR.equals(node.getName())) {
				Node parent = node.getParent();
				if (parent != null) {
					String anchorId = node.getAttribute(ATTRIBUTE_ID);
					if (anchorId != null) {
						TocContribution destContribution = getContribution(id);
						TocContribution[] srcContributions = getAnchorContributions(destContribution.getId() + '#' +  anchorId);
						for (int i=0;i<srcContributions.length;++i) {
							process(srcContributions[i]);
							Node[] children = srcContributions[i].getToc().getChildren();
							for (int j=0;j<children.length;++j) {
								parent.insertBefore(copy(children[j]), node);
							}
							
							// combine extra docs
							String[] srcExtraDocuments = srcContributions[i].getExtraDocuments();
							String[] destExtraDocuments = destContribution.getExtraDocuments();
							if (srcExtraDocuments.length != 0) {
								String[] combinedExtraDocuments;
								if (destExtraDocuments.length == 0) {
									combinedExtraDocuments = destExtraDocuments;
								}
								else {
									combinedExtraDocuments = new String[destExtraDocuments.length + srcExtraDocuments.length];
									System.arraycopy(srcExtraDocuments, 0, combinedExtraDocuments, 0, srcExtraDocuments.length);
									System.arraycopy(destExtraDocuments, 0, combinedExtraDocuments, srcExtraDocuments.length, destExtraDocuments.length);
								}
								destContribution.setExtraDocuments(combinedExtraDocuments);
							}
						}
						parent.removeChild(node);
					}
				}
				return HANDLED_SKIP;
			}
			return UNHANDLED;
		}
	}

	/*
	 * Normalizes topic hrefs, by prepending the plug-in id to form an href.
	 * e.g. "path/myfile.html" -> "/my.plugin/path/myfile.html"
	 */
	private class NormalizeHandler extends NodeHandler {
		public short handle(Node node, String id) {
			if (Topic.NAME.equals(node.getName())) {
				String href = node.getAttribute(Topic.ATTRIBUTE_HREF);
				if (href != null) {
					TocContribution contribution = getContribution(id);
					if (contribution != null) {
						String pluginId = contribution.getContributorId();
						node.setAttribute(Topic.ATTRIBUTE_HREF, HrefUtil.normalizeHref(pluginId, href));
					}
				}
				return HANDLED_CONTINUE;
			}
			return UNHANDLED;
		}
	}
}