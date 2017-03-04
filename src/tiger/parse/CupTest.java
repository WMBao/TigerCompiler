package tiger.parse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import tiger.errormsg.ErrorMsg;
import Absyn.Exp;
import Absyn.Print;

public class CupTest {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String filename = "Testcases/test.tig";
//		String filename = "Testcases/More/Bad/8.tig";
		ErrorMsg errorMsg = new ErrorMsg(filename);
		InputStream inp = new FileInputStream(filename);
		Yylex lexer = new Yylex(inp, errorMsg);
		parser p = new parser(lexer);
		Object result = null;
		try {
			result = p.parse().value;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Print print = new Print(System.out);
		print.prExp((Exp) result, 0);
		System.out.println("\n");
	}

}
