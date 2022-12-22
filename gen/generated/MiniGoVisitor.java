// Generated from java-escape by ANTLR 4.11.1
package generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MiniGoParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MiniGoVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MiniGoParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecl(MiniGoParser.DeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl(MiniGoParser.Var_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#dec_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDec_spec(MiniGoParser.Dec_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#type_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_spec(MiniGoParser.Type_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#fun_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFun_decl(MiniGoParser.Fun_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParams(MiniGoParser.ParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(MiniGoParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(MiniGoParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#expr_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_stmt(MiniGoParser.Expr_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#for_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_stmt(MiniGoParser.For_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#compound_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompound_stmt(MiniGoParser.Compound_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#local_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocal_decl(MiniGoParser.Local_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#if_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_stmt(MiniGoParser.If_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#return_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_stmt(MiniGoParser.Return_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(MiniGoParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniGoParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgs(MiniGoParser.ArgsContext ctx);
}