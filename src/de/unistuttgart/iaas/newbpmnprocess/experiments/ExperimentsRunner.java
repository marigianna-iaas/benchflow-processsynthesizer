/**
 * 
 */
package de.unistuttgart.iaas.newbpmnprocess.experiments;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import de.unistuttgart.iaas.newbpmnprocess.composer.NewProcessComposer;
import de.unistuttgart.iaas.newbpmnprocess.composer.SizeCombinationsCalculator;
import de.unistuttgart.iaas.newbpmnprocess.configuration.ErrorHandler;
import de.unistuttgart.iaas.newbpmnprocess.configuration.Experiment;
import de.unistuttgart.iaas.newbpmnprocess.configuration.ExperimentsConfigurationLoader;
import de.unistuttgart.iaas.newbpmnprocess.configuration.IError;
import de.unistuttgart.iaas.newbpmnprocess.configuration.IErrorHandler;
import de.unistuttgart.iaas.newbpmnprocess.configuration.Settings;
import de.unistuttgart.iaas.newbpmnprocess.criteria.UserDefinedCriteria;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentExt;
import de.unistuttgart.iaas.newbpmnprocess.processengines.ProcessEngine;

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
							ArrayList<ArrayList<Integer>> sizesCombinations =  
									SizeCombinationsCalculator.getValidSizeCombinations(experiment.getCounterOfNodes(), experiment.getCounterOfCriteria(), settings.getMinAllowedNumberOfNodes());
							String newProcessFilePath = null;
							int counter = 0;
							int noOfTriedCombinations = 0;
							log.setNoOfTriedCombinations(sizesCombinations.size());
							for(List<Integer> sizesCombination : sizesCombinations)
							{
								noOfTriedCombinations++;
								System.out.println("sizesCombinations: " + (++counter) + " out of " + sizesCombinations.size() + " sizesCombination" + sizesCombination);
								log.startSelectFragments();
								
								List<List<FragmentExt>> selectedFragments = udc.getSelectedFragments(sizesCombination); //this will return the selected fragments
								
								log.endSelectFragments();
								
								if(selectedFragments.size()>=2) 
								{
									log.startComposition();
									newProcessFilePath = composition(selectedFragments, experiment);
									log.endComposition();
									
									if(newProcessFilePath  != null)
									{
										log.startEngineSpecific();
										engineSpecific(experiment.getProcessEngine(), newProcessFilePath);
										//engineSpecific(experiment.getProcessEngine(), Constants.NewProcessesDirectoryFullPath);
										log.endEngineSpecific();
										log.setModelFound(true);
										log.setCombinations(sizesCombination.toString());
										log.setModelName(newProcessFilePath);
										log.setNoOfTriedCombinations(noOfTriedCombinations);
										break;
									}
								}
								udc.dumpSelectedFragments();
							}
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
	
	//
	public static List<List<FragmentExt>> selectFragments (Experiment experiment, List<Integer> sizesCombination){
		
		return new UserDefinedCriteria(experiment.getPath()).getSelectedFragments(sizesCombination);
	}
	
	public static String composition(List<List<FragmentExt>> selectedFragments, Experiment experiment){
		//check fragment count inside class
		//settings.getMinAllowedNumberOfNodes()
		String newProcessFilePath = null;
		if(selectedFragments.size()>=2) 
		{
			newProcessFilePath = NewProcessComposer.composeNewProcess(selectedFragments ,  experiment.getCounterOfNodes());
		}
		else
		{
			System.err.println("Could not find matching fragments on the DB. Change the criteria and try again");
		}
		return newProcessFilePath;
	}
	
	public static void engineSpecific(ProcessEngine processEngine, String path){
		switch (processEngine) {
			case Camunda:
				ProcessEngine.Camunda.modify(path);
				break;
	
			default:
				break;
		}
		
	}
}
