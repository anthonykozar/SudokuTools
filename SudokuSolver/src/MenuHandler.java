/*	MenuHandler.java

	A simple interface for classes that can respond to menu commands.
		
	Anthony Kozar
	September 27, 2015
 */

public interface MenuHandler {

	// menu command constants
	public final static int Cmd_Quit					= 1;
	public final static int Cmd_About					= 2;
	public final static int Cmd_Help					= 3;

	public final static int Cmd_New_Puzzle				= 4;
	public final static int Cmd_Open					= 5;
	public final static int Cmd_Close					= 6;
	public final static int Cmd_Save					= 7;
	public final static int Cmd_Save_As					= 8;
	public final static int Cmd_Export					= 9;
	
	public final static int Cmd_Edit_Cell_Values		= 20;
	public final static int Cmd_Edit_Clues				= 21;
	public final static int Cmd_Edit_Reserved_Cells		= 22;
	public final static int Cmd_Edit_Regions			= 23;
	public final static int Cmd_Solve_Next				= 24;
	public final static int Cmd_Solve_All				= 25;
	public final static int Cmd_Reset_Puzzle			= 26;
	public final static int Cmd_Clear_Puzzle			= 27;

	public final static int Cmd_4x4_Easy_1				= 30;
	public final static int Cmd_9x9_My_First			= 31;
	public final static int Cmd_9x9_My_Second			= 32;
	public final static int Cmd_5x5_Easy_1				= 33;
	
	// Should return true if the command was handled, otherwise false.
	public boolean DoMenuCommand(int menuCommand);

}
