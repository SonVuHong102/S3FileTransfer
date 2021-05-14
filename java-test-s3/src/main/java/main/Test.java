package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Test {
	public static void main(String arsg[]) {
		try {
			File file = new File("C:\\Users\\Admin\\.aws\\credentials");
			FileWriter inp = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(inp);
			String temp = "[default]\r\n"
					+ "aws_access_key_id = AKIA46JGEQA2WXLTVANK\r\n"
					+ "aws_secret_access_key = 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";
			out.write(temp);
			out.close();
			inp.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
