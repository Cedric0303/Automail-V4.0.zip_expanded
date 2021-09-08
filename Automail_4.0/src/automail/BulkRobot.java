package automail;

import java.util.Stack;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.Clock;
import simulation.IMailDelivery;
import util.Configuration;

public class BulkRobot extends Robot {

	private static final int INDIVIDUAL_MAX_TUBE_SIZE = 5;
    private static final double B_BASE_RATE = 0.01;

	private IMailDelivery delivery;
    private final String id;
    private RobotState current_state;
    private int current_floor;
    private int destination_floor;
    private MailPool mailPool;
    private boolean receivedDispatch;
    
    private MailItem deliveryItem = null;
    private Stack<MailItem> tube = null;
    
    private int deliveryCounter;
    
    private double bServiceFee;
    private static Charge charge = new Charge();

    //private static Maintenance maintenance = new Maintenance();
    private Configuration configuration = Configuration.getInstance();
    
	public BulkRobot(IMailDelivery delivery, MailPool mailPool, int number) {
		super();
		this.id = "B" + number;
    	current_state = RobotState.RETURNING;
        current_floor = Building.getInstance().getMailroomLocationFloor();
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.tube = new Stack<MailItem>();
        this.deliveryCounter = 0;

        charge.addNum();
	}

	public void dispatch() {
    	this.receivedDispatch = true;
    }
	
	public void operate() throws ExcessiveDeliveryException {
    	switch(current_state) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == Building.getInstance().getMailroomLocationFloor()) {
        			/** Tell the sorter the robot is ready */
        			mailPool.registerWaiting(this);
                	changeState(RobotState.WAITING);
                    charge.addUnitCounter();

                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.getInstance().getMailroomLocationFloor());
                	break;
                }
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if (!isEmpty() && receivedDispatch) {
                	receivedDispatch = false;
                	deliveryItem = tube.peek();
                	deliveryCounter = 0; // reset delivery counter
                	setDestination();
                	changeState(RobotState.DELIVERING);
                }
                break;
    		case DELIVERING:
    			if(current_floor == destination_floor) { // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
                    charge.addUnitCounter();
                    deliveryCounter++;
                    //get service fee
                    getServiceFee();
                    tube.pop();
                    delivery.deliver(this, deliveryItem, additionalLog());
                    deliveryItem = null;
                    
                   if (deliveryCounter > INDIVIDUAL_MAX_TUBE_SIZE) {  // Implies a simulation bug
                   	throw new ExcessiveDeliveryException();
                   }
                    /** Check if want to return, i.e. if there is no item in the tube*/
                    if (tube.size() == 0) {
                    	changeState(RobotState.RETURNING);
                    }
                    else if (tube.size() > 0) {
                        /** If there is another item, set the robot's route to the location to deliver the item */
                        deliveryItem = tube.peek();
                        setDestination();
                        changeState(RobotState.DELIVERING);
                    }
    			} else {
	        		/** The robot is not at the destination yet, move towards it! */
	                moveTowards(destination_floor);
    			}
                break;
    	}
    }
	
	void setDestination() {
        /** Set the destination floor */
        destination_floor = deliveryItem.getDestFloor();
    }
	
    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    void moveTowards(int destination) {
        charge.addUnitCounter();
        if(current_floor < destination) {
            current_floor++;
        } else {
            current_floor--;
        }
    }
    
    public String getIdTube() {
    	return String.format("%s(%1d)", this.id, tube.size());
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    void changeState(RobotState nextState) {
    	assert(!(tube.size() == 0));
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING) {
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    }

	public MailItem getTube() {
		return tube.peek();
	}

	public boolean isEmpty() {
		return (deliveryItem == null && tube.size() == 0);
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
	}

	public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
		assert(tube.size() < INDIVIDUAL_MAX_TUBE_SIZE);
		tube.add(mailItem);
		if (tube.peek().weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

    /**
     * implement a new method to print out the additionalLog in the console
     * including the service fee, average time and maintain fee of each type
     * of robots.
     *
     */


    public void getServiceFee(){
        if (Boolean.parseBoolean(configuration.getProperty(Configuration.FEE_CHARGING_KEY))) {
            //charge = new Charge(
                    //Integer.parseInt(configuration.getProperty(Configuration.MAILROOM_LOCATION_FLOOR_KEY)));
            bServiceFee = charge.retrieveServiceFee(destination_floor) * deliveryCounter;
        }
    }

    public double getTotal(){
        double total = bServiceFee + charge.getMaintenanceFee(B_BASE_RATE);
        return total;
    }

    public String additionalLog() {
        boolean feeCharging = Boolean.parseBoolean(configuration.getProperty(Configuration.FEE_CHARGING_KEY));
        if (feeCharging) {
            return String.format(
                    " | Service Fee:  %.2f | Maintenance:  %.2f | Avg. Operating Time:  %.2f | Total Charge:  %.2f",
                    bServiceFee, charge.getMaintenanceFee(B_BASE_RATE), charge.getAvgTime(), getTotal());
        }
        else
            return String.format("");
    }
}
