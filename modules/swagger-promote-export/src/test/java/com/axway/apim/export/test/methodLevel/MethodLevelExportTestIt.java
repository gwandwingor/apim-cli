package com.axway.apim.export.test.methodLevel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.export.test.ExportTestAction;
import com.axway.apim.swagger.api.properties.authenticationProfiles.AuthenticationProfile;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.axway.apim.swagger.api.properties.tags.TagMap;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class MethodLevelExportTestIt extends TestNGCitrusTestRunner {

	private ExportTestAction swaggerExport;
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {		
		ObjectMapper mapper = new ObjectMapper();

		swaggerExport = new ExportTestAction();
		swaggerImport = new ImportTestAction();
		description("Import an API with method level settings to export it afterwards");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api/test/"+this.getClass().getSimpleName()+"-${apiNumber}");
		variable("apiName", this.getClass().getSimpleName()+"-${apiNumber}");
		variable("state", "published");
		variable("exportLocation", "citrus:systemProperty('java.io.tmpdir')");
		variable(ExportTestAction.EXPORT_API,  "${apiPath}");
		
		// These are the folder and filenames generated by the export tool 
		variable("exportFolder", "api-test-${apiName}");
		variable("exportAPIName", "${apiName}.json");

		echo("####### Importing the API with method level setting, which should exported in the second step #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/test/export/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/test/export/files/methodLevel/inbound-api-key-and-cors.json");
		createVariable("backendBasepath", "http://petstore.swagger.io/v3");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

		echo("####### Export the API from the API-Manager #######");
		createVariable("expectedReturnCode", "0");
		swaggerExport.doExecute(context);
		
		String exportedAPIConfigFile = context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/api-config.json";
		
		echo("####### Reading exported API-Config file: '"+exportedAPIConfigFile+"' #######");
		JsonNode exportedAPIConfig = mapper.readTree(new FileInputStream(new File(exportedAPIConfigFile)));
		JsonNode importedAPIConfig = mapper.readTree(this.getClass().getResourceAsStream("/test/export/files/methodLevel/inbound-api-key-and-cors.json"));
		
		

		assertEquals(exportedAPIConfig.get("path").asText(), 				context.getVariable("apiPath"));
		assertEquals(exportedAPIConfig.get("name").asText(), 				context.getVariable("apiName"));
		assertEquals(exportedAPIConfig.get("state").asText(), 				context.getVariable("state"));
		assertEquals(exportedAPIConfig.get("version").asText(), 			"1.0.7");
		assertEquals(exportedAPIConfig.get("organization").asText(),		"API Development "+context.getVariable("orgNumber"));
		//assertEquals(exportedAPIConfig.get("backendBasepath").asText(), 	context.getVariable("backendBasepath"));
		
		Map<String, InboundProfile> importedInboundProfiles = mapper.convertValue(importedAPIConfig.get("inboundProfiles"), new TypeReference<Map<String, InboundProfile>>(){});
		Map<String, InboundProfile> exportedInboundProfiles = mapper.convertValue(exportedAPIConfig.get("inboundProfiles"), new TypeReference<Map<String, InboundProfile>>(){});
		assertEquals(importedInboundProfiles, exportedInboundProfiles, "InboundProfiles are not equal.");
		
		Map<String, OutboundProfile> importedOutboundProfiles = mapper.convertValue(importedAPIConfig.get("outboundProfiles"), new TypeReference<Map<String, OutboundProfile>>(){});
		Map<String, OutboundProfile> exportedOutboundProfiles = mapper.convertValue(exportedAPIConfig.get("outboundProfiles"), new TypeReference<Map<String, OutboundProfile>>(){});
		assertEquals(importedOutboundProfiles, exportedOutboundProfiles, "OutboundProfiles are not equal.");
		
		List<SecurityProfile> importedSecurityProfiles = mapper.convertValue(importedAPIConfig.get("securityProfiles"), new TypeReference<List<SecurityProfile>>(){});
		List<SecurityProfile> exportedSecurityProfiles = mapper.convertValue(exportedAPIConfig.get("securityProfiles"), new TypeReference<List<SecurityProfile>>(){});
		assertEquals(importedSecurityProfiles, exportedSecurityProfiles, "SecurityProfiles are not equal.");
		
		List<SecurityProfile> importedAuthenticationProfiles = mapper.convertValue(importedAPIConfig.get("authenticationProfiles"), new TypeReference<List<AuthenticationProfile>>(){});
		List<SecurityProfile> exportedAuthenticationProfiles = mapper.convertValue(exportedAPIConfig.get("authenticationProfiles"), new TypeReference<List<AuthenticationProfile>>(){});
		assertEquals(importedAuthenticationProfiles, exportedAuthenticationProfiles, "AuthenticationProfiles are not equal.");
		
		TagMap<String, String[]> importedTags = mapper.convertValue(importedAPIConfig.get("tags"), new TypeReference<TagMap<String, String[]>>(){});
		TagMap<String, String[]> exportedTags = mapper.convertValue(exportedAPIConfig.get("tags"), new TypeReference<TagMap<String, String[]>>(){});
		assertEquals(importedTags, exportedTags, "Tags are not equal.");
		
		List<CorsProfile> importedCorsProfiles = mapper.convertValue(importedAPIConfig.get("corsProfiles"), new TypeReference<List<CorsProfile>>(){});
		List<CorsProfile> exportedCorsProfiles = mapper.convertValue(exportedAPIConfig.get("corsProfiles"), new TypeReference<List<CorsProfile>>(){});
		assertEquals(importedCorsProfiles, exportedCorsProfiles, "CorsProfiles are not equal.");
		
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/"+context.getVariable("exportAPIName")).exists(), "Exported Swagger-File is missing");
	}
}
