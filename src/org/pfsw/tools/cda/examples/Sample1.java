// ===========================================================================
// CONTENT  : CLASS Sample1
// AUTHOR   : M.Duchrow
// VERSION  : 1.0 - 23/03/2014
// HISTORY  :
//  23/03/2014  mdu  CREATED
//
// Copyright (c) 2014, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.tools.cda.examples;

// ===========================================================================
// IMPORTS
// ===========================================================================
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.pf.text.CommandLineArguments;
import org.pf.tools.cda.base.model.ClassContainer;
import org.pf.tools.cda.base.model.ClassInformation;
import org.pf.tools.cda.base.model.ClassPackage;
import org.pf.tools.cda.base.model.GenericClassContainer;
import org.pf.tools.cda.base.model.Workset;
import org.pf.tools.cda.base.model.util.StringFilter;
import org.pf.tools.cda.base.model.workset.ClasspathPartDefinition;
import org.pf.tools.cda.core.init.WorksetInitializer;
import org.pf.tools.cda.core.processing.IProgressMonitor;
import org.pf.tools.cda.core.processing.WaitingIElementsProcessingResultHandler;
import org.pf.util.SysUtil;
import org.pfsw.odem.DependencySet;
import org.pfsw.odem.IDependencyFilter;

/**
 * An example for working with CDA without GUI.
 *
 * @author M.Duchrow
 * @version 1.0
 */
public class Sample1
{
  // =========================================================================
  // CONSTANTS
  // =========================================================================
  private static final boolean IS_MONITORING = false;
  //private static final String CLASS_TO_ANALYZE = "com.shutterfly.apps.AddressEditingUi";
  private static final String CLASS_TO_ANALYZE = "com.shutterfly.cards.BoxCardConf";
  //private static final String CLASS_TO_ANALYZE = "com.shutterfly.db.transaction.FetchSubBatch";
  //private static final String CLASS_TO_ANALYZE = "com.shutterfly.order.SubBatch";
  //private static final String CLASS_TO_ANALYZE = "com.shutterfly.apps.LabelServicesStation";

  // =========================================================================
  // INSTANCE VARIABLES
  // =========================================================================

  // =========================================================================
  // CLASS METHODS
  // =========================================================================
  public static void main(String[] args) throws InterruptedException
  {
    CommandLineArguments commandArgs;
    Sample1 inst;

    inst = new Sample1();
    commandArgs = new CommandLineArguments(args);
    inst.run(commandArgs);
    System.err.flush();
    System.out.flush();
    SysUtil.current().exit(0, 100);
  } // main()

  // =========================================================================
  // CONSTRUCTORS
  // =========================================================================
  public Sample1()
  {
    super();
  } 

  // =========================================================================
  // PROTECTED INSTANCE METHODS
  // =========================================================================
  protected void run(CommandLineArguments args)
  {
    Workset workset;
    String sample;
    
    // Create a workset with a defined classpath
    workset = this.createWorkset();
    // Load all elements on the claspath and pre-analyze them (might take a while!)
    this.initializeWorkset(workset);


//    ClassInformation classInfo;
//    ClassInformation[] classes;
//    ClassInformation[] classespk;
//    GenericClassContainer[] container;
//    container = workset.getClassContainers();
//    ClassPackage[] packages;
//    packages = container[0].getPackages();
//    for (ClassPackage pk : packages)
//    {
//    	this.saveOnePackage(pk);
//    	
//    	classespk = pk.getClassInfos();
//    	System.out.println(pk.getName()+"----------------------------------------"+classespk.length);
//    	for (ClassInformation classInformation : classespk)
//        {
//    		System.out.println(classInformation.getName());
//        }
//    }
//    System.out.println(container[0].getName()+"----------------------------------------"+packages.length);

    this.saveAllClass(workset);
    
    
 
   // this.saveOneClass(CLASS_TO_ANALYZE, workset);
  }
  
  protected void saveOneClass(String className, Workset workset)
  {
    ClassInformation classInfo;
    ClassInformation[] providers;
    classInfo = workset.getClassInfo(className);
List<ClassInformation> providersList = classInfo.getReferredClasses(); 
       
    //System.out.println(classInfo.getName() + " dependes on " + providers.length + " classes:");
   // this.showResult(providers);    

  //  ClassInformation classInfo;
	ClassInformation[] dependantsAll;
    ClassInformation[] dependants;
    ClassInformation[] overlaps;
    
    

    
    // Lookup the class of interest
  //  classInfo = workset.getClassInfo(className);
    
    WaitingIElementsProcessingResultHandler searchHandler = new WaitingIElementsProcessingResultHandler();
    dependantsAll = searchHandler.findDependantsOfClass(classInfo, "01", null, true);
    List dependantsList = new ArrayList(); 

    Collections.addAll(dependantsList, dependantsAll); 
    List overlapsList = new ArrayList();
    Collections.addAll(overlapsList, dependantsAll); 
    overlapsList.retainAll(providersList);
    providersList.removeAll(overlapsList);
    dependantsList.removeAll(overlapsList);
    providers = ClassInformation.collectionToArray(providersList);
    dependants = ClassInformation.collectionToArray(dependantsList); 
    overlaps = ClassInformation.collectionToArray(overlapsList); 
    System.out.println(classInfo.getName() + " has " + dependants.length + " classes depending on it:");
    this.showResult(dependants); 
		try {
			 
			String content = "digraph dependencyGraph {\n concentrate=true;\n ranksep=\"2.0\";\n rankdir=\"LR\"; \n splines=\"ortho\";\n";
 
			File file = new File("outputGV/"+classInfo.getJustClassName()+".gv");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.write("\"" + classInfo.getClassName() + "\" [fontcolor=\"red\"];\n");
		    for (ClassInformation classInformation : providers)
		    {
		    	bw.write("\"" + classInformation.getClassName() + "\" [ fontcolor=\"blue\" ];\n");
		    	bw.write("\"" + classInfo.getClassName() + "\"->\"" + classInformation.getClassName() + "\";\n");
		    }
		    for (ClassInformation classInformation : dependants)
		    {
		    	bw.write("\"" + classInformation.getClassName() + "\" [ fontcolor=\"green\" ];\n");
		    	bw.write("\"" + classInformation.getClassName() + "\"->\"" + classInfo.getClassName() + "\";\n");
		    }
		    for (ClassInformation classInformation : overlaps)
		    {
		    	bw.write("\"" + classInformation.getClassName() + "\" [ fontcolor=\"turquoise\" ];\n");
		    	bw.write("\"" + classInformation.getClassName() + "\"->\"" + classInfo.getClassName() + "\" [dir=both];\n");
		    }
		    bw.write("}");
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}

  }
  
  protected void saveOnePackage(ClassPackage pk)
  {
		try {
			boolean hasEdge = false; 
			String content = "digraph dependencyGraph {\n concentrate=true;\n rankdir=\"LR\"; \nranksep=\"2.0\";\n";
 
			File file = new File("./output/"+pk.getName()+".gv");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			
			ClassInformation[] classespk;
			ClassInformation[] dependons;
	    	classespk = pk.getClassInfos();
	    	for (ClassInformation classInformation : classespk)
	        {
	    		bw.write("\"" + classInformation.getClassName() + "\"; \n");
	    		dependons = ClassInformation.collectionToArray(classInformation.getReferredClasses());  
	    		for (ClassInformation dependon : dependons)
		        {
	    			//bw.write("\"" + dependon.getClassName() + "\" [ shape=\"hexagon\" ];\n");
	    			if(dependon.getClassName().matches("com.shutterfly.*") )
	    			{
			    	bw.write("\"" + classInformation.getClassName() + "\"->\"" + dependon.getClassName() + "\";\n");
			    	hasEdge = true;
	    			}
		        }
	        }
			if (hasEdge)
		    	bw.write("splines=\"ortho\";\n");
		    bw.write("}");
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}

  }
  protected void saveAllClass(Workset workset)
  {
	    ClassInformation[] allClasses;
	    allClasses = workset.getAllContainedClasses();
	    this.showResult(allClasses);    
	    System.out.println(" has: " + allClasses.length + " classes:");
		try {
			 
			String content = "digraph dependencyGraph {\n concentrate=true;\n ranksep=\"2.0\";\n splines=\"ortho\";\n";
 
			File file = new File("lab.gv");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
		    for (ClassInformation classInformation : allClasses)
		    {
		    	bw.write("\"" + classInformation.getClassName() + "\" [ shape=\"hexagon\" ];\n");
		    }
		    bw.write("}");
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (ClassInformation classInformation : allClasses)
	    {
			String className = classInformation.getClassName();
			this.saveOneClass(className, workset);
	    }

  }
  
  protected void showResult(ClassInformation[] resultData)
  {
    for (ClassInformation classInformation : resultData)
    {
      System.out.println(classInformation.getName());
    }
  }

  protected Workset createWorkset()
  {
    Workset workset;
    workset = new Workset("Sample1");
    ClasspathPartDefinition partDefinition;
    
    partDefinition = new ClasspathPartDefinition("/Users/bcheng/perforce/Lab/main/lab.jar");//("example-libs/*.jar");
    workset.addClasspathPartDefinition(partDefinition);
    workset.addViewFilter(new StringFilter("com.shutterfly.*"));
    workset.addIgnoreFilter(new StringFilter("java.*"));
    workset.addIgnoreFilter(new StringFilter("javax.*"));
    return workset;
  }

  protected void initializeWorkset(Workset workset)
  {
    WorksetInitializer wsInitializer;
    IProgressMonitor monitor = null;

    if (IS_MONITORING)
    {      
      monitor = new ConsoleMonitor();
    }
    
    wsInitializer = new WorksetInitializer(workset);
    
    // Running with no progress monitor (null) is okay as well.
    wsInitializer.initializeWorksetAndWait(monitor); 
  }

}
