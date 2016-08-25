/**
 * 
 */
package de.unistuttgart.iaas.newbpmnprocess.experiments;

import java.util.List;
import java.util.Scanner;

import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;

import de.unistuttgart.iaas.newbpmnprocess.composer.NewProcessComposer;
import de.unistuttgart.iaas.newbpmnprocess.configuration.ErrorHandler;
import de.unistuttgart.iaas.newbpmnprocess.configuration.Experiment;
import de.unistuttgart.iaas.newbpmnprocess.configuration.ExperimentsConfigurationLoader;
import de.unistuttgart.iaas.newbpmnprocess.configuration.IError;
import de.unistuttgart.iaas.newbpmnprocess.configuration.IErrorHandler;
import de.unistuttgart.iaas.newbpmnprocess.configuration.Settings;
import de.unistuttgart.iaas.newbpmnprocess.criteria.UserDefinedCriteria;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentExt;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentsCollectionSingleton;

/**
 * @author skourama
 *
 */
public class ExperimentsRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("bpmn2", new Bpmn2ResourceFactoryImpl());

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("bpmn", new Bpmn2ResourceFactoryImpl());
		final int LOAD_DB = 1;
		final int SYNTHESIZE_PROCESS = 2;
		
		System.out.println("=========================================================");
		System.out.println("Welcome to the Process Model Synthesizer. Press:");
		System.out.println("1. To load a new database from the default locations (indicated in Constants.class)");
		System.out.println("2. Execute a syntehsis with respect to the criteria");
		System.out.println("=========================================================");
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		int num = in.nextInt();
		if(num == LOAD_DB)
		{
			FragmentsCollectionSingleton fragmentsCollectionInstance =  FragmentsCollectionSingleton.getInstance();
			fragmentsCollectionInstance.loadCollectionFromFiles();
			
			fragmentsCollectionInstance.loadFromCollectionToDB();
		}
		else if (num == SYNTHESIZE_PROCESS)
		{
			
			ExperimentsConfigurationLoader loader = new ExperimentsConfigurationLoader();
			
			//the uri is static, but we can get it from command line
			IErrorHandler errorHandler = new ErrorHandler();
			if(!loader.loadConfiguration("configuration.xml", errorHandler)){
				IError error = errorHandler.getError();
				System.err.println(error.getDescription());
				
				if(error.hasException()){
					error.getException().printStackTrace();
				}
			}
			else{
				
				try{
					Settings settings = loader.getSettings();
					StatisticsLogger logger = new StatisticsLogger();
					for(Experiment experiment : settings.getExperiments()){
						System.out.println("Experiment: " + experiment);
						
						for(int i = 0; i < settings.getNnumberOfExecutions(); ++i){
							System.out.println("Execution: " + i);
							try {
								LoggingEntry log = new LoggingEntry();
								log.setCounterOfCriteria(experiment.getCounterOfCriteria());
								log.setCounterOfNodes(experiment.getCounterOfNodes());
								log.setExecutionNumber(i);
								log.startExperimentTime();
								
								UserDefinedCriteria udc = new UserDefinedCriteria(experiment.getPath());
								udc.readUserDefinedCriteria(); //reads the criteria and creates corresponding criteria objects
								String newProcessFilePath = null;
								log.startSelectFragments();
									
								List<List<FragmentExt>> selectedFragments = udc.getSelectedFragments(); //this will return the selected fragments
									
								log.endSelectFragments();
									
								if(selectedFragments.size()>=2 && selectedFragments.size() == experiment.getCounterOfCriteria()) 
								{
									log.startComposition();
									newProcessFilePath = composition(selectedFragments, experiment);

									log.endComposition();
										
										if(newProcessFilePath  != null)
										{
											//log.startEngineSpecific();
											//engineSpecific(experiment.getProcessEngine(), newProcessFilePath);
											//log.endEngineSpecific();
											log.setModelFound(true);
											log.setModelName(newProcessFilePath);
											System.out.println("Process created: " + newProcessFilePath);							
	
										}
										else{
											System.err.println("Process file path was null");
										}

								}
								else {
									System.err.println("Could not find matching fragments on the DB. Change the criteria and try again");								
								}
									
								udc.dumpSelectedFragments();
								udc.dumpCriteriaList();
								log.endExperimentTime();
								logger.add(log);
								logger.write();
							}catch (Throwable e){
								System.err.println("Execution: " + i);
								System.err.println(experiment.toString());
								e.printStackTrace();
							}
						}
						
					}	
				} catch (Throwable e){
					e.printStackTrace();
				} 
			}

		}
		else
		{
			System.err.println("Wrong selection");
		}
	}
	

	public static String composition(List<List<FragmentExt>> selectedFragments, Experiment experiment){
		//check fragment count inside class
		String newProcessFilePath = null;
		if(selectedFragments.size()>=2 && selectedFragments.size() == experiment.getCounterOfCriteria()) 
		{
			newProcessFilePath = NewProcessComposer.composeNewProcess(selectedFragments);
		}
		else
		{
			System.err.println("Could not find matching fragments on the DB. Change the criteria and try again");
		}
		return newProcessFilePath;
	}
	
//README: keep the following until it is clear what is happenning with the automatic executability creation of the process models	
//	public static void engineSpecific(ProcessEngine processEngine, String path){
//		switch (processEngine) {
//			case Camunda:
//				//ProcessEngine.Camunda.modify(path);
//				break;
//	
//			default:
//				break;
//		}
//		
//	}
}
