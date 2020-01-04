package com.apereg24.spreadsheet;

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
			for (int l = 0; l < input[0].length; l++) {
				if (Solver.isANum(entry[k][l])) {
					solution[k][l] = Integer.parseInt(entry[k][l]);
					entry[k][l] = "";
				} else {
					solution[k][l] = 0;
				}
			}
		}
	}
	
	public void resolve() throws SpreadsheetException {		
		for (int i = 0; i < this.numRows; i++) {
			for (int j = 0; j < this.numCols; j++) {
				if(!(this.entry[i][j].isEmpty())) {
					try {
						this.solution[i][j] = resolveFormula(this.entry[i][j], i, j);
					}catch(StackOverflowError e) {
						throw new SpreadsheetException("Error en la celda " +getLetter(i)+""+(j+1)+ "\nDa lugar a dependencias cíclicas.\n");
					}
					this.entry[i][j] = "";
				}
			}
		}
	}

	private int resolveCell(int row, int column) throws SpreadsheetException {
		if (row > this.numRows-1 || row < 0 || column > this.numCols-1 || column < 0) throw new SpreadsheetException("Se esta intentando acceder a una celda que no existe.");

		if (!(this.entry[row][column].isEmpty())) 
			return resolveFormula(this.entry[row][column], row, column);
		return this.solution[row][column];
	}

	private int resolveFormula(String formula, int row, int column) throws SpreadsheetException {
		int result = 0, letra = -1;
		if(!this.entry[row][column].startsWith("=")) throw new SpreadsheetException("Error en la celda " +getLetter(row)+""+(column+1)+ "\nNo es una formula.\n");
		formula = formula.substring(1, formula.length());
		formula = formula.replaceAll("\\+", ",");
		String[] formulaSplitted = formula.split(",");
		for (int i = 0; i < formulaSplitted.length; i++) {
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
						throw new SpreadsheetException("Error en la celda " +getLetter(row)+""+(column+1)+ "\nNo esta correctamente redactada.\n");
					}
				}
			
			}
			if(letra == -1) throw new SpreadsheetException("Error en la celda " +getLetter(row)+""+(column+1)+ "\nNo esta correctamente redactada.\n");
			if(Num.toString().isEmpty() || Letter.toString().isEmpty()) throw new SpreadsheetException("Error en la celda " +getLetter(row)+""+(column+1)+ "\nNo esta correctamente redactada.\n");
			
			int num = Integer.parseInt(Num.toString())-1;
			
			result += resolveCell(num, letra);
		}
		return result;
	}

	public int[][] getSolution() throws SpreadsheetException {
		return this.solution;
	}

	public static boolean areRowsOk(int input) {
		return input >= 0 && input <= 999;
	}

	public static boolean areColsOk(int input) {
		return input >= 0 && input <= 18278;
	}

	public static boolean isANum(String input) {
		try {
			Integer.parseInt(input);
		}catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private static int getAsciiNum(char c) {
		return(((int)c)-64);
	}
	
	public static String getLetter(int i) {
	    int quot = i/26;
	    int rem = i%26;
	    char letter = (char)((int)'A' + rem);
	    if( quot == 0 ) {
	        return ""+letter;
	    } else {
	        return getLetter(quot-1) + letter;
	    }
	}

	private static int getFormulaCol(String s) {
		int suma = 0;
		for (int i = 0; i < s.length(); i++)
			suma += Math.pow(26, i) * getAsciiNum(s.charAt(i));
		return --suma;
	}

}
