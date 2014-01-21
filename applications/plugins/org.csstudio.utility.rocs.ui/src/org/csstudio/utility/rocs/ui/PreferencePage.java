package org.csstudio.utility.rocs.ui;


import org.csstudio.utility.rocs.PreferenceConstants;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private StringFieldEditor templateDirectories;
	private StringFieldEditor templateColumn;
	private StringFieldEditor instanceColumns;

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE,
				org.csstudio.utility.rocs.Activator.PLUGIN_ID));
		setMessage("ROCS Client Preferences");
		setDescription("ROCS preference page");
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected void createFieldEditors() {
		templateDirectories = new StringFieldEditor(PreferenceConstants.Template_Directories,
				"Template directories (separated by ';'):", getFieldEditorParent());
		templateDirectories.setEmptyStringAllowed(false);
		addField(templateDirectories);
		
		templateColumn = new StringFieldEditor(PreferenceConstants.Template_Column,
				"Column name with Template name:", getFieldEditorParent());
		templateColumn.setEmptyStringAllowed(false);
		addField(templateColumn);
		
		instanceColumns = new StringFieldEditor(PreferenceConstants.Instance_Columns,
				"Column names that make up a unqiue instance (separated by ';'):", getFieldEditorParent());
		instanceColumns.setEmptyStringAllowed(false);
		addField(instanceColumns);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

}
