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


public class SudokuSolverApp extends JFrame implements MenuHandler
{
	private final static String APP_NAME = "Uncle A's Sudoku Solver";
	
	private final static int	winWidth = 500;
	private final static int	winHeight = 75;

	// my puzzles
	String  size4    = "4.../.1.2/..../2.3.";
	String	first9x9 = "..57.64../...5.3.8./1...8...2/42.....18/..6....../71......9/....3...6/.....1.../..34.29..";
	String	second9x9 = "";
	
	private ActionListener	appmenulistener;	// listener for application-specific menu commands
	private static boolean	runningOnMacOSX;
	private static int		primaryCommandKey;
	
	public static void main(String[] args)
	{
        // Set application name and system menu bar on Mac OS X 
        String os = System.getProperty("os.name");
        if (os.contains("Mac OS X")) {
        	System.setProperty("apple.laf.useScreenMenuBar", "true");
        	// The next line doesn't work in recent JVMs on OS X,
        	// so we also include -Xdock:name="App name" in the runtime config
        	System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
        	// use the Apple/Command key for command shortcuts
         	primaryCommandKey = ActionEvent.META_MASK;
         	runningOnMacOSX = true;
        }
        else {
        	// use the Control key for command shortcuts
         	primaryCommandKey = ActionEvent.CTRL_MASK;
         	runningOnMacOSX = false;
        }
		
        // create main window
        new SudokuSolverApp().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public SudokuSolverApp() throws HeadlessException
	{
		super(APP_NAME);

        appmenulistener = new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				MyMenuItem item;
				
				// System.out.println("Menu listener command: " + event.getActionCommand());
				item = (MyMenuItem)event.getSource();
				// System.out.println("  command ID: " + item.getCommandID());
				DoMenuCommand(item.getCommandID());
			}
		};
		
		this.setJMenuBar(CreateMenubar(this));
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
	
	private void AddMenuItem(JMenu menu, ActionListener listener, String menutext, int commandID, int mnemonic)
	{
		JMenuItem	menuitem;

		menuitem = new MyMenuItem(menutext, commandID, mnemonic);
		menuitem.setAccelerator(KeyStroke.getKeyStroke(mnemonic, primaryCommandKey));
		menuitem.addActionListener(listener);
		menu.add(menuitem);		
	}
	
	private void AddMenuItem(JMenu menu, ActionListener listener, String menutext, int commandID)
	{
		JMenuItem	menuitem;

		menuitem = new MyMenuItem(menutext, commandID);
		menuitem.addActionListener(listener);
		menu.add(menuitem);		
	}
	
	private JMenuBar CreateMenubar(MenuHandler targetwindow)
	{
		JMenuBar	mbar;
		JMenu		file, edit, puzzle, examples, help;
		
		// Menu commands apply either to the front window or the application. So,
		// each menu item is assigned either the application menu listener or a
		// unique listener for commands specific to the target window.
		final MenuHandler windowhandler = targetwindow;
		ActionListener	winmenulistener = new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				MyMenuItem item;
				
				// System.out.println("Menu listener command: " + event.getActionCommand());
				item = (MyMenuItem)event.getSource();
				// System.out.println("  command ID: " + item.getCommandID());
				windowhandler.DoMenuCommand(item.getCommandID());
			}
		};
		
		// define menus
		mbar = new JMenuBar();
		file = new JMenu("File");
		edit = new JMenu("Edit");
		puzzle = new JMenu("Puzzle");
		examples = new JMenu("Examples");
		help = new JMenu("Help");
		
		// File menu items
		AddMenuItem(file, appmenulistener, "New Puzzle...", MenuHandler.Cmd_New_Puzzle, KeyEvent.VK_N);
		AddMenuItem(file, appmenulistener, "Open...", MenuHandler.Cmd_Open, KeyEvent.VK_O);
		file.addSeparator();
		AddMenuItem(file, winmenulistener, "Close", MenuHandler.Cmd_Close, KeyEvent.VK_W);
		AddMenuItem(file, winmenulistener, "Save", MenuHandler.Cmd_Save, KeyEvent.VK_S);
		AddMenuItem(file, winmenulistener, "Save As...", MenuHandler.Cmd_Save_As);
		AddMenuItem(file, winmenulistener, "Export...", MenuHandler.Cmd_Export);
		if (!runningOnMacOSX) {
			file.addSeparator();
			AddMenuItem(file, appmenulistener, "Exit", MenuHandler.Cmd_Quit);
		}
		
		// Help menu items
		AddMenuItem(help, appmenulistener, "About " + APP_NAME + "...", MenuHandler.Cmd_About);
		AddMenuItem(help, appmenulistener, "Help...", MenuHandler.Cmd_Help, KeyEvent.VK_H);
		
		// Puzzle menu items
		AddMenuItem(puzzle, winmenulistener, "Edit Cell Values", MenuHandler.Cmd_Edit_Cell_Values);
		AddMenuItem(puzzle, winmenulistener, "Edit Clues", MenuHandler.Cmd_Edit_Clues);
		AddMenuItem(puzzle, winmenulistener, "Edit Reserved Cells", MenuHandler.Cmd_Edit_Reserved_Cells);
		AddMenuItem(puzzle, winmenulistener, "Edit Regions", MenuHandler.Cmd_Edit_Regions);
		puzzle.addSeparator();
		AddMenuItem(puzzle, winmenulistener, "Solve Next", MenuHandler.Cmd_Solve_Next);
		AddMenuItem(puzzle, winmenulistener, "Solve All", MenuHandler.Cmd_Solve_All);
		AddMenuItem(puzzle, winmenulistener, "Reset Puzzle", MenuHandler.Cmd_Reset_Puzzle, KeyEvent.VK_R);
		puzzle.addSeparator();
		AddMenuItem(puzzle, winmenulistener, "Clear Puzzle...", MenuHandler.Cmd_Clear_Puzzle);

		// Examples menu items
		AddMenuItem(examples, appmenulistener, "4x4 Easy", MenuHandler.Cmd_4x4_Easy_1);
		examples.addSeparator();
		AddMenuItem(examples, appmenulistener, "My first 9x9", MenuHandler.Cmd_9x9_My_First);
		// AddMenuItem(examples, appmenulistener, "My second 9x9", MenuHandler.Cmd_9x9_My_Second);

		mbar.add(file);
		mbar.add(edit);
		mbar.add(puzzle);
		mbar.add(examples);
		mbar.add(help);
		
		return mbar;
	}
	
	public boolean DoMenuCommand(int menuCommand)
	{
		SudokuView newwindow = null;
		
		switch (menuCommand) {
			case MenuHandler.Cmd_Quit:
				break;
			case MenuHandler.Cmd_About:
				break;
			case MenuHandler.Cmd_Help:
				break;
			case MenuHandler.Cmd_New_Puzzle:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(9));
				break;
			case MenuHandler.Cmd_Open:
				break;	
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
				break;
			case MenuHandler.Cmd_Solve_All:
				break;
			case MenuHandler.Cmd_Reset_Puzzle:
				break;
			case MenuHandler.Cmd_Clear_Puzzle:
				break;
			case MenuHandler.Cmd_4x4_Easy_1:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(4, size4));
				break;
			case MenuHandler.Cmd_9x9_My_First:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(9, first9x9));
				break;
			case MenuHandler.Cmd_9x9_My_Second:
				newwindow = new SudokuView();
				newwindow.setPuzzle(new SudokuPuzzle(9, second9x9));
				break;
		}
		
		if (newwindow != null) {
			newwindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			newwindow.setJMenuBar(CreateMenubar(newwindow));
		}
		
		return true;
	}
}
