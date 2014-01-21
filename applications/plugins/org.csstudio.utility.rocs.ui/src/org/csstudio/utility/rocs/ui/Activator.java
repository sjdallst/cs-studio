package org.csstudio.utility.rocs.ui;

import java.util.logging.Logger;

import org.csstudio.utility.rocs.ROCSFromPreferences;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.csstudio.utility.rocs.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static IPropertyChangeListener preferenceListener;
	
	private static Logger log = Logger.getLogger(PLUGIN_ID);
	
	private static ROCSFromPreferences rocsFromPreferences = new ROCSFromPreferences();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		

		preferenceListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {

				// Fetch the instantiated extension and reload the configuration
				// TODO: need to reload only changed preferences (reloading all right now)
				log.info("ROCS property changed = creating new templates");
				rocsFromPreferences.reloadConfiguration();

			}
		};
		org.csstudio.utility.rocs.Activator.getDefault()
				.getPreferenceStore()
				.addPropertyChangeListener(preferenceListener);
		plugin = this;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		org.csstudio.utility.rocs.Activator.getDefault()
				.getPreferenceStore()
				.removePropertyChangeListener(preferenceListener);
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}


}
