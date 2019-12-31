package com.apereg24.spreadsheet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.management.RuntimeErrorException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.Font;
import java.awt.Color;

public class Spreadsheet extends JFrame {

	int rows, cols;

	JPanel pano;
	JLabel editLabel;
	JScrollPane scroll;
	JTable table;
	JButton btnResolver;

	JScrollPane desplaLateral, desplaHorizontal;

	JMenuBar mnuBar;
	JMenu mnuModificar, mnuArchivo, mnuGuardar;
	JMenuItem mnuItemNuevo, mnuItemAbrir, mnuItemGuardar, mnuItemGuardarComo, mnuItemSalir;
	JMenuItem mnuItemDeshacer, mnuItemRehacer;

	File fichero;

	public static void main(String args[]) {
		Spreadsheet app = new Spreadsheet();
        app.setExtendedState(MAXIMIZED_BOTH);
		app.setVisible(true);
	}

	public Spreadsheet() {

		this.setTitle("Hoja de calculo");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pano = new JPanel();
		pano.setAutoscrolls(true);
		pano.setLayout(new BorderLayout(0, 0));

		/* Creacion de la menubar y los submenus requeridos */
		mnuBar = new JMenuBar();

		mnuArchivo = new JMenu("Archivo");
		mnuItemNuevo = new JMenuItem("Nuevo");
		mnuItemAbrir = new JMenuItem("Abrir");
		mnuGuardar = new JMenu("Guardar");
		mnuItemGuardar = new JMenuItem("Guardar");
		mnuItemGuardar.setEnabled(false);
		mnuItemGuardarComo = new JMenuItem("Guardar como");
		mnuGuardar.add(mnuItemGuardar);
		mnuGuardar.add(mnuItemGuardarComo);
		mnuGuardar.add(new JSeparator());
		mnuItemSalir = new JMenuItem("Salir");
		mnuArchivo.add(mnuItemNuevo);
		mnuArchivo.add(mnuItemAbrir);
		mnuArchivo.add(mnuGuardar);
		mnuArchivo.add(mnuItemSalir);
		mnuBar.add(mnuArchivo);

		mnuModificar = new JMenu("Modificar");
		mnuItemDeshacer = new JMenuItem("Deshacer");
		mnuItemRehacer = new JMenuItem("Rehacer");
		mnuModificar.add(mnuItemDeshacer);
		mnuModificar.add(mnuItemRehacer);
		mnuBar.add(mnuModificar);

		/* Creacion del JDialog que recoge los parametros de inicio de la ejecucion. */
		DialogoParametros params = new DialogoParametros(this);
		this.rows = params.getRows();
		this.cols = params.getCols();

		/* Creacion de la tabla con los valores recogidos del JDialog */
		DefaultTableModel model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				if (row == 0 || column == 0)
					return false;
				return true;
			}
		};
		table = new JTable(model);
		table.setFont(new Font("Arial", Font.PLAIN, 12));
		// table.setDefaultRenderer(Object.class, new GradeRenderer());

		/* Se añaden las filas y columnas con los identificadores */
		JTable tableAux = new JTable(this.rows + 1, this.cols + 1); // Se crea una tabla aux para obtener los nombres de
																	// las columnas
		model.addColumn("Index");
		for (int i = 0; i <= this.rows; i++) {
			model.addRow(new String[] { "" });
			if (i > 0)
				table.setValueAt(i, i, 0);
		}
		for (int i = 1; i <= this.cols; i++) {
			model.addColumn(tableAux.getColumnName(i - 1));
			table.setValueAt(table.getColumnName(i), 0, i);
		}

		final TableColour tce = new TableColour();
		for (int i = 0; i <= this.cols; i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(tce);
		}

		/* Asociacion de la jtable a un scroll */
		table.setTableHeader(null);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.doLayout();
		scroll = new JScrollPane(table);
		pano.add(scroll, BorderLayout.CENTER);

		/* Creacion de la etiqueta que marca la fila que esta siendo editada */
		editLabel = new JLabel("No se está seleccionando ninguna celda");
		editLabel.setBackground(Color.ORANGE);
		editLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		editLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pano.add(editLabel, BorderLayout.NORTH);

		/* Creacion del boton de resolver situado al sur de la interfaz */
		btnResolver = new JButton("Resolver");
		btnResolver.setBackground(Color.LIGHT_GRAY);
		btnResolver.setFont(new Font("Arial", Font.BOLD, 16));
		pano.add(btnResolver, BorderLayout.SOUTH);

		/* Ultimos retoques de la interfaz */
		this.setJMenuBar(mnuBar);
		getContentPane().add(pano);
		/*
		 * Listener que modifica el label segun la celda sobre la que se esta
		 * seleccionando
		 */
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = table.rowAtPoint(evt.getPoint());
				String col = table.getColumnName(table.columnAtPoint(evt.getPoint()));
				if (row == 0 || col == "Index")
					editLabel.setText("Se esta pulsando sobre el encabezado");
				else
					editLabel.setText("Se esta pulsando sobre la celda " + col + "" + row);
			}
		});
		/* Accion del boton de resolver */
		btnResolver.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				String[][] tableToSolve = new String[rows][cols];
				StringBuffer out = new StringBuffer("");

				for (int i = 1; i <= tableToSolve.length; i++) {
					for (int j = 1; j <= tableToSolve[0].length; j++) {
						if (table.getValueAt(i, j) == null) {
							out.append("La posicion " + Solver.getLetter(i) + "" + j + " esta vacia\n");
							table.setValueAt("0", i, j);
							tableToSolve[i - 1][j - 1] = "0";
						} else {
							tableToSolve[i - 1][j - 1] = table.getValueAt(i, j) + "";
						}
					}
				}

				int[][] solution = new int[rows][cols];
				if (!out.toString().isEmpty()) {
					if (out.toString().length() > 116) {
						out.setLength(0);
						out.append("Varias celdas estan vacias.\n");
						out.append("Se sustituiran por 0.\n");
					} else if (out.toString().length() >= 25 && out.toString().length() <= 29) {
						out.append("Se sustituira por 0.\n");
					} else {
						out.append("Se sustituiran por 0.\n");
					}
					JOptionPane.showMessageDialog(null, out.toString(), "Celdas vacias", JOptionPane.WARNING_MESSAGE);
				}

				try {
					Solver solver = new Solver(rows, cols, tableToSolve);
					solver.resolve();
					solution = solver.getSolution();
					for (int i = 1; i <= solution.length; i++) {
						for (int j = 1; j <= solution[0].length; j++) {
							model.setValueAt(solution[i - 1][j - 1], i, j);
						}
					}
				} catch (SpreadsheetException e2) {
					JOptionPane.showMessageDialog(null, e2.getMessage(), "No se pudo resolver",
							JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		/* Creacion de cada uno de los listener asociados a cada submenu */
		mnuItemNuevo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();
				int resp = JOptionPane.YES_OPTION;
				if(!isEmpty() && !mnuItemGuardar.isEnabled()){
					resp = JOptionPane.showConfirmDialog(null, "Hoja actual sin guardar.\n¿Quiere continuar?", "Alerta",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				}
				if (resp == JOptionPane.YES_OPTION) {
					fichero = null;
					mnuItemGuardar.setEnabled(false);
					int[] newParams = askForParameters();
					int newRows = newParams[0];
					int newCols = newParams[1];
					redimensionateTable(newRows, newCols);
				}
			}
		});

		mnuItemAbrir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				int result = fileChooser.showOpenDialog(getParent());
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    ArrayList<String> newSheet = new ArrayList<String>();
				    try {
						BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
						while(reader.readLine() != null) {
							newSheet.add(reader.readLine()); //TODO Se lee mal, no todas las lineas // TODO usar strinbuffer
						}
						reader.close();
				    }catch (Exception e){
				    	JOptionPane.showMessageDialog(null, "Error durante la lectura de la hoja", "No se pudo abrir", JOptionPane.ERROR_MESSAGE);
					}
				    
				    for (int k = 0; k < newSheet.size(); k++) {
						System.out.println(newSheet.get(k));
					}
				    
				    try {
				    	for (int k = 0; k < newSheet.size(); k++) {
							newSheet.get(k).replaceAll(" ", ",");
						}
				    	if(newSheet.isEmpty()) throw new RuntimeException("Fichero vacio");
				    	if(newSheet.get(0).split(",").length != 2) throw new RuntimeException("Filas o columnas incorrectas");
				    	if(!Solver.isANum(newSheet.get(0).split(",")[0]) && !Solver.isANum(newSheet.get(0).split(",")[1])) throw new RuntimeException("Filas o columnas incorrectas");
				    	
				    	int tempRows = Integer.parseInt(newSheet.get(0).split(" ")[0]);
				    	int tempCols = Integer.parseInt(newSheet.get(0).split(" ")[1]);
				    	if(!Solver.areRowsOk(tempRows) || !Solver.areColsOk(tempCols)) throw new RuntimeException("Filas o columnas incorrectas");
				 
				    	newSheet.remove(0);
				    	String[][] newContent = new String[tempRows][tempCols];
				    	if(newSheet.size() != tempRows) throw new RuntimeException("Filas incorrectas");
				    	for (int i = 0; i < tempRows; i++) {
							if(newSheet.get(i).split(" ").length != tempCols) throw new RuntimeException("Columnas incorrectas");
							for (int j = 0; j < tempCols; j++) {
								newContent[i][j] = newSheet.get(i).split(",")[j];
							}
						}
				    	/* Si se ha llegado aqui todo es correcto. */
					    redimensionateTable(tempRows, tempCols);
					    for (int i = 0; i <= newContent.length; i++) {
							for (int j = 0; j <= newContent[0].length; j++) {
								table.setValueAt(newContent[i+1][j+1], i, j);
							}
						}
					    fichero = selectedFile;
				    }catch(RuntimeException e) {
				    	String exception = "Error durante la lectura de la hoja\n" + e.getMessage();
				    	JOptionPane.showMessageDialog(null, exception, "No se pudo abrir", JOptionPane.ERROR_MESSAGE);
				    }
				    
				    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
				}
			}
		});

		mnuItemGuardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();
				try {
					PrintWriter pw;
					pw = new PrintWriter(fichero);
					pw.write(generateFileFormat());
					pw.close();
					String message = "Se ha guardado correctamente en " + fichero.toString();
					JOptionPane.showMessageDialog(null, message, "Guardado correctamente",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "Fichero no encontrado", "Error inesperado",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		mnuItemGuardarComo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();
				try {
					JFileChooser dlg;
					dlg = new JFileChooser();
					dlg.showDialog(null, "Guardar Como");
					fichero = dlg.getSelectedFile();
					PrintWriter pw;
					pw = new PrintWriter(fichero);
					pw.write(generateFileFormat());
					pw.close();
					mnuItemGuardar.setEnabled(true);
					String message = "Se ha guardado correctamente en " + fichero.toString();
					JOptionPane.showMessageDialog(null, message, "Guardado correctamente",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "Fichero no encontrado", "Error inesperado",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		mnuItemSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();
				System.exit(0);
			}
		});

		mnuItemDeshacer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();	
			}
		});

		mnuItemRehacer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(table.isEditing()) table.getCellEditor().stopCellEditing();			
			}
		});
	}

	private String generateFileFormat() {
		StringBuffer out = new StringBuffer();
		boolean celdasVacias = false;
		out.append(this.rows).append(" ").append(this.cols).append("\n");
		for (int i = 1; i <= this.rows; i++) {
			for (int j = 1; j <= this.cols; j++) {
				if (table.getValueAt(i, j) == null) {
					celdasVacias = true;
					out.append("0");
				} else {
					out.append(table.getValueAt(i, j));
				}

				if (j == this.cols)
					out.append("\n");
				else
					out.append(" ");
			}
		}

		if (celdasVacias) {
			JOptionPane.showMessageDialog(null, "Las celdas vacias se sustituiran por 0's", "Celdas vacias",
					JOptionPane.WARNING_MESSAGE);
		}

		return out.toString();
	}

	private boolean isEmpty() {
		for (int i = 1; i <= this.rows; i++)
			for (int j = 1; j <= this.cols; j++)
				if (!(table.getValueAt(i, j) == null))
					return false;
		return true;
	}

	private int[] askForParameters() {
		DialogoParametros params = new DialogoParametros(this);
		return new int[] { params.getRows(), params.getCols() };
	}
	
	private void redimensionateTable(int newRows, int newCols) {
		JTable tableAux;
		if(newCols > cols) tableAux = new JTable(newRows, newCols);
		else tableAux = new JTable(rows, cols);
		
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		
		if(newCols > cols) {
			for (int i = cols + 1; i <= newCols; i++) {
				model.addColumn(tableAux.getColumnName(i - 1));
				table.setValueAt(table.getColumnName(i), 0, i);
			}
		} else if(newCols < cols){
			model.setColumnCount(newCols+1);
		}
		
		if(newRows > rows) {
			for (int i = rows + 1; i <= newRows; i++) {
				model.addRow(new String[] { "" });
				table.setValueAt(i, i, 0);
			}
		} else if(newRows < rows) {
			model.setRowCount(newRows + 1);
		}
		final TableColour tce = new TableColour();
		for (int i = 0; i <= newCols; i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(tce);
		}
		
		this.rows = newRows;
		this.cols = newCols;
		
		for (int i = 1; i <= this.rows; i++) {
			for (int j = 1; j <= this.cols; j++) {
				table.setValueAt(" ", i, j);
			}
		}
		
	}

}

class DialogoParametros extends JDialog {

	private JTextField tfRows, tfCols;

	public DialogoParametros(Spreadsheet spreadsheet) {

		super(spreadsheet, "Parametros de la hoja", true);

		/* Colocacion de todos los componentes del Verifier */
		JPanel panelBotones = new JPanel();
		JPanel panelIzdo = new JPanel();
		JPanel panelDerecho = new JPanel();
		panelIzdo.setLayout(new BoxLayout(panelIzdo, BoxLayout.Y_AXIS));
		panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
		JButton bCrearHoja = new JButton("Crear");
		JButton bSalir = new JButton("Cancelar");
		tfRows = new JTextField(5);
		tfRows.setMaximumSize(tfRows.getPreferredSize());
		tfCols = new JTextField(5);
		tfCols.setMaximumSize(tfCols.getPreferredSize());
		panelIzdo.add(new JLabel("Filas"));
		panelIzdo.add(Box.createRigidArea(new Dimension(10, 5)));
		panelIzdo.add(new JLabel("Columnas"));
		panelDerecho.add(tfRows);
		panelDerecho.add(Box.createRigidArea(new Dimension(10, 5)));
		panelDerecho.add(tfCols);
		panelBotones.add(bCrearHoja);
		panelBotones.add(bSalir);
		getContentPane().add(panelIzdo, BorderLayout.WEST);
		getContentPane().add(panelDerecho, BorderLayout.EAST);
		getContentPane().add(panelBotones, BorderLayout.SOUTH);

		/*
		 * Comprobacion al intentar crear la hoja de que todos los parametros son
		 * correctos.
		 */
		bCrearHoja.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuffer exception = new StringBuffer("");
				if (!Solver.isANum(tfRows.getText()))
					exception.append("El valor introducido para las filas no es un valor numérico.\n");
				if (!Solver.isANum(tfCols.getText()))
					exception.append("El valor introducido para las columnas no es un valor numérico.\n");
				if (!exception.toString().isEmpty()) {
					JOptionPane.showMessageDialog(null, exception.toString(), "Parametros incorrectos",
							JOptionPane.ERROR_MESSAGE);
				} else {
					int tempRows = Integer.parseInt(tfRows.getText());
					int tempCols = Integer.parseInt(tfCols.getText());
					if (Solver.areRowsOk(tempRows) && Solver.areColsOk(tempCols)) {
						dispose();
					} else {
						if (tempRows < 0)
							exception.append("El valor introducido para las filas es demasiado pequeño.\n");
						else if (tempRows > 999)
							exception.append("El valor introducido para las filas es demasiado grande.\n");

						if (tempCols < 0)
							exception.append("El valor introducido para las columnas es demasiado pequeño.\n");
						else if (tempCols > 18278)
							exception.append("El valor introducido para las filas es demasiado grande.\n");

						JOptionPane.showMessageDialog(null, exception.toString(), "Parametros incorrectos",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		/* El salir hace que la aplicacion se detenga. */
		bSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		/* Cerrar este cuadro de dialogo tambien hace que la aplicacion se detenga. */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		/* Configuraciones finales del dialogo. */
		((BorderLayout) getContentPane().getLayout()).setHgap(5);
		((BorderLayout) getContentPane().getLayout()).setVgap(5);
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
	}

	public int getRows() {
		return Integer.parseInt(this.tfRows.getText());
	}

	public int getCols() {
		return Integer.parseInt(this.tfCols.getText());
	}

}

class TableColour extends javax.swing.table.DefaultTableCellRenderer {

	@Override
	public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, java.lang.Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		java.awt.Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
				column);
		if (row == 0) {
			cellComponent.setBackground(new java.awt.Color(204, 204, 204));
			cellComponent.setFont(new Font("Arial", Font.BOLD, 12));
		} else if (column == 0) {
			cellComponent.setBackground(new java.awt.Color(162, 196, 201));
			cellComponent.setFont(new Font("Arial", Font.BOLD, 12));
		} else if (row % 2 == 0) {
			cellComponent.setBackground(new java.awt.Color(243, 243, 243));
		} else {
			cellComponent.setBackground(java.awt.Color.WHITE);
		}

		return cellComponent;
	}

}