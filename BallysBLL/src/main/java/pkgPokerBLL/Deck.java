package pkgPokerBLL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import pkgPokerEnum.eRank;
import pkgPokerEnum.eSuit;

public class Deck implements Serializable {

	private UUID DeckID;
	private ArrayList<Card> DeckCards = new ArrayList<Card>();

	public Deck() {

		super();
		int iCardNbr = 0;
		for (eSuit suit : eSuit.values()) {
			for (eRank rank : eRank.values()) {
				// if ((suit != eSuit.JOKER) && (rank != eRank.JOKER)) {
				DeckCards.add(new Card(rank, suit, ++iCardNbr));
				// }
			}
		}
		Collections.shuffle(DeckCards);
	}

	public Deck(int iNbrOfJokers) {
		this();

		for (int a = 0; a < iNbrOfJokers; a++) {
			DeckCards.add(new Card(eRank.JOKER, eSuit.JOKER, 53));
		}
		Collections.shuffle(DeckCards);
	}

	public Deck(int iNbrOfJokers, ArrayList<Card> Wilds) {
		this(iNbrOfJokers);

		for (Card c1 : Wilds)
			for (Card c2 : DeckCards) {
				if ((c1.geteRank() == c2.geteRank()) && (c1.geteSuit() == c2.geteSuit())) {
					c2.setWild(true);
				}
			}
		Collections.shuffle(DeckCards);
	}

	public Card Draw() {

		return DeckCards.remove(0);
	}
}
