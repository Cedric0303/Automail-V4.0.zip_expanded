package automail;

import java.util.HashMap;

import com.unimelb.swen30006.wifimodem.WifiModem;

public class ServiceFee {
	private HashMap<Integer, Double> latestServiceFee = new HashMap<Integer, Double>();
	private WifiModem wifiModem;
	private double sf;
	//private Configuration configuration = Configuration.getInstance();
	
	/**
	 * forwardCallToAPI_LookupPrice(onFloor: int) -> the service fee
	 * onFloor -> floor# that the robot is delivering a mail item
	 * failed request -> forwardCallToAPI_LookupPrice get negative value 
	 * @throws Exception 
	 */
	public ServiceFee(int mailRoom) {
		
		try {
			this.wifiModem = WifiModem.getInstance(mailRoom);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public double retrieveServiceFee(int destFloor) {
		/**
		 * If the lookup request fails, the most recent service fee retrieved by any other robot for the same floor should be used. 
		 * If no service fee retrieved for this floor before the value of the service fee should be 0.
		 */
		
		sf = wifiModem.forwardCallToAPI_LookupPrice(destFloor);
		if (sf > 0) {
			latestServiceFee.put(destFloor, sf);
		}
		
		else {
			// System.out.println("Request Fails");
			if (latestServiceFee.containsKey(destFloor)) {
				sf = latestServiceFee.get(destFloor);
				return sf;
			}
			else {
				return 0;
			}
		}
		
		return sf;	
	}
	
	public double getServiceFee(int destFloor) {
		return sf;
	}
}
