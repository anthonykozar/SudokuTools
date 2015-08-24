/*	CellCoord.java

	Minimal class for specifying cell coordinates in rectangular puzzles.
		
	Anthony Kozar
	August 23, 2015
 */

public class CellCoord {
	private int row;
	private int col;

	public CellCoord() {
		this.row = 0;
		this.col = 0;
	}
	public CellCoord(int row, int column) {
		this.row = row;
		this.col = column;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return col;
	}

	public CellCoord setRow(int row) {
		this.row = row;
		return this;
	}

	public CellCoord setColumn(int column) {
		this.col = column;
		return this;
	}

	public CellCoord setCoord(int row, int column) {
		this.row = row;
		this.col = column;
		return this;
	}
}
