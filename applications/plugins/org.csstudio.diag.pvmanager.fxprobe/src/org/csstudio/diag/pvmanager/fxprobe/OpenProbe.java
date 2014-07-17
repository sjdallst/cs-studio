package org.csstudio.diag.pvmanager.fxprobe;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swt.FXCanvas;

import javax.swing.SwingUtilities;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.ui.util.AdapterUtil;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler for opening probe on the current selection.
 * 
 * @author carcassi
 * @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class OpenProbe extends AbstractHandler implements IHandler
{
	
    /** {@inheritDoc} */
    @Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
	    final Shell shell = HandlerUtil.getActiveShell(event);
		try
		{
			// Retrieve the selection and the current page
			final ISelection selection = HandlerUtil.getActiveMenuSelection(event);
			final ProcessVariable[] pvs = AdapterUtil.convert(selection,
                    ProcessVariable.class);
			
			final IWorkbenchPage page;
			final IWorkbenchSite site = HandlerUtil.getActiveSite(event);
			if (site != null)
			    page = site.getPage();
			else // When all panels are closed, the event's site would be null
			    page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			// Were PVs provided (opened from context menu?)
			if (pvs == null  ||  pvs.length <= 0)
			    openProbe(page);
			else
			{
			    // Confirm if this could open many new probe instances
			    if (pvs.length > 5 &&
			        ! MessageDialog.openConfirm(shell,
			                Messages.MultipleInstancesTitle,
			                NLS.bind(Messages.MultipleInstancesFmt, pvs.length))
	                )
	                return null;
			    
			    for (ProcessVariable pv : pvs)
			    {   // One instance per PV
			        ProbeViewPart probe = openProbe(page);
			        probe.setPVName(pv.getName());
			    }
			}
		}
		catch (Exception ex)
		{
            ExceptionDetailsErrorDialog.openError(shell,
	            Messages.Probe_errorOpenProbe, ex);
		}
		return null;
	}
	
    /** @param page {@link IWorkbenchPage}on which to open Probe
     *  @return {@link PVManagerProbe} that was opened
     *  @throws Exception on error
     */
	private ProbeViewPart openProbe(final IWorkbenchPage page) throws Exception
	{
	    return (ProbeViewPart) page.showView(
	    		ProbeViewPart.VIEW_ID,
	    		ProbeViewPart.createNewInstance(),
                IWorkbenchPage.VIEW_ACTIVATE);
	}
}
