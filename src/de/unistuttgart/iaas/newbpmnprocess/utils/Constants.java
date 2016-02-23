package de.unistuttgart.iaas.newbpmnprocess.utils;



//FIXME: replace all \\ with File.separator
public class Constants {

	// Section 1
	// Constants used in CreateWarArchive class
//	public static   String BpmnFilesPath="C:\\Users\\Abdul Wahab\\workspace\\thesis\\newProcess";
//	public static   String ClassFilesPath = "C:\\Users\\Abdul Wahab\\camundaworkspace\\loan-approval\\target\\classes\\org\\camunda\\bpm\\getstarted";
//	public static   String ProcessXmlFilePath = "C:\\Users\\Abdul Wahab\\workspace\\thesis\\DeploymentDescriptor\\processes.xml";
//	public static   String JavaFilePath = "C:\\Users\\Abdul Wahab\\camundaworkspace\\loan-approval\\src\\main\\java\\org\\camunda\\bpm\\getstarted\\";
//	public static   String CmdCommand = "cd \"C:\\Users\\Abdul Wahab\\camundaworkspace\\loan-approval\" && mvn clean install -Dmaven.test.skip=true";

	public static   String BpmnFilesPath="../processsynthesis/newProcess";
//TODO: how to handle these paths
//	public static   String ClassFilesPath = "C:\\Users\\Abdul Wahab\\camundaworkspace\\loan-approval\\target\\classes\\org\\camunda\\bpm\\getstarted";
//	public static   String ProcessXmlFilePath = "C:\\Users\\Abdul Wahab\\workspace\\thesis\\DeploymentDescriptor\\processes.xml";
//	public static   String JavaFilePath = "C:\\Users\\Abdul Wahab\\camundaworkspace\\loan-approval\\src\\main\\java\\org\\camunda\\bpm\\getstarted\\";
//	public static   String CmdCommand = "cd \"C:\\Users\\Abdul Wahab\\camundaworkspace\\loan-approval\" && mvn clean install -Dmaven.test.skip=true";

	public static   String JavaType=".java";
	public static   String ClassType=".class";
	public static   String BpmnType=".bpmn";
	public static   String ClassnamePrefix="Process";
	public static   String PomFilename="pom.xml";
	public static   String WarClassFilesPathPrefix="org/camunda/bpm/getstarted/";
	public static   String WarDeploymentXmlPathPostfix="/META-INF/processes.xml";
	public static   String WindowsOpenCommandPrompt="cmd.exe";
	public static   String WindowsCommandPromptStyle="/c";
	public static   String ClassAnnotationName="ProcessApplication";
	public static   String InheritClassName="ServletProcessApplication";
	public static   String [] ImportDecleration1 = { "org", "camunda", "bpm", "application", "ProcessApplication" };
	public static   String [] ImportDecleration2 = { "org", "camunda", "bpm", "application", "impl", "ServletProcessApplication" };
	public static   String [] PackageName = { "org", "camunda", "bpm", "getstarted" };
	// End Section 1
	
	// Section 2
	// Constants used in FileLoad class
	//public static   String FragmentsDirectoryPath="C:\\Users\\Abdul Wahab\\workspace\\CleanBpmnFiles\\Fragments";
	public static   String FragmentsDirectoryPath="../../fragments";
	//public static   String FragmentsDirectoryPath="/home/skourama/workspace/processsynthesis/testFragments/newTest";
	//End Section 2
	
	// Section 3
	// Constants used in ModelInstanceNew class
	public static   String BpmnStartEventElement="StartEventImpl";
	public static   String BpmnEndEventElement="EndEventImpl";
	public static   String BpmnParallelGatewayElement="ParallelGatewayImpl";
	public static   String BpmnExclusiveGatewayElement="ExclusiveGatewayImpl";
	public static   String activitiesRulebookPath="src/de/unistuttgart/iaas/newbpmnprocess/utils/ActivitiesRules2.drl";
	public static   String gatewaysRulebookPath="src/de/unistuttgart/iaas/newbpmnprocess/utils/GatewaysRules.drl";
	//End Section 3
	
	// Section 4
	// Constants used in FragmentsCompatibility class
	public static   String IncomingAttribute="incoming";
	public static   String OutgoingAtribute="outgoing";
	public static   String StartElement="start";
	public static   String EndElement="end";
	//End Section 4
	
	// Section 5
	// Constants used in NewProcessComposer class
	public static   String StartEventId="theStart";
	public static   String EndEventId="theEnd";
	public static   String NewProcessesDirectoryPath="../processsynthesis/newProcess";
	public static   String BpmnFileName="fragment1";
	public static   String TargetNamespaceValue="sample1.main";
	public static   String BpmnCallActivityElement="CallActivityImpl";
	//End Section 5
	
	// Section 6
	// Constants used in CamundaSpecificModification class
	public static  String ActivityId="node";
	public static   String GatewayId="gate";
	public static   String NewProcessesDirectoryFullPath="../processsynthesis/newProcess/fragment1.bpmn";
	public static   String NewProcessFullPath="../processsynthesis/newProcess/EngineSpecificNewProcess.bpmn";
	public static   String StartEventElement="bpmn2:startEvent";
	public static   String IdAttribute="id";
	public static   String SequenceFlowElement="bpmn2:sequenceFlow";
	public static   String SourceRefAttribute="sourceRef";
	public static   String TargetRefAttribute="targetRef";
	public static   String EndEventElement="bpmn2:endEvent";
	public static   String CallActivityElement="bpmn2:callActivity";
	public static   String ServiceTaskElement="bpmn2:serviceTask";
	public static   String ScriptTaskElement="bpmn2:scriptTask";
	public static   String ExclusiveGatewayElement="bpmn2:exclusiveGateway";
	public static   String ParallelGatewayElement="bpmn2:parallelGateway";
	public static   String ProcessElement="bpmn2:process";
	public static   String DefinitionsElement="bpmn2:definitions";
	public static   String NameAtribute="name";
	public static   String CalledElementAttribute="calledElement";
	public static   String CamundaCalledElementValue="invoice";
	public static   String CamundaScriptFormatValue="javascript";
	public static   String ExtensionElement="bpmn2:extensionElements";
	public static   String OutgoingElement="bpmn2:outgoing";
	public static   String CamundaConnectorElement="camunda:connector";
	public static   String CamundaConnectorIdElement="camunda:connectorId";
	public static   String CamundaConnectorIdValue="http-connector";
	public static   String CamundaInputOutputElement="camunda:inputOutput";
	public static   String CamundaInputElement="camunda:inputParameter";
	public static   String CamundaInputUrlValue="http://localhost:8080/engine-rest/deployment";
	public static   String NameAttributeUrlValue="url";
	public static   String CamundaInputMethodValue="GET";
	public static   String NameAttributeMethodValue="method";
	public static   String CamundaOutputElement="camunda:outputParameter";
	public static   String CamundaOutputValue="${statusCode}";
	public static   String NameOutputValue="statusCode";
	public static   String FormActivityElement="activiti:formProperty";
	public static   String BpmnVariable="input";
	public static   String TypeAttribute="type";
	public static   String TypeAttributeValue="string";
	public static   String VariableAttribute="variable";
	public static   String VariableAttributeValue="input";
	public static   String DefaultAttribute="default";
	public static   String DefaultAttributeValue="null";
	public static   String RequiredAttribute="required";
	public static   String RequiredAttributeValue="true";
	public static   String ConditionExpressionElement="bpmn2:conditionExpression";
	public static   String ConditionExpressionAttribute="xsi:type";
	public static   String ConditionExpressionAttributeValue="bpmn2:tFormalExpression";
	public static   String DefinitionAttributeXsi="xmlns:xsi";
	public static   String DefinitionAttributeXsiValue="http://www.w3.org/2001/XMLSchema-instance";
	public static   String DefinitionAttributeCamunda="xmlns:camunda";
	public static   String DefinitionAttributeCamundaValue="http://activiti.org/bpmn";
	public static   String DefinitionAttributeDc="xmlns:dc";
	public static   String DefinitionAttributeDcValue="http://www.omg.org/spec/DD/20100524/DC";
	public static   String DefinitionAttributeDi="xmlns:di";
	public static   String DefinitionAttributeDiValue="http://www.omg.org/spec/DD/20100524/DI";
	public static   String DefinitionAttributeSchemaLocation="xsi:schemaLocation";
	public static   String DefinitionAttributeSchemaLocationValue="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd";
	public static   String DefinitionAttributeActiviti="xmlns:activiti";
	public static   String DefinitionAttributeActivitiValue="http://activiti.org/bpmn";
	//End Section 6
	
	// Section 7
	// Constants for DB Connection
	public static   String ConnectionUser = "root";
	public static   String ConnectionPassword = "thesis@iaas";
	//change this here
	public static   String ConnectionUrl = "jdbc:mysql://localhost:3306/FragmentsRepositoryFull3";
	public static   String JdbcDriverName = "com.mysql.jdbc.Driver";
	//End Section 7
	
	// Section 8
	// Constants for JbpmSpecificModification
	public static   String JbpmItemDefinitionElement="bpmn2:itemDefinition";
	public static   String JbpmItemDefIdAttributeValue="InMessageType";
	public static   String JbpmItemDefIdAttributeValue2="_Integer";
	public static   String JbpmItemDefStrucRefAttribute="structureRef";
	public static   String JbpmItemDefStrucRefAttributeValue="java.lang.String";
	public static   String JbpmItemDefStrucRefAttributeValue2="Integer";
	public static   String JbpmMessageElement="bpmn2:message";
	public static   String JbpmMsgIdAttributeValue="InMessage";
	public static   String JbpmMsgItemRefAttribute="itemRef";
	public static   String JbpmInterfaceElement="bpmn2:interface";
	public static   String JbpmIntfIdAttributeValue="ServiceInterface";
	public static   String JbpmIntfNameAttributeValue="com.sample.ClassService";
	public static   String JbpmIntfImplRefAttribute="implementationRef";
	public static   String JbpmOperationElement="bpmn2:operation";
	public static   String JbpmOperNameAttributeValue="sayHello";
	public static   String JbpmInMessageElement="bpmn2:inMessageRef";
	public static   String JbpmPropertyElement="bpmn2:property";
	public static   String JbpmPropItemSubRefAttribute="itemSubjectRef";
	public static   String JbpmCondExpLanguageAttribute="language";
	public static   String JbpmCondExpLanguageAttributeValue="http://www.mvel.org/2.0";
	public static   String JbpmGatewayDirectionAtribute="gatewayDirection";
	public static   String JbpmGatewayDirectionDivergeValue="Diverging";
	public static   String JbpmGatewayDirectionConvergeValue="Converging";
	public static   String JbpmServiceTaskImplAttribute="implementation";
	public static   String JbpmServiceTaskImplAttributeValue="Other";
	public static   String JbpmServiceTaskOperRefAttribute="operationRef";
	public static   String JbpmServiceTaskOperRefAttributeValue="ServiceOperation";
	public static   String JbpmCalledElementValue="com.sample.bpmn.hello";
	public static   String JbpmScriptFormatAttribute="scriptFormat";
	public static   String JbpmScriptFormatAttributeValue="http://www.java.com/java";
	public static   String JbpmScriptElement="bpmn2:script";
	public static   String ProcessTypeAttribute="processType";
	public static   String ProcessTypeAttributeValue="Private";
	public static   String DefinitionAttributeTypeLanguage="typeLanguage";
	public static   String DefinitionAttributeTypeLanguageValue="http://www.java.com/javaTypes";
	public static   String DefinitionAttributeExpressionLanguage="expressionLanguage";
	public static   String DefinitionAttributeExpressionLanguageValue="http://www.mvel.org/2.0";
	public static   String DefinitionAttributeXmlns="xmlns";
	public static   String DefinitionAttributeXmlnsValue="http://www.omg.org/spec/BPMN/20100524/MODEL";
	public static   String DefinitionAttributeXmlnsTns="xmlns:tns";
	public static   String DefinitionAttributeXmlnsTnsValue="http://www.jboss.org/drools";	
	//End Section 8
	
	// Section 9
	// Constants for UserDefinedCriteria
public static   String CriteriaPath ="../processsynthesis/UserCriteria/Criteria.xml";
	public static   String CriteriaFragmentElement ="fragment";
	public static   String CriteriaActivitiesElement ="Activities";
	public static   String CriteriaScrTaskElement="ScriptTasks";
	public static   String CriteriaSerTaskElement="ServiceTasks";
	public static   String CriteriaCallActElement="CallActivities";
	public static   String CriteriaExclGatewayElement="ExclusiveGateways";
	public static   String CriteriaParalGatewayElement="ParallelGateways";
	public static   String CriteriaCfcElement="CFC";
	public static   String CriteriaCfcMinAttribute="Min";
	public static   String CriteriaCfcMaxAttribute="Max";
	public static   String CriteriaProcEngineElement="ProcessEngine";
	//End Section 9
	
	// Main
	public static   String CamundaEngine="Camunda";
	public static   String JbpmEngine="Jbpm";
}
