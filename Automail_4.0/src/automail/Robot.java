package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;

/**
 * The robot delivers mail!
 */
public abstract class Robot {

	protected static final int INDIVIDUAL_MAX_WEIGHT = 2000;
	
	public enum RobotState { DELIVERING, WAITING, RETURNING }
	
    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot() {
    }
    
    /**
     * This is called when a robot is assigned the mail items and ready to dispatch for the delivery 
     */
    public abstract void dispatch();

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public abstract void operate() throws ExcessiveDeliveryException;

    /**
     * Sets the route for the robot
     */
    abstract void setDestination();

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    abstract void moveTowards(int destination);
    
    public abstract String getIdTube();
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    abstract void changeState(RobotState nextState);

	public abstract MailItem getTube();

	public abstract boolean isEmpty();

	public abstract void addToHand(MailItem mailItem) throws ItemTooHeavyException;

	public abstract void addToTube(MailItem mailItem) throws ItemTooHeavyException;

}
