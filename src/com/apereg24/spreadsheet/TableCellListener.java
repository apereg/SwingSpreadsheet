package com.apereg24.spreadsheet;

import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

public class TableCellListener implements PropertyChangeListener, Runnable {
	
	private JTable table;
	private Action action;

	int row;
	int column;
	Object oldValue;
	Object newValue;

	public TableCellListener(JTable table, Action action) {
		this.table = table;
		this.action = action;
		this.table.addPropertyChangeListener(this);
	}

	private TableCellListener(JTable table, int row, int column, Object oldValue, Object newValue) {
		this.table = table;
		this.row = row;
		this.column = column;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public int getColumn() {
		return column;
	}

	public Object getNewValue() {
		return newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public int getRow() {
		return row;
	}

	public JTable getTable() {
		return table;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if ("tableCellEditor".equals(e.getPropertyName())) {
			if (table.isEditing())
				processEditingStarted();
			else
				processEditingStopped();
		}
	}

	private void processEditingStarted() {
		SwingUtilities.invokeLater(this);
	}

	@Override
	public void run() {
		row = table.convertRowIndexToModel(table.getEditingRow());
		column = table.convertColumnIndexToModel(table.getEditingColumn());
		oldValue = table.getModel().getValueAt(row, column);
		newValue = null;
	}

	private void processEditingStopped() {
		newValue = table.getModel().getValueAt(row, column);
		if (!newValue.equals(oldValue)) {
			TableCellListener tcl = new TableCellListener(getTable(), getRow(), getColumn(), getOldValue(),
					getNewValue());
			ActionEvent event = new ActionEvent(tcl, ActionEvent.ACTION_PERFORMED, "");
			action.actionPerformed(event);
		}
	}
}