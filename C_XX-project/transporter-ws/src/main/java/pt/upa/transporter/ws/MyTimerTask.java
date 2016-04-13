package pt.upa.transporter.ws;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
	private JobView job;
	
	public MyTimerTask(JobView job) {
		this.job=job;
	}

	@Override
	public void run() {
		switch(job.getJobState()){
		case ACCEPTED:
			job.setJobState(JobStateView.HEADING);
		case HEADING:
			job.setJobState(JobStateView.ONGOING);
		case COMPLETED:
			job.setJobState(JobStateView.COMPLETED);
		}
	}

}
