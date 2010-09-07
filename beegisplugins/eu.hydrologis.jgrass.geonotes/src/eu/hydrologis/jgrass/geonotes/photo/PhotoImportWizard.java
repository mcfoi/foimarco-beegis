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
package eu.hydrologis.jgrass.geonotes.photo;

import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.PHOTO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Coordinate;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;

/**
 * Photo import wizard.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PhotoImportWizard extends Wizard implements IImportWizard {
    private PhotoImportWizardPage mainPage;
    private long shift = 0L;
    private int intervalMinutes;

    public PhotoImportWizard() {
        super();
    }

    public boolean performFinish() {
        final FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
        final String path = mainPage.getPhotoFolder();
        shift = mainPage.getTime();
        intervalMinutes = mainPage.getIntervalMinutes();

        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                try {
                    IWorkbench wb = PlatformUI.getWorkbench();
                    IProgressService ps = wb.getProgressService();
                    ps.busyCursorWhile(new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) {

                            File f = new File(path);
                            File[] listFiles = f.listFiles();
                            HashMap<DateTime, List<File>> imageFiles = new HashMap<DateTime, List<File>>();
                            HashMap<DateTime, Coordinate> timestamp2Coordinates = new HashMap<DateTime, Coordinate>();
                            List<String> nonTakenFilesList = new ArrayList<String>();

                            pm.beginTask("Browsing pictures...", listFiles.length);
                            for( File file : listFiles ) {
                                try {
                                    String name = file.getName();
                                    if (name.endsWith("jpg") || name.endsWith("JPG") || name.endsWith("png")
                                            || name.endsWith("PNG")) {
                                        // check the date
                                        long lastModified = file.lastModified();
                                        // correct with the given shift
                                        lastModified = lastModified + shift;
                                        // search for gps points of that timestamp
                                        DateTime ts = new DateTime(lastModified);
                                        Coordinate coordinate = GeonotesHandler.getGpsCoordinateForTimeStamp(ts, intervalMinutes);

                                        if (coordinate == null) {
                                            // could not find date
                                            nonTakenFilesList.add(file.getAbsolutePath());
                                        } else {
                                            List<File> fileList = imageFiles.get(ts);
                                            if (fileList == null) {
                                                fileList = new ArrayList<File>();
                                                imageFiles.put(ts, fileList);
                                            }
                                            fileList.add(file);
                                            timestamp2Coordinates.put(ts, coordinate);
                                        }
                                    }
                                    pm.worked(1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            pm.done();

                            Set<DateTime> timeSet = imageFiles.keySet();
                            pm.beginTask("Importing matching photos...", timeSet.size());
                            for( DateTime timestamp : timeSet ) {
                                try {
                                    List<File> fileList = imageFiles.get(timestamp);
                                    StringBuilder sB = new StringBuilder("");
                                    for( File file : fileList ) {
                                        sB.append(file.getName());
                                        sB.append(" ");
                                    }

                                    Coordinate coordinate = timestamp2Coordinates.get(timestamp);

                                    String title = sB.toString();
                                    String info = "Date:" + timestamp.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM)
                                            + "\nN:" + coordinate.y + "\nE:" + coordinate.x;

                                    GeonotesHandler geonotesHandler = new GeonotesHandler(coordinate.x, coordinate.y, title,
                                            info, PHOTO, timestamp, null, null, null, null);
                                    for( File mFile : fileList ) {
                                        geonotesHandler.addMedia(mFile, mFile.getName());
                                    }

                                    if (fieldBookView != null) {
                                        geonotesHandler.addObserver(fieldBookView);
                                    }
                                    geonotesHandler.notifyObservers(NOTIFICATION.NOTEADDED);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                pm.worked(1);
                            }
                            pm.done();

                            if (nonTakenFilesList.size() > 0) {
                                final StringBuilder sB = new StringBuilder();
                                sB.append("For the following images no gps point within the threshold could be found:\n");
                                for( String p : nonTakenFilesList ) {
                                    sB.append(p).append("\n");
                                }

                                Display.getDefault().asyncExec(new Runnable(){
                                    public void run() {
                                        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                        MessageDialog.openWarning(shell, "Warning", sB.toString());
                                    }
                                });
                            } else {
                                Display.getDefault().asyncExec(new Runnable(){
                                    public void run() {
                                        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                        MessageDialog.openInformation(shell, "Info", "All photos were successfully imported.");
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e1) {
                    e1.printStackTrace();
                    String message = "An error occurred while importing pictures";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e1);
                }
            }
        });

        return true;
    }
    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Photo Import Wizard"); // NON-NLS-1
        setNeedsProgressMonitor(true);
        mainPage = new PhotoImportWizardPage("Import Photos", selection); // NON-NLS-1
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

}
