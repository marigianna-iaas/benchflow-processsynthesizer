package de.unistuttgart.iaas.newbpmnprocess.validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Process;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import de.unistuttgart.iaas.bpmn.util.CompareUtils;
import de.unistuttgart.iaas.newbpmnprocess.composer.ProcessValidationParameters;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentExt;
import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;

/**
 * 
 * @author awahab, skourama
 *
 */
public class Validator {
	
	private static Collection<EClass> activitytypes = Arrays.asList(
			Bpmn2Package.Literals.ACTIVITY);
	private static Collection<EClass> gatewaytypes = Arrays.asList(
			Bpmn2Package.Literals.GATEWAY);
	
		//TODO: rename for criteria validation
		public static boolean doValidation(Process process)
		{
			//Every validation condition is checked in a separate function.
			//If all validations are satisfied return true, else false.
			if(validateTasksCount(process) && validateGatewaysCount(process) && validateCallActivitiesFanout(process) && validateGatewaysFanin(process) && validateGatewaysFanout(process))
			{
				return true;
			}
			else
			{
				return false;
			}
			
		}
		
		//Validating against the total number of tasks in a typical Process
		public static boolean validateTasksCount(Process process)
		{
			int countTasks=0;
			
			for (TreeIterator<EObject> iterator = CompareUtils
					.getAllContentsWithSpecificTypes(process, activitytypes, true); iterator
					.hasNext();) {

				EObject current = iterator.next();
				if (current instanceof FlowNode) {

						countTasks=countTasks+1;
			
				}
			}
			
			if(countTasks>ProcessValidationParameters.MaxTasks)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		
		//Validate against the total number of gateways in a Process
		public static boolean validateGatewaysCount(Process process)
		{
			int countGateways=0;
			
			for (TreeIterator<EObject> iterator = CompareUtils
					.getAllContentsWithSpecificTypes(process, gatewaytypes, true); iterator
					.hasNext();) {

				EObject current = iterator.next();
				if (current instanceof FlowNode) {
					FlowNode node = (FlowNode) current;
					
					if(node.getClass().getSimpleName().matches(Constants.BpmnExclusiveGatewayElement) || node.getClass().getSimpleName().matches(Constants.BpmnParallelGatewayElement))
					{
						countGateways=countGateways+1;
					}				
				}
			}
			
			if(countGateways>ProcessValidationParameters.MaxGateways)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		
		//Validate against the total number of Call Activities in a Process
		public static boolean validateCallActivitiesFanout(Process process)
		{
			boolean Condition=true;
			
			for (TreeIterator<EObject> iterator = CompareUtils
					.getAllContentsWithSpecificTypes(process, activitytypes, true); iterator
					.hasNext();) {

				EObject current = iterator.next();
				if (current instanceof FlowNode) {
					FlowNode node = (FlowNode) current;
					
					if(node.getClass().getSimpleName().matches(Constants.BpmnCallActivityElement))
					{
						if(node.getOutgoing().size()>ProcessValidationParameters.MaxCallActivitiesFanout)
						{
							Condition=false;
							break;
						}
					}				
				}
			}
			
			return Condition;
		}
		
		//Validate against the maximum Fanin value a gateway can have in a Process
		public static boolean validateGatewaysFanin(Process process)
		{
			boolean Condition=true;
			
			for (TreeIterator<EObject> iterator = CompareUtils
					.getAllContentsWithSpecificTypes(process, gatewaytypes, true); iterator
					.hasNext();) {

				EObject current = iterator.next();
				if (current instanceof FlowNode) {
					FlowNode node = (FlowNode) current;
					
					if(node.getClass().getSimpleName().matches(Constants.BpmnExclusiveGatewayElement) || node.getClass().getSimpleName().matches(Constants.BpmnParallelGatewayElement))
					{
						if(node.getIncoming().size()>ProcessValidationParameters.MaxGatewayFanin)
						{
							Condition=false;
							break;
						}
					}				
				}
			}
			
			return Condition;
		}
		
		//Validate against the maximum Fanout value a gateway can have in a Process
		public static boolean validateGatewaysFanout(Process process)
		{
			boolean condition=true;
			
			for (TreeIterator<EObject> iterator = CompareUtils
					.getAllContentsWithSpecificTypes(process, gatewaytypes, true); iterator
					.hasNext();) {

				EObject current = iterator.next();
				if (current instanceof FlowNode) {
					FlowNode node = (FlowNode) current;
					
					if(node.getClass().getSimpleName().matches(Constants.BpmnExclusiveGatewayElement) || node.getClass().getSimpleName().matches(Constants.BpmnParallelGatewayElement))
					{
						if(node.getOutgoing().size()>ProcessValidationParameters.MaxGatewayFanout)
						{
							condition=false;
							break;
						}
					}				
				}
			}
			
			return condition;
		}


		public static boolean checkSizeValidity(
				List<FragmentExt> fragmentsToSynthesize,
				int totalNumberOfFlowNodes) {
			int sumCurrNumberOfFlowNodes = 0;
			for(FragmentExt fragment : fragmentsToSynthesize)
			{
				sumCurrNumberOfFlowNodes += fragment.getNumberOfFlowNodes();
			}
			return sumCurrNumberOfFlowNodes == totalNumberOfFlowNodes;
		}
		
}
