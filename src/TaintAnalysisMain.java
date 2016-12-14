// TAINT ANALYSIS VARIANT 1
// ASSUMING THAT EACH PRINT AND RETURN STATEMENT WILL HAVE ONLY ONE VARIABLE IN THEM

import java.util.ArrayList;
import java.util.Iterator;

import soot.Body;
import soot.Local;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.tagkit.AbstractHost;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

@SuppressWarnings("rawtypes")
public class TaintAnalysisMain extends ForwardFlowAnalysis {

	Body body;
	FlowSet inval, outval;
	ArrayList<String> origVars = new ArrayList<String>();
	
	@SuppressWarnings("unchecked")
	public TaintAnalysisMain(UnitGraph g) {
		super(g);
		body = g.getBody();
		// carry out actual flow analysis 
		doAnalysis();
	}
	
	@Override
	// defining gen and kill set for a statment
	protected void flowThrough(Object in, Object unit, Object out) {
		inval = (FlowSet)in;
		outval = (FlowSet)out;
		Stmt stmt = (Stmt)unit;
		
		inval.copy(outval);
		
		// Kill and Gen operations
		
		// check if the statement is an assignment statment
		if(stmt instanceof AssignStmt) {
			String[] str = stmt.toString().split("[\\s\\<\\.]+");
			
			// if the assignment statement correspond to a print statement
			if(str.length > 7 && str[3].equalsIgnoreCase("lang") &&
					str[7].equalsIgnoreCase("printstream")) {
				
				outval.remove(((AssignStmt) stmt).getLeftOp().toString());
			}
			// if the assignment statement correspond to an invoke statement
			else if(str.length > 5 && (str[2].equalsIgnoreCase("virtualinvoke") ||
					str[2].equalsIgnoreCase("staticinvoke") || str[2].equalsIgnoreCase("specialinvoke"))) {
				//System.out.println("voila" + stmt);
				outval.remove(((AssignStmt) stmt).getLeftOp().toString());
			}
			else {
				String[] vars = ((AssignStmt) stmt).getRightOp().toString().split("[\\+\\-\\*\\/\\%]+");
				boolean tainted = false;
				
				for(int i = 0; i < vars.length; ++i) {
					vars[i] = vars[i].trim();
					
					if(inval.contains(vars[i])) {
						tainted = true;
						break;
					}
				}
				
				// if the operand on the LHS of assignment statement is not tainted, kill it
				if(!tainted) {
					outval.remove(((AssignStmt) stmt).getLeftOp().toString());
				}
				// else, generate it
				else {
					outval.add(((AssignStmt) stmt).getLeftOp().toString());
				}
			}
		}
		
		if(stmt instanceof ReturnStmt) {
			LineNumberTag tag = (LineNumberTag) ((AbstractHost) unit).getTag("LineNumberTag");
			int lineNumber = 0;
			
			if(tag != null) {
				lineNumber = tag.getLineNumber();
			}
			
			System.out.println();
			System.out.println("Sink present at line : " + lineNumber);
			System.out.print("Variables which are tainted : ");
			
			Iterator it = inval.iterator();
			
			while(it.hasNext()) {
				String var = it.next().toString();
				
				if(origVars.contains(var)) {	
					System.out.print(var + " ");
				}
			}
			
			System.out.println();
		}
		
		if(stmt instanceof InvokeStmt) {
			LineNumberTag tag = (LineNumberTag) ((AbstractHost) unit).getTag("LineNumberTag");
			int lineNumber = 0;
			if(tag != null) {
				lineNumber = tag.getLineNumber();
			}
			
			String invokeStmtName = stmt.getInvokeExpr().getMethod().getName();
			
			if(invokeStmtName.length() >= 5 && invokeStmtName.substring(0, 5).equalsIgnoreCase("print")) {
				System.out.println();
				System.out.println("Sink present at line : " + lineNumber);
				System.out.print("Variables which are tainted : ");
				
				Iterator it = inval.iterator();
				
				while(it.hasNext()) {
					String var = it.next().toString();
					
					if(origVars.contains(var)) {
						System.out.print(var + " ");
					}
				}
				
				System.out.println();
			}
		}
	}
	
	@Override
	protected void copy(Object source, Object dest) {
		FlowSet srcSet = (FlowSet)source;
		FlowSet	destSet = (FlowSet)dest;
		srcSet.copy(destSet);
		
	}
	
	@Override
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inval1=(FlowSet)in1;
		FlowSet inval2=(FlowSet)in2;
		FlowSet outSet=(FlowSet)out;
		// Taint analysis is a MAY analysis
		inval1.union(inval2, outSet);
	}
	
	
	@Override
	// Contents of the lattice element for the entry point
	protected Object entryInitialFlow() {
		ArraySparseSet arraySparseSet = new ArraySparseSet();
		
		// get method parameters
		ArrayList<String> params = getMethodParamsAndLocals(body.getMethod().getParameterCount());
		
		for(String param : params) {
			// all method parameters are tainted
			// so, add them to the content of the lattice element for the entry point
			arraySparseSet.add(param);
		}
		
		//System.out.println(params);
		return arraySparseSet;
	}
	
	@Override
	// Contents of the lattice element for all the other points
	protected Object newInitialFlow() {
		return new ArraySparseSet();
	}
	
	// function to get parameters of a method
	private ArrayList<String> getMethodParamsAndLocals(int paramsCount) {
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<String> locals = new ArrayList<String>();
		
		// loop through all local variables inside the soot representation of the java method
		// method parameters are also represented as local variables in soot representation of the java method
		for(Local local: body.getLocals()) {
			
			if(local.getName().equals("this")) {
				locals.add(local.getName());
			}
			else if(paramsCount == 0) {
				locals.add(local.getName());
				
				if(!local.getName().substring(0, 1).equals("$")) {
					origVars.add(local.getName());
				}
			}
			else {
				params.add(local.getName());
				origVars.add(local.getName());
				--paramsCount;
			}
		}
		
		return params;
	}
}
