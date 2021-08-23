package automail;

import simulation.IMailDelivery;

public class Automail {

    private Robot[] robots;
    private MailPool mailPool;
    
    public Automail(MailPool mailPool, IMailDelivery delivery, int numRegRobots, int numFastRobots, int numBulkRobots) {  	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	
    	/** Initialize robots, currently only regular robots */
    	int numAllRobots = numRegRobots + numFastRobots + numBulkRobots;
    	robots = new Robot[numAllRobots];
    	for (int i = 0; i < numAllRobots; i++) {
    		if (numRegRobots > 0 && i < numRegRobots) {
    			robots[i] = new RegRobot(delivery, mailPool, i);
    		}
    		else if (numFastRobots > 0 && i < numRegRobots + numFastRobots) {
    			robots[i] = new FastRobot(delivery, mailPool, i);
    		}
    		else if (numBulkRobots > 0 && i < numAllRobots) {
    			robots[i] = new BulkRobot(delivery, mailPool, i);
    		}
    	}
    }

    public Robot[] getRobots() {
        return robots;
    }

    public MailPool getMailPool() {
        return mailPool;
    }
}
