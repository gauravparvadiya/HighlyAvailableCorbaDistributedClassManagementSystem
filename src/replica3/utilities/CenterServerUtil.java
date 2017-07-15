package replica3.utilities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Class for containing common utility methods for Center Server.
 * @author Jyotsana Gupta
 */
public class CenterServerUtil 
{
	/**
	 * Writes some text to a specific file.
	 * @param 	fileName	Complete name (with path and extension) of the destination file
	 * @param 	text		Text to be written to the file
	 * @return	true, if the write operation is successful
	 * 			false, otherwise
	 */
	public static synchronized boolean writeToFile(String fileName, String text)
	{
		boolean writeSuccessful = true;
		PrintWriter output = null;
		
		try
		{
			output = new PrintWriter(new FileOutputStream(fileName, true));
			output.println(text);
		}
		catch(FileNotFoundException fnf)
		{
			writeSuccessful = false;
		}
		finally
		{
			output.close();
		}
		
		return writeSuccessful;
	}
}