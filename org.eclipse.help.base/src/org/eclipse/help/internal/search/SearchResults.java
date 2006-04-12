/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.internal.workingset.AdaptableHelpResource;
import org.eclipse.help.internal.workingset.AdaptableToc;
import org.eclipse.help.internal.workingset.WorkingSet;

/**
 * Search result collector. Performs filtering and collects hits into an array
 * of SearchHit
 */
public class SearchResults implements ISearchHitCollector {
	// Collection of AdaptableHelpResource[]
	private ArrayList scopes;
	private int maxHits;
	private String locale;
	protected SearchHit[] searchHits = new SearchHit[0];
	/**
	 * Constructor
	 * 
	 * @param workingSets
	 *            working sets or null if no filtering
	 */
	public SearchResults(WorkingSet[] workingSets, int maxHits, String locale) {
		this.maxHits = maxHits;
		this.locale = locale;
		this.scopes = getScopes(workingSets);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.search.ISearchHitCollector#addHits(List, String)
	 */
	public void addHits(List hits, String highlightTerms) {
		String urlEncodedWords = URLCoder.encode(highlightTerms);
		List searchHitList = new ArrayList();
		float scoreScale = 1.0f;
		boolean scoreScaleSet = false;
		
		Iterator iter = hits.iterator();
		for (int i=0;i<maxHits && iter.hasNext();i++) {
			SearchHit rawHit = (SearchHit)iter.next();
			String href = rawHit.getHref();
			IToc toc = null; // the TOC containing the topic
			AdaptableHelpResource scope = null;
			// the scope for the topic, if any
			if (scopes == null) {
				toc = getTocForTopic(href, locale);
			} else {
				scope = getScopeForTopic(href);
				if (scope == null) {
					// topic outside of scope
					continue;
				} else if (scope instanceof AdaptableToc) {
					toc = (IToc) scope.getAdapter(IToc.class);
				} else { // scope is AdaptableTopic
					toc = (IToc) scope.getParent().getAdapter(IToc.class);
				}
			}

			// adjust score
			float score = rawHit.getScore();
			if (!scoreScaleSet) {
				if (score > 0) {
					scoreScale = 0.99f / score;
					score = 1;
				}
				scoreScaleSet = true;
			} else {
				score = score * scoreScale + 0.01f;
			}

			// Set the document label
			String label = rawHit.getLabel();
			if ("".equals(label) && toc != null) { //$NON-NLS-1$
				ITopic t;
				if (scope != null) {
					t = scope.getTopic(href);
				} else {
					t = toc.getTopic(href);
				}
				if (t != null) {
					label = t.getLabel();
				}
			}
			if (label == null || "".equals(label)) { //$NON-NLS-1$
				label = href;
			}
			
			// Set document href
			href += "?resultof=" + urlEncodedWords; //$NON-NLS-1$
			searchHitList.add(new SearchHit(href, label, rawHit.getRawSummary(), score, toc, rawHit.getRawId(), rawHit.getParticipantId(), rawHit.getFilters()));
		}
		searchHits = (SearchHit[]) searchHitList
				.toArray(new SearchHit[searchHitList.size()]);

	}
	/**
	 * Finds a topic within a scope
	 */
	private AdaptableHelpResource getScopeForTopic(String href) {
		for (int i = 0; i < scopes.size(); i++) {
			AdaptableHelpResource scope = (AdaptableHelpResource) scopes.get(i);
			if (scope.getTopic(href) != null)
				return scope;
		}
		return null;
	}

	/**
	 * Finds a topic in a toc or within a scope if specified
	 */
	private IToc getTocForTopic(String href, String locale) {
		IToc[] tocs = HelpPlugin.getTocManager().getTocs(locale);
		for (int i = 0; i < tocs.length; i++) {
			ITopic topic = tocs[i].getTopic(href);
			if (topic != null)
				return tocs[i];
		}
		return null;
	}

	/**
	 * Gets the searchHits.
	 * 
	 * @return Returns a SearchHit[]
	 */
	public SearchHit[] getSearchHits() {
		return searchHits;
	}

	/**
	 * Returns a collection of adaptable help resources that are roots for
	 * filtering.
	 * 
	 * @return Collection
	 */
	private ArrayList getScopes(WorkingSet[] wSets) {
		if (wSets == null)
			return null;

		scopes = new ArrayList(wSets.length);
		for (int w = 0; w < wSets.length; w++) {
			AdaptableHelpResource[] elements = wSets[w].getElements();
			for (int i = 0; i < elements.length; i++)
				scopes.add(elements[i]);
		}
		return scopes;
	}
}
