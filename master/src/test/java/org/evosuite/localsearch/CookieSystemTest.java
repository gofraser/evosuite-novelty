package org.evosuite.localsearch;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.SolverType;
import org.evosuite.TestGenerationContext;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.DSEType;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTest;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.mutation.MutationFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.localsearch.DefaultLocalSearchObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.localsearch.TestSuiteLocalSearch;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.Randomness;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.DseBar;
import com.examples.with.different.packagename.localsearch.DseFoo;
import com.examples.with.different.packagename.strings.Cookie;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class CookieSystemTest extends SystemTest {

	private static final double DEFAULT_LS_PROBABILITY = Properties.LOCAL_SEARCH_PROBABILITY;
	private static final int DEFAULT_LS_RATE = Properties.LOCAL_SEARCH_RATE;
	private static final LocalSearchBudgetType DEFAULT_LS_BUDGET_TYPE = Properties.LOCAL_SEARCH_BUDGET_TYPE;
	private static final long DEFAULT_LS_BUDGET = Properties.LOCAL_SEARCH_BUDGET;
	private static final long DEFAULT_SEARCH_BUDGET = Properties.SEARCH_BUDGET;
	private static final boolean DEFAULT_MINIMIZE = Properties.MINIMIZE;
	private static final boolean DEFAULT_ASSERTIONS = Properties.ASSERTIONS;
	private static final String DEFAULT_Z3_PATH = Properties.Z3_PATH;
	private static final SolverType DEFAULT_DSE_SOLVER = Properties.DSE_SOLVER;
	private static final Criterion[] DEFAULT_CRITERION = Properties.CRITERION;
	
	@Before
	public void init() {
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 100;
		Properties.SEARCH_BUDGET = 50000;
	}

	@After
	public void restoreProperties() {
		Properties.LOCAL_SEARCH_PROBABILITY = DEFAULT_LS_PROBABILITY;
		Properties.LOCAL_SEARCH_RATE = DEFAULT_LS_RATE;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = DEFAULT_LS_BUDGET_TYPE;
		Properties.LOCAL_SEARCH_BUDGET = DEFAULT_LS_BUDGET;
		Properties.SEARCH_BUDGET = DEFAULT_SEARCH_BUDGET;
		Properties.MINIMIZE = DEFAULT_MINIMIZE;
		Properties.ASSERTIONS = DEFAULT_ASSERTIONS;
		Properties.Z3_PATH = DEFAULT_Z3_PATH;
		Properties.DSE_SOLVER = DEFAULT_DSE_SOLVER;
		Properties.CRITERION = DEFAULT_CRITERION;
	}

	@Test
	public void testZ3DSE() {

		if (System.getenv("z3_path")==null) {
			System.out.println("z3_path should be configured for running this test case");
			return;
		}
		
		Properties.Z3_PATH = System.getenv("z3_path");
		Properties.DSE_SOLVER = SolverType.Z3_SOLVER;
		
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 120;
		
		// should it be trivial for DSE ?

		EvoSuite evosuite = new EvoSuite();
		String targetClass = Cookie.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Criterion[] {Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION, Criterion.WEAKMUTATION, 
				Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION, Criterion.CBRANCH};
		
		Properties.MINIMIZE = false;
		Properties.ASSERTIONS = false;
		
		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);


	}
}
