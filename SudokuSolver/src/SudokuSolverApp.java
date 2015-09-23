/*	SudokuSolverApp.java
	
	The main application class of the Sudoku Solver project.
	Provides a menu bar to create new puzzles and load example
	puzzles.  Multiple puzzle windows can be opened and controlled
	individually.
	
	Initially copied most of the code from Main.java in Circles 
	and Spirals project.
	
	Anthony Kozar
	September 22, 2015
	
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class SudokuSolverApp extends JFrame
{
	private final static String APP_NAME = "Uncle A's Sudoku Solver";
	
	private final static int	winWidth = 500;
	private final static int	winHeight = 75;

	// menu command constants
	private final static int MenuCmd_Quit					= 1;
	private final static int MenuCmd_About					= 2;
	private final static int MenuCmd_Help					= 3;

	private final static int MenuCmd_New_Puzzle				= 4;
	private final static int MenuCmd_Open					= 5;
	private final static int MenuCmd_Close					= 6;
	private final static int MenuCmd_Save					= 7;
	private final static int MenuCmd_Save_As				= 8;
	private final static int MenuCmd_Export					= 9;
	
	private final static int MenuCmd_Edit_Cell_Values		= 20;
	private final static int MenuCmd_Edit_Clues				= 21;
	private final static int MenuCmd_Edit_Reserved_Cells	= 22;
	private final static int MenuCmd_Edit_Regions			= 23;
	private final static int MenuCmd_Solve_Next				= 24;
	private final static int MenuCmd_Solve_All				= 25;
	private final static int MenuCmd_Reset_Puzzle			= 26;
	private final static int MenuCmd_Clear_Puzzle			= 27;

	private final static int MenuCmd_4x4_Easy_1				= 30;
	private final static int MenuCmd_9x9_My_First			= 31;
	private final static int MenuCmd_9x9_My_Second			= 32;
	
	// my puzzles
	String  size4    = "4.../.1.2/..../2.3.";
	String	first9x9 = "..57.64../...5.3.8./1...8...2/42.....18/..6....../71......9/....3...6/.....1.../..34.29..";
	String	second9x9 = "";
	
	private ActionListener	menulistener;

	
	public static void main(String[] args)
	{
        // Set application name and system menu bar on Mac OS X 
        String os = System.getProperty("os.name");
        if (os.contains("Mac OS X")) {
        	System.setProperty("apple.laf.useScreenMenuBar", "true");
        	// The next line doesn't work in recent JVMs on OS X,
        	// so we also include -Xdock:name="App name" in the runtime config
        	System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
        }
		
        // create main window
        new SudokuSolverApp().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public SudokuSolverApp() throws HeadlessException
	{
		super(APP_NAME);

        menulistener = new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				MyMenuItem item;
				
				// System.out.println("Menu listener command: " + event.getActionCommand());
				item = (MyMenuItem)event.getSource();
				// System.out.println("  command ID: " + item.getCommandID());
				DoMenuItem(item.getCommandID());
			}
		};
		
		this.setJMenuBar(CreateMenubar());
		setSize(winWidth, winHeight);
		setVisible(true);
	}

	/** Simple extension of JMenuItem that allows associating a unique
	 *  command ID with a menu item.  This ID can be used to distinguish
	 *  between menu items in a shared event listener.
	 */
	private class MyMenuItem extends JMenuItem
	{
		private int	commandID;
		
		public MyMenuItem(String text, int commandID)
		{
			super(text);
			this.commandID = commandID;
		}

		public MyMenuItem(String text, int commandID, int mnemonic)
		{
			super(text, mnemonic);
			this.commandID = commandID;
		}
		
		public int getCommandID() { return commandID; };
	}
	
	private void AddMenuItem(JMenu menu, String menutext, int commandID, int mnemonic)
	{
		JMenuItem	menuitem;

		menuitem = new MyMenuItem(menutext, commandID, mnemonic);
		menuitem.addActionListener(menulistener);
		menu.add(menuitem);		
	}
	
	private void AddMenuItem(JMenu menu, String menutext, int commandID)
	{
		JMenuItem	menuitem;

		menuitem = new MyMenuItem(menutext, commandID);
		menuitem.addActionListener(menulistener);
		menu.add(menuitem);		
	}
	
	private JMenuBar CreateMenubar()
	{
		JMenuBar	mbar;
		JMenu		file, edit, puzzle, examples, help;
		
		mbar = new JMenuBar();
		file = new JMenu("File");
		edit = new JMenu("Edit");
		puzzle = new JMenu("Puzzle");
		examples = new JMenu("Examples");
		help = new JMenu("Help");
		
		// File menu items
		AddMenuItem(file, "New Puzzle...", MenuCmd_New_Puzzle, 'N');
		AddMenuItem(file, "Open...", MenuCmd_Open, 'O');
		// TODO: add separator
		AddMenuItem(file, "Close", MenuCmd_Close, 'W');
		AddMenuItem(file, "Save", MenuCmd_Save, 'S');
		AddMenuItem(file, "Save As...", MenuCmd_Save_As);
		AddMenuItem(file, "Export...", MenuCmd_Export);
		// TODO: add separator
		AddMenuItem(file, "Quit", MenuCmd_Quit, 'Q');		

		// Help menu items
		AddMenuItem(help, "About " + APP_NAME + "...", MenuCmd_About, 'A');
		AddMenuItem(help, "Help...", MenuCmd_Help, 'H');
		
		// Puzzle menu items
		AddMenuItem(puzzle, "Edit Cell Values", MenuCmd_Edit_Cell_Values);
		AddMenuItem(puzzle, "Edit Clues", MenuCmd_Edit_Clues);
		AddMenuItem(puzzle, "Edit Reserved Cells", MenuCmd_Edit_Reserved_Cells);
		AddMenuItem(puzzle, "Edit Regions", MenuCmd_Edit_Regions);
		// TODO: add separator
		AddMenuItem(puzzle, "Solve Next", MenuCmd_Solve_Next);
		AddMenuItem(puzzle, "Solve All", MenuCmd_Solve_All);
		AddMenuItem(puzzle, "Reset Puzzle", MenuCmd_Reset_Puzzle);
		// TODO: add separator
		AddMenuItem(puzzle, "Clear Puzzle...", MenuCmd_Clear_Puzzle);

		// Examples menu items
		AddMenuItem(examples, "4x4 Easy", MenuCmd_4x4_Easy_1);
		// TODO: add separator
		AddMenuItem(examples, "My first 9x9", MenuCmd_9x9_My_First);
		// AddMenuItem(examples, "My second 9x9", MenuCmd_9x9_My_Second);

		mbar.add(file);
		mbar.add(edit);
		mbar.add(puzzle);
		mbar.add(examples);
		mbar.add(help);
		
		return mbar;
	}
	
	private void DoMenuItem(int menuCommand)
	{
		SudokuView newwindow = null;
		
		switch (menuCommand) {
			case MenuCmd_Quit:
				break;
			case MenuCmd_About:
				break;
			case MenuCmd_Help:
				break;
			case MenuCmd_New_Puzzle:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(9));
				break;
			case MenuCmd_Open:
				break;	
			case MenuCmd_Close:
				break;
			case MenuCmd_Save:
				break;
			case MenuCmd_Save_As:
				break;
			case MenuCmd_Export:
				break;
			case MenuCmd_Edit_Cell_Values:
				break;
			case MenuCmd_Edit_Reserved_Cells:
				break;
			case MenuCmd_Edit_Clues:
				break;
			case MenuCmd_Edit_Regions:
				break;
			case MenuCmd_Solve_Next:
				break;
			case MenuCmd_Solve_All:
				break;
			case MenuCmd_Reset_Puzzle:
				break;
			case MenuCmd_Clear_Puzzle:
				break;
			case MenuCmd_4x4_Easy_1:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(4, size4));
				break;
			case MenuCmd_9x9_My_First:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(9, first9x9));
				break;
			case MenuCmd_9x9_My_Second:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(9, second9x9));
				break;
		}
		
		if (newwindow != null) {
			newwindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			newwindow.setJMenuBar(CreateMenubar());
		}
	}
}
