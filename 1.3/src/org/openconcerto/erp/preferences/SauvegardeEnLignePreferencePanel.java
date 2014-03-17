/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 OpenConcerto, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU General Public License Version 3
 * only ("GPL"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.gnu.org/licenses/gpl-3.0.html See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 */
 
 package org.openconcerto.erp.preferences;

import org.openconcerto.ui.DefaultGridBagConstraints;
import org.openconcerto.ui.preferences.DefaultPreferencePanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SauvegardeEnLignePreferencePanel extends DefaultPreferencePanel {
    public SauvegardeEnLignePreferencePanel() {
        this.setLayout(new GridBagLayout());
        final GridBagConstraints c = new DefaultGridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 2;
        c.weightx = 1;

        final JLabel label = new JLabel("Identifiant de connexion:", JLabel.RIGHT);
        final JLabel label2 = new JLabel("Mot de passe:", JLabel.RIGHT);
        final JTextField textLogin = new JTextField("");
        final JTextField textPassword = new JTextField("");
        textLogin.setEnabled(false);
        textPassword.setEnabled(false);
        final JCheckBox b = new JCheckBox("Activer la sauvegarde en ligne");
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                textLogin.setEnabled(b.isSelected());
                textPassword.setEnabled(b.isSelected());
            }
        });
        this.add(b, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;

        this.add(label, c);
        c.gridx++;

        this.add(textLogin, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;

        this.add(label2, c);
        c.gridx++;

        this.add(textPassword, c);

        c.weightx = 1;
        c.weighty = 1;
        this.add(new JPanel(), c);
    }

    public void storeValues() {
    }

    public void restoreToDefaults() {
    }

    public String getTitleName() {
        return "Sauvegarde en ligne";
    }
}
