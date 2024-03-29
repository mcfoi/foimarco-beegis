/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.featureeditor.xml.annotatedguis;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.ASeparator;
import eu.hydrologis.jgrass.featureeditor.xml.annotated.FormElement;

/**
 * Class representing an swt separator label gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ASeparatorGui extends FormGuiElement {

    private final ASeparator separator;
    public ASeparatorGui( ASeparator separator ) {
        this.separator = separator;

    }
    @Override
    public Control makeGui( Composite parent ) {
        int style = SWT.HORIZONTAL | SWT.SEPARATOR;
        if (separator.orientation != null && separator.orientation.toLowerCase().startsWith("ver")) {
            style = SWT.VERTICAL | SWT.SEPARATOR;
        } else if (separator.orientation == null || separator.orientation.equals("")) {
            style = SWT.None;
        }

        org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(parent, style);
        label.setLayoutData(separator.constraints);

        return label;
    }

    public void setFeature( SimpleFeature feature ) {
    }

    public FormElement getFormElement() {
        return separator;
    }
}
