package pkgPoker.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import netgame.common.Hub;
import pkgPokerBLL.Action;
import pkgPokerBLL.Card;
import pkgPokerBLL.CardDraw;
import pkgPokerBLL.Deck;
import pkgPokerBLL.GamePlay;
import pkgPokerBLL.GamePlayPlayerHand;
import pkgPokerBLL.Player;
import pkgPokerBLL.Rule;
import pkgPokerBLL.Table;

import pkgPokerEnum.eAction;
import pkgPokerEnum.eCardDestination;
import pkgPokerEnum.eDrawCount;
import pkgPokerEnum.eGame;
import pkgPokerEnum.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private int iDealNbr = 0;

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {

		if (playerID == 2) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			Player actPlayer = (Player) ((Action) message).getPlayer();
			Action act = (Action) message;
			switch (act.getAction()) {
			case Sit:
				HubPokerTable.AddPlayerToTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case Leave:			
				HubPokerTable.RemovePlayerFromTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case StartGame:
				// Get the rule from the Action object.
				Rule rle = new Rule(act.geteGame());
				
				//TODO Lab #5 - If neither player has 'the button', pick a random player
				//		and assign the button.
				UUID DealerID = null;
				
				// If Player in table pressed start, set that player as dealer
				for(UUID id:HubPokerTable.getHmPlayer().keySet()){
					if(id.equals(act.getPlayer().getPlayerID())){
						DealerID = id;
					}
				}
				
				// Else set random Player as dealer
				if(DealerID==null){
					DealerID = HubPokerTable.getHmPlayer().keySet().iterator().next();
				}
				
				//TODO Lab #5 - Start the new instance of GamePlay
				HubGamePlay = new GamePlay(rle,DealerID);
				
				// Add Players to Game
				HubGamePlay.setGamePlayers(HubPokerTable.getHmPlayer());
				
				// Set the order of players
				
				// Initalizes Integer Array 'Order'... I'm using Order methods in GamePlay class to set order of players.
				// Order will be a Integer Array starting with the Dealer's player position.
				int[] Order = null;
				
				for(Player p:HubPokerTable.getHmPlayer().values()){
					if(p.getPlayerID().equals(DealerID)){
						Order = GamePlay.GetOrder(p.getiPlayerPosition());
					}
				}
				HubGamePlay.setiActOrder(Order);


			case Draw:

				//TODO Lab #5 -	Draw card(s) for each player in the game.
				//TODO Lab #5 -	Make sure to set the correct visiblity
				//TODO Lab #5 -	Make sure to account for community cards
				switch(act.geteGame()){
				case FiveStud:
					break;
				case FiveStudOneJoker:
					break;
				case FiveStudTwoJoker:
					break;
				case TexasHoldEm:
					break;
				case Omaha:
					break;
				case DeucesWild:
					break;
				case AcesAndEights:
					break;
				case SevenDraw:
					break;
				case SuperOmaha:
					break;
				}
				
				//TODO Lab #5 -	Check to see if the game is over
				HubGamePlay.isGameOver();
				
				resetOutput();
				//	Send the state of the gameplay back to the clients
				sendToAll(HubGamePlay);
				break;
			case ScoreGame:
				// Am I at the end of the game?

				resetOutput();
				sendToAll(HubGamePlay);
				break;
			}
			
		}

	}

}