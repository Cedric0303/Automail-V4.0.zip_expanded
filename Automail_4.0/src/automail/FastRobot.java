package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.Clock;
import simulation.IMailDelivery;

public class FastRobot extends Robot {

	private static final int INDIVIDUAL_MAX_MOVE_SPEED = 3;
	
    private IMailDelivery delivery;
    private final String id;
    private RobotState current_state;
    private int current_floor;
    private int destination_floor;
    private MailPool mailPool;
    private boolean receivedDispatch;
    
    private MailItem deliveryItem = null;
    
    private int deliveryCounter;
    
	public FastRobot(IMailDelivery delivery, MailPool mailPool, int number) {
		super();
		this.id = "F" + number;
    	current_state = RobotState.RETURNING;
        current_floor = Building.getInstance().getMailroomLocationFloor();
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
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
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.getInstance().getMailroomLocationFloor());
                	break;
                }
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && receivedDispatch) {
                	receivedDispatch = false;
                	deliveryCounter = 0; // reset delivery counter
                	setDestination();
                	changeState(RobotState.DELIVERING);
                }
                break;
    		case DELIVERING:
    			if(current_floor == destination_floor) { // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
                    delivery.deliver(this, deliveryItem, "");
                    deliveryItem = null;
                    deliveryCounter++;
                    if(deliveryCounter > 2) {  // Implies a simulation bug
                    	throw new ExcessiveDeliveryException();
                    }
                    /** Check if want to return, i.e. if there is no item in the tube*/
                    else {
                    	changeState(RobotState.RETURNING);
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
    	int distance = Math.abs(destination - current_floor);
    	if (distance <= INDIVIDUAL_MAX_MOVE_SPEED) {
    		current_floor = destination;
    	}
    	else if(current_floor < destination) {
    		current_floor += INDIVIDUAL_MAX_MOVE_SPEED;
        } else {
    		current_floor -= INDIVIDUAL_MAX_MOVE_SPEED;
        }
    }
    
    public String getIdTube() {
    	return String.format("%s(0)", this.id);
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    void changeState(RobotState nextState) {
    	assert(!(deliveryItem == null));
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING) {
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    }

    public MailItem getTube() {
    	return null;
    }
    
	public boolean isEmpty() {
		return (deliveryItem == null);
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
		assert(deliveryItem == null);
		deliveryItem = mailItem;
		if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}
	public void addToTube(MailItem mailItem) {
	}
}
