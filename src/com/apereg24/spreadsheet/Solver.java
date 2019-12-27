package com.apereg24.spreadsheet;

public class Solver {
	
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
	
	public boolean isSheetFullfilled(String[][] input) {
		return true;
	}

	public static int[][] solveSheet(String[][] tableToSolve) throws SpreadsheetException {
		throw new SpreadsheetException("Está sin hacer xd");
	}
	
}
