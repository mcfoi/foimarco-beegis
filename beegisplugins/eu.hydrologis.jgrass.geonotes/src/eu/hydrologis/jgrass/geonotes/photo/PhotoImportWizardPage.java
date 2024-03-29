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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;

/**
 * Photo import wizard page.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PhotoImportWizardPage extends WizardPage implements KeyListener {

    protected DirectoryFieldEditor editor;
    private float timeShift = 0f;
    private Text gpsTimeText;
    private Text photoTimeText;
    private Text folderText;
    private int intervalMinutes = 30;
    private boolean doNotImport = false;
    private boolean createFeatureLayer = true;
    private Text intervalText;

    public PhotoImportWizardPage( String pageName, IStructuredSelection selection ) {
        super(pageName);
        setTitle(pageName); // NON-NLS-1
        setDescription("Import a folder of photos into the application wrapped as geonotes and syncronised with the gps log present in the embedded database."); // NON-NLS-1
        ImageDescriptor imageDescriptorFromPlugin = AbstractUIPlugin.imageDescriptorFromPlugin(GeonotesPlugin.PLUGIN_ID,
                "icons/photo.png");
        setImageDescriptor(imageDescriptorFromPlugin);
    }

    public void createControl( Composite parent ) {
        final Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        fileSelectionArea.setLayoutData(fileSelectionData);

        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 3;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);

        // folder chooser
        Label folderLabel = new Label(fileSelectionArea, SWT.NONE);
        folderLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        folderLabel.setText("Choose photo folder");
        folderText = new Text(fileSelectionArea, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        folderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        folderText.setText("");
        Button folderButton = new Button(fileSelectionArea, SWT.PUSH);
        folderButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        folderButton.setText("...");
        folderButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                DirectoryDialog fileDialog = new DirectoryDialog(fileSelectionArea.getShell(), SWT.OPEN);
                String lastFolderChosen = BeegisUtilsPlugin.getDefault().getLastFolderChosen();
                fileDialog.setFilterPath(lastFolderChosen);
                fileDialog.setText("Choose photo folder");
                String path = fileDialog.open();
                if (path == null || path.length() < 1) {
                    folderText.setText("");
                } else {
                    folderText.setText(path);
                    BeegisUtilsPlugin.getDefault().setLastFolderChosen(path);
                }
            }
        });

        Label intervalLabel = new Label(fileSelectionArea, SWT.NONE);
        intervalLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        intervalLabel.setText("Time threshold [min]");
        intervalLabel.setToolTipText("The time threshold in minutes used to associate a picture to a gps point timestamp");

        intervalText = new Text(fileSelectionArea, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        intervalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        intervalText.setText("30");
        intervalText.addKeyListener(new KeyAdapter(){
            public void keyReleased( KeyEvent e ) {
                String intervalStr = intervalText.getText();
                try {
                    intervalMinutes = (int) Double.parseDouble(intervalStr);
                } catch (Exception ex) {
                    intervalMinutes = 30;
                }
            }
        });

        new Label(fileSelectionArea, SWT.NONE);

        Group timeGroup = new Group(fileSelectionArea, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.horizontalSpan = 3;
        timeGroup.setLayoutData(gd);
        timeGroup.setLayout(new GridLayout(2, false));
        timeGroup.setText("gps and camera time info for syncronisation");

        // time shift between pictures and gps utc time
        Label gpsTimeLabel = new Label(timeGroup, SWT.NONE);
        gpsTimeLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        gpsTimeLabel.setText("gps time [hh:mm]");
        gpsTimeText = new Text(timeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        gpsTimeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        gpsTimeText.setText("");
        gpsTimeText.addKeyListener(this);

        Label photoTimeLabel = new Label(timeGroup, SWT.NONE);
        photoTimeLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        photoTimeLabel.setText("camera time [hh:mm]");
        photoTimeText = new Text(timeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        photoTimeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        photoTimeText.setText("");
        photoTimeText.addKeyListener(this);

        final Button doNotImportButton = new Button(fileSelectionArea, SWT.CHECK);
        GridData doNotImportButtonGD = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
        doNotImportButtonGD.horizontalSpan = 3;
        doNotImportButton.setLayoutData(doNotImportButtonGD);
        doNotImportButton.setText("Do not import the pictures, just add the gps exif tags.");
        doNotImportButton.setSelection(false);
        doNotImportButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                doNotImport = doNotImportButton.getSelection();
            }
        });

        final Button createShapefileButton = new Button(fileSelectionArea, SWT.CHECK);
        GridData createShapefileButtonGD = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
        createShapefileButtonGD.horizontalSpan = 3;
        createShapefileButton.setLayoutData(createShapefileButtonGD);
        createShapefileButton.setText("Also create feature layer for the pictures.");
        createShapefileButton.setSelection(true);
        createShapefileButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                createFeatureLayer = createShapefileButton.getSelection();
            }
        });

        setControl(fileSelectionArea);
    }

    public String getPhotoFolder() {
        return folderText.getText();
    }

    /**
     * @return the timeshift in seconds (gps - camera) or 0.
     */
    public float getTime() {
        return timeShift;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public boolean getDoNotImport() {
        return doNotImport;
    }
    
    public boolean doCreateFeatureLayer() {
        return createFeatureLayer;
    }

    public void keyPressed( KeyEvent e ) {
    }

    public void keyReleased( KeyEvent e ) {
        String gpsText = gpsTimeText.getText();
        String photoText = photoTimeText.getText();

        if (gpsText.length() == 5 && photoText.length() == 5) {
            try {
                String[] gpsSplit = gpsText.split(":");
                String[] photoSplit = photoText.split(":");

                long gpsTime = Long.parseLong(gpsSplit[0]) * 60 * 60 + Long.parseLong(gpsSplit[1]) * 60;
                long photoTime = Long.parseLong(photoSplit[0]) * 60 * 60 + Long.parseLong(photoSplit[1]) * 60;

                timeShift = (float) (gpsTime - photoTime) * 1000f;
                System.out.println(timeShift);
            } catch (Exception ex) {
                // do nothing, just disable time using
                timeShift = 0f;
            }
        } else {
            timeShift = 0f;
        }
    }

}
