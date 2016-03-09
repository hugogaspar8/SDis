package ttt;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TTTServer {
	
	public static void main(String args[]){
	
		int registryPort = 1111;
        System.out.println("Main OK");
        
        try{
        	TTTService aTTTService = new TTT();
            System.out.println("Server created.");
            
            Registry reg = LocateRegistry.createRegistry(registryPort);
			reg.rebind("TTTService", aTTTService);
			           
            System.out.println("Server ready!");
        }catch(Exception e) {
            System.out.println("Server main " + e.getMessage());
        }
    }
}
