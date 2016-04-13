package pt.upa.broker;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPort;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

public class BrokerApplication {

	public static void main(String[] args) throws Exception {
		
		// Create a hash map
	      HashMap<String, String> regioes = new HashMap<String, String>();
	      // Put elements to the map
	      regioes.put("Porto", "RegiaoNorte");
	      regioes.put("Braga", "RegiaoNorte");
	      regioes.put("Viana do Castelo", "RegiaoNorte");
	      regioes.put("Vila Real", "RegiaoNorte");
	      regioes.put("Bragança", "RegiaoNorte");
	      regioes.put("Lisboa", "RegiaoCentro");
	      regioes.put("Leiria", "RegiaoCentro");
	      regioes.put("Santarém", "RegiaoCentro");
	      regioes.put("Castelo Branco", "RegiaoCentro");
	      regioes.put("Coimbra", "RegiaoCentro");
	      regioes.put("Aveiro", "RegiaoCentro");
	      regioes.put("Viseu", "RegiaoCentro");
	      regioes.put("Guarda", "RegiaoCentro");
	      regioes.put("Setúbal", "RegiaoSul");
	      regioes.put("Évora", "RegiaoSul");
	      regioes.put("Portalegre", "RegiaoSul");
	      regioes.put("Beja", "RegiaoSul");
	      regioes.put("Faro", "RegiaoSul");
	      
	      BrokerPort.regioes = regioes;
		
		System.out.println(BrokerApplication.class.getSimpleName() + " starting...");
		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL%n", BrokerApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];
		BrokerPort.name = name;
		String name2 = "UpaTransporter%";

		Endpoint endpoint = null;
		UDDINaming uddiNaming = null;
		try {
			BrokerPort port = new BrokerPort();
			endpoint = Endpoint.create(port);

			// publish endpoint
			System.out.printf("Starting %s%n", url);
			endpoint.publish(url);

			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);
			BrokerPort.uddiNaming= uddiNaming;
			
			System.out.println("Creating stub ...");
			TransporterService service = new TransporterService();
			TransporterPortType port2 = service.getTransporterPort();
			//1
			BrokerPort.port2 = port2;
			
			System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port2;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			//2
			BrokerPort.requestContext = requestContext;
			
			System.out.printf("Looking for '%s'%n", name2);
			Collection<String> x = uddiNaming.list(name2);
			BrokerPort.x = x;
			
			for(int i = 0 ; i < x.size() ; i++){
				String endpointAddress = ( String ) x.toArray()[i];
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
				System.out.println(port2.ping("friend"));
				//port2.requestJob("Lisboa", "aaaaaaaaaaaa", 10);
				//port2.requestJob("Lisboa", "Coimbra", -1);
				//port2.requestJob("bbbbbbbbbb", "Coimbra", 10);
				//port2.requestJob("Porto", "Faro", 10);
				//port2.requestJob("Lisboa", "Coimbra", 10);
				
	            System.out.println(endpointAddress);
	        }
			
			/*if (endpointAddress == null) {
				System.out.println("Not found!");
			} else {
				System.out.printf("Found %s%n", endpointAddress);
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
			}*/
			
			//BrokerPort.test = port2;

			// wait
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
			System.in.read();

		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			//e.printStackTrace();

		} finally {
			try {
				if (endpoint != null) {
					// stop endpoint
					endpoint.stop();
					System.out.printf("Stopped %s%n", url);
				}
			} catch (Exception e) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
			try {
				if (uddiNaming != null) {
					// delete from UDDI
					uddiNaming.unbind(name);
					System.out.printf("Deleted '%s' from UDDI%n", name);
				}
			} catch (Exception e) {
				System.out.printf("Caught exception when deleting: %s%n", e);
			}
		}
	}

}
