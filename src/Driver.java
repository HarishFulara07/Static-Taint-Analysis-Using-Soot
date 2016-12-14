import soot.Pack;
import soot.PackManager;
import soot.SootClass;
import soot.SootResolver;
import soot.Transform;
import soot.options.Options;

public class Driver {

	public static void main(String[] args) {
		if(args.length==0) {
			System.err.println("ERROR: No input file");
			System.exit(0);
		}
		
		System.out.println(args);
		
		// apply some options to the soot
		// option specifying soot to use original variable names
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_keep_line_number(true);
		// option specifying soot to produce Jimple file as output
		Options.v().set_output_format(Options.output_format_jimple);
		
		// add a phase to transformer pack by calling Pack.add
		Pack jtp = PackManager.v().getPack("jtp");
		jtp.add(new Transform("jtp.instrumenter", new TaintAnalysisWrapper()));
		
		// resolves the given class
		SootResolver.v().resolveClass("java.lang.CloneNotSupportedException", SootClass.SIGNATURES);
		
		// give control to Soot to process all options
	    // TaintAnalysisWrapper.internalTransform will get called
		soot.Main.main(args);
	}
}