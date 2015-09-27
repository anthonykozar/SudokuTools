/*	Sudoku View class

	A Swing-based view for the SudokuPuzzle class.
		
	Anthony Kozar
	March 19, 2014
 */

import java.awt.*;
import java.awt.event.*;
import java.math.*;
import javax.swing.*;

import java.util.*;

public class SudokuView extends JFrame implements MouseListener, KeyListener, MenuHandler
{
	private final static int	winWidth = 500;
	private final static int	winHeight = 500;
	private final static int	maxSqrWidth = 100;
	private final static int	marginSize = 10;
	private final static int	cellSize = 50;
	private final static int	lgNumXOffset1digit = 15;
	private final static int	lgNumXOffset2digit = 6;
	private final static int	lgNumYOffset = 37;
	private final static int	smNumXOffset[] = {7, 22, 37, 7, 22, 37, 7, 22, 37};
	private final static int	smNumYOffset[] = {16, 16, 16, 31, 31, 31, 46, 46, 46};
	private final static Color	selectionColor = new Color(255, 255, 180);
	
	enum Direction { UP, RIGHT, DOWN, LEFT, NEXT, PREVIOUS };

	private Font	largeNumFont;
	private Font	smallNumFont;
	
	private SudokuPuzzle	puzzleModel;						// ref to the puzzle model object to be displayed
	private CellCoord		selectedCell = new CellCoord();		// top left corner (0,0)
	private	boolean			showCandidates = false;
	private boolean			solving = false;	// TEMP -- REMOVE
	
	public SudokuView()
	{
		super("Uncle A's Sudoku Solver");
		setSize(winWidth, winHeight);
		setVisible(true);
		addMouseListener(this);
		addKeyListener(this);
		
		largeNumFont = new Font("Lucida Grande", Font.PLAIN, 30);
		smallNumFont = new Font("Lucida Grande", Font.PLAIN, 13);
	}
	
	public SudokuPuzzle getPuzzle()
	{
		return puzzleModel;
	}
	
	public void setPuzzle(SudokuPuzzle puzzle)
	{
		puzzleModel = puzzle;
	}
	
	public void paint( Graphics g )
	{
		int	gridTop, gridLeft, gridBottom, gridRight, gridSize;
		
		super.paint(g);
		
		/* final Font defaultFont = g.getFont();
		System.out.printf("Font (logical): %s\n", defaultFont.getName());
		System.out.printf("Font face: %s\n", defaultFont.getFontName());
		System.out.printf("Font size: %f\n", defaultFont.getSize2D());
		System.out.printf("Font style: %s %s %s\n", defaultFont.isPlain() ? "plain" : "",
													defaultFont.isBold() ? "bold" : "",
													defaultFont.isItalic() ? "italic" : ""); */
		
		// clear the window to white
		Dimension winSize = this.getSize();		
		g.setColor(Color.white);
		g.fillRect(0, 0, winSize.width, winSize.height);

		// get the size of our visible drawing area to avoid drawing underneath the window frame
		Insets visibleArea = this.getInsets();
		// define our own margins within that
		Insets margins = new Insets(visibleArea.top    + marginSize, 
		                            visibleArea.left   + marginSize, 
		                            visibleArea.bottom + marginSize, 
		                            visibleArea.right  + marginSize);
		
		/* System.out.printf("ViewRect: (%d, %d) (%d, %d)\n",  margins.top,
															margins.left,
															winHeight - margins.bottom,
															winWidth - margins.right); */
		
		// draw Sudoku grid
		g.setColor(Color.black);
		gridSize = puzzleModel.getSize();
		gridTop = margins.top;
		gridLeft = margins.left;
		gridBottom = margins.top + cellSize*gridSize;
		gridRight = margins.right + cellSize*gridSize;
		
		for	( int i = 0; i <= gridSize; i++ )	{
			// horizontal lines
			g.drawLine(gridLeft, gridTop + cellSize*i, gridRight, gridTop + cellSize*i);
			// vertical lines
			g.drawLine(gridLeft + cellSize*i, gridTop, gridLeft + cellSize*i, gridBottom);
			// make region boundaries thicker (only for puzzles with default regions for now) 
			if (SudokuPuzzle.regionBoxHeights[gridSize] > 0) {
				if (i % SudokuPuzzle.regionBoxHeights[gridSize] == 0) {
					// horizontal
					g.drawLine(gridLeft, gridTop + cellSize*i + 1, gridRight, gridTop + cellSize*i + 1);
					g.drawLine(gridLeft, gridTop + cellSize*i - 1, gridRight, gridTop + cellSize*i - 1);
				}
				if (i % SudokuPuzzle.regionBoxWidths[gridSize] == 0) {
					// vertical
					g.drawLine(gridLeft + cellSize*i - 1, gridTop, gridLeft + cellSize*i - 1, gridBottom);
					g.drawLine(gridLeft + cellSize*i + 1, gridTop, gridLeft + cellSize*i + 1, gridBottom);
				}
			}
		}
		
		// fill background of the selected cell
		g.setColor(selectionColor);
		g.fillRect(gridLeft + cellSize*selectedCell.getColumn() + 2, gridTop + cellSize*selectedCell.getRow() + 2, cellSize-3, cellSize-3);

		// draw puzzle cell contents
		int cellstatus, cellvalue, curCellX, curCellY, maxCandidate, lgNumXOffset;
		
		maxCandidate = (gridSize < 9) ? gridSize : 9;				// can only display candidates up to gridSize or 9
		for ( int row = 0; row < gridSize; row++ )	{
			for ( int col = 0; col < gridSize; col++ )	{
				cellstatus = puzzleModel.getCellStatus(row, col);
				if (cellstatus == SudokuPuzzle.UNSOLVED) {
					if (showCandidates) {
						// draw (small) candidate numbers
						curCellX = gridLeft + col*cellSize;
						curCellY = gridTop + row*cellSize;
						
						g.setFont(smallNumFont);
						g.setColor(Color.darkGray);
						for ( int i = 0; i < maxCandidate; i++ )	{
							if (puzzleModel.testCellCandidate(row, col, i)) {
								g.drawString(String.valueOf(i+1), curCellX + smNumXOffset[i], curCellY + smNumYOffset[i]);
							}
						}
					}
				}
				else {
					// draw clues and (large) answer numbers
					g.setFont(largeNumFont);
					if (cellstatus == SudokuPuzzle.CLUE)
						 g.setColor(Color.black);
					else g.setColor(Color.blue);
					cellvalue = puzzleModel.getCellValue(row, col);
					lgNumXOffset = (cellvalue < 10) ? lgNumXOffset1digit : lgNumXOffset2digit;
					g.drawString(String.valueOf(cellvalue), gridLeft + col*cellSize + lgNumXOffset, 
								 gridTop + row*cellSize + lgNumYOffset);
				}
			}
		}
		
		
	}
	
	public void moveSelection(Direction dir)
	{
		int row, col;
		
		switch (dir) {
			case UP:
				// % operator can give a negative result if the dividend is negative!
				selectedCell.setRow((selectedCell.getRow()-1+puzzleModel.getSize()) % puzzleModel.getSize());
				break;
			case DOWN:
				selectedCell.setRow((selectedCell.getRow()+1) % puzzleModel.getSize());
				break;
			case LEFT:
				// % operator can give a negative result if the dividend is negative!
				selectedCell.setColumn((selectedCell.getColumn()-1+puzzleModel.getSize()) % puzzleModel.getSize());
				break;
			case RIGHT:
				selectedCell.setColumn((selectedCell.getColumn()+1) % puzzleModel.getSize());
				break;
			case NEXT:
				col = selectedCell.getColumn()+1;
				if (col >= puzzleModel.getSize()) {
					// wrap around to the next row
					col = 0;
					row = selectedCell.getRow()+1;
					if (row >= puzzleModel.getSize()) {
						// wrap back to cell (0,0)
						row = 0;
					}
				}
				else row = selectedCell.getRow();
				selectedCell.setCoord(row, col);
				break;
			case PREVIOUS:
				// FIXME: finish implementing wrapping behavior
				selectedCell.setColumn((selectedCell.getColumn()-1+puzzleModel.getSize()) % puzzleModel.getSize());
				break;
		}
		// System.out.printf("Selected cell: (%d, %d)\n", selectedCell.getRow(), selectedCell.getColumn());
		this.repaint();
	}
	
	public void solveNextStep()
	{		
		// first click fills in all candidates
		if (!showCandidates) {
			puzzleModel.resetAllCandidates();
			showCandidates = !showCandidates;
		}
		// second click recalculates only valid candidates
		else if (!solving) {
			puzzleModel.recalculateAllCandidates();
			solving = true;
		}
		// subsequent clicks step thru the solution process
		else {
			if (!puzzleModel.acceptSingleCandidateSolutions()) {
				int psize;
				int[] counts, cell;
				
				// when there are no more single-candidate cells, try next technique
				for (SudokuPuzzle.RegionType type : SudokuPuzzle.RegionType.values()) {
					psize = puzzleModel.getSize();
					for (int i = 0; i < psize; i++) {
						counts = puzzleModel.countRegionCandidateOccurences(type, i);
						if (counts[0] == SudokuPuzzle.NO_ERR) {
							// search for candidates that occur exactly once
							for (int cand = 1; cand < counts.length; cand++) {
								if (counts[cand] == 1) {
									// get the matching cell and set its value
									cell = puzzleModel.findCandidateInRegion(cand, type, i);
									if (cell[0] >= 0) {
										puzzleModel.setCellValue(cell[0], cell[1], cand);
										// (do we care that candidate doesn't get cleared? YES!)
										// need to clear candidate value in all of the cell's regions
										puzzleModel.recalculateAllCandidates();	// FIXME: overkill ...
										System.out.println("Solved cell " + Arrays.toString(cell));
									}
								}
							}
						}
					}
				}
			}
			puzzleModel.recalculateAllCandidates();
		}
	}
	
	public void resetPuzzle()
	{
		puzzleModel.resetAllCells();
		showCandidates = false;
		solving = false;
		this.repaint();
	}
	
	public void mouseClicked(MouseEvent event)
	{		
	}
	
	public void mousePressed(MouseEvent event)	{}
	public void mouseReleased(MouseEvent event)	{}
	public void mouseEntered(MouseEvent event)	{}
	public void mouseExited(MouseEvent event)	{}
	
	/* These 3 methods are the implementation of the KeyListener interface. */
	
	public void keyPressed(KeyEvent event)
	{
		int		key = event.getKeyCode();
		// System.out.println("keyPressed event: " + key);
		
		// Tab and arrow keys change the selected square when editing
		if	(key == KeyEvent.VK_TAB) {
			// System.out.println("Received tab ");
			moveSelection(Direction.NEXT);
		}
		else if	(key == KeyEvent.VK_RIGHT) {
			// System.out.println("Received right arrow");
			moveSelection(Direction.RIGHT);
		}
		else if	(key == KeyEvent.VK_LEFT) {
			// System.out.println("Received left arrow");
			moveSelection(Direction.LEFT);
		}
		else if	(key == KeyEvent.VK_UP) {
			// System.out.println("Received up arrow");
			moveSelection(Direction.UP);
		}
		else if	(key == KeyEvent.VK_DOWN) {
			// System.out.println("Received down arrow");
			moveSelection(Direction.DOWN);
		}
		
	}
	
	public void keyReleased(KeyEvent event)
	{
		int		key = event.getKeyCode();
		// System.out.println("keyReleased event: " + key);
	}
	
	public void keyTyped(KeyEvent event) 
	{
		char	key = event.getKeyChar();
		// System.out.println("keyTyped event: " + key);
		
		if	(key == '!') {
			// '!' exits the program
			System.exit(0);
		}
		else if	(key == 'e' || key == 'E') {
			// 'e' and 'E' 
		}
		else if	(key == 's' || key == 'S') {
			// 's' and 'S' 
		}
		else if	(key == 'r' || key == 'R') {
			// 'r' and 'R' reset the puzzle to clues only
			resetPuzzle();
		}
		else if	(Character.isDigit(key)) {
			// number keys change the value of the current cell and
			// advance the selection to the next cell
			int value = Integer.parseInt("" + key);
			if (value <= puzzleModel.getSize()) {
				if (value != 0) {
					puzzleModel.setCellClue(selectedCell.getRow(), selectedCell.getColumn(), value);
				}
				else puzzleModel.setCellValue(selectedCell.getRow(), selectedCell.getColumn(), SudokuPuzzle.EMPTY_CELL);
				moveSelection(Direction.NEXT);
			}
			else {
				// TODO: Beep at user to let them know they entered an invalid value ?
			}
		}
		else if	(key =='\n') {
			// when 'Enter' is pressed
			solveNextStep();
			this.repaint();			
		}
		else {
			// for all other keys, advance the selection to the next cell
			moveSelection(Direction.NEXT);
		}
		
		return;
	}
	
	// method for the MenuHandler interface
	public boolean DoMenuCommand(int menuCommand)
	{
		switch (menuCommand) {
			case MenuHandler.Cmd_Close:
				break;
			case MenuHandler.Cmd_Save:
				break;
			case MenuHandler.Cmd_Save_As:
				break;
			case MenuHandler.Cmd_Export:
				break;
			case MenuHandler.Cmd_Edit_Cell_Values:
				break;
			case MenuHandler.Cmd_Edit_Reserved_Cells:
				break;
			case MenuHandler.Cmd_Edit_Clues:
				break;
			case MenuHandler.Cmd_Edit_Regions:
				break;
			case MenuHandler.Cmd_Solve_Next:
				solveNextStep();
				this.repaint();			
				break;
			case MenuHandler.Cmd_Solve_All:
				break;
			case MenuHandler.Cmd_Reset_Puzzle:
				resetPuzzle();
				break;
			case MenuHandler.Cmd_Clear_Puzzle:
				break;
			default:
				return false;
		}
		
		return true;
	}

	public static void main( String args[] )
	{
		// my puzzles
		String  size4    = "4.../.1.2/..../2.3.";
		String	first9x9 = "..57.64../...5.3.8./1...8...2/42.....18/..6....../71......9/....3...6/.....1.../..34.29..";
		
		SudokuView app = new SudokuView();
		app.setPuzzle(new SudokuPuzzle(9, first9x9));
		app.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	}

}
