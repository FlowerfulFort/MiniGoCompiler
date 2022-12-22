package bytecodegen;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.MiniGoBaseListener;
import generated.MiniGoParser.*;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bytecodegen.BytecodeGenListenerHelper.*;
import static bytecodegen.SymbolTable.*;

public class BytecodeGenListener extends MiniGoBaseListener implements ParseTreeListener {
    public ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    SymbolTable symbolTable = new SymbolTable();

    int tab = 0;
    int label = 0;
    final String filename;
    public BytecodeGenListener(String filename) {
        super();
        this.filename = filename;
    }
    // program	: decl+
    @Override
    public void enterFun_decl(Fun_declContext ctx) {
        // symbolTable 초기화. _localVarId = 0.
        symbolTable.initFunDecl();

        String fname = getFunName(ctx);
        ParamsContext params;

        if (fname.equals("main")) {
            symbolTable.putLocalVar("args", Type.INTARRAY);
        } else {
            symbolTable.putFunSpecStr(ctx);
            params = (ParamsContext) ctx.getChild(3);
            symbolTable.putParams(params);
        }
    }


    // var_decl	:  dec_spec IDENT  type_spec
    //		| dec_spec IDENT type_spec '=' LITERAL
    //		| dec_spec IDENT '[' LITERAL ']' type_spec
    @Override
    public void enterVar_decl(Var_declContext ctx) {
        String varName = ctx.IDENT().getText();

        if (isArrayDecl(ctx)) {
//            symbolTable.putGlobalVar(varName, Type.INTARRAY);
            symbolTable.putLocalVar(varName, Type.INTARRAY);
        }
        else if (isDeclWithInit(ctx)) {
            int lit = Integer.parseInt(ctx.LITERAL().getText());
//            symbolTable.putGlobalVarWithInitVal(varName, Type.INT, lit);
            symbolTable.putLocalVarWithInitVal(varName, Type.INT, lit);
            // Fill here
        }
        else  { // simple decl
//            symbolTable.putGlobalVar(varName, Type.INT);
            symbolTable.putLocalVar(varName, Type.INT);
        }
    }

    @Override
    public void exitProgram(ProgramContext ctx) {
        String classProlog = getFunProlog();

        String fun_decl = "", var_decl = "";

        for(int i = 0; i < ctx.getChildCount(); i++) {
            if(isFunDecl(ctx, i))
                fun_decl += newTexts.get(ctx.decl(i));
            else
                var_decl += newTexts.get(ctx.decl(i));
        }

        newTexts.put(ctx, classProlog + var_decl + fun_decl);

        String program = newTexts.get(ctx);
        System.out.println(program);

        File file = new File(filename);
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(program);      // 문자열을 write함.
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // decl	: var_decl | fun_decl
    @Override
    public void exitDecl(DeclContext ctx) {
        String decl = "";
        if(ctx.getChildCount() == 1)
        {
            if(ctx.var_decl() != null)				//var_decl
                decl += newTexts.get(ctx.var_decl());
            else							//fun_decl
                decl += newTexts.get(ctx.fun_decl());
        }
        newTexts.put(ctx, decl);
    }

    // stmt	: expr_stmt | compound_stmt | if_stmt | for_stmt | return_stmt
    @Override
    public void exitStmt(StmtContext ctx) {
        String stmt = "";
        if(ctx.getChildCount() > 0)
        {
            stmt += newTexts.get(ctx.getChild(0));
            // <(0) Fill here>				
        }
        newTexts.put(ctx, stmt);
    }

    // expr_stmt	: expr ';'
    @Override
    public void exitExpr_stmt(Expr_stmtContext ctx) {
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
    }

    // fun_decl := FUNC IDENT ( params ) type_spec compound_stmt
    @Override
    public void exitFun_decl(Fun_declContext ctx) {
        // <(2) Fill here!>
        String fname = ctx.IDENT().getText();
        String func = funcHeader(ctx, fname);
        // TODO: fill more
        func += newTexts.get(ctx.compound_stmt());
        // 만약 메소드에 return 문이 없다면 VerifyError를 뿜는다.
        // return_stmt가 source code에 없어도 자동으로 넣어주는 synaptic sugar.
        if(!func.matches("[\\s\\S]*\n(i?)return\n[\\s\\S]*"))
            func += "return\n";
        func += ".end method\n";
        newTexts.put(ctx, func);
    }


    private String funcHeader(Fun_declContext ctx, String fname) {
        return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n"
                + "\t" + ".limit stack " 	+ getStackSize(ctx) + "\n"
                + "\t" + ".limit locals " 	+ getLocalVarSize(ctx) + "\n";

    }



    @Override
    public void exitVar_decl(Var_declContext ctx) {
        String varName = ctx.IDENT().getText();
        String varDecl = "";

//        if (isDeclWithInit(ctx)) {
//            varDecl += "putfield " + varName + "\n";
//            // v. initialization => Later! skip now..:
//        }
        if (isDeclWithInit(ctx)) {
            String init = ctx.LITERAL().getText();
            varDecl = String.format(
                    "ldc %s\nistore %s\n", init, symbolTable.getVarId(ctx)
            );
        } else if (ctx.LITERAL() == null) {
            // 초기화가 없을 경우, 0으로 초기화 하는 synaptic sugar.
            varDecl = String.format("ldc 0\nistore %s\n", symbolTable.getVarId(ctx));
        }
        newTexts.put(ctx, varDecl);
    }

    @Override
    public void exitLocal_decl(Local_declContext ctx) {
        // local_decl := var_decl
        newTexts.put(ctx, newTexts.get(ctx.var_decl()));
    }



    // compound_stmt	: '{' local_decl* stmt* '}'
    @Override
    public void exitCompound_stmt(Compound_stmtContext ctx) {
        // <(3) Fill here>
        List<Local_declContext> list_decl = ctx.local_decl();
        List<StmtContext> list_stmt = ctx.stmt();
        StringBuilder sb = new StringBuilder();
        for (Local_declContext local_declContext: list_decl) {
            sb.append(newTexts.get(local_declContext));
        }
        for (StmtContext stmtContext: list_stmt) {
            sb.append(newTexts.get(stmtContext));
        }

        newTexts.put(ctx, sb.toString());
    }

    // if_stmt		:  IF  expr  stmt
    //		| IF  expr  stmt ELSE stmt   ;
    @Override
    public void exitIf_stmt(If_stmtContext ctx) {
        String stmt = "";
        String condExpr= newTexts.get(ctx.expr());
        String thenStmt = newTexts.get(ctx.stmt(0));

        String lend = symbolTable.newLabel();
        String lelse = symbolTable.newLabel();

        if(noElse(ctx)) {
            stmt += condExpr + "\n"
                    + "ifeq " + lend + "\n"
                    + thenStmt + "\n"
                    + lend + ":"  + "\n";
        }
        else {
            String elseStmt = newTexts.get(ctx.stmt(1));
            stmt += condExpr + "\n"
                    + "ifeq " + lelse + "\n"
                    + thenStmt + "\n"
                    + "goto " + lend + "\n"
                    + lelse + ":\n" + elseStmt + "\n"
                    + lend + ":"  + "\n";
        }

        newTexts.put(ctx, stmt);
    }
    // for_stmt := FOR expr stmt
    @Override
    public void exitFor_stmt(For_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        String loopStart = symbolTable.newLabel();
        String loopEnd = symbolTable.newLabel();
        String condExpr = newTexts.get(ctx.expr());
        String statement = newTexts.get(ctx.stmt());
        sb.append(loopStart).append(":\n");
        sb.append(condExpr).append("ifeq ").append(loopEnd).append('\n');
        sb.append(statement).append("goto ").append(loopStart).append('\n');
        sb.append(loopEnd).append(":\n");
        newTexts.put(ctx, sb.toString());
    }

    // return_stmt	: RETURN
    //		| RETURN expr
    //		| RETURN expr ',' expr	 ;
    @Override
    public void exitReturn_stmt(Return_stmtContext ctx) {
        // <(4) Fill here>
        // return_stmt := RETURN | RETURN expr;
        String stmt = "";
        if (ctx.getChildCount() == 1) {
            stmt += "return\n";
        }
        else if (ctx.getChildCount() == 2) { // RETURN expr.
            stmt += newTexts.get(ctx.expr(0));
            stmt += "ireturn\n";
        }
        newTexts.put(ctx, stmt);
    }


    // warning! Too many holes. You should check the rules rather than use them as is.
    @Override
    public void exitExpr(ExprContext ctx) {
        String expr = "";

        if(ctx.getChildCount() <= 0) {
            newTexts.put(ctx, "");
            return;
        }

        if(ctx.getChildCount() == 1) { // IDENT | LITERAL
            if(ctx.IDENT() != null) {
                String idName = ctx.IDENT().getText();
                if(symbolTable.getVarType(idName) == Type.INT) {
                    expr += "iload " + symbolTable.getVarId(idName) + " \n";
                }
                //else	// Type int array => Later! skip now..
                //	expr += "           lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
            } else if (ctx.LITERAL() != null) {
                String literalStr = ctx.LITERAL().getText();
                expr += "ldc " + literalStr + " \n";
            }
        } else if(ctx.getChildCount() == 2) { // UnaryOperation
            expr = handleUnaryExpr(ctx, expr);
        } else if(ctx.getChildCount() == 3) {
            if(ctx.getChild(0).getText().equals("(")) { 		// '(' expr ')'
                expr = newTexts.get(ctx.expr(0));

            } else if(ctx.getChild(1).getText().equals("=")) { 	// IDENT '=' expr
                expr = newTexts.get(ctx.expr(0))
                        + "istore " + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";

            } else { 											// binary operation
                expr = handleBinExpr(ctx, expr);

            }
        }
        // IDENT '(' args ')' |  IDENT '[' expr ']'
        else if(ctx.getChildCount() == 4) {
            if(ctx.args() != null){		// function calls
                expr = handleFunCall(ctx, expr);
            } else { // expr
                // Arrays: TODO  
            }
        }
        // IDENT '[' expr ']' '=' expr
        else { // Arrays: TODO			*/
        }
        newTexts.put(ctx, expr);
    }


    private String handleUnaryExpr(ExprContext ctx, String expr) {
        String l1 = symbolTable.newLabel();
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        // expr := op expr
        expr += newTexts.get(ctx.expr(0));
        String name = ctx.expr(0).IDENT().getText();
        switch(ctx.getChild(0).getText()) {
            case "-":
                expr += "ineg \n"; break;
            case "--":
                expr += "ldc 1" + "\n"
                        + "isub" + "\n";
                expr += "istore " + symbolTable.getVarId(name) + "\n";
                break;
            case "++":
                expr += "ldc 1" + "\n"
                        + "iadd" + "\n";
                expr += "istore " + symbolTable.getVarId(name) + "\n";
                break;
            case "!":
                expr += "ifeq " + l2 + "\n"
                        + l1 + ": " + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": " + "ldc 1" + "\n"
                        + lend + ": " + "\n";
                break;
            case "+":
        }
        return expr;
    }


    private String handleBinExpr(ExprContext ctx, String expr) {
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        expr += newTexts.get(ctx.expr(0));  // iload_x
        expr += newTexts.get(ctx.expr(1));  // iload_y

        // expr := expr op expr
        switch (ctx.getChild(1).getText()) {    // op
            case "*":
                expr += "imul \n"; break;
            case "/":
                expr += "idiv \n"; break;
            case "%":
                expr += "irem \n"; break;
            case "+":		// expr(0) expr(1) iadd
                expr += "iadd \n"; break;
            case "-":
                expr += "isub \n"; break;

            case "==":
                expr += "isub " + "\n"
                        + "ifeq " + l2 + "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ":\n" + "ldc 1\n"
                        + lend + ": " + "\n";
                break;
            case "!=":
                expr += "isub " + "\n"
                        + "ifne " + l2 + "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ":\n" + "ldc 1\n"
                        + lend + ": " + "\n";
                break;
            case "<=":
                // <(5) Fill here> lesser equal
                expr += "isub " + "\n"
                        + "ifle " + l2 + "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ":\n" + "ldc 1\n"
                        + lend + ": " + "\n";
                break;
            case "<":
                // <(6) Fill here> lesser than
                expr += "isub " + "\n"
                        + "iflt " + l2 + "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ":\n" + "ldc 1\n"
                        + lend + ": " + "\n";
                break;

            case ">=":
                // <(7) Fill here> greater equal
                expr += "isub " + "\n"
                        + "ifge " + l2 + "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ":\n" + "ldc 1\n"
                        + lend + ": " + "\n";
                break;

            case ">":
                // <(8) Fill here> greater than
                expr += "isub " + "\n"
                        + "ifgt " + l2 + "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ":\n" + "ldc 1\n"
                        + lend + ": " + "\n";
                break;
                /* 만약 stack의 top이(expr2) 1인 경우, 결과값은 expr1 임.
                 * 만약 stack의 top이 0인경우, 다음 스택도 pop하고 0을 집어넣음(false). */
            case "and":
                expr +=  "ifne "+ lend + "\n"
                        + "pop" + "\n" + "ldc 0" + "\n"
                        + lend + ": " + "\n"; break;

                /* 만약 stack의 top이(expr2) 0인 경우, 결과값은 expr1 임.
                 * 만약 stack의 top이 1인 경우, 다음 변수를 pop하고 1을 집어넣음(true). */
            case "or":
                // <(9) Fill here>
                expr += "ifeq " + lend + "\n"
                        + "pop" + "\n" + "ldc 1" + "\n"
                        + lend + ": " + "\n";
                break;

        }
        return expr;
    }
    private String handleFunCall(ExprContext ctx, String expr) {
        String fname = getFunName(ctx);

        if (fname.equals("_print")) {		// System.out.println	
            expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n"
                    + newTexts.get(ctx.args())
                    + "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
        } else {
            expr = newTexts.get(ctx.args())
                    + "invokestatic " + getCurrentClassName()+ "/" + symbolTable.getFunSpecStr(fname) + "\n";
        }

        return expr;

    }

    // args	: expr (',' expr)* | ;
    @Override
    public void exitArgs(ArgsContext ctx) {

        String argsStr = "\n";

        for (int i=0; i < ctx.expr().size() ; i++) {
            argsStr += newTexts.get(ctx.expr(i)) ;
        }
        newTexts.put(ctx, argsStr);
    }

}
