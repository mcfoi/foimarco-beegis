/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.formeditor.model.widgets;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Descriptor for a property that has a file path which should be edited.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FormComboPropertyDescriptor extends PropertyDescriptor {

    /**
     * Creates an property descriptor with the given id and display name.
     * 
     * @param id the id of the property.
     * @param displayName the name to display for the property.
     */
    public FormComboPropertyDescriptor( Object id, String displayName ) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor( Composite parent ) {
        CellEditor editor = new FormComboCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
