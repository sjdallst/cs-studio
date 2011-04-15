package org.csstudio.display.waterfall;

import org.csstudio.csdata.ProcessVariableName;
import org.csstudio.ui.util.AdapterUtil;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Opens the waterfall view.
 * 
 * @author carcassi
 */
public class OpenWaterfall extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getSelection();

			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			WaterfallView waterfall = (WaterfallView) page
					.showView(WaterfallView.ID);
			ProcessVariableName[] pvs = AdapterUtil.convert(selection, ProcessVariableName.class);
			
			if (pvs.length > 0) {
				waterfall.setPVName(pvs[0].getProcessVariableName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}