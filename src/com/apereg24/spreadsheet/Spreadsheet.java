package com.apereg24.spreadsheet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Stack;

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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import java.awt.Font;
import java.awt.Color;

public class Spreadsheet extends JFrame {

	int rows, cols;

	JPanel pano;
	JLabel editLabel;
	JScrollPane scroll;
	JTable table;
	JButton btnResolver;

	Stack<BoxSheet> undoStack = new Stack<BoxSheet>();
	Stack<BoxSheet> redoStack = new Stack<BoxSheet>();

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

		/*
		 * Creacion de la menubar y los submenus requeridos con los shortcuts de acceso
		 */
		mnuBar = new JMenuBar();

		KeyStroke keyStrokeToOpen;
		mnuArchivo = new JMenu("Archivo");
		mnuItemNuevo = new JMenuItem("Nuevo");
		keyStrokeToOpen = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
		mnuItemNuevo.setAccelerator(keyStrokeToOpen);
		mnuItemAbrir = new JMenuItem("Abrir");
		keyStrokeToOpen = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
		mnuItemAbrir.setAccelerator(keyStrokeToOpen);
		mnuGuardar = new JMenu("Guardar");
		mnuItemGuardar = new JMenuItem("Guardar");
		keyStrokeToOpen = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
		mnuItemGuardar.setAccelerator(keyStrokeToOpen);
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
		keyStrokeToOpen = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
		mnuItemDeshacer.setAccelerator(keyStrokeToOpen);
		mnuItemRehacer = new JMenuItem("Rehacer");
		keyStrokeToOpen = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
		mnuItemRehacer.setAccelerator(keyStrokeToOpen);
		mnuModificar.add(mnuItemDeshacer);
		mnuModificar.add(mnuItemRehacer);
		mnuBar.add(mnuModificar);

		/*
		 * Llamada al JDialog de inicializacion y recogida de los parametros de inicio
		 * de la ejecucion.
		 */
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
		table.setCellSelectionEnabled(true);
		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/* Se añaden las filas y columnas con los identificadores */

		/* Se crea una tabla aux para obtener los nombres de las columnas */

		JTable tableAux = new JTable(this.rows + 1, this.cols + 1);

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

		/* Se crea un table colour para pintar los encabezados */
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

		/* Modificador del JLabel segun la casilla seleccionada */
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
		
		table.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int row = -1;
				String col = "Index";
				for (int i = 0; i <= rows; i++) {
					for (int j = 0; j <= cols; j++) {
						if(table.isCellSelected(i, j)) {
							row = i;
							col = table.getColumnName(j);
							break;
						}
						
					}
				}
				if (row == 0 || col == "Index")
					editLabel.setText("Se esta pulsando sobre el encabezado");
				else if(row != -1)
					editLabel.setText("Se esta pulsando sobre la celda " + col + "" + row);
			}

		});

		/* Accion del boton de resolver */
		btnResolver.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (table.isEditing())
					table.getCellEditor().stopCellEditing();
				editLabel.setText("No se está seleccionando ninguna celda");
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				String[][] tableToSolve = new String[rows][cols];
				StringBuffer out = new StringBuffer("");

				for (int i = 1; i <= tableToSolve.length; i++) {
					for (int j = 1; j <= tableToSolve[0].length; j++) {
						if (table.getValueAt(i, j) == null || table.getValueAt(i, j) == "") {
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
				if (table.isEditing())
					table.getCellEditor().stopCellEditing();
				editLabel.setText("No se está seleccionando ninguna celda");
				int resp = JOptionPane.YES_OPTION;
				if (!isEmpty() && !mnuItemGuardar.isEnabled()) {
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
					undoStack.setSize(0);
					redoStack.setSize(0);
				}
			}
		});

		mnuItemAbrir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (table.isEditing())
					table.getCellEditor().stopCellEditing();
				editLabel.setText("No se está seleccionando ninguna celda");
				int resp = JOptionPane.YES_OPTION;
				if (!isEmpty() && !mnuItemGuardar.isEnabled()) {
					resp = JOptionPane.showConfirmDialog(null, "Hoja actual sin guardar.\n¿Quiere continuar?", "Alerta",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				}
				if (resp == JOptionPane.YES_OPTION) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

					int result = fileChooser.showOpenDialog(getParent());
					if (result == JFileChooser.APPROVE_OPTION) {
						File selectedFile = fileChooser.getSelectedFile();
						StringBuffer newSheetBuffer = new StringBuffer("");
						try {
							BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
							String st;
							while ((st = reader.readLine()) != null) {
								newSheetBuffer.append(st).append(" ");
							}
							reader.close();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error durante la lectura de la hoja",
									"No se pudo abrir", JOptionPane.ERROR_MESSAGE);
						}

						String newSheet = newSheetBuffer.toString();
						newSheet = newSheet.replaceAll(" ", ",");
						String[] newSheetArray = newSheet.split(",");

						try {
							if (!Solver.isANum(newSheetArray[0]) || !Solver.isANum(newSheetArray[1]))
								throw new RuntimeException("Filas o columnas incorrectas");
							int tempRows = Integer.parseInt(newSheetArray[1]);
							int tempCols = Integer.parseInt(newSheetArray[0]);
							if (!Solver.areRowsOk(tempRows) || !Solver.areColsOk(tempCols))
								throw new RuntimeException("Filas o columnas incorrectas");

							if (newSheetArray.length - 2 != tempRows * tempCols)
								throw new RuntimeException("Numero de elementos incorrectos");

							redimensionateTable(tempRows, tempCols);
							for (int i = 1; i <= tempRows; i++) {
								for (int j = 1; j <= tempCols; j++) {
									table.setValueAt(newSheetArray[(i - 1) * tempCols + (j - 1) + 2], i, j);
								}
							}
							rows = tempRows;
							cols = tempCols;
							fichero = selectedFile;
							mnuItemGuardar.setEnabled(true);
							undoStack.setSize(0);
							redoStack.setSize(0);
						} catch (RuntimeException e) {
							String exception = "Error durante la lectura de la hoja\n" + e.getMessage();
							JOptionPane.showMessageDialog(null, exception, "No se pudo abrir",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});

		mnuItemGuardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (table.isEditing())
					table.getCellEditor().stopCellEditing();
				editLabel.setText("No se está seleccionando ninguna celda");
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
				if (table.isEditing())
					table.getCellEditor().stopCellEditing();
				editLabel.setText("No se está seleccionando ninguna celda");
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
				if (table.isEditing())
					table.getCellEditor().stopCellEditing();
				editLabel.setText("Se esta pulsando sobre el encabezado");
				System.exit(0);
			}
		});

		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TableCellListener tcl = (TableCellListener) e.getSource();
				if(!(tcl.getRow() == 0 || tcl.getColumn() == 0)) {	
					if (tcl.getOldValue() == null) {
						if(!tcl.getNewValue().equals(""))
							undoStack.push(new BoxSheet("", tcl.getRow(), tcl.getColumn()));	
						
					} else {
						undoStack.push(new BoxSheet(tcl.getOldValue(), tcl.getRow(), tcl.getColumn()));
					}
					if(!redoStack.isEmpty())
						redoStack.setSize(0);				
				}
			}
		};
		TableCellListener tcl = new TableCellListener(table, action);

		mnuItemDeshacer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (undoStack.empty())
						throw new RuntimeException("Nada que deshacer");
					BoxSheet undoable = undoStack.pop();
					BoxSheet toRedo = new BoxSheet(table.getValueAt(undoable.getI(), undoable.getJ()), undoable.getI(), undoable.getJ());
					table.setValueAt(undoable.getValue(), undoable.getI(), undoable.getJ());
					redoStack.push(toRedo);
				} catch (RuntimeException exc) {
					JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

			}

		});

		mnuItemRehacer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (redoStack.empty())
						throw new RuntimeException("Nada que rehacer");
					BoxSheet redoable = redoStack.pop();
					BoxSheet toUndo = new BoxSheet(table.getValueAt(redoable.getI(), redoable.getJ()), redoable.getI(), redoable.getJ());
					table.setValueAt(redoable.getValue(), redoable.getI(), redoable.getJ());
					undoStack.push(toUndo);
				} catch (RuntimeException exc) {
					JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	private String generateFileFormat() {
		StringBuffer out = new StringBuffer();
		boolean celdasVacias = false;
		out.append(this.cols).append(" ").append(this.rows).append("\n");
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
		if (newCols > cols)
			tableAux = new JTable(newRows, newCols);
		else
			tableAux = new JTable(rows, cols);

		DefaultTableModel model = (DefaultTableModel) table.getModel();

		if (newCols > cols) {
			for (int i = cols + 1; i <= newCols; i++) {
				model.addColumn(tableAux.getColumnName(i - 1));
				table.setValueAt(table.getColumnName(i), 0, i);
			}
		} else if (newCols < cols) {
			model.setColumnCount(newCols + 1);
		}

		if (newRows > rows) {
			for (int i = rows + 1; i <= newRows; i++) {
				model.addRow(new String[] { "" });
				table.setValueAt(i, i, 0);
			}
		} else if (newRows < rows) {
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
				table.setValueAt("", i, j);
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

class BoxSheet {

	Object value;

	int i;

	int j;

	public BoxSheet(Object object, int i, int j) {
		this.value = object;
		this.i = i;
		this.j = j;
	}

	public Object getValue() {
		return value;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}
}