package bytecodegen;

import generated.MiniGoParser.*;
import java.util.HashMap;
public class BytecodeGenListenerHelper {
	
	// <boolean functions>
	static HashMap<String, String> TypeBook = new HashMap<>() {{
		put("int", "I");
	}};
	static boolean isFunDecl(ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof Fun_declContext;
	}
	
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}
	
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5;
	}


	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}
	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}
	static String getTypeText(Type_specContext typespec) {
			// <Fill in>
		// type_spec := | INT | '[' LITERAL ']' INT
		// assume that array is not available.
		// getTypeText("int") returns "I".
		if (typespec == null)	// type_spec이 없으면 void로 처리함.
			return "V";
		if (typespec.getChildCount() >= 1)
			return TypeBook.get(typespec.INT().getText());
		else return "V";
	}

	// params
	static String getParamName(ParamContext param) {
		// <Fill in>
		// param := IDENT | IDENT type_spec  => extract index(0)
		return param.IDENT().getText();
	}
	
	static String getParamTypesText(ParamsContext params) {
		String typeText = "";
		
		for(int i = 0; i < params.param().size(); i++) {
			Type_specContext typespec = params.param(i).type_spec();
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}
	
	static String getLocalVarName(Local_declContext local_decl) {
		// TODO <Fill in>
		// local_decl := var_decl  => extract index(0)
		// var_decl := dec_spec IDENT type_spec  => extract index(1)
		return local_decl.var_decl().IDENT().getText();
	}
	
	static String getFunName(Fun_declContext ctx) {
		// TODO <Fill in>
		// fun_decl := FUNC IDENT ( params ) type_spec compound_stmt
		// extract index(1)
		return ctx.IDENT().getText();
	}
	
	static String getFunName(ExprContext ctx) {
		// TODO <Fill in>
		// expr := IDENT ( args )
		return ctx.IDENT().getText();
	}
	
	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() < 5;
	}
	
	static String getFunProlog() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(".class public %s\n", getCurrentClassName()));
		sb.append(".super java/lang/Object\n");
		sb.append(".method public <init>()V\n");
		sb.append("aload_0\n");
		sb.append("invokenonvirtual java/lang/Object/<init>()V\n");
		sb.append("return\n");
		sb.append(".end method\n");
		return sb.toString();
	}
	
	static String getCurrentClassName() {
		return "Test";
	}
}
