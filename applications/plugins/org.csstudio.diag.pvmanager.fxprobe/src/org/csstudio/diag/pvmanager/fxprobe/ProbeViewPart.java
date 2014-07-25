package org.csstudio.diag.pvmanager.fxprobe;

import javafx.embed.swt.FXCanvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class ProbeViewPart extends ViewPart {
	
	 // The ID of the view as specified by the extension point
 	public static final String VIEW_ID = "org.csstudio.diag.pvmanager.fxprobe"; //$NON-NLS-1$
	public static final String ID = "org.csstudio.diag.pvmanager.fxprobe.ProbeViewPart";
	private FXCanvas canvas;
	private JavaFXProbeWidget probe;
	// Next secondary view ID, i.e. next instance of probe should use this number.
 	// SYNC on JavaFXProbe.class for access
 	private static int next_instance = 1;

	@Override
	public void createPartControl(Composite parent) {
		
		
		canvas = new FXCanvas(parent, SWT.NONE) {
            
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                getScene().getWindow().sizeToScene();
                int width = (int) getScene().getWidth();
                int height = (int) getScene().getHeight();
                return new Point(width, height);
            }
    	};
    	
    	probe = new JavaFXProbeWidget(this);
		probe.start();
    	canvas.setScene(probe.getScene());

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	public static String createNewInstance() {
		synchronized (ProbeViewPart.class)
		{
			return Integer.toString(next_instance++);
		}
	}
	
	public void setPVName(String pVName){
    	if(probe.setPVName(pVName)) {
    		setPartName(pVName);
    	}
    }
	
	public void writePVName(String pVName) {
		setPartName(pVName);
	}
	
	@Override
	public void dispose() {
		probe.close();
		super.dispose();
	}

}
