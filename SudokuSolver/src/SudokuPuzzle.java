/*	Sudoku Puzzle class

	This class stores the data model for a Sudoku puzzle including the cell statuses & values,
	remaining "candidates" (possible values) for empty cells, and the lists of cells that make
	up each exclusive group ("blocks", "regions", or "sub-squares").
	
	Assumptions:
	
		- puzzles are square (NxN), rectangular grids
		- each row and column of cells is a region
		- a third set of regions, each with N cells, can be defined (may overlap ??)
		- grid size is 9x9 with standard 3x3 regions if unspecified
		- grid sizes of 4x4, 6x6, 12x12, 16x16, 20x20, 25x25 have non-overlapping regions 
		       that are 2x2, 2x3, 3x4,   4x4,   4x5, or 5x5, respectively, if unspecified
		- irregular regions can be defined for any grid size (NOT YET!)
		- cell values range between 1 and N
		- cell status can be "clue", "solved", or "unsolved":
			- "clue" & "solved" cells have a cell value between 1 and N
			- "unsolved" cells have a cell value of EMPTY_CELL
	
	"Puzzle strings" are a simple text representation of the clues for a puzzle that can be
	used to initialize a new puzzle.  Cell values, starting with row 1 col 1 then row 1 col 2
	etc, are concatenated together using '.', ' ', or '-' for empty cells.  Each digit in the
	string is normally interpreted as a separate cell value between 1 and 9 ('0' is ignored).
	If an '=' is encountered, then the next two characters must be digits and are interpreted
	together as a two-digit cell value.  The sequence "=00" is interpreted as the value 100. 
	(If you want to make puzzles larger than 100x100, then you will have to find another way
	to set the cell values! :)  All other characters are ignored so other separators such as
	commas (',') or newlines may be inserted between rows for readability, but they are not
	required.
	
	Eg. "1...,.2..,..3.,...4" could be used to specify a 4x4 puzzle with the clues 1, 2, 3, 4
		in a diagonal.
	
	
	Anthony Kozar
	March 19, 2014
 */

import java.util.*;


public class SudokuPuzzle
{
	public static final int		EMPTY_CELL = 0;		// cell "value" for an empty cell

	public static final int		NO_ERR = 0;			// return value when method completed successfully
	public static final int		PARAM_ERR = -1;		// return value when method parameters are invalid
	public static final int		NOT_FOUND = -2;		// return value when a search found no results
	
	// cell status constants
	public static final int		CLUE = 2;			// cell value is one of the puzzle's initial givens
	public static final int		SOLVED = 1;			// cell value was determined by solving the puzzle
	public static final int		UNSOLVED = 0;		// cell has no valid value (EMPTY_CELL)
	
	// regions are either rows, columns, or other arbitrary sets of cells
	public	enum  RegionType	{ ROW, COLUMN, OTHER }
	
	// widths & heights of default region boxes for sizes up to 25
	public static final int[]	regionBoxWidths  = {0,0,0,0,2,0,2,0,0,3,0,0,4,0,0,0,4,0,0,0,4,0,0,0,0,5};
	public static final int[]	regionBoxHeights = {0,0,0,0,2,0,3,0,0,3,0,0,3,0,0,0,4,0,0,0,5,0,0,0,0,5};

	// puzzle has size x size cells
	private final int	size;
	
	// values for each puzzle cell (0 = empty)
	private int[][]			cells;
	private boolean[][]		isClue;
	private boolean[][][]	candidates;
	
	// lists of the cells contained in each of the "other" (non-row, non-column) regions
	// dim'd int[n][size][2]:  n regions each with size cells specified by a {row,col} pair
	private int[][][]		regionCellLists;
	

	// create an empty, standard, 9x9 puzzle 
	public SudokuPuzzle()
	{
		size = 9;
		InitializeArrays();
		MakeDefaultRegionLists();
	}
	
	// create a new standard, 9x9 puzzle using puzzleString to assign cell values
	public SudokuPuzzle(String puzzleString)
	{
		size = 9;
		InitializeArrays();
		MakeDefaultRegionLists();
		setAllCells(puzzleString);
		resetAllCandidates();
	}
	
	// create an empty NxN puzzle with "standard" regions 
	public SudokuPuzzle(int puzzleSize)
	{
		size = puzzleSize;
		InitializeArrays();
		MakeDefaultRegionLists();
	}
	
	// create a new NxN puzzle with "standard" regions using puzzleString to assign cell values
	public SudokuPuzzle(int puzzleSize, String puzzleString)
	{
		size = puzzleSize;
		InitializeArrays();
		MakeDefaultRegionLists();
		setAllCells(puzzleString);
		resetAllCandidates();
	}
	
	
	private void InitializeArrays()
	{
		// Java arrays are initiliazed to 0/false/null
		cells = new int[size][size];
		isClue = new boolean[size][size];
		candidates = new boolean[size][size][size];
		regionCellLists = new int[size][size][2];		// assume 'size' other regions for now 
	}
	
	private void MakeDefaultRegionLists()
	{
		int		boxwidth, boxheight, rgnidx;
		int[]	listidx;
		
		if (size >= regionBoxWidths.length || regionBoxWidths[size] == 0) {
			// would like to call a function here to make "broken diagonals"
			// the default regions for other sizes (see Wikipedia)
			System.err.printf("Can't make default regions yet for size=%d\n", size);
			return;
		}
		
		boxwidth = regionBoxWidths[size];
		boxheight = regionBoxHeights[size];
		listidx = new int[size];				// per list indices for where to add next cell
		
		// map every row/col pair to one of the region sets
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				rgnidx = (row/boxheight)*boxheight + (col/boxwidth);
				regionCellLists[rgnidx][ listidx[rgnidx] ][0] = row;
				regionCellLists[rgnidx][ listidx[rgnidx] ][1] = col;
				++listidx[rgnidx];
				// System.out.printf("%2d ", rgnidx);
			}
			// System.out.printf("\n");
		}
	}
	
	public int getSize()
	{
		return size;
	}
	
	// returns the number of regions of the specified type
	public int getRegionCount(RegionType type)
	{
		return ((type == RegionType.OTHER) ? regionCellLists.length : size);
	}
	
	/* public SudokuCell getCell(int row, int col)
	{
		return  ;
	} */
	
	public int getCellStatus(int row, int col)
	{
		if (row >= 0 && row < size && col >= 0 && col < size) {
			if (cells[row][col] == EMPTY_CELL)	return UNSOLVED;
			else if (isClue[row][col])			return CLUE;
			else								return SOLVED;
		}
		else {
			System.err.printf("SudokuPuzzle.getCellStatus(): parameter(s) out of range, row=%d, col=%d\n", row, col);
			return PARAM_ERR;
		}
	}
		
	public int getCellValue(int row, int col)
	{
		if (row >= 0 && row < size && col >= 0 && col < size) {
			return cells[row][col];
		}
		else {
			System.err.printf("SudokuPuzzle.getCellValue(): parameter(s) out of range, row=%d, col=%d\n", row, col);
			return PARAM_ERR;
		}
	}
	
	/** Returns the index of the first OTHER-type region for the cell at (row,col)
		NOTE: only works for the "default" regions created for some sizes at this time
	 */
	public int getCellRegionIdx(int row, int col)
	{
		if (size >= regionBoxWidths.length || regionBoxWidths[size] == 0) {
			// FIXME: make this function work always
			System.err.printf("SudokuPuzzle.getCellRegionIdx(): This function doesn't work for size=%d yet!\n", size);
			return PARAM_ERR;
		}
		
		if (row >= 0 && row < size && col >= 0 && col < size) {
			return (row/regionBoxHeights[size])*regionBoxHeights[size] + (col/regionBoxWidths[size]);
		}
		else {
			System.err.printf("SudokuPuzzle.getCellRegionIdx(): parameter(s) out of range, row=%d, col=%d\n", row, col);
			return PARAM_ERR;
		}
	}
	
	/** testCellCandidate() tests if value is a candidate for the specified cell.
		Note that this may not be *accurate* according to the CLUE & SOLVED cell values,
		but merely reflects the current state of the SudokuPuzzle object as set by the user.
		Call (some method??) to recalculate an accurate candidate list for the cell if desired ?
	 */
	public boolean testCellCandidate(int row, int col, int value)
	{
		return candidates[row][col][value];
	}
	
	public int[] getCellCandidates(int row, int col)
	{
		return new int[0];
	}
	
	/** setCellValue() sets  the cell's value and its status either to SOLVED (if value is positive)
		or to UNSOLVED (if value is EMPTY_CELL).  I.e.  This function removes CLUE status from the cell.
	 */
	public void setCellValue(int row, int col, int value)
	{
		if (row >= 0 && row < size && col >= 0 && col < size && (value == EMPTY_CELL || (value >= 1 && value <= size))) {
			cells[row][col] = value;
			isClue[row][col] = false;
		}
		else {
			System.err.printf("SudokuPuzzle.setCellValue(): parameter(s) out of range, row=%d, col=%d, value=%d\n", row, col, value);
			// return PARAM_ERR;  ??
		}
	}
	
	/** setCellClue() sets the cell's value and its status to CLUE. */
	public void setCellClue(int row, int col, int value)
	{
		if (row >= 0 && row < size && col >= 0 && col < size && (value >= 1 && value <= size)) {
			cells[row][col] = value;
			isClue[row][col] = true;
		}
		else {
			System.err.printf("SudokuPuzzle.setCellClue(): parameter(s) out of range, row=%d, col=%d, value=%d\n", row, col, value);
			// return PARAM_ERR;  ??
		}
	}
	
	public void setCellCandidates(int row, int col, int[] candidates)
	{
	
	}
	
	public int countCellCandidates(int row, int col)
	{
		int count = 0;
		
		if (row >= 0 && row < size && col >= 0 && col < size) {
			if (cells[row][col] == EMPTY_CELL) {				// status is UNSOLVED
				for (boolean b : candidates[row][col]) {
					if (b)	++count;
				}
			}
			else return 0;
		}
		else {
			System.err.printf("SudokuPuzzle.countCellCandidates(): parameter(s) out of range, row=%d, col=%d\n", row, col);
			return PARAM_ERR;
		}
		
		return count;
	}
	
	/** countRegionCandidateOccurences() counts the number of occurences of each
		candidate value within one region (specified by the region type and its
		index between 0 and num-1 for that region type) and returns the counts as
		an array int[size+1].  The count of value 1 is in a[1], of value 2 in a[2],
		etc.  The first element, a[0] is NO_ERR or an error code if any.  
	 */
	public int[] countRegionCandidateOccurences(RegionType type, int rgnidx)
	{
		int		rgnmax = getRegionCount(type);	// num of regions of type
		int[]	counts = new int[size+1];
		
		if (rgnidx < 0 || rgnidx >= rgnmax) {
			System.err.printf("SudokuPuzzle.countRegionCandidateOccurences(): rgnidx out of range, rgnidx=%d, rgnmax=%d\n", rgnidx, rgnmax);
			counts[0] = PARAM_ERR;
			return counts;
		}
		
		switch (type) {
			case ROW:
				// iterate over the row's empty cells & count each candidate
				for (int col = 0; col < size; col++) {
					if (cells[rgnidx][col] == EMPTY_CELL) {				// status is UNSOLVED
						for (int cand = 0; cand < size; cand++) {
							if (candidates[rgnidx][col][cand])	++(counts[cand+1]);
						}
					}
				}
				System.out.println("Row " + rgnidx + " counts: " + Arrays.toString(counts));
				break;
			case COLUMN:
				// iterate over the column's empty cells & count each candidate
				for (int row = 0; row < size; row++) {
					if (cells[row][rgnidx] == EMPTY_CELL) {				// status is UNSOLVED
						for (int cand = 0; cand < size; cand++) {
							if (candidates[row][rgnidx][cand])	++(counts[cand+1]);
						}
					}
				}
				System.out.println("Column " + rgnidx + " counts: " + Arrays.toString(counts));
				break;
			case OTHER:
				// iterate over the region's empty cells & count each candidate
				for (int[] cell : regionCellLists[rgnidx]) {
					int row = cell[0];
					int col = cell[1];
					if (cells[row][col] == EMPTY_CELL) {				// status is UNSOLVED
						for (int cand = 0; cand < size; cand++) {
							if (candidates[row][col][cand])	++(counts[cand+1]);
						}
					}
				}
				System.out.println("Other " + rgnidx + " counts: " + Arrays.toString(counts));
				break;
		}
		
		counts[0] = NO_ERR;		// redundant, but ...
		return counts;
	}
	
	/** findCandidateInRegion() returns an int[2] array containing the {row, col}
		of the first occurence of the candidate value (1 to size) within one region 
		(specified by the region type and its index between 0 and num-1 for that 
		region type).  Returns NOT_FOUND or an error code in place of the row index 
		if the candidate does not occur in the region or some other error occurred.
		(All error codes should be less than zero).
	 */
	public int[] findCandidateInRegion(int candidate, RegionType type, int rgnidx)
	{
		int		rgnmax = getRegionCount(type);	// num of regions of type
		int[]	cell = new int[2];
		
		if (rgnidx < 0 || rgnidx >= rgnmax) {
			System.err.printf("SudokuPuzzle.findCandidateInRegion(): rgnidx out of range, rgnidx=%d, rgnmax=%d\n", rgnidx, rgnmax);
			cell[0] = PARAM_ERR;
			return cell;
		}
		
		candidate = candidate - 1;	// candidate arrays are zero-indexed!
		switch (type) {
			case ROW:
				// iterate over the row's empty cells & search for candidate
				for (int col = 0; col < size; col++) {
					if (cells[rgnidx][col] == EMPTY_CELL) {				// status is UNSOLVED
						if (candidates[rgnidx][col][candidate]) {
							cell[0] = rgnidx;
							cell[1] = col;
							System.out.println("Row " + rgnidx + " cell: " + Arrays.toString(cell));
							return cell;
						}
					}
				}
				break;
			case COLUMN:
				// iterate over the column's empty cells & search for candidate
				for (int row = 0; row < size; row++) {
					if (cells[row][rgnidx] == EMPTY_CELL) {				// status is UNSOLVED
						if (candidates[row][rgnidx][candidate]) {
							cell[0] = row;
							cell[1] = rgnidx;
							System.out.println("Column " + rgnidx + " cell: " + Arrays.toString(cell));
							return cell;
						}
					}
				}
				break;
			case OTHER:
				// iterate over the region's empty cells & search for candidate
				for (int[] c : regionCellLists[rgnidx]) {
					if (cells[c[0]][c[1]] == EMPTY_CELL) {				// status is UNSOLVED
						if (candidates[c[0]][c[1]][candidate]) {
							cell[0] = c[0];
							cell[1] = c[1];
							System.out.println("Other " + rgnidx + " cell: " + Arrays.toString(cell));
							return cell;
						}
					}
				}
				break;
		}
		
		// candidate was not found
		cell[0] = NOT_FOUND;
		return cell;
	}
	
	
	/** resetAllCandidates() sets all candidates (1 to size) to true for UNSOLVED cells
		and to false for CLUE and SOLVED cells
	 */
	public void resetAllCandidates()
	{
		// iterate over the puzzle's cells
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				// set all candidates to true if EMPTY_CELL (assumes this is the same as UNSOLVED)
				// else set them to false
				Arrays.fill(candidates[row][col], (cells[row][col] == EMPTY_CELL));
			}
		}
	}
	
	public void recalculateAllCandidates()
	{
		resetAllCandidates();
		
		final int	numRegions = 2*size + regionCellLists.length;
		final int	ROW0 = 0;							// offset of row lists in regionKnowns[]
		final int	COL0 = size;						// offset of col lists in regionKnowns[]
		final int	OTHER0 = 2*size;					// offset of other regions' lists in regionKnowns[]
		int			otherRegion, cand;
		
		// make empty sets to store all cell values found within each row/col/region
		Set[]	regionKnowns = new Set[numRegions];
		for (int i = 0; i < numRegions; i++) {
			regionKnowns[i] = new HashSet<Integer>();
		}
		
		// add each cell's value (if any) to its region sets
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (cells[row][col] != EMPTY_CELL) {
					regionKnowns[ROW0+row].add(cells[row][col]);	// add to row's set
					regionKnowns[COL0+col].add(cells[row][col]);	// add to col's set
					otherRegion = getCellRegionIdx(row, col);
					if (otherRegion != PARAM_ERR) {
						// System.out.printf("Region map: row=%d, col=%d, otherRegion=%d\n", row, col, otherRegion);
						regionKnowns[OTHER0+otherRegion].add(cells[row][col]);	// add to other set(s)
					}
				}
			}
		}
		
		/*
		// check set contents
		for (int reg = 0; reg < numRegions; reg++) {
			System.out.println("" + reg + ": " + regionKnowns[reg].toString());
		} */
		
		// now look at each empty cell and remove candidates present in its region sets
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (cells[row][col] == EMPTY_CELL) {
					for (Object obj : regionKnowns[ROW0+row]) {
						cand = (Integer) obj;
						candidates[row][col][cand-1] = false;
					}
					for (Object obj : regionKnowns[COL0+col]) {
						cand = (Integer) obj;
						candidates[row][col][cand-1] = false;
					}
					otherRegion = getCellRegionIdx(row, col);
					if (otherRegion != PARAM_ERR) {
						for (Object obj : regionKnowns[OTHER0+otherRegion]) {
							cand = (Integer) obj;
							candidates[row][col][cand-1] = false;
						}
					}
				}
			}
		}
		
	}
	
	/** acceptSingleCandidateSolutions() finds all UNSOLVED cells with only one
		candidate value and sets the cell value to the candidate.  Returns true
		if any cells were changed, otherwise false.
	 */
	public boolean acceptSingleCandidateSolutions()
	{
		int			numCand, cellval;
		boolean		madeChanges = false;
		
		// look at each empty cell and if it has only one candidate, then set it as the cell's value
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (cells[row][col] == EMPTY_CELL) {
					numCand = countCellCandidates(row, col);
					if (numCand == 1) {
						// find the candidate value to set
						for (cellval = 0; cellval < size; cellval++) {
							if (candidates[row][col][cellval])	break;
						}
						// set cell value and clear candidate
						cells[row][col] = cellval + 1;				// candidate idx is 1 less than value
						candidates[row][col][cellval] = false;
						madeChanges = true;
					}
				}
			}
		}
		
		return madeChanges;
	}
	
	// set all cell values based on the given puzzleString (see description above)
	public void setAllCells(String puzzleString)
	{
		boolean	cellset, printederr = false;
		char c;
		int pos = 0, len = puzzleString.length(), value;
		
		// iterate over the puzzle's cells
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				cellset = false;
				// find the next significant character in puzzleString
				while (!cellset && pos < len) {
					c = puzzleString.charAt(pos);
					if (Character.isDigit(c)) {
						// digits are cell values
						value = Integer.parseInt(puzzleString.substring(pos,pos+1));
						if (value >= 1 && value <= size) {
							cells[row][col] = value;
							isClue[row][col] = true;
						}
						else {
							System.err.printf("SudokuPuzzle.setAllCells(): cell value is out of range, value=%d\n", value);
						}
						cellset = true;
					}
					else if (c == '=') {
						// beginning of double-digit cell value
						if (pos+2 < len) {
							// read the next two characters
							String digits = puzzleString.substring(pos+1,pos+3);
							pos += 2;
							if (digits.equals("00")) {
								// "=00" means 100
								value = 100;
							}
							else  {
								try {
									value = Integer.parseInt(digits);
								}
								catch (NumberFormatException nfe) {
									System.err.printf("SudokuPuzzle.setAllCells(): found '%s' following '=' instead of two digits\n", digits);
									value = 0;
								}
							}
							if (value >= 1 && value <= size) {
								cells[row][col] = value;
								isClue[row][col] = true;
							}
							else {
								System.err.printf("SudokuPuzzle.setAllCells(): cell value is out of range, value=%d\n", value);
							}
							cellset = true;
						}
						else {
							System.err.println("SudokuPuzzle.setAllCells(): puzzle string too short ('=' must be followed by two digits)");
						}
					}
					else if (c == '.' || c == ' ' || c == '-') {
						// cell is empty
						cells[row][col] = EMPTY_CELL;
						isClue[row][col] = false;
						cellset = true;
					}
					else {
						// ignore all other characters
					}
					++pos;
				}
				if (!printederr && !cellset && pos >= len) {
					System.err.println("SudokuPuzzle.setAllCells(): puzzle string does not specify every cell in puzzle");
					printederr = true;
				}
			}
		}
	}
	
	// set all cell values from an 2D integer array; array should be at least size x size
	public int setAllCells(int[][] cellvalues)
	{
		int value;
		
		// verify that the array is large enough
		if (cellvalues.length < size) {
			System.err.println("SudokuPuzzle.setAllCells(): not enough rows in cellvalues");
			return PARAM_ERR;
		}
		for (int row = 0; row < size; row++) {
			if (cellvalues[row].length < size) {
				System.err.printf("SudokuPuzzle.setAllCells(): not enough columns in cellvalues, row %d\n", row);
				return PARAM_ERR;
			}
		}
		
		// iterate over the puzzle's cells
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				value = cellvalues[row][col];
				if (value >= 1 && value <= size) {
					cells[row][col] = value;
					isClue[row][col] = true;
				}
				else if (value == EMPTY_CELL) {
					cells[row][col] = EMPTY_CELL;
					isClue[row][col] = false;
				}
				else {
					System.err.printf("SudokuPuzzle.setAllCells(): cell value is out of range, value=%d\n", value);
					return PARAM_ERR;
				}
			}
		}
		
		return NO_ERR;
	}

	// this main() is just for testing the SudokuPuzzle class
	public static void main( String args[] )
	{
		SudokuPuzzle puzzle = new SudokuPuzzle();
		
		System.out.println(puzzle.getSize());
		System.out.println(Arrays.toString(puzzle.cells[0]));
		System.out.println(Arrays.toString(puzzle.candidates[0][0]));
		
		// test puzzle string initialization
		puzzle = new SudokuPuzzle("7..45...1,.8.54.2..");
		for (int row = 0; row < puzzle.size; row++) {
			System.out.println(Arrays.toString(puzzle.cells[row]));
			System.out.println(Arrays.toString(puzzle.isClue[row]));
		}
		// print candidates of first row
		System.out.println("");
		for (int col = 0; col < puzzle.size; col++) {
			System.out.println(Arrays.toString(puzzle.candidates[0][col]));
		}
		
		// test default region assignments
		puzzle = new SudokuPuzzle(4);	System.out.println("\n");
		puzzle = new SudokuPuzzle(6);	System.out.println("\n");
		puzzle = new SudokuPuzzle(12);	System.out.println("\n");
		puzzle = new SudokuPuzzle(16);	System.out.println("\n");
		puzzle = new SudokuPuzzle(20);	System.out.println("\n");
		puzzle = new SudokuPuzzle(25);	System.out.println("\n");
		puzzle = new SudokuPuzzle(17);	System.out.println("\n");
		
		// print region cell lists
		int rgn = 0;
		puzzle = new SudokuPuzzle(9);	System.out.println("");
		for ( int[][] list : puzzle.regionCellLists) {
			System.out.printf("Region %d:  ", rgn++);
			for (int[] cell : list) {
				System.out.printf("(%d,%d) ", cell[0], cell[1]);	// (row,col)
			}
			System.out.println("");
		}
		
		// test puzzle string with =dd values and error detection
		puzzle = new SudokuPuzzle(20, "7..4,5..=10,=201.8,=15.=23.,=02=000.,=2..=");
		System.out.println(Arrays.toString(puzzle.cells[0]));

	}

}
