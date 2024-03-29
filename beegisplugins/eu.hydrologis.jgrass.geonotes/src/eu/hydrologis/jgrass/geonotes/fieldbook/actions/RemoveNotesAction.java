package eu.hydrologis.jgrass.geonotes.fieldbook.actions;

import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.GeonotesListViewer;

public class RemoveNotesAction extends Action {

    private final GeonotesListViewer geonotesViewer;

    public RemoveNotesAction( GeonotesListViewer geonotesViewer ) {
        super("Remove geonotes");
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {

        boolean answer = MessageDialog.openQuestion(geonotesViewer.getControl().getShell(), "Removal warning",
                "Do you really want to remove the selected geonotes?");

        if (!answer) {
            return;
        }

        List<GeonotesHandler> currentGeonotesSelection = geonotesViewer.getCurrentGeonotesSelection();
        for( GeonotesHandler geoNote : currentGeonotesSelection ) {
            try {
                geoNote.deleteNote();
            } catch (Exception e) {
                String message = "An error occurred while removing the Geonote.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
            }
        }
        
        List<GeonotesHandler> geonotesList = (List<GeonotesHandler>) geonotesViewer.getInput();
        geonotesList.removeAll(currentGeonotesSelection);
        geonotesViewer.setInput(geonotesList);
        ILayer geonotesLayer = GeonotesPlugin.getDefault().getGeonotesLayer();
        geonotesLayer.refresh(null);
    }
}
