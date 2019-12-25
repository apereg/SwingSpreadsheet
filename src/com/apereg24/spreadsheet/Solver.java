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
	
}
