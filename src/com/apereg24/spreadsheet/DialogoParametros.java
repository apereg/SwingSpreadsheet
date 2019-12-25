package com.apereg24.spreadsheet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DialogoParametros extends JDialog {
	
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