package org.csstudio.service.rocs;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;

import org.csstudio.utility.rocs.Activator;
import org.csstudio.utility.rocs.PreferenceConstants;
import org.csstudio.utility.rocs.ui.ROCSFactory;
import org.csstudio.utility.rocs.ui.RunOPI;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.epics.pvmanager.WriteFunction;
import org.epics.pvmanager.service.ServiceMethod;
import org.epics.pvmanager.service.ServiceMethodDescription;
import org.epics.util.array.ListNumber;
import org.epics.util.time.Timestamp;
import org.epics.vtype.VEnum;
import org.epics.vtype.VString;
import org.epics.vtype.VTable;




public class TableMethod extends ServiceMethod{

	private final IPreferencesService prefs = Platform.getPreferencesService();

	public TableMethod() {
		super(new ServiceMethodDescription("table", "Fill in templates from a table")
			.addArgument("table", "table to fill templates", VTable.class)	
			.addArgument("option", "run OPI in DETACHED, ATTACHED, EDIT", VString.class));
	    }

	@Override
	public void executeMethod(Map<String, Object> parameters,
			WriteFunction<Map<String, Object>> callback,
			WriteFunction<Exception> errorCallback) {
		
		VString option = ((VString) parameters.get("option"));
		VTable table = ((VTable) parameters.get("table"));
		int columnCount = table.getColumnCount();
		int rowCount = table.getRowCount();
		
		String templateColumn = prefs.getString(Activator.PLUGIN_ID,
				PreferenceConstants.Template_Column,
				"Device", null);

		
		//TreeMap<String,TreeMap<String,String>> devices = new TreeMap<String,TreeMap<String,String>>();
		Map<String, TreeSet<Integer>> devices = new HashMap<String, TreeSet<Integer>>();
		
		ArrayList<XMLPropertyCollectionSet> xmlset = new ArrayList<XMLPropertyCollectionSet>();
		

			for (int j = 0; j < columnCount; j++) {
				if (table.getColumnName(j).equals(templateColumn)){
					if (table.getColumnType(j).equals(String.class)) {
						for (int i = 0; i < rowCount; i++) {
							String templateName = ((List<?>)table.getColumnData(j)).get(i).toString();
							TreeSet<Integer> index;
							if (devices.get(templateName) == null){
								index = new TreeSet<Integer>();
							}else{
								index = devices.get(templateName);
							}
							index.add(i);
							devices.put(templateName, index);
						}
					}	
				}	
			}
			
			for(Map.Entry<String, TreeSet<Integer>> device: devices.entrySet()){
				TreeSet<XMLPropertyCollection> propTreeSet =new TreeSet<XMLPropertyCollection>();
				for (Integer row : device.getValue()) {
					VTableRow tableRow = new VTableRow(table,row);
					Map<String,String> props = new HashMap<String,String>();
					for (int column = 0; column < columnCount; column++) {
						String columnName = table.getColumnName(column);
						Object value = tableRow.getValue(column);
						if (table.getColumnType(column).equals(String.class)){
							props.put(columnName, (String)value);
						} else if (table.getColumnType(column).equals(Integer.TYPE)) {
							props.put(columnName, ((Integer)value).toString());
						} else if (table.getColumnType(column).equals(Double.TYPE)) {
							props.put(columnName, ((Double)value).toString());
						}
					}
					propTreeSet.add(new XMLPropertyCollection(row.toString(),props));
				}
				xmlset.add(new XMLPropertyCollectionSet(device.getKey(),propTreeSet));
			}
				
			
			if (xmlset != null) {
				for ( XMLPropertyCollectionSet entry : xmlset) {
					try{
						JAXBContext jc = JAXBContext.newInstance(XMLPropertyCollectionSet.class, XMLPropertyCollection.class);
						ROCSFactory.create(entry.getId(), new JAXBSource(jc,entry), RunOPI.valueOf(option.getValue()));	
					} catch (Exception e){
						// clean up opi file if it didn't get registered
						errorCallback.writeValue(e);
					}
				}
			}		
		
	}
	public static class VTableRow {
		private final int row;
		private final VTable vTable;
		
		public VTableRow(VTable vTable, int row) {
			this.row = row;
			this.vTable = vTable;
		}
		
		public int getRow() {
			return row;
		}
		
		public VTable getVTable() {
			return vTable;
		}
		
		public Object getValue(int column) {
			if (vTable.getColumnType(column).equals(Integer.TYPE)) {
				return ((ListNumber) vTable.getColumnData(column)).getInt(row);
			} else if (vTable.getColumnType(column).equals(Double.TYPE)) {
				return ((ListNumber) vTable.getColumnData(column)).getDouble(row);
			} else if (vTable.getColumnType(column).equals(String.class)) {
				return ((List<?>) vTable.getColumnData(column)).get(row).toString();
			} else if (vTable.getColumnType(column).equals(Timestamp.class)){
			 	return ((List<?>) vTable.getColumnData(column)).get(row);
		 	} else {
				throw new RuntimeException("Table contain unsupported type " + vTable.getColumnType(column).getName());
			}
		}
		
	}		
}
