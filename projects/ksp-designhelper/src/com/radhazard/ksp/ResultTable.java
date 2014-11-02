package com.radhazard.ksp;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.radhazard.ksp.data.Engine;

public class ResultTable extends JPanel {
	private static final long serialVersionUID = 1L;

	private Engine[] engines;
	
	private ResultTableModel resultTableModel;
	private JTable resultTable;
	
	// preferred table widths 
	static private final int [] prefWidths = {120, 130, 110, 110, 120};

	public ResultTable(Engine[] engines) {
		this.engines = engines.clone();
		
		this.resultTableModel = new ResultTableModel(this.engines.length);
		this.resultTable = new JTable(resultTableModel);

		for (int i = 0; i < resultTable.getColumnCount(); i++) {
			TableColumn column = resultTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(prefWidths[i]);
			column.setResizable(false);
		}
		
		for (int i = 0; i < resultTable.getRowCount(); i++) {
			resultTableModel.setValueAt(this.engines[i].name, i, 0);
		}
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultTableModel);
		this.resultTable.setRowSorter(sorter);
		
		ResultTableCellRenderer resultRenderer = new ResultTableCellRenderer(); 
		this.resultTable.setDefaultRenderer(Integer.class, resultRenderer);
		this.resultTable.setDefaultRenderer(Double.class, resultRenderer);
		
		this.add(this.resultTable.getTableHeader(), BorderLayout.PAGE_START);
		this.add(this.resultTable);
	}

	/**
	 * A custom table model for holding our design data
	 */
	private class ResultTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private final String[] columnNames = {"Name", "Engines Required", "Total Fuel Mass", "Total Ship Mass", "Payload Fraction"};
		private Object[][] data;
		
		ResultTableModel(int numEngines) {
			data = new Object[numEngines][columnNames.length];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class<?> getColumnClass(int col) {
			switch(col) {
			case 0:
				return String.class;
			case 1:
				return Integer.class;
			default:
				return Double.class;
			}
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
	}
	
	/**
	 * A custom table cell renderer that replaces null values
	 * with a center-aligned bar.
	 */
	private class ResultTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public void setValue(Object value) {
			if (value == null) {
				setHorizontalAlignment(JLabel.CENTER);
				setText("-");
			} else {
				setHorizontalAlignment(JLabel.RIGHT);
				setText(value.toString());
			}
		}
	}
	
	/**
	 * Sets the result for an engine to the given data
	 * @param engine
	 * @param res
	 */
	public void setResult(Engine engine, Result res) {
		// Find the engine
		int row = -1;
		for (int i = 0; i < engines.length; i++) {
			if (engines[i] == engine) {
				row = i;
				break;
			}
		}
		if (row == -1) throw new RuntimeException("Engine " + engine.name + " was not in the table");
		
		// Set the table value
		if (res != null) {
			resultTableModel.setValueAt(res.getEnginesRequired(), row, 1);
			resultTableModel.setValueAt(Math.floor(res.getFuelTankMass() * 100) / 100, row, 2);
			resultTableModel.setValueAt(Math.floor(res.getShipMass() * 100) / 100, row, 3);
			resultTableModel.setValueAt(Math.floor(res.getPayloadFraction() * 10000) / 10000, row, 4);
		} else {
			for (int j = 1; j < 5; j++) {
				resultTableModel.setValueAt(null, row, j);
			}
		}
	}
}
