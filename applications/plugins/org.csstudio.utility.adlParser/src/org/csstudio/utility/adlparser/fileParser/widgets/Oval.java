package org.csstudio.utility.adlparser.fileParser.widgets;

import java.util.ArrayList;

import org.csstudio.utility.adlparser.Activator;
import org.csstudio.utility.adlparser.IImageKeys;
import org.csstudio.utility.adlparser.fileParser.ADLWidget;
import org.csstudio.utility.adlparser.fileParser.WrongADLFormatException;
import org.csstudio.utility.adlparser.fileParser.widgetParts.ADLBasicAttribute;
import org.csstudio.utility.adlparser.fileParser.widgetParts.ADLDynamicAttribute;
import org.csstudio.utility.adlparser.fileParser.widgetParts.ADLObject;

/**
 * 
 * @author hammonds
 *
 */
public class Oval extends ADLAbstractWidget {

	public Oval(ADLWidget adlWidget) {
		super(adlWidget);
		name = new String("oval");
		descriptor = Activator.getImageDescriptor(IImageKeys.ADL_OVAL);
		try {
			for (ADLWidget childWidget : adlWidget.getObjects()) {
	        	if (childWidget.getType().equals("basic attribute")){
	        		_adlBasicAttribute = new ADLBasicAttribute(childWidget);
	        		if (_adlBasicAttribute != null){
	        			_hasBasicAttribute = true;
	        		}
	        	}
	        	else if (childWidget.getType().equals("object")){
	        		_adlObject = new ADLObject(childWidget);
	        		if (_adlObject != null){
	        			_hasObject = true;
	        		}
	        		
	        	}
	        	else if (childWidget.getType().equals("dynamic attribute")){
	        		_adlDynamicAttribute = new ADLDynamicAttribute(childWidget);
	        		if (_adlDynamicAttribute != null){
	        			_hasDynamicAttribute = true;
	        		}
	        	}
	        }
		}
		catch (WrongADLFormatException ex) {
			
		}
	}

	@Override
	public Object[] getChildren() {
		ArrayList<Object> ret = new ArrayList<Object>();
		if (_adlObject != null) ret.add( _adlObject);
		if (_adlBasicAttribute != null) ret.add( _adlBasicAttribute);
		if (_adlDynamicAttribute != null) ret.add( _adlDynamicAttribute);
		return ret.toArray();
	}

}
