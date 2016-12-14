import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class TaintAnalysisWrapper extends BodyTransformer {

	@Override
	@SuppressWarnings("rawtypes")
	protected void internalTransform(Body body, String phase, Map options) {
		SootMethod sootMethod = body.getMethod();
		//System.out.println(body);
		/*System.out.println("Soot Method");
		System.out.println(sootMethod);*/
		UnitGraph g = new BriefUnitGraph(sootMethod.getActiveBody());
		/*System.out.println("Unit Graph");
		System.out.println(g);*/
		new TaintAnalysisMain(g);
	}
}
