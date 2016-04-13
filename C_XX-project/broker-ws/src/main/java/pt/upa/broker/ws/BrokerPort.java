package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPort;
import pt.upa.transporter.ws.TransporterPortType;

@WebService(
	endpointInterface="pt.upa.broker.ws.BrokerPortType",
	wsdlLocation="broker.1_0.wsdl",
	name="Broker",
	portName="BrokerPort",
	targetNamespace="http://ws.broker.upa.pt/",
	serviceName="BrokerService"
)
public class BrokerPort implements BrokerPortType{

	static public TransporterPortType port2;
	static public UDDINaming uddiNaming;
	static public Map<String, Object> requestContext;
	static public HashMap<String, String> regioes;
	static public String name;
	String name2 = "UpaTransporter%";
	public int id2 = 0;
	static public Collection<String> x;
	public List<TransportView> listTransporters = new ArrayList<TransportView>();
	
	@Override
	public String ping(String name) {
		// TODO Auto-generated method stub
		String text = null;
		System.out.printf("Looking for '%s'%n", name2);
		for(int i = 0 ; i < x.size() ; i++){
			String endpointAddress = ( String ) x.toArray()[i];
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
			text = port2.ping(name);
			System.out.println(text);
            System.out.println(endpointAddress);
        }
		return text;
	}

	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		Map<String, HashMap<String, Integer>> precos = new HashMap<String, HashMap<String, Integer>>();
		String id = null;
		if(price <= 0){
			InvalidPriceFault faultInfo = new InvalidPriceFault();
			faultInfo.setPrice(price);
			throw new InvalidPriceFault_Exception("error in server", faultInfo);
		}
		else if(regioes.containsKey(origin) == false){
			UnknownLocationFault faultInfo = new UnknownLocationFault();
			faultInfo.setLocation(origin);
			throw new UnknownLocationFault_Exception("error in server", faultInfo);
		}
		else if(regioes.containsKey(destination) == false){
			UnknownLocationFault faultInfo = new UnknownLocationFault();
			faultInfo.setLocation(destination);
			throw new UnknownLocationFault_Exception("error in server", faultInfo);
		}
		try{
			//transport view
			for(int i = 0 ; i < x.size() ; i++){
				String name2 ="UpaTransporter";
				String endpointAddress = ( String ) x.toArray()[i];
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
				String number = String.valueOf(endpointAddress.charAt(20));
				name2 = name2.concat(number);
				TransportView tv = new TransportView();
				tv.setDestination(destination);
				tv.setOrigin(origin);
				tv.setId(Integer.toString(id2));
				tv.setPrice(price);
				tv.setState(TransportStateView.REQUESTED);
				tv.setTransporterCompany(name2);
				listTransporters.add(tv);
				//TransporterPort.var=id2;
				//System.out.println(id2);
				JobView job = port2.requestJob(origin, destination, price);
				if(job != null){
					id = job.getJobIdentifier();
					for(int j = 0; j < listTransporters.size(); j++){
						if(id == listTransporters.get(j).getId()){
							listTransporters.get(j).setState(TransportStateView.BUDGETED);
						}
					}
					int price2 = job.getJobPrice();
					if(price2 >= price){
						for(int j = 0; j < listTransporters.size(); j++){
							if(id == listTransporters.get(j).getId()){
								listTransporters.get(j).setState(TransportStateView.FAILED);
								port2.decideJob(id, false);
							}
						}
					}
					else if(price2 < price){
						HashMap<String, Integer> idprice = new HashMap<String, Integer>();
						idprice.put(id, price2);
						precos.put(endpointAddress, idprice);
					}
				}
				else{
					for(int j = 0; j < listTransporters.size(); j++){
						if(String.valueOf(id2) == listTransporters.get(j).getId()){
							listTransporters.get(j).setState(TransportStateView.FAILED);
							port2.decideJob(id, false);
						}
					}
				}
				id2++;
			}
			int lower = 100;
			String endpoint = null;
			for (String key: precos.keySet()) {
				for(String key2: precos.get(key).keySet()){
					if(precos.get(key).get(key2) < lower){
						id = key2;
						lower = precos.get(key).get(key2);
						endpoint = key;
						requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpoint);
					}
				}
			}
			for(int j = 0; j < listTransporters.size(); j++){
				if(id == listTransporters.get(j).getId()){
					listTransporters.get(j).setState(TransportStateView.BOOKED);
					port2.decideJob(id, true);
				}
			}
			precos.remove(endpoint);
			for (String key: precos.keySet()) {
				for(String key2: precos.get(key).keySet()){
					id = key2;
					for(int j = 0; j < listTransporters.size(); j++){
						if(id == listTransporters.get(j).getId()){
							listTransporters.get(j).setState(TransportStateView.FAILED);
							port2.decideJob(id, false);
						}
					}
				}
			}
			precos.clear();
			
			
		}catch (BadLocationFault_Exception e){
			UnknownLocationFault faultInfo = new UnknownLocationFault();
			throw new UnknownLocationFault_Exception("error in server", faultInfo);
			
		}catch (BadPriceFault_Exception e) {
			e.printStackTrace();
			UnavailableTransportPriceFault faultInfo = new UnavailableTransportPriceFault();
			faultInfo.setBestPriceFound(price);
			throw new UnavailableTransportPriceFault_Exception("error in server", faultInfo);
		} catch (BadJobFault_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(id == null){
			UnavailableTransportFault faultInfo = new UnavailableTransportFault();
			//faultInfo.setBestPriceFound(price);
			throw new UnavailableTransportFault_Exception("error in server", faultInfo);
		}
		return "";
	}

	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		for(int i = 0 ; i < listTransporters.size() ; i++){
			System.out.println(listTransporters.get(i).getId());
			System.out.println(id);
			if(listTransporters.get(i).getId().equals(id)){
				try {
					System.out.println(listTransporters.get(i).getTransporterCompany());
					String endpointAddress = uddiNaming.lookup(listTransporters.get(i).getTransporterCompany());
					requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
					JobView job = port2.jobStatus(id);
					JobStateView state = job.getJobState();
					if(state == JobStateView.PROPOSED){
						listTransporters.get(i).setState(TransportStateView.BUDGETED);
					}
					else if(state == JobStateView.ACCEPTED){
						listTransporters.get(i).setState(TransportStateView.BOOKED);
					}
					else if(state == JobStateView.REJECTED){
						listTransporters.get(i).setState(TransportStateView.FAILED);
					}
					else if(state == JobStateView.HEADING){
						listTransporters.get(i).setState(TransportStateView.HEADING);
					}
					else if(state == JobStateView.ONGOING){
						listTransporters.get(i).setState(TransportStateView.ONGOING);
					}
					else if(state == JobStateView.COMPLETED){
						listTransporters.get(i).setState(TransportStateView.COMPLETED);
					}
					System.out.println("Hello");
					return listTransporters.get(i);
				} catch (JAXRException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public List<TransportView> listTransports() {
		return listTransporters;
	}

	@Override
	public void clearTransports() {
		for(int i = 0 ; i < x.size() ; i++){
			String endpointAddress = ( String ) x.toArray()[i];
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
			port2.clearJobs();
        }
		listTransporters.clear();
	}
}
