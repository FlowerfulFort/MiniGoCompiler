/**************************
 * OS: Windows 11
 * Compiler: OpenJDK 16
 * Author: 201802077 김준엽
 **************************/
package prettyprinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import generated.*;

public class MiniGoPrintListener extends MiniGoBaseListener {
    public MiniGoPrintListener(String filename) {
        super();
        this.filename = filename;
    }
    final String filename;
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<>();
    final String tab = "....";  // tab 문자열.
    int depth = 0;              // tab 깊이.
    /* tab("....")을 depth만큼 넣기위해 문자열을 리턴해주는 메소드 */
    private String insertTab() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<depth;i++) {
            sb.append(tab);     // depth만큼 반복해 문자열을 빌드함.
        }
        return sb.toString();
    }
    private String insertTab(int d) {   // 임의의 depth를 사용하는 버전.
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<d;i++) {
            sb.append(tab);
        }
        return sb.toString();
    }
    @Override
    public void exitProgram(MiniGoParser.ProgramContext ctx) {
        // program:= decl+
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<ctx.getChildCount();i++) {
            newTexts.put(ctx, ctx.decl(i).getText());
            // 하위의 모든 decl을 돌면서 문자열을 빌드한다.
            sb.append(newTexts.get(ctx.getChild(i)));
        }
        // 이상하게도, 개행이 여러번 들어가는 문제가 발생하여 만든 조치.
        // (\n)+ 를 모두 \n으로 교체함.
        String program = sb.toString().replaceAll("(\n)+", "\n");

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
    @Override
    public void exitDecl(MiniGoParser.DeclContext ctx) {
        // decl:= var_decl | fun_decl
        // 둘 중 하나로 파생되므로 그냥 newTexts.get() 하여 put한다.
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
    }
    @Override
    public void exitVar_decl(MiniGoParser.Var_declContext ctx) {
        int count = ctx.getChildCount();
        if (count == 6) {     // array declaration
            // 말그대로, dec_spec IDENT [LITERAL] type_spec을 문자열로 만들어 put함.
            newTexts.put(ctx, String.format("%s %s [%s] %s\n",
                    newTexts.get(ctx.dec_spec()),
                    ctx.getChild(1).getText(),
                    ctx.getChild(3).getText(),
                    newTexts.get(ctx.type_spec())));
        }
        else { // var_decl:= dec_spec IDENT type_spec ('=' LITERAL)?
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<count;i++) {
                // dec_spec, type_spec 모두 terminal로 파생되므로 getText로 가져온다.
                sb.append(ctx.getChild(i).getText());
                sb.append(' ');
            }
            // type_spec이 공백이라서 whitespace가 두칸이 되는 상황 방지.
            newTexts.put(ctx,
                    sb.toString().replaceAll("  ", " ").trim());
        }
    }
    @Override
    public void exitDec_spec(MiniGoParser.Dec_specContext ctx) {
        // dec_spec:= VAR 이므로 getText()만 하여 put.
        newTexts.put(ctx, ctx.getChild(0).getText());
    }
    @Override
    public void exitType_spec(MiniGoParser.Type_specContext ctx) {
        /* type_spec:= (|INT|('['LITERAL']') INT) */
        if (ctx.getChildCount() == 0) {          // type_spec:=
            newTexts.put(ctx, "");
        }
        else if (ctx.getChildCount() == 1) {     // type_spec:= INT
            newTexts.put(ctx, ctx.getChild(0).getText());
        }
        else {                                   // type_spec:= '['LITERAL']' INT
            // a[3] int ==> '[' '3' ']' 'int'
            newTexts.put(ctx, String.format("[%s] %s",
                    ctx.getChild(1).getText(), ctx.getChild(3).getText()));
        }
    }
    @Override
    public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
        /* fun_decl:= FUNC IDENT '('params')' type_spec compound_stmt */
        // type_spec이 없을경우.
        if (newTexts.get(ctx.type_spec()).compareTo("") != 0) {
            newTexts.put(ctx, String.format("%s %s(%s) %s %s",
                    ctx.getChild(0).getText(),
                    ctx.getChild(1).getText(),
                    newTexts.get(ctx.params()),
                    newTexts.get(ctx.type_spec()),
                    newTexts.get(ctx.compound_stmt())));
        } else {    // type_spec이 존재할 경우.
            newTexts.put(ctx, String.format("%s %s(%s) %s",
                    ctx.getChild(0).getText(),
                    ctx.getChild(1).getText(),
                    newTexts.get(ctx.params()),
                    newTexts.get(ctx.compound_stmt())));
        }
    }
    @Override
    public void exitParams(MiniGoParser.ParamsContext ctx) {
        /* params:= (|param (',' param)*) */
        // param이 여러개이므로 컨텍스트를 List<>로 뽑는다.
        List<MiniGoParser.ParamContext> p = ctx.param();
        if (ctx.getChildCount() != 0) { // param이 존재할 경우,
            if (p.size() <= 1) {        // param이 1개일 경우,
                newTexts.put(ctx, newTexts.get(p.get(0)));
            } else {                    // param이 여러개일 경우,
                StringBuilder sb = new StringBuilder();
                sb.append(newTexts.get(p.get(0)));
                for (int i=1;i<p.size();i++) {  // ", "을 추가하며 param을 덧붙인다.
                    sb.append(", "); sb.append(newTexts.get(p.get(i)));
                }
                newTexts.put(ctx, sb.toString());
            }
        } else {
            newTexts.put(ctx, "");  // param이 없을경우 빈 문자열을 put한다.
        }
    }
    @Override
    public void exitParam(MiniGoParser.ParamContext ctx) {
        /* param:= (IDENT|IDENT type_spec) */
        if (ctx.getChildCount() == 1) { // param:= IDENT 일 경우,
            newTexts.put(ctx, ctx.getChild(0).getText());   // IDENT만 put한다.
        } else {                        // param:= IDENT type_spec 일 경우,
            newTexts.put(ctx, String.format("%s %s",
            /* 두 텍스트를 whitespace와 함께 포장하여 put한다. */
                    ctx.getChild(0).getText(),
                    newTexts.get(ctx.type_spec())));
        }
    }
    // TODO: 더 추가해야함.
    @Override
    public void exitExpr(MiniGoParser.ExprContext ctx) {
        // Literal, IDENT
        int count = ctx.getChildCount();
        switch (count) {
            case 1: // expr:= (LITERAL | IDENT)
                // 이 경우에는 바로 String으로 변환하여 put함.
                newTexts.put(ctx, ctx.getChild(0).getText()); break;
            case 2: // expr:= op expr
                // 하위 트리에서 expr의 값을 불러와 op와 함께 put.
                newTexts.put(ctx, String.format("%s%s",
                        ctx.getChild(0).getText(),
                        newTexts.get(ctx.expr(0))));
                break;
            case 3:
                // child의 개수가 3개인 문법들을 구분하기 위해 expr 개수로 구분.
                List<MiniGoParser.ExprContext> exprs = ctx.expr();
                if (exprs.size() == 2) {    // expr:= expr op expr
                    newTexts.put(ctx, String.format("%s %s %s",
                            newTexts.get(exprs.get(0)),
                            ctx.getChild(1).getText(),
                            newTexts.get(exprs.get(1))));
                } else if (exprs.size() == 1) {
                    // expr:= (expr)
                    // 첫 character로 '('가 올때는 (expr) 임.
                    if (ctx.getChild(0).getText().compareTo("(") == 0) {
                        newTexts.put(ctx, String.format("(%s)",
                                newTexts.get(exprs.get(0))));
                    } else {    // expr:= IDENT '=' expr
                        // 아닐 경우 IDENT = expr로 put.
                        newTexts.put(ctx, String.format("%s = %s",
                                ctx.getChild(0).getText(),
                                newTexts.get(exprs.get(0))));
                    }
                } break;
            case 4: // expr:= IDENT ('['expr']' | '('args')')
                // 문자열의 형태는 비슷하므로 newTexts.get(ctx.getChild(2))로
                // 통합하여 처리함.
                newTexts.put(ctx, String.format("%s %s%s%s",
                        ctx.getChild(0).getText(),
                        ctx.getChild(1).getText(),
                        newTexts.get(ctx.getChild(2)),
                        ctx.getChild(3).getText()));
                break;
            case 6: // expr:= IDENT '['expr']' = expr
                // 한가지 경우밖에 없으므로 적절하게 pretty print 해줌.
                newTexts.put(ctx, String.format("%s [%s] = %s",
                        ctx.getChild(0).getText(),
                        newTexts.get(ctx.expr(0)),
                        newTexts.get(ctx.expr(1))));
                break;
        }
    }
    @Override
    public void exitArgs(MiniGoParser.ArgsContext ctx) {
        // args:= (expr (',' expr)*|)
        List<MiniGoParser.ExprContext> exprs = ctx.expr();
        if (exprs.size() == 0) {            // args:=
            newTexts.put(ctx, "");  // 비어있으므로 당연히 공백.
        } else if (exprs.size() == 1) {     // args:= expr
            newTexts.put(ctx, newTexts.get(exprs.get(0)));
        } else {    // args:= expr (',' expr)+
            StringBuilder sb = new StringBuilder(newTexts.get(exprs.get(0)));
            sb.append(' ');
            // argument가 여러개이므로 Context List를 돌면서 문자열을 빌드한다.
            for (int i=1;i<exprs.size();i++) {
                sb.append(", ").append(newTexts.get(exprs.get(i))).append(' ');
            }
            // 맨끝에 whitespace가 하나 남으므로 trim()한다.
            newTexts.put(ctx, sb.toString().trim());
        }
    }
    @Override
    public void exitStmt(MiniGoParser.StmtContext ctx) {
        // 모든 statement에 개행을 추가함.
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)) + '\n');
    }
    // TODO: if-else 추가해야함.(완료)
    @Override
    public void exitIf_stmt(MiniGoParser.If_stmtContext ctx) {
        // if_stmt:= (IF expr stmt | IF expr stmt ELSE stmt)
        if (ctx.getChildCount() == 3) { // if_stmt:= IF expr stmt
            // 마찬가지로 각 terminal/non-terminal 마다 띄워준다.
            newTexts.put(ctx, String.format("if %s %s",
                    newTexts.get(ctx.expr()), newTexts.get(ctx.stmt(0))));
        } else {        // if_stmt:= IF expr stmt ELSE stmt
            /* "}\n\telse" 의 형태가 되어야 하므로 else 이전에 insertTab()으로
               depth 카운터만큼 들여쓰기 후 else를 붙인다. */
            newTexts.put(ctx, String.format("if %s %s%selse %s",
                    newTexts.get(ctx.expr()), newTexts.get(ctx.stmt(0)),
                    insertTab(), newTexts.get(ctx.stmt(1))));
        }
    }
    @Override
    public void enterCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
        // Compound_stmt에 진입하기 전, depth 카운터를 1 증가시킨다.
        depth++;
    }
    @Override
    public void exitCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
        //int count_mem = depth;
        // compound_stmt:= {local_decl* stmt*}
        StringBuilder sb = new StringBuilder();
        // 하위 Context(local_decl, stmt)를 모두 순회하기 위해 리스트로 참조한다.
        List<MiniGoParser.Local_declContext> localdec = ctx.local_decl();
        List<MiniGoParser.StmtContext> stmt = ctx.stmt();
        // '{' 다음에 개행한다.
        sb.append("{\n");
        for (int i=0;i<localdec.size();i++) {
            /*for (int j=0;j<depth;j++) */
            sb.append(insertTab());     // 멤버변수 depth만큼 들여쓰기를 한다.
            sb.append(newTexts.get(localdec.get(i)));   // 그후 get한 값을 더한다.
        }
        for (int i=0;i<stmt.size();i++) {
            /*for (int j=0;j<depth;j++) */
            sb.append(insertTab());     // 위와 마찬가지.
            sb.append(newTexts.get(stmt.get(i)));
        }
        // String 빌드가 끝났으면 {} 스코프를 벗어난것이므로 depth 카운터를 1 내린다.
        depth--;
        // 카운터를 내린 후, 다시 들여쓰기를 하고 닫는괄호와 개행문자를 넣는다.
        sb.append(insertTab()); sb.append("}\n");
        newTexts.put(ctx, sb.toString());   // 문자열을 빌드하여 Context에 put한다.
    }
    @Override
    public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
        // local_decl:= var_decl
        // 한쪽으로만 전개되기 때문에 바로 get한 값을 바로 put함.
        // local_decl에 다른 문법이 추가되는 일이 생길 수 있으므로
        // var_decl에서 개행하지 않고 local_decl에서 개행처리.
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)) + '\n');
    }
    @Override
    public void exitReturn_stmt(MiniGoParser.Return_stmtContext ctx) {
        // return_stmt:= (RETURN|RETURN expr|RETURN expr, expr)
        if (ctx.getChildCount() == 1)       // return_stmt:= RETURN
            // RETURN 뿐이므로 단순하게 String으로 가져옴.
            newTexts.put(ctx, ctx.getChild(0).getText());
        else if (ctx.getChildCount() == 2)  // return_stmt:= RETURN expr
            // expr을 하위트리에서 가져오고 포맷팅하여 put.
            newTexts.put(ctx, String.format("%s %s",
                    ctx.getChild(0).getText(),
                    newTexts.get(ctx.expr(0))));
        else {                              // return_stmt:= RETURN expr, expr
            // 위와 마찬가지.
            newTexts.put(ctx, String.format("%s %s, %s",
                    ctx.getChild(0).getText(),
                    newTexts.get(ctx.expr(0)),
                    newTexts.get(ctx.expr(1))));
        }
    }
    // TODO: Fill the method.
    @Override
    public void exitFor_stmt(MiniGoParser.For_stmtContext ctx) {
        // for_stmt:= FOR expr stmt
        // 단순하게, newTexts.get()으로 하위트리에서 값을 끌고옴.
        newTexts.put(ctx, String.format("%s %s %s",
                ctx.getChild(0).getText(),
                newTexts.get(ctx.expr()),
                newTexts.get(ctx.stmt())));
    }
}
