/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
�* All rights reserved. This program and the accompanying materials
�* are made available under the terms of the Eclipse Public License v1.0
�* which accompanies this distribution, and is available at
�* http://www.eclipse.org/legal/epl-v10.html
�*
�* Contributors:
�*����Elias Volanakis - initial API and implementation
�*******************************************************************************/
package eu.hydrologis.jgrass.formeditor.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;

/**
 * This edit policy enables the removal of a Shapes instance from its container. 
 * @see ShapeEditPart#createEditPolicies()
 * @see WidgetsTreeEditPart#createEditPolicies()
 * @author Elias Volanakis
 */
public class WidgetComponentEditPolicy extends ComponentEditPolicy {

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(org.eclipse.gef.requests.GroupRequest)
     */
    protected Command createDeleteCommand( GroupRequest deleteRequest ) {
        Object parent = getHost().getParent().getModel();
        Object child = getHost().getModel();
        if (parent instanceof WidgetsDiagram && child instanceof AWidget) {
            return new WidgetDeleteCommand((WidgetsDiagram) parent, (AWidget) child);
        }
        return super.createDeleteCommand(deleteRequest);
    }
}