package edu.miami.ccs.goma.utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sreeharsha Venkatapuram, UM Center for Computational Science
 * 
 * Flips lat-lon to lon-lat on a WKT 
 *
 */
public class CoordinateFlipper
{
	public static void main(String[] args)
	{
		String wktStr = args[0];
		System.out.println("original WKT:"+wktStr);
		String flippedCoordsWkt = flipIt(wktStr);
		System.out.println("flipped WKT:"+flippedCoordsWkt);
	}

	public static String flipIt(String wktStr)
    {
		Pattern pattern = Pattern.compile("\\(+.*\\)+"); //one or more open parantheses followed by zero or more characters followed by one or more close parantheses 
		String innerStr = wktStr;
		while(innerStr.contains("("))
		{
			Matcher matcher = pattern.matcher(innerStr);
			if(matcher.find()) 
				innerStr = matcher.group(0).substring(1, matcher.group(0).length()-1);
		}
		String[] prePost = wktStr.split(innerStr);
		String[] coords = innerStr.split(",");
		wktStr = "";
		for(int i=0; i<coords.length; i++)
		{
			String[] coord = coords[i].trim().split(" "); 
			wktStr += coord[1] + " " + coord[0];
			if(i != coords.length-1)
				wktStr += ",";
		}
		return prePost[0]+wktStr+prePost[1];
    }
}
