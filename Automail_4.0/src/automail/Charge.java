package automail;

import java.util.HashMap;

import com.unimelb.swen30006.wifimodem.WifiModem;
import util.Configuration;

public class Charge {
	private static HashMap<Integer, Double> latestServiceFee = new HashMap<Integer, Double>();
	private WifiModem wifiModem;
	private Configuration configuration = Configuration.getInstance();
	private int initial_floor = Integer.parseInt(this.configuration.getProperty(Configuration.MAILROOM_LOCATION_FLOOR_KEY));
	private double sf;

	private int unitCounter;
	private double maintenanceFee;
	private int num;
	private double avgTime;

	/**
	 * forwardCallToAPI_LookupPrice(onFloor: int) -> the service fee
	 * onFloor -> floor# that the robot is delivering a mail item
	 * failed request -> forwardCallToAPI_LookupPrice get negative value
	 *
	 * @throws Exception
	 */
	public Charge() {

		try {
			this.wifiModem = WifiModem.getInstance(initial_floor);
			this.unitCounter = 0;
			this.maintenanceFee = 0;
			this.num = 0;
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
		} else {
			// System.out.println("Request Fails");
			if (latestServiceFee.containsKey(destFloor)) {
				sf = latestServiceFee.get(destFloor);
				return sf;
			} else {
				return 0;
			}
		}

		return sf;
	}

	public void addUnitCounter() {
		unitCounter++;
	}

	public void addNum() {
		num++;
	}

	public double getAvgTime() {
		avgTime = (float) unitCounter / (float) num;
		return avgTime;
	}

	public double getMaintenanceFee(double rate) {
		maintenanceFee = this.getAvgTime() * rate;
		return maintenanceFee;
	}
}



