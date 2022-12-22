package main;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


import generated.*;
import prettyprinter.MiniGoPrintListener;
import bytecodegen.BytecodeGenListener;


public class Main {
	enum OPTIONS {
		PRETTYPRINT, BYTECODEGEN, UCODEGEN, ERROR
	}
	private static OPTIONS getOption(String[] args){
		if (args.length < 1)
			return OPTIONS.BYTECODEGEN;
		
		if (args[0].startsWith("-p") 
				|| args[0].startsWith("-P"))
			return OPTIONS.PRETTYPRINT;
		
		if (args[0].startsWith("-b") 
				|| args[0].startsWith("-B"))
			return OPTIONS.BYTECODEGEN;
		
		if (args[0].startsWith("-u") 
				|| args[0].startsWith("-U"))
			return OPTIONS.UCODEGEN;
		
		return OPTIONS.ERROR;
	}

	
	public static void main(String[] args) throws Exception
	{
		if (args.length != 3) {
			System.out.println("Usage: java -jar [compiler_name] [option] [target] [output]");
			System.out.println("\n[options]");
			System.out.println("-p, -P\tMinigo to minigo pretty print");
			System.out.println("-b, -B\tMinigo to JVM bytecode compile");
			return;
		}
		CharStream codeCharStream = CharStreams.fromFileName(args[1]);
		MiniGoLexer lexer = new MiniGoLexer(codeCharStream);
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		MiniGoParser parser = new MiniGoParser( tokens );
		ParseTree tree = parser.program();

		ParseTreeWalker walker = new ParseTreeWalker();
		switch (getOption(args)) {
			case PRETTYPRINT:
				walker.walk(new MiniGoPrintListener(args[2]), tree);
				break;
			case BYTECODEGEN:
				walker.walk(new BytecodeGenListener(args[2]), tree);
				break;
//			case UCODEGEN:
//				walker.walk(new UCodeGenListener(), tree );
//				break;
				
			default:
				break;
		}

	}
}