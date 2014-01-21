package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelQuery.Result;
import gov.bnl.channelfinder.api.ChannelUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;

import org.csstudio.ui.util.widgets.ErrorBar;
import org.csstudio.utility.pvmanager.widgets.ConfigurableWidget;
import org.csstudio.utility.rocs.Activator;
import org.csstudio.utility.rocs.PreferenceConstants;
import org.csstudio.utility.rocs.ui.ROCSFactory;
import org.csstudio.utility.rocs.ui.RunOPI;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;


public class ROCSWidget extends AbstractChannelQueryResultWidget
		implements ISelectionProvider, ConfigurableWidget {

	private ErrorBar errorBar;

	// Simple Model
	private Collection<Channel> channels = new ArrayList<Channel>();
	private AbstractSelectionProviderWrapper selectionProvider;

	private List<String> properties;
	private List<String> propertyValues;
	private List<String> tags;

	private RunOPI option = RunOPI.DETACHED;
	private final IPreferencesService prefs = Platform.getPreferencesService();
	
	private Collection<XMLPropertyCollectionSet> xmlset;

	public Collection<Channel> getChannels() {
		return channels;
	}
	
	Listener buttonListener = new Listener () { 
		@Override
		public void handleEvent(Event event) {
			buttonSelection((Button)event.widget);
		} 
	};

	private void buttonSelection(Button button) { 
		if (button.getSelection()){ 
			//for selection
			RunOPI oldOption = option;
			option = RunOPI.valueOf(button.getText());
			changeSupport.firePropertyChange("runOptions", oldOption, option);
		} else { 
			//for deselection 
		} 
	} 

	private void setChannels(Collection<Channel> channels) {
		Collection<Channel> oldChannels = this.channels;
		String templateColumn = prefs.getString(Activator.PLUGIN_ID,
				PreferenceConstants.Template_Column,
				"Device", null);
		this.channels = channels;
		if (channels != null) {
			this.properties = new ArrayList<String>(
					ChannelUtil.getPropertyNames(channels));
			this.propertyValues = new ArrayList<String>(
					ChannelUtil.getPropValues(channels, templateColumn));
			this.tags = new ArrayList<String>(
					ChannelUtil.getAllTagNames(channels));
			this.xmlset =  new ArrayList<XMLPropertyCollectionSet>();
			
			for (String propertyValue: propertyValues){
				Collection<Channel> chans = filterByProperty(channels, templateColumn, propertyValue);
				TreeSet<XMLPropertyCollection> propTreeSet =new TreeSet<XMLPropertyCollection>();
				for(Channel chan: chans){
					String id = chan.getProperty("System").getValue()
							+chan.getProperty("SubSystem").getValue()
							+chan.getProperty("D").getValue();
					propTreeSet.add(new XMLPropertyCollection(id,chan.getProperties()));
				}
				
				xmlset.add(new XMLPropertyCollectionSet(propertyValue,propTreeSet));
				
			}
		} else {
			this.properties = Collections.emptyList();
			this.tags = Collections.emptyList();
			this.propertyValues = Collections.emptyList();
			this.xmlset = Collections.emptyList();
		}
		changeSupport.firePropertyChange("channels", oldChannels, channels);
	}

	public Collection<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		List<String> oldProperties = this.properties;
		this.properties = properties;
		changeSupport.firePropertyChange("properties", oldProperties,
				properties);
	}
	
	private Collection<Channel> filterByProperty(Collection<Channel> channels, String propertyName, String propertyValue){
        Collection<Channel> result = new ArrayList<Channel>();
        for (Channel channel : channels) {
            if(channel.getPropertyNames().contains(propertyName)){
            	if(channel.getProperty(propertyName).getValue().equals(propertyValue)){
            		result.add(channel);
            	}
            }
        }
        return result;
    }

	public Collection<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		List<String> oldTags = this.tags;
		this.tags = tags;
		changeSupport.firePropertyChange("tags", oldTags, tags);
	}

	private void updateWidget() {

		if (xmlset != null) {
			for ( XMLPropertyCollectionSet entry : xmlset) {
				try{
					JAXBContext jc = JAXBContext.newInstance(XMLPropertyCollectionSet.class, XMLPropertyCollection.class);
					ROCSFactory.create(entry.getId(), new JAXBSource(jc,entry), option);	
				} catch (Exception e){
					// clean up opi file if it didn't get registered
					errorBar.setException(e);
				}
			}
		}		
	}

	public ROCSWidget(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		
		Group group = new Group(this, SWT.SHADOW_IN);
		group.setText("View Mode for OPI");
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		for (RunOPI option : RunOPI.values()) {
			Button button=new Button(group,SWT.RADIO);
			button.setText(option.name());
			if (buttonListener != null)     button.addListener(SWT.Selection,buttonListener);
		}
		
		if (((Group)getChildren()[0]).getChildren().length > 0)   
			((Button)((Group)getChildren()[0]).getChildren()[0]).setSelection(true);

		errorBar = new ErrorBar(this, SWT.NONE);
		errorBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		errorBar.setMarginBottom(5);
	

		addPropertyChangeListener(new PropertyChangeListener() {

			List<String> properties = Arrays.asList("channels", "properties",
					"tags","runOptions");

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (properties.contains(evt.getPropertyName())) {
					updateWidget();
				}
			}

		});
	}

	@Override
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		//table.setMenu(menu);
	}

	@Override
	protected void queryCleared() {
		this.channels = null;
		this.errorBar.setException(null);
		setChannels(null);
	}

	@Override
	protected void queryExecuted(Result result) {
		Exception e = result.exception;
		errorBar.setException(e);
		if (e == null) {
			setChannels(result.channels);
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionProvider.addSelectionChangedListener(listener);

	}

	@Override
	public ISelection getSelection() {
		return selectionProvider.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionProvider.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		selectionProvider.setSelection(selection);
	}

	private boolean configurable = true;

	private ROCSConfigurationDialog dialog;

	public void openConfigurationDialog() {
		if (dialog != null)
			return;
		dialog = new ROCSConfigurationDialog(this);
		dialog.open();
	}

	@Override
	public boolean isConfigurable() {
		return configurable;
	}

	@Override
	public void setConfigurable(boolean configurable) {
		boolean oldConfigurable = configurable;
		this.configurable = configurable;
		changeSupport.firePropertyChange("configurable", oldConfigurable,
				configurable);
	}

	@Override
	public boolean isConfigurationDialogOpen() {
		return dialog != null;
	}

	@Override
	public void configurationDialogClosed() {
		dialog = null;
	}

}
