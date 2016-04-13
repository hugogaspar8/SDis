package pt.upa.transporter;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

public class TransporterClientApplication {

	public static void main(String[] args) throws Exception {
		//System.out.println(TransporterClientApplication.class.getSimpleName() + " starting...");

		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", TransporterClientApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		
		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);
		
		System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		TransporterPortType port = service.getTransporterPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		
		for(int i=1;i<10;i++){
			System.out.printf("Looking for '%s'%n", name.concat(String.valueOf(i)));
			String endpointAddress = uddiNaming.lookup(name.concat(String.valueOf(i)));
	
			if (endpointAddress == null) {
				System.out.println("Not found!");
			} else {
				System.out.printf("Found %s%n", endpointAddress);
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
				try {
					String result = port.ping("friend");
					JobView result2 = port.requestJob("origin", "String destination", 0);
					System.out.println(result);

				} catch (Exception pfe) {
					System.out.println("Caught: " + pfe);
				}
			}
		}
	}
}
