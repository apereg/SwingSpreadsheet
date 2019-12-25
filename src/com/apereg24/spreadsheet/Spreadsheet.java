package com.apereg24.spreadsheet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Color;

public class Spreadsheet extends JFrame{
	
	private static final long serialVersionUID = 1L;

	int rows, cols;
	
	JPanel pano;
	JTextArea txt;
	JScrollPane despla;
	JTable table;
	JMenuBar menuBar;
	JMenu mnuModificar, mnuArchivo, mnuGuardar;
	JMenuItem mnuItemNuevo, mnuItemAbrir, mnuItemGuardar, mnuItemGuardarComo, mnuItemSalir;
	JMenuItem mnuItemDeshacer, mnuItemRehacer;
	File fichero;
	private JLabel label;

	public Spreadsheet(){
		
		this.setTitle("Hoja de calculo");
		this.setBounds(0,0,1300,444);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pano = new JPanel();
		pano.setLayout(new GridLayout(0, 1, 0, 0));

		/* Creacion de la menubar y los submenus requeridos */	
		menuBar = new JMenuBar();
		
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
		menuBar.add(mnuArchivo);
		
		mnuModificar = new JMenu("Modificar");
		mnuItemDeshacer = new JMenuItem("Deshacer");
		mnuItemRehacer = new JMenuItem("Rehacer");
		mnuModificar.add(mnuItemDeshacer);
		mnuModificar.add(mnuItemRehacer);
		menuBar.add(mnuModificar);
		
		this.setJMenuBar(menuBar);
		
		/* Creacion del JDialog que recoge los parametros de inicio de la ejecucion. */
		DialogoParametros params = new DialogoParametros(this);
		this.rows = params.getRows();
		this.cols = params.getCols();
		
		/* Creacion de la tabla con los valores recogidos y asignacion de los margenes. */
		table = new JTable(this.rows + 1, this.cols + 1);
		for (int i = 0; i < table.getRowCount(); i++) {
			//TODO Poner no editable azul y en el numero que le toque a cada fila
		}
		for (int i = 0; i < table.getColumnCount(); i++) {
			//TODO Poner no editable azul y la letra que le toque a cada columna
		}
		pano.add(table);
		
		/* Creacion de la etiqueta de control en la parte posterior */
		label = new JLabel("Marcador de que fila esta siendo editada");
		label.setBackground(Color.LIGHT_GRAY);
		label.setFont(new Font("Arial", Font.PLAIN, 12));
		pano.add(label);
		
		/* Asignacion del panel a la ventana */
		getContentPane().add(pano);
		
		/* Creacion de cada uno de los listener asociados a cada submenu */
		mnuItemNuevo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				fichero=null;
				txt.setText (" ");
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
					String linea;
					txt.setText(" ");
					//El contenido que vamos leyendo lo metemos en el JTextArea
					while ((linea=br.readLine() ) !=null){
						//Este \n puede ser diferente (/n/r) en Linux
						txt.append(linea + "\n");
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
				txt.copy();
			}
		});

		mnuItemRehacer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				txt.cut();
			}
		});

		mnuItemGuardar.addActionListener(new ActionListener(){
			public void actionPerformed (ActionEvent arg0){
				try{
					PrintWriter pw;
					pw=new PrintWriter (fichero);
					pw.write (txt.getText());
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
					pw.write (txt.getText () );
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

