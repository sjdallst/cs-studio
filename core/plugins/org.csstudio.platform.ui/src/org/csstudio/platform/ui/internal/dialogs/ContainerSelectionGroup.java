/* 
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton, 
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. 
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND 
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE 
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR 
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. 
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, 
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION, 
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS 
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY 
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.platform.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.platform.ui.CSSPlatformUiPlugin;
import org.csstudio.platform.ui.internal.localization.Messages;
import org.csstudio.platform.ui.util.ImageUtil;
import org.csstudio.platform.util.ResourceUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Workbench-level composite for choosing a container.
 * 
 * <p>
 * <b>Code is based upon
 * <code>org.eclipse.ui.internal.ide.misc.ContainerSelectionGroup</code> in
 * plugin <code>org.eclipse.ui.ide</code>.</b>
 * </p>
 * 
 * @author Alexander Will
 * @version $Revision$
 */
public final class ContainerSelectionGroup extends Composite {
	
	/**
	 * This action is for creating a new folder.
	 * 
	 * @author Kai Meyer
	 *
	 */
	private final class NewFolderAction extends Action {
		
		/**
		 * The Shell.
		 */
		private final Shell _shell;
		
		/**
		 * Constructor.
		 * @param shell
		 * 			The Shell for this Action
		 */
		public NewFolderAction(final Shell shell) {
			_shell = shell;
			this.setText("Create new folder");
			this.setToolTipText("Creates a new folder");
			this.setImageDescriptor(ImageUtil.getInstance().getImageDescriptor(CSSPlatformUiPlugin.ID, "icons/folder.gif")); //$NON-NLS-1$
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void run() {
			final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(getContainerFullPath());
			final StringBuffer buffer = new StringBuffer(Messages.getString("CreateFolderAction.DIALOG_MESSAGE"));
			buffer.append(" (");
			buffer.append(resource.getFullPath());
			buffer.append("/..)");
			final InputDialog inputDialog = new InputDialog(_shell, Messages.getString("CreateFolderAction.DIALOG_TITLE"), //$NON-NLS-1$
					buffer.toString(), "", null);
			final int ret = inputDialog.open();

			if (ret == Window.OK) {
				final String folderName = inputDialog.getValue();
				if (folderName != null) {
					if (resource instanceof IContainer) {
						if (ResourceUtil.getInstance().createFolder((IContainer) resource, folderName)==ResourceUtil.FOLDEREXISTS) {
							MessageDialog.openInformation(_shell, Messages.getString("CreateFolderAction.ERROR_TITLE"), //$NON-NLS-1$
									Messages.getString("CreateFolderAction.ERROR_MESSAGE")); //$NON-NLS-1$
						}
						refreshTree();
					}
				}
			}
		}
	}
	
	/**
	 * This action is for creating a new project.
	 * 
	 * @author Kai Meyer
	 *
	 */
	private final class NewProjectAction extends Action {
		
		/**
		 * The Shell.
		 */
		private final Shell _shell;
		
		/**
		 * Constructor.
		 * @param shell
		 * 			The Shell for this Action
		 */
		public NewProjectAction(final Shell shell) {
			_shell = shell;
			this.setText("Create new project");
			this.setToolTipText("Creates a new project");
			this.setImageDescriptor(ImageUtil.getInstance().getImageDescriptor(CSSPlatformUiPlugin.ID, "icons/project_open.gif"));
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void run() {
			InputDialog inputDialog = new InputDialog(_shell, Messages.getString("CreateProjectAction.DIALOG_TITLE"), //$NON-NLS-1$
					Messages.getString("CreateProjectAction.DIALOG_MESSAGE"), "", null); //$NON-NLS-1$ //$NON-NLS-2$
			int ret = inputDialog.open();

			if (ret == Window.OK) {
				String projectName = inputDialog.getValue();
				if (projectName != null) {
					if (ResourceUtil.getInstance().createProject(projectName)==ResourceUtil.PROJECTEXISTS) {
						MessageDialog.openInformation(_shell, Messages.getString("CreateProjectAction.ERROR_TITLE"), //$NON-NLS-1$
								Messages.getString("CreateProjectAction.ERROR_MESSAGE")); //$NON-NLS-1$
					}
					refreshTree();
				}
			}
		}
	}
	
	
	/**
	 * The listener to notify of events.
	 */
	private Listener _listener;

	/**
	 * Show all projects by default.
	 */
	private boolean _showClosedProjects = true;

	/**
	 * Last selection made by user.
	 */
	private IContainer _selectedContainer;

	/**
	 * The tree widget.
	 */
	private TreeViewer _treeViewer;
	/**
	 * The NewFolderAction.
	 */
	private Action _newFolderAction;
	/**
	 * The NewProjectAction.
	 */
	private Action _newProjectAction;

	/**
	 * Sizing constant for the width of the tree.
	 */
	private static final int SIZING_SELECTION_PANE_WIDTH = 320;

	/**
	 * Sizing constant for the height of the tree.
	 */
	private static final int SIZING_SELECTION_PANE_HEIGHT = 300;

	/**
	 * Creates a new instance of the widget.
	 * 
	 * @param parent
	 *            The parent widget of the group.
	 * @param listener
	 *            A listener to forward events to. Can be null if no listener is
	 *            required.
	 */
	public ContainerSelectionGroup(final Composite parent,
			final Listener listener) {
		this(parent, listener, null);
	}

	/**
	 * Creates a new instance of the widget.
	 * 
	 * @param parent
	 *            The parent widget of the group.
	 * @param listener
	 *            A listener to forward events to. Can be null if no listener is
	 *            required.
	 * @param message
	 *            The text to present to the user.
	 */
	public ContainerSelectionGroup(final Composite parent,
			final Listener listener, final String message) {
		this(parent, listener, message, true);
	}

	/**
	 * Creates a new instance of the widget.
	 * 
	 * @param parent
	 *            The parent widget of the group.
	 * @param listener
	 *            A listener to forward events to. Can be null if no listener is
	 *            required.
	 * @param message
	 *            The text to present to the user.
	 * @param showClosedProjects
	 *            Whether or not to show closed projects.
	 */
	public ContainerSelectionGroup(final Composite parent,
			final Listener listener, final String message,
			final boolean showClosedProjects) {
		this(parent, listener, message, showClosedProjects,
				SIZING_SELECTION_PANE_HEIGHT, SIZING_SELECTION_PANE_WIDTH);
	}

	/**
	 * Creates a new instance of the widget.
	 * 
	 * @param parent
	 *            The parent widget of the group.
	 * @param listener
	 *            A listener to forward events to. Can be null if no listener is
	 *            required.
	 * @param message
	 *            The text to present to the user.
	 * @param showClosedProjects
	 *            Whether or not to show closed projects.
	 * @param heightHint
	 *            height hint for the drill down composite
	 * @param widthHint
	 *            width hint for the drill down composite
	 */
	public ContainerSelectionGroup(final Composite parent,
			final Listener listener, final String message,
			final boolean showClosedProjects, final int heightHint,
			final int widthHint) {
		super(parent, SWT.NONE);
		_listener = listener;
		_showClosedProjects = showClosedProjects;
		if (message != null) {
			createContents(message, heightHint, widthHint);
		} else {
			createContents(Messages.getString("ContainerSelectionGroup.TITLE"), heightHint, widthHint); //$NON-NLS-1$
		}
	}

	/**
	 * The container selection has changed in the tree view. Update the
	 * container name field value and notify all listeners.
	 * 
	 * @param container
	 *            The container that changed
	 */
	public void containerSelectionChanged(final IContainer container) {
		_selectedContainer = container;

		// fire an event so the parent can update its controls
		if (_listener != null) {
			Event changeEvent = new Event();
			changeEvent.type = SWT.Selection;
			changeEvent.widget = this;
			_listener.handleEvent(changeEvent);
		}
	}

	/**
	 * Creates the contents of the composite.
	 * 
	 * @param message
	 *            The text to present to the user.
	 */
	public void createContents(final String message) {
		createContents(message, SIZING_SELECTION_PANE_HEIGHT,
				SIZING_SELECTION_PANE_WIDTH);
	}

	/**
	 * Creates the contents of the composite.
	 * 
	 * @param message
	 *            The text to present to the user.
	 * @param heightHint
	 *            The height of the tree widget.
	 * @param widthHint
	 *            The width of the tree widget.
	 */
	public void createContents(final String message, final int heightHint,
			final int widthHint) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(this, SWT.WRAP);
		label.setText(message);
		label.setFont(getFont());

		createTreeViewer(heightHint);
		Dialog.applyDialogFont(this);
	}
	
	/**
	 * Makes all Actions for this Component.
	 */
	private void makeActions() {
		_newFolderAction = new NewFolderAction(this.getShell());
		_newFolderAction.setEnabled(false);
		_newProjectAction = new NewProjectAction(this.getShell());
	}

	/**
	 * Returns a new drill down viewer for this dialog.
	 * 
	 * @param heightHint
	 *            height hint for the drill down composite
	 */
	protected void createTreeViewer(final int heightHint) {
		// Create drill down.
		DrillDownComposite drillDown = new DrillDownComposite(this, SWT.BORDER);
		GridData spec = new GridData(SWT.FILL, SWT.FILL, true, true);
		spec.widthHint = SIZING_SELECTION_PANE_WIDTH;
		spec.heightHint = heightHint;
		drillDown.setLayoutData(spec);

		// Create tree viewer inside drill down.
		_treeViewer = new TreeViewer(drillDown, SWT.NONE);
		drillDown.setChildTree(_treeViewer);
		ContainerContentProvider cp = new ContainerContentProvider();
		cp.showClosedProjects(_showClosedProjects);
		_treeViewer.setContentProvider(cp);
		_treeViewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		_treeViewer.setSorter(new ViewerSorter());
		_treeViewer.setUseHashlookup(true);
		_treeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(
							final SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
						containerSelectionChanged((IContainer) selection
								.getFirstElement()); // allow null
						if (_newFolderAction!=null) {
							_newFolderAction.setEnabled(selection!=null);
						}
					}
				});
		_treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object item = ((IStructuredSelection) selection)
							.getFirstElement();
					if (item == null) {
						return;
					}
					if (_treeViewer.getExpandedState(item)) {
						_treeViewer.collapseToLevel(item, 1);
					} else {
						_treeViewer.expandToLevel(item, 1);
					}
				}
			}
		});

		// This has to be done after the viewer has been laid out
		_treeViewer.setInput(ResourcesPlugin.getWorkspace());
		this.makeActions();
		this.addAdditionalActions(drillDown.getToolBarManager());
		this.addPopupMenu(_treeViewer);
	}
	
	/**
	 * Adds a NewFolder- and a NewProjectAction to the given ToolbarManager.
	 * @param manager 
	 * 			The ToolBarManager, where the Actions are added
	 */
	private void addAdditionalActions(final ToolBarManager manager) {
		manager.add(new Separator());
		manager.add(_newFolderAction);
		manager.add(_newProjectAction);
		manager.update(true);
	}
	
	/**
	 * Adds a PopupMenu to the given TreeViewer.
	 * @param viewer
	 * 			The TreeViewer, where the PopupMenu is added
	 */
	private void addPopupMenu(final TreeViewer viewer) {
		MenuManager popupMenu = new MenuManager();
		popupMenu.add(_newFolderAction);
		popupMenu.add(_newProjectAction);
		Menu menu = popupMenu.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
	}
	
	/**
	 * Refreshes the Tree in an async-thread.
	 */
	private void refreshTree() {
		_treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
			public void run() {
				_treeViewer.refresh();
			}
		});
	}

	/**
	 * Returns the currently entered container name. Null if the field is empty.
	 * Note that the container may not exist yet if the user entered a new
	 * container name in the field.
	 * 
	 * @return IPath
	 */
	public IPath getContainerFullPath() {
		if (_selectedContainer == null) {
			return null;
		}
		return _selectedContainer.getFullPath();

	}

	/**
	 * Gives focus to one of the widgets in the group, as determined by the
	 * group.
	 */
	public void setInitialFocus() {
		_treeViewer.getTree().setFocus();
	}

	/**
	 * Sets the selected existing container.
	 * 
	 * @param container
	 *            The selected existing container.
	 */
	@SuppressWarnings("unchecked")
	public void setSelectedContainer(final IContainer container) {
		_selectedContainer = container;

		// expand to and select the specified container
		List itemsToExpand = new ArrayList();
		IContainer parent = container.getParent();
		while (parent != null) {
			itemsToExpand.add(0, parent);
			parent = parent.getParent();
		}
		_treeViewer.setExpandedElements(itemsToExpand.toArray());
		_treeViewer.setSelection(new StructuredSelection(container), true);
	}
}
