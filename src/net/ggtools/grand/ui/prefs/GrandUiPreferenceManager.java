// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2004, Christophe Labouisse All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ggtools.grand.ui.prefs;

import net.ggtools.grand.ui.Application;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

/**
 * @author Christophe Labouisse
 */
public class GrandUiPreferenceManager extends PreferenceManager {

    public GrandUiPreferenceManager() {
        final PreferencePage generalPage = new GeneralPreferencePage();
        final IPersistentPreferenceStore preferenceStore = Application.getInstance().getPreferenceStore();
        generalPage.setPreferenceStore(preferenceStore);
        final IPreferenceNode generalPageNode = new PreferenceNode("General", generalPage);
        addToRoot(generalPageNode);
        final PreferencePage graphPage = new GraphPreferencePage();
        graphPage.setPreferenceStore(preferenceStore);
        final IPreferenceNode graphNode = new PreferenceNode("Graph", graphPage);
        addToRoot(graphNode);
        final PreferencePage nodePage = new NodesPreferencePage();
        nodePage.setPreferenceStore(preferenceStore);
        final IPreferenceNode nodesNode = new PreferenceNode("Nodes", nodePage);
        graphNode.add(nodesNode);
        final PreferencePage linksPage = new LinksPreferencePage();
        linksPage.setPreferenceStore(preferenceStore);
        final IPreferenceNode linksNode = new PreferenceNode("Nodes", linksPage);
        graphNode.add(linksNode);
    }
}