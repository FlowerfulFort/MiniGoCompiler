package bytecodegen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import generated.MiniGoParser.*;

import static bytecodegen.BytecodeGenListenerHelper.*;


public class SymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR
	}
	
	static public class VarInfo {
		Type type; 
		int id;
		int initVal;
		
		public VarInfo(Type type,  int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}
	
	static public class FInfo {
		public String sigStr;
	}
	
	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function 
	
		
	private int _globalVarID = 0;
	private int _localVarID = 0;
	private int _labelID = 0;
	private int _tempVarID = 0;
	
	SymbolTable(){
		initFunDecl();
		initFunTable();
	}
	
	void initFunDecl(){		// at each func decl
		_localVarID = 0;
		_labelID = 0;
		_tempVarID = 32;		
	}
	
	void putLocalVar(String varname, Type type){
		//<Fill here>
		VarInfo v = new VarInfo(type, _localVarID++);
		_lsymtable.put(varname, v);
	}
	
	void putGlobalVar(String varname, Type type){
		//<Fill here>
		VarInfo v = new VarInfo(type, _globalVarID++);
		_gsymtable.put(varname, v);
	}
	
	void putLocalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		VarInfo v = new VarInfo(type, _localVarID++, initVar);
		_lsymtable.put(varname, v);
	}
	void putGlobalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		VarInfo v = new VarInfo(type, _globalVarID++, initVar);
		_gsymtable.put(varname, v);
	}
	
	void putParams(ParamsContext params) {
		List<ParamContext> list = params.param();
		for(int i = 0; i < list.size(); i++) {
		//<Fill here>
			// param := IDENT | IDENT type_spec
			ParamContext param = list.get(i);
			putLocalVar(param.IDENT().getText(), Type.INT);
		}
	}
	
	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";
		
		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}
	
	public String getFunSpecStr(String fname) {		
		// <Fill here>
		return _fsymtable.get(fname).sigStr;
	}

	public String getFunSpecStr(Fun_declContext ctx) {
		// <Fill here>
		// fun_decl := FUNC IDENT '(' params ')' type_spec compound_stmt;
		// extract index(1)
		return getFunSpecStr(ctx.getChild(1).getText());
	}
	
	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		String argtype = getParamTypesText((ParamsContext) ctx.getChild(3));
		String rtype = getTypeText((Type_specContext) ctx.getChild(5));
		String res = "";
		
		// <Fill here>	
		
		res =  fname + "(" + argtype + ")" + rtype;
		
		FInfo finfo = new FInfo();
		finfo.sigStr = res;
		_fsymtable.put(fname, finfo);
		
		return res;
	}
	
	String getVarId(String name){
		// <Fill here>
		VarInfo lvar = _lsymtable.get(name);
		if (lvar != null) {
			return String.valueOf(lvar.id);
		}

		VarInfo gvar = _gsymtable.get(name);
		if (gvar != null) {
			return String.valueOf(gvar.id);
		}
		System.err.printf("label %s is not available.", name);
		System.exit(255);
		return "-1";
	}
	
	Type getVarType(String name){
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}
		
		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}
		
		return Type.ERROR;	
	}
	String newLabel() {
		return "label" + _labelID++;
	}
	
	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		// <Fill here>
		// var_decl := dec_spec IDENT type_spec |
		// 			   dec_spec IDENT type_spec '=' LITERAL |
		//             dec_spec IDENT '[' LITERAL ']' type_spec;
		return getVarId(ctx.getChild(1).getText());
	}

	// local
	public String getVarId(Local_declContext ctx) {
		// <Fill here>
		// local_decl := var_decl;
		return getVarId((Var_declContext) ctx.getChild(0));
	}
}
