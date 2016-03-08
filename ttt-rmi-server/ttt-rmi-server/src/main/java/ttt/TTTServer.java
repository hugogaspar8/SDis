package ttt;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TTTServer {
	
	public static void main(String args[]){
	
		int registryPort = 8252;
        System.out.println("Main OK");
        
        try{
        	TTTService aTTTService = new TTT();
            System.out.println("After create");
            
            Registry reg = LocateRegistry.createRegistry(registryPort);
			reg.rebind("TTTService", aTTTService);
			           
            System.out.println("ShapeList server ready");
        }catch(Exception e) {
            System.out.println("ShapeList server main " + e.getMessage());
        }
    }
}
