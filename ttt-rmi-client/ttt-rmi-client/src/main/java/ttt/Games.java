package ttt;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Games implements Remote{
	
	TTT ttt;
	Scanner keyboardSc;
	int winner = 0;
	int player = 1;

	public Games() throws RemoteException {
		ttt = new TTT();
		keyboardSc = new Scanner(System.in);
	}

	public int readPlay() throws RemoteException {
		int play;
		do {
			System.out.printf("\nPlayer %d, please enter the number of the square "
							+ "where you want to place your %c (or 0 to refresh the board): \n",
							player, (player == 1) ? 'X' : 'O');
			play = keyboardSc.nextInt();
		} while (play > 10 || play < 0);
		return play;
	}

	public void playGame() throws RemoteException {
		int play;
		boolean playAccepted;

		do {
			player = ++player % 2;
			do {
				System.out.println(ttt.currentBoard());
				play = readPlay();
				if (play != 0 && play!= 10) {
					playAccepted = ttt.play( --play / 3, play % 3, player);
					if (!playAccepted)
						System.out.println("Invalid play! Try again.");
				} else if(play==10){
					do{
						play=ttt.playRandom();
						System.out.println(play);
						playAccepted = ttt.play( --play / 3, play % 3, player);
					} while (!playAccepted);
				} else
					playAccepted = false;
			} while (!playAccepted);
			winner = ttt.checkWinner();
		} while (winner == -1);
	}

	public void congratulate() throws RemoteException {
		if (winner == 2)
			System.out.printf("\nHow boring, it is a draw\n");
		else {
			System.out.printf(
					"\nCongratulations, player %d, YOU ARE THE WINNER!\n",
					winner);
		}
	}

	public static void main(String[] args) throws RemoteException {
		
		Games game = new Games();
		System.out.println("Lets begin the game!");
		
		try {
			@SuppressWarnings("unused")
			TTTService aTTTServer = (TTTService) Naming.lookup("//localhost:1111/TTTService");
			System.out.println("Found server");
			game.playGame();
			game.congratulate();
		} catch (RemoteException e) {
			System.out.println("allShapes: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Lookup: " + e.getMessage());
		}
	}
}
