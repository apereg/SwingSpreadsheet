package com.apereg24.spreadsheet;

import java.util.Arrays;

public class Solver {
	
	private String[][] entry;

	private int[][] solution;

	private int numRows;
	
	private int numCols;
	
	public Solver(int i, int j, String[][] input) {
		this.numRows = i;
		this.numCols = j;
		this.entry = input;
		this.solution = new int[numRows][numCols];
		for (int k = 0; k < input.length; k++) {
			for (int l = 0; l < input.length; l++) {
				if (Solver.isANum(entry[k][l])) {
					solution[k][l] = Integer.parseInt(entry[k][l]);
					entry[k][l] = "";
				} else {
					solution[k][l] = Integer.MIN_VALUE;
				}
			}
		}
	}
	
	public void resolve() {		
		for (int i = 0; i < this.numRows; i++) {
			for (int j = 0; j < this.numCols; j++) {
				if(!(this.entry[i][j].isEmpty())) {
					this.solution[i][j] = resolveFormula(this.entry[i][j]);
					this.entry[i][j] = "";
				}
			}
		}
	}

	private int resolveCell(int row, int col) {
		if (row > this.numRows || row < 0 || col > this.numCols || col < 0)
			//TODO Tirar excepcion
		if (!(this.entry[row][col].isEmpty())) 
			return resolveFormula(this.entry[row][col]);
		return this.solution[row][col];
	}

	private int resolveFormula(String formula) {
		int result = 0, letra = -1;
		//TODO filtrar si no es una formula;
		System.out.println("Se va a resolver la formula " +formula);
		formula = formula.substring(1, formula.length());
		formula = formula.replaceAll("\\+", ",");
		String[] formulaSplitted = formula.split(",");
		for (int i = 0; i < formulaSplitted.length; i++) {
			System.out.println("Se va a resolver el trozico " +formulaSplitted[i]);
			letra = -1;
			StringBuffer Num = new StringBuffer();
			StringBuffer Letter = new StringBuffer();	
			for (int k = 0; k < formulaSplitted[i].length(); k++) {
				if(Solver.isANum(formulaSplitted[i].substring(k, k+1))) {
					letra = getFormulaCol(Letter.toString());
					Num.append(formulaSplitted[i].charAt(k));
				}
				else {
					if(letra == -1 || !(Character.isUpperCase(formulaSplitted[i].charAt(k))) ) {
						Letter.append(formulaSplitted[i].charAt(k));
					}
					else {
						//TODO Tirar excepcion
					}
				}
			
			}
			if(letra == -1) {} //TODO tirar excepcion
			if(Num.toString().isEmpty() || Letter.toString().isEmpty()) {}
				//TODO tirar excepcion
			
			int num = Integer.parseInt(Num.toString())-1;
			
			result += resolveCell(num, letra);
		}
		return result;
	}

	public static boolean isANum(String input) {
		try {
			Integer.parseInt(input);
		}catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public static boolean areRowsOk(int input) {
		return input >= 0 && input <= 999;
	}
	
	public static boolean areColsOk(int input) {
		return input >= 0 && input <= 18278;
	}

	public static String getLetter(int input) {
		// TODO Auto-generated method stub
		char a = (char)(input + '@');
		return Character.toString(a);
	}
	
	private static int getAsciiNum(char c) {
		return(((int)c)-64);
	}
	
	private static int getFormulaCol(String s) {
		int suma = 0;
		for (int i = 0; i < s.length(); i++)
			suma += Math.pow(26, i) * getAsciiNum(s.charAt(i));
		return --suma;
	}
	
	public int[][] getSolution() throws SpreadsheetException {
		return this.solution; //TODO Si no esta lleno es que no se ha resuelto la hoja y tengo que tirar una excepcion
	}
	
	public static void print2D(int mat[][]) {
        // Loop through all rows
        for (int[] row : mat) {

            // converting each row as string
            // and then printing in a separate line
            System.out.println(Arrays.toString(row));
        }
        System.out.println("\n");
    }

}
