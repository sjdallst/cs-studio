package org.csstudio.utility.rocs.ui;

public enum RunOPI {
	DETACHED("Detached"),EDIT("Edit"),ATTACHED("Attached");
	
	private String option;
	private RunOPI(String s){
		option = s;
	}
}
