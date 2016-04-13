package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import javax.jws.WebService;

@WebService(
		endpointInterface="pt.upa.transporter.ws.TransporterPortType",
		wsdlLocation="transporter.1_0.wsdl",
		name="Transporter",
		portName="TransporterPort",
		targetNamespace="http://ws.transporter.upa.pt/",
		serviceName="TransporterService"
	)
public class TransporterPort implements TransporterPortType{
	
	static public HashMap<String, String> regioes;
	static public int transporter;
	static public String transport;
	public int var = 0;
	public JobView job;
	public Timer timer;
	public List<JobView> listJob = new ArrayList<JobView>();
	
	@Override
	public String ping(String name) {
		System.out.println(" ping...");
		return " ping...daqui transporter\n" + name + "!";
	}

	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
		int valor = 0;
		JobView job = new JobView();
		job.setCompanyName(transport);
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobIdentifier(Integer.toString(var/*10+transporter*/));
		System.out.println(var);
		job.setJobPrice(valor);
		job.setJobState(JobStateView.PROPOSED);
		if(price <= 0){
			BadPriceFault faultInfo = new BadPriceFault();
			faultInfo.setPrice(price);
			throw new BadPriceFault_Exception("error in server", faultInfo);
		}
		else if(regioes.containsKey(origin) == false){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(origin);
			throw new BadLocationFault_Exception("error in server", faultInfo);
		}
		else if(regioes.containsKey(destination) == false){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(destination);
			throw new BadLocationFault_Exception("error in server", faultInfo);
		}
		else if((regioes.get(origin) == "RegiaoNorte" && transporter%2 == 1) || 
				(regioes.get(origin) == "RegiaoSul" && transporter%2 == 0) ||
				(regioes.get(destination) == "RegiaoNorte" && transporter%2 == 1) || 
				(regioes.get(destination) == "RegiaoSul" && transporter%2 == 0)){
			listJob.add(job);
			return null;
			/*BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(origin);
			throw new BadLocationFault_Exception("error in server", faultInfo);*/
		}
		else{
			if(price > 100){
				listJob.add(job);
				return null;
			}
			else if(price <= 10){
				valor = ThreadLocalRandom.current().nextInt(0, price-1);
			}
			else{
				if(price%2==1){
					if(transporter%2 == 1){
						valor = ThreadLocalRandom.current().nextInt(0, price-1);
					}
					else{
						valor = ThreadLocalRandom.current().nextInt(price+1,99);
					}
				}
				else{
					if(transporter%2 == 0){
						valor = ThreadLocalRandom.current().nextInt(0, price-1);
					}
					else{
						valor = ThreadLocalRandom.current().nextInt(price+1,99);
					}
				}	
			}
			job.setJobPrice(valor);
			listJob.add(job);
		}
		return job;
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		final int val = 5000;
		for(int i=0; i<listJob.size(); i++){
			if(listJob.get(i).getJobIdentifier()==id){
				if(accept == true){
					listJob.get(i).setJobState(JobStateView.ACCEPTED);
					Timer timer = new Timer();
					TimerTask task = new MyTimerTask(job);
					int delay= ThreadLocalRandom.current().nextInt(1000, val-1);
					timer.schedule(task, delay);
					
					delay= ThreadLocalRandom.current().nextInt(1000, val-1);
					timer.schedule(task, delay);
					
					delay= ThreadLocalRandom.current().nextInt(1000, val-1);
					timer.schedule(task, delay);
				}
				else{
					listJob.get(i).setJobState(JobStateView.REJECTED);
				}
				return listJob.get(i);
			}
		}
		return null;
	}

	@Override
	public JobView jobStatus(String id) {
		for(int i=0; i<listJob.size(); i++){
			if(listJob.get(i).getJobIdentifier()==id){
				return listJob.get(i);
			}
		}
		return null;
	}

	@Override
	public List<JobView> listJobs() {
		return listJob;
	}

	@Override
	public void clearJobs() {
		listJob.clear();
	}
}
