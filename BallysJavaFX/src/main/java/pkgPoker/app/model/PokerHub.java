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
					if(id.equals(actPlayer.getPlayerID())){
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
				
				// Initializes Integer Array 'Order'... I'm using Order methods in GamePlay class to set order of players.
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
				//TODO Lab #5 -	Make sure to set the correct visibility
				//TODO Lab #5 -	Make sure to account for community cards
				
				//Check if dealer drew card
				if(actPlayer.getPlayerID().equals(HubGamePlay.getGameDealer())){
					//Increments DrawCount by 1
					HubGamePlay.seteDrawCountLast(eDrawCount.geteDrawCount(HubGamePlay.geteDrawCountLast().getDrawNo()+1));
					
					//Gets the new CardDraw from gameplay's rule
					CardDraw draw = HubGamePlay.getRule().GetDrawCard(HubGamePlay.geteDrawCountLast());
					
					//If Draw to be sent to Players...
					if(draw.getCardDestination()==eCardDestination.Player){
						//Get order from HubGamePlay
						for(int x:HubGamePlay.getiActOrder()){
							//Get Players currently sitting
							for(Player p:HubPokerTable.getHmPlayer().values()){
								//If PlayerPosition equals order#
								if(x==p.getiPlayerPosition()){
									//Draw cards for players, dependent on count value from CardDraw
									for(int i=0;i<draw.getCardCount().getCardCount();i++){
										HubGamePlay.drawCard(p, eCardDestination.Player);
									}
								}
							}
						}
					}
					//Else drawn cards should be sent to common hand... # of cards dependent on count value from CardDraw
					else{
						for(int i=0;i<draw.getCardCount().getCardCount();i++){
							HubGamePlay.drawCard(new Player(), eCardDestination.Community);
						}
					}
				}

				
				//TODO Lab #5 -	Check to see if the game is over
				if(HubGamePlay.geteDrawCountLast().getDrawNo()==HubGamePlay.getRule().GetMaxDrawCount()){
					HubGamePlay.isGameOver();
				}
				
				resetOutput();
				//	Send the state of the gameplay back to the clients
				sendToAll(HubGamePlay);
				break;
			case ScoreGame:
				// Am I at the end of the game?
				HubGamePlay.ScoreGame();
				resetOutput();
				sendToAll(HubGamePlay);
				break;
			}
			
		}

	}

}