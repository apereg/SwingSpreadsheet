package com.apereg24.spreadsheet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.ScrollPaneConstants;

public class Spreadsheet extends JFrame{
	
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

	public Spreadsheet(){
		
		this.setTitle("Hoja de calculo");
		this.setBounds(0,0,1300,444);
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
		mnuGuardar.add (new JSeparator());
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
		
		this.setJMenuBar(mnuBar);
		
		/* Creacion del JDialog que recoge los parametros de inicio de la ejecucion. */
		DialogoParametros params = new DialogoParametros(this);
		this.rows = params.getRows();
		this.cols = params.getCols();
		
		/* Creacion de la tabla con los valores recogidos del JDialog */
		DefaultTableModel model = new DefaultTableModel(){
			@Override
			public boolean isCellEditable(int row, int column) {
				if(row == 0 || column == 0) return false;
				return true;
			}
		};
		table = new JTable(model);
		
		/* Se añaden las filas y columnas con los identificadores */
		JTable tableAux = new JTable(this.rows+1, this.cols+1); // Se crea una tabla aux para obtener los nombres de las columnas
		model.addColumn(tableAux.getColumnName(0));
		for (int i = 0; i <= this.rows; i++) {
			model.addRow(new String[] {""});
			if(i > 0) table.setValueAt(i, i, 0);	
		}
		for (int i = 1; i <= this.cols ; i++) {
			model.addColumn(tableAux.getColumnName(i));
			table.setValueAt(table.getColumnName(i-1), 0, i);
		}
		
		/* Asociacion de la jtable a un scroll */
		table.setTableHeader(null);
		scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pano.add(scroll, BorderLayout.CENTER);
		
		
		/* Creacion de la etiqueta que marca la fila que esta siendo editada */
		editLabel = new JLabel("No se está modificando ninguna celda");
		editLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pano.add(editLabel, BorderLayout.NORTH);
		
		/* Creacion del boton de resolver situado al sur de la interfaz */
		btnResolver = new JButton("Resolver");
		pano.add(btnResolver, BorderLayout.SOUTH);
		
		/* Ultimos retoques de la interfaz */
		getContentPane().add(pano);
		
		/* Accion del boton de resolver */
		btnResolver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				// TODO Comprobar si la hoja de calculo esta llena y resolverla
				
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				String[][] tableToSolve = new String[rows][cols];
				StringBuffer exception = new StringBuffer("");
				for (int i = 0; i < tableToSolve.length; i++) {
					for (int j = 0; j < tableToSolve.length; j++) {
						if(table.getValueAt(i+1, j+1).toString().length() == 0) {
							exception.append("La posici�n " +Solver.getLetter(cols)+ "" +rows+ "est� vacia");
						} else {
							tableToSolve[i][j] = table.getValueAt(i+1, j+1).toString();
						}
					}
				}
				
				int[][] solution = new int[rows][cols];
				if(!exception.toString().isEmpty()) {
					JOptionPane.showMessageDialog(null,exception.toString(), "No se pudo resolver", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						solution = Solver.solveSheet(tableToSolve);
					} catch(SpreadsheetException e2) {
						JOptionPane.showMessageDialog(null,e2.toString(), "No se pudo resolver", JOptionPane.ERROR_MESSAGE);
					}
					for (int i = 0; i < solution.length; i++) {
						for (int j = 0; j < solution.length; j++) {
							model.setValueAt(solution[i][j], i+1, j+1);
						}
					}
				}	
				*/
				System.out.println("Se pulso el calcula pero nanai");
			}		
		});
				
		/* Creacion de cada uno de los listener asociados a cada submenu */
		mnuItemNuevo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				fichero=null;
				mnuItemGuardar.setEnabled (false);
			}
		});

		mnuItemAbrir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				JFileChooser dlg;
				dlg=new JFileChooser();
				dlg.showDialog(null, "Abrir");
				fichero=dlg.getSelectedFile();
				
				//leemos el fichero seleccionado
				FileInputStream in;
				try{
					in=new FileInputStream(fichero);
					BufferedReader br;
					br=new BufferedReader(new InputStreamReader(in));
					//El contenido que vamos leyendo lo metemos en el JTextArea
					while ((br.readLine() ) !=null){
						//Este \n puede ser diferente (/n/r) en Linux
					}
					br.close();
					mnuItemGuardar.setEnabled(true);
				}catch (FileNotFoundException e){
					e.printStackTrace ();
				} catch (IOException e){
					e.printStackTrace ();
				}
			}
		});

		mnuItemSalir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit (0);
			}
		});

		mnuItemDeshacer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			}
		});

		mnuItemRehacer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			}
		});

		mnuItemGuardar.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent arg0){
				try{
					PrintWriter pw;
					pw=new PrintWriter (fichero);
					pw.close ();
				}catch (FileNotFoundException e){
					e.printStackTrace () ;
				}
			}
		});

		mnuItemGuardarComo.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent arg0){
				try{
					JFileChooser dlg;
					dlg=new JFileChooser ();
					dlg.showDialog (null, "guardar como");
					fichero=dlg.getSelectedFile ();
					PrintWriter pw;
					pw=new PrintWriter (fichero);
					pw.close ();
				}catch (FileNotFoundException e){
					e.printStackTrace ();
				}
			}
		});
	}
	
	public static void main(String args[]){
		Spreadsheet te = new Spreadsheet();
		te.setBounds(0,0,400,400);
		te.setVisible(true);
	}
	
}

class DialogoParametros extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	private JTextField tfRows, tfCols;
	
	public DialogoParametros(Spreadsheet spreadsheet) {
		
		super(spreadsheet, "Parametros de la hoja", true);		
		
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
		
		/* Comprobacion al intentar crear la hoja de que todos los parametros son correctos. */
		bCrearHoja.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuffer exception = new StringBuffer("");
				if(!Solver.isANum(tfRows.getText())) exception.append("El valor introducido para las filas no es un valor numérico.\n");
				if(!Solver.isANum(tfCols.getText())) exception.append("El valor introducido para las columnas no es un valor numérico.\n");
				if(!exception.toString().isEmpty()){
					JOptionPane.showMessageDialog(null,exception.toString(), "Parametros incorrectos", JOptionPane.ERROR_MESSAGE);
				} else {
					int tempRows = Integer.parseInt(tfRows.getText());
					int tempCols = Integer.parseInt(tfCols.getText());
					if(Solver.areRowsOk(tempRows) && Solver.areColsOk(tempCols)) {
						dispose();
					} else {
						if(tempRows<0) exception.append("El valor introducido para las filas es demasiado pequeño.\n");
						else if(tempRows > 999) exception.append("El valor introducido para las filas es demasiado grande.\n");
						
						if(tempCols < 0) exception.append("El valor introducido para las columnas es demasiado pequeño.\n");
						else if(tempCols > 18278) exception.append("El valor introducido para las filas es demasiado grande.\n");
						
						JOptionPane.showMessageDialog(null,exception.toString(), "Parametros incorrectos", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		/* El salir hace que la aplicacion se detenga. */
		bSalir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
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
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
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

