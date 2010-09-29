package eu.hydrologis.jgrass.beegisutils.jgrassported;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

public class SaveAction extends Action {

    private final SimpleSWTImageEditor simpleSWTImageEditor;

    public SaveAction( SimpleSWTImageEditor simpleSWTImageEditor ) {
        super("Save image to disk (jpg supported)");
        this.simpleSWTImageEditor = simpleSWTImageEditor;

    }

    public void run() {

        FileDialog fileDialog = new FileDialog(simpleSWTImageEditor.getMainControl().getShell(),
                SWT.SAVE);
        String path = fileDialog.open();
        if (path == null || path.length() < 1) {
            return;
        }
        if (!path.toLowerCase().endsWith("jpg")) {
            path = path + ".jpg";
        }
        final String newpath = path;

        try {

            IWorkbench wb = PlatformUI.getWorkbench();
            IProgressService ps = wb.getProgressService();
            ps.busyCursorWhile(new IRunnableWithProgress(){
                public void run( IProgressMonitor pm ) {
                    pm.beginTask("Saving image to file: " + newpath, IProgressMonitor.UNKNOWN);
                    Image img = simpleSWTImageEditor.getImage();
                    ImageLoader imgLoader = new ImageLoader();
                    ImageData imageData = img.getImageData();
                    imgLoader.data = new ImageData[]{imageData};
                    imgLoader.save(newpath, SWT.IMAGE_JPEG);
                    pm.done();
                }
            });

        } catch (Exception e) {
            String message = "An error occurred while saving the image";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    BeegisUtilsPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }

    }
}
