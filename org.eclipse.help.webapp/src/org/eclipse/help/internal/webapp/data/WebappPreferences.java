/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.util.ProductPreferences;

/**
 * Preferences for availiable to webapp
 */
public class WebappPreferences {
	Preferences prefs;
	/**
	 * Constructor.
	 */
	public WebappPreferences() {
		prefs = HelpBasePlugin.getDefault().getPluginPreferences();
	}
	/**
	 * @return String - URL of banner page or null
	 */
	public String getBanner() {
		return prefs.getString("banner"); //$NON-NLS-1$
	}

	public String getBannerHeight() {
		return prefs.getString("banner_height"); //$NON-NLS-1$
	}

	public String getHelpHome() {
		return prefs.getString("help_home"); //$NON-NLS-1$
	}

	public boolean isIndexView() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "indexView"); //$NON-NLS-1$
	}

	public boolean isBookmarksView() {
		return BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER
				&& ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "bookmarksView"); //$NON-NLS-1$
	}

	public boolean isBookmarksAction() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "bookmarksView"); //$NON-NLS-1$
	}

	public String getImagesDirectory() {
		String imagesDirectory = prefs.getString("imagesDirectory"); //$NON-NLS-1$
		if (imagesDirectory != null && imagesDirectory.startsWith("/")) //$NON-NLS-1$
			imagesDirectory = UrlUtil.getHelpURL(imagesDirectory);
		return imagesDirectory;

	}

	public String getToolbarBackground() {
		return prefs.getString("advanced.toolbarBackground"); //$NON-NLS-1$
	}

	public String getBasicToolbarBackground() {
		return prefs.getString("basic.toolbarBackground"); //$NON-NLS-1$
	}

	public String getToolbarFont() {
		return prefs.getString("advanced.toolbarFont"); //$NON-NLS-1$
	}

	public String getViewBackground() {
		return prefs.getString("advanced.viewBackground"); //$NON-NLS-1$
	}
	
	public String getViewBackgroundStyle() {
		String viewBackground = prefs.getString("advanced.viewBackground"); //$NON-NLS-1$
		if (viewBackground == null || viewBackground.length() == 0) {
			return (""); //$NON-NLS-1$
		}
		return "background-color: " + viewBackground + ";";  //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getBasicViewBackground() {
		return prefs.getString("basic.viewBackground"); //$NON-NLS-1$
	}

	public String getViewFont() {
		return prefs.getString("advanced.viewFont"); //$NON-NLS-1$
	}

	public boolean isWindowTitlePrefix() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "windowTitlePrefix"); //$NON-NLS-1$
	}
	public boolean isDontConfirmShowAll() {
		return prefs.getBoolean("dontConfirmShowAll"); //$NON-NLS-1$
	}
	public void setDontConfirmShowAll(boolean dontconfirm) {
		prefs.setValue("dontConfirmShowAll", dontconfirm); //$NON-NLS-1$
	}
	public boolean isActiveHelp() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "activeHelp"); //$NON-NLS-1$
	}

	public boolean isIndexInstruction() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "indexInstruction"); //$NON-NLS-1$
	}

	public boolean isIndexButton() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "indexButton"); //$NON-NLS-1$
	}

	public boolean isIndexPlusMinus() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "indexPlusMinus"); //$NON-NLS-1$
	}

	public boolean isIndexExpandAll() {
		return ProductPreferences.getBoolean(HelpBasePlugin.getDefault(), "indexExpandAll"); //$NON-NLS-1$
	}
	public boolean isHighlightDefault() {
		return prefs.getBoolean("default_highlight"); //$NON-NLS-1$
	}
	public void setHighlightDefault(boolean highlight) {
		prefs.setValue("default_highlight", highlight); //$NON-NLS-1$
	}
	
	public boolean isRestrictTopicParameter() {
		return prefs.getBoolean("restrictTopicParameter"); //$NON-NLS-1$
	}
	
}