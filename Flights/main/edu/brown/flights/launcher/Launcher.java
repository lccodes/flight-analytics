package edu.brown.flights.launcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.brown.flights.api.versions.V1;
import edu.brown.flights.types.World;
import edu.brown.flights.types.planes.FlightInstance;

public class Launcher {
	
	private Map<String, World> worlds = new HashMap<String, World>();
	
	@SuppressWarnings("unchecked")
	public void commandline() {
		Scanner scan = new Scanner(System.in);
		boolean go = true;
		while(go){
			System.out.print("$");
			switch(scan.nextLine()) {
			case "exit": 
				go = false;
				continue;
			case "load":
				//TODO: Load world from csv
				continue;
			case "call":
				System.out.print("Function: ");
				String func = scan.nextLine();
				System.out.print("World: ");
				World hold = worlds.get(scan.nextLine());
				if (hold == null) {
					System.err.println("Not a valid world. Try 'load'");
					continue;
				}
				System.out.print("Args: ");
				try {
					Map<FlightInstance, Integer> results = (Map<FlightInstance, Integer>) V1.class.getMethod(func).invoke(null, (Object[]) scan.nextLine().split(" "));
					for (Map.Entry<FlightInstance, Integer> r : results.entrySet()) {
						System.out.println("\t Flight: " + r.getKey());
						System.out.println("\t Delay: " + r.getValue());
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					System.err.println("Call failed due to " + e.getMessage());
				}
				continue;
			}	
		}
		scan.close();
	}
	
	public static void main(String[] args) throws IOException {
		Launcher balrog = new Launcher();
		if (args.length > 0 && args[0].equals("web")) {
			//TODO: Spark web ui
		}else{
			balrog.commandline();
		}
	}

}
