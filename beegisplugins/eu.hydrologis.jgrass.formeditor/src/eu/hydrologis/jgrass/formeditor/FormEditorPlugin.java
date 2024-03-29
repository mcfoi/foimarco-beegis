package eu.hydrologis.jgrass.formeditor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ILayerListener;
import net.refractions.udig.project.IMapListener;
import net.refractions.udig.project.IStyleBlackboard;
import net.refractions.udig.project.LayerEvent;
import net.refractions.udig.project.LayerEvent.EventType;
import net.refractions.udig.project.MapEvent;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.internal.MapPart;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.featureeditor.utils.ISelectionObserver;
import eu.hydrologis.jgrass.featureeditor.utils.Utilities;
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;

/**
 * The activator class controls the plug-in life cycle
 */
public class FormEditorPlugin extends AbstractUIPlugin implements IPartListener2, IMapListener, ILayerListener {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.formeditor"; //$NON-NLS-1$

    // The shared instance
    private static FormEditorPlugin plugin;

    /**
     * The constructor
     */
    public FormEditorPlugin() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;

    }

    /**
     * Registers listeners, called from earlystartup, to make sure the workbench is alive.
     */
    public void registerPartListener() {
        if (page != null)
            return;
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null)
            return;
        page = activeWorkbenchWindow.getActivePage();
        if (page != null) {
            page.addPartListener(this);

            // add close listener for the form editor
            workbench.addWorkbenchListener(new IWorkbenchListener(){
                public boolean preShutdown( IWorkbench workbench, boolean forced ) {
                    IEditorReference[] editorArray = page.getEditorReferences();
                    for( IEditorReference editorReference : editorArray ) {
                        IEditorPart editor = editorReference.getEditor(false);
                        if (editor instanceof FormEditor) {
                            page.closeEditor(editor, false);
                        }
                    }
                    return true;
                }

                public void postShutdown( IWorkbench workbench ) {
                }
            });

        }
    }
    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        if (page != null) {
            page.removePartListener(this);
        }

        ImageCache.getInstance().dispose();

        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static FormEditorPlugin getDefault() {
        return plugin;
    }

    private Map activeMap;
    private ILayer selectedLayer;
    private SimpleFeature lastSelectedFeature;

    private IWorkbenchPage page;

    private void setActiveMap( Map map ) {
        if (map == null) {
            if (activeMap != null) {
                activeMap.removeMapListener(this);
                activeMap = null;
            }
            return;
        }
        if (!map.equals(activeMap)) {
            if (activeMap != null)
                activeMap.removeMapListener(this);
            activeMap = map;
            activeMap.addMapListener(this);

            ILayer tmpSelectedLayer = map.getEditManager().getSelectedLayer();
            if (tmpSelectedLayer != null) {
                selectedLayer = tmpSelectedLayer;
                Filter filter = selectedLayer.getFilter();
                LayerImpl layerImpl = (LayerImpl) selectedLayer;

                SimpleFeature selectedFeature = getSelectedFeature(layerImpl, filter);
                if (lastSelectedFeature == null || !lastSelectedFeature.equals(selectedFeature)) {
                    lastSelectedFeature = selectedFeature;
                } else {
                    return;
                }
            }
        }
        notifySelectionListeners();
    }

    public void partActivated( IWorkbenchPartReference partRef ) {
        registerPartListener();
        // make this the active map(if it is a MapPart)
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof MapPart) {
            setActiveMap(((MapPart) part).getMap());
        }
    }

    public void partClosed( IWorkbenchPartReference partRef ) {
        // if active map then make previous map be the active map
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof MapPart) {
            setActiveMap(null);
        }
    }

    public void partVisible( IWorkbenchPartReference partRef ) {
        registerPartListener();
        // if no active map then make this the active map (if it is a MapPart)
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof MapPart) {
            setActiveMap(((MapPart) part).getMap());
        }
    }

    public void partHidden( IWorkbenchPartReference partRef ) {
    }

    public void partOpened( IWorkbenchPartReference partRef ) {
        registerPartListener();
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof MapPart) {
            setActiveMap(((MapPart) part).getMap());
        }
    }

    public void partBroughtToTop( IWorkbenchPartReference partRef ) {
    }

    public void partDeactivated( IWorkbenchPartReference partRef ) {
    }

    public void partInputChanged( IWorkbenchPartReference partRef ) {
    }
    /* 
     * Map events.
     * 
     * @see net.refractions.udig.project.IMapListener#changed(net.refractions.udig.project.MapEvent)
     */
    public void changed( MapEvent event ) {
        registerPartListener();
        ILayer tmpSelectedLayer = activeMap.getEditManager().getSelectedLayer();
        if (tmpSelectedLayer == null) {
            return;
        }
        if (tmpSelectedLayer != selectedLayer || lastSelectedFeature == null) {
            // layer has changed, reset listeners
            if (selectedLayer != null) {
                selectedLayer.removeListener(this);
            }
            selectedLayer = tmpSelectedLayer;
            selectedLayer.addListener(this);

            Filter filter = selectedLayer.getFilter();
            LayerImpl layerImpl = (LayerImpl) selectedLayer;

            SimpleFeature selectedFeature = getSelectedFeature(layerImpl, filter);
            if (lastSelectedFeature == null || !lastSelectedFeature.equals(selectedFeature)) {
                lastSelectedFeature = selectedFeature;
            } else {
                return;
            }

            notifySelectionListeners();
        }
    }

    /* 
     * Layer events.
     * 
     * @see net.refractions.udig.project.ILayerListener#refresh(net.refractions.udig.project.LayerEvent)
     */
    public void refresh( LayerEvent event ) {
        selectedLayer = event.getSource();
        EventType type = event.getType();
        Object newValue = event.getNewValue();
        Object oldValue = event.getOldValue();
        if (newValue != null && (newValue.equals(oldValue) || type == EventType.EDIT_EVENT)) {
            return;
        }
        if (type == EventType.FILTER) {
            Filter filter = selectedLayer.getFilter();
            LayerImpl layerImpl = (LayerImpl) selectedLayer;

            SimpleFeature selectedFeature = getSelectedFeature(layerImpl, filter);
            if (lastSelectedFeature == null || !lastSelectedFeature.equals(selectedFeature)) {
                lastSelectedFeature = selectedFeature;
            } else {
                return;
            }
        }
        notifySelectionListeners();
    }

    @SuppressWarnings("unchecked")
    private SimpleFeature getSelectedFeature( LayerImpl layerImpl, Filter filter ) {
        try {
            FeatureSource<SimpleFeatureType, SimpleFeature> resource = layerImpl.getResource(FeatureSource.class,
                    new NullProgressMonitor());
            if (resource == null) {
                return null;
            }
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = resource.getFeatures(filter);
            if (!features.isEmpty()) {
                FeatureIterator<SimpleFeature> featureIterator = null;
                try {
                    featureIterator = features.features();
                    if (featureIterator.hasNext()) {
                        SimpleFeature tmpSelectedFeature = featureIterator.next();
                        return tmpSelectedFeature;
                    }
                } finally {
                    featureIterator.close();
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<ISelectionObserver> observers = new ArrayList<ISelectionObserver>();
    public void addSelectionListener( ISelectionObserver observer ) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    public void removeSelectionListener( ISelectionObserver observer ) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    public void notifySelectionListeners() {
        for( ISelectionObserver observer : observers ) {
            observer.selectionChanged(activeMap, selectedLayer, lastSelectedFeature);
        }
    }

    public File getFormFile( ILayer layer ) {
        ID id = layer.getGeoResource().getID();
        File formFile = null;
        if (id.isFile()) {
            File file = id.toFile();
            formFile = Utilities.getFormFile(file);
        } else {
            // if it is not a file, it has to have the file
            // reference in the blackboard
            IStyleBlackboard blackboard = layer.getStyleBlackboard();
            String path = blackboard.getString(FormEditor.ID);
            if (path != null) {
                formFile = new File(path);
            }
        }
        return formFile;
    }

    /**
     * The permitted text length in textfields.
     */
    private int textLimit = 254;
    private String PREF_KEY_TEXTLIMIT = PLUGIN_ID + ".textlimit";

    public int getTextLimit() {
        int tmp = getPreferenceStore().getInt(PREF_KEY_TEXTLIMIT);
        if (tmp != 0) {
            textLimit = tmp;
        } else {
            textLimit = 254;
        }
        return textLimit;
    }

    public void setTextLimit( int textLimit ) {
        getPreferenceStore().setValue(PREF_KEY_TEXTLIMIT, textLimit);
        this.textLimit = textLimit;
    }
}
