package pkgPokerBLL;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import pkgPokerEnum.eCardNo;
import pkgPokerEnum.eHandStrength;
import pkgPokerEnum.eRank;
import pkgPokerEnum.eSuit;
 

public class Hand implements Serializable{

	
	private UUID HandId;
	private Player HandPlayer;	
	private boolean bIsScored;
	private HandScore HS;
	private ArrayList<Card> CardsInHand = new ArrayList<Card>();

	public Hand() {

	}
	
	public Hand(Player handPlayer, UUID HandID) {
		
		this.HandId = (HandID == null) ? UUID.randomUUID() : HandID;		
		this.HandPlayer = handPlayer;
	}	

	public void AddCardToHand(Card c) {
		CardsInHand.add(c);
	}

	public ArrayList<Card> getCardsInHand() {
		return CardsInHand;
	}

	public HandScore getHandScore() {
		return HS;
	}

	public void AddToCardsInHand(Card c) {
		CardsInHand.add(c);
	}

	public Hand EvaluateHand() {

		Hand h = null;

		ArrayList<Hand> ExplodedHands = ExplodeHands(this);

		for (Hand hand : ExplodedHands) {
			hand = Hand.EvaluateHand(hand);
		}

		//	Figure out best hand
		Collections.sort(ExplodedHands, Hand.HandRank);
		
		//	Return best hand.  
		return ExplodedHands.get(0);
	}

	
 
	public static ArrayList<Hand> ExplodeHands(Hand h) {

		ArrayList<Hand> ReturnHands = new ArrayList<Hand>();
		
		ReturnHands.add(h);
			
		return ReturnHands;
	}

	private static Hand EvaluateHand(Hand h) {

		Collections.sort(h.getCardsInHand());

		// Another way to sort
		// Collections.sort(h.getCardsInHand(), Card.CardRank);

		HandScore hs = new HandScore();
		try {
			Class<?> c = Class.forName("pkgPokerBLL.Hand");

			for (eHandStrength hstr : eHandStrength.values()) {
				Class[] cArg = new Class[2];
				cArg[0] = pkgPokerBLL.Hand.class;
				cArg[1] = pkgPokerBLL.HandScore.class;

				Method meth = c.getMethod(hstr.getEvalMethod(), cArg);
				Object o = meth.invoke(null, new Object[] { h, hs });

				// If o = true, that means the hand evaluated- skip the rest of
				// the evaluations
				if ((Boolean) o) {
					break;
				}
			}

			h.bIsScored = true;
			h.HS = hs;

		} catch (ClassNotFoundException x) {
			x.printStackTrace();
		} catch (IllegalAccessException x) {
			x.printStackTrace();
		} catch (NoSuchMethodException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return h;
	}

	public static boolean isStraight(ArrayList<Card> cards, Card c) {
		boolean isStraight = false;
		int iStartCard = (Hand.isAce(cards)) ? eCardNo.SecondCard.getCardNo() : eCardNo.FirstCard.getCardNo();

		for (; iStartCard < 4; iStartCard++) {
			if (cards.get(iStartCard).geteRank().getiRankNbr() - 1 == cards.get(iStartCard + 1).geteRank()
					.getiRankNbr()) {
				isStraight = true;
			} else {
				isStraight = false;
				break;
			}
		}

		if (isStraight) {
			if (cards.get(eCardNo.FirstCard.getCardNo()).geteRank() == eRank.ACE) {
				if (cards.get(eCardNo.SecondCard.getCardNo()).geteRank() == eRank.KING) {
					c.seteRank(cards.get(eCardNo.FirstCard.getCardNo()).geteRank());
					c.seteSuit(cards.get(eCardNo.FirstCard.getCardNo()).geteSuit());
				} else if (cards.get(eCardNo.SecondCard.getCardNo()).geteRank() == eRank.FIVE) {
					c.seteRank(cards.get(eCardNo.SecondCard.getCardNo()).geteRank());
					c.seteSuit(cards.get(eCardNo.SecondCard.getCardNo()).geteSuit());

				}
			} else {
				c.seteRank(cards.get(eCardNo.FirstCard.getCardNo()).geteRank());
				c.seteSuit(cards.get(eCardNo.FirstCard.getCardNo()).geteSuit());
			}
		}

		return isStraight;
	}

	public static boolean isAce(ArrayList<Card> cards) {
		if ((cards.get(eCardNo.FirstCard.getCardNo()).geteRank() == eRank.ACE)
				&& (cards.get(eCardNo.SecondCard.getCardNo()).geteRank() == eRank.KING)
				|| (cards.get(eCardNo.SecondCard.getCardNo()).geteRank() == eRank.FIVE)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isFlush(ArrayList<Card> cards) {
		boolean isFlush = false;

		int iCount = 0;
		for (eSuit Suit : eSuit.values()) {
			iCount = 0;
			for (Card c : cards) {
				if (c.geteSuit() == Suit)
					iCount++;
			}

			if (iCount == 5) {
				isFlush = true;
				break;
			}
			if (iCount > 0)
				break;

		}

		return isFlush;

	}

	public static boolean isHandRoyalFlush(Hand h, HandScore hs) {

		boolean isHandRoyalFlush = false;
		Card c = new Card();

		if ((Hand.isFlush(h.getCardsInHand())) && (Hand.isStraight(h.getCardsInHand(), c))
				&& (Hand.isAce(h.getCardsInHand()))) {
			hs.setHandStrength(eHandStrength.RoyalFlush);
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			hs.setLoHand(null);
			isHandRoyalFlush = true;
		}

		return isHandRoyalFlush;
	}

	public static boolean isHandStraightFlush(Hand h, HandScore hs) {

		boolean isHandStraightFlush = false;
		Card c = new Card();
		if ((Hand.isFlush(h.getCardsInHand())) && (Hand.isStraight(h.getCardsInHand(), c))) {
			hs.setHandStrength(eHandStrength.StraightFlush);
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			hs.setLoHand(null);
			isHandStraightFlush = true;
		}

		return isHandStraightFlush;

	}

 
	public static boolean isHandFourOfAKind(Hand h, HandScore hs) {

		boolean isHandFourOfAKind = false;

		ArrayList<Card> kickers = new ArrayList<Card>();
		if (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FourthCard.getCardNo()).geteRank()) {
			isHandFourOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		} else if (h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FifthCard.getCardNo()).geteRank()) {
			isHandFourOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
		}

		if (isHandFourOfAKind) {
			hs.setHandStrength(eHandStrength.FourOfAKind);
			hs.setLoHand(null);
			hs.setKickers(kickers);
		}

		return isHandFourOfAKind;
	}

	public static boolean isHandFlush(Hand h, HandScore hs) {

		boolean bIsFlush = false;
		if (isFlush(h.getCardsInHand())) {
			hs.setHandStrength(eHandStrength.Flush);
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			hs.setLoHand(null);

			hs.getKickers().add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));
			hs.getKickers().add(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()));
			hs.getKickers().add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
			hs.getKickers().add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));

			bIsFlush = true;
		}

		return bIsFlush;
	}

	public static boolean isHandStraight(Hand h, HandScore hs) {

		boolean bIsStraight = false;
		Card c = new Card();

		if (isStraight(h.getCardsInHand(), c)) {
			hs.setHandStrength(eHandStrength.Straight);
			hs.setHiHand(c.geteRank());
			hs.setLoHand(null);
			bIsStraight = true;
		}
		return bIsStraight;
	}

	public static boolean isHandThreeOfAKind(Hand h, HandScore hs) {

		boolean isThreeOfAKind = false;
		ArrayList<Card> kickers = new ArrayList<Card>();
		if (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank()) {
			isThreeOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		} else if (h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FourthCard.getCardNo()).geteRank()) {
			isThreeOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));

		} else if (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FifthCard.getCardNo()).geteRank()) {
			isThreeOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));

		}

		if (isThreeOfAKind) {
			hs.setHandStrength(eHandStrength.ThreeOfAKind);
			hs.setLoHand(null);
			hs.setKickers(kickers);
		}

		return isThreeOfAKind;
	}

	public static boolean isHandTwoPair(Hand h, HandScore hs) {

		boolean isHandTwoPair = false;

		ArrayList<Card> kickers = new ArrayList<Card>();
		if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FourthCard.getCardNo()).geteRank())) {
			isHandTwoPair = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));

		} else if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isHandTwoPair = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()));

		} else if ((h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isHandTwoPair = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));

		}

		if (isHandTwoPair) {
			hs.setHandStrength(eHandStrength.TwoPair);
			hs.setKickers(kickers);
		}
		return isHandTwoPair;

	}

	public static boolean isHandPair(Hand h, HandScore hs) {

		boolean isHandPair = false;

		ArrayList<Card> kickers = new ArrayList<Card>();
		if (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank()) {
			isHandPair = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		} else if (h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank()) {
			isHandPair = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		} else if (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FourthCard.getCardNo()).geteRank()) {
			isHandPair = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		} else if (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FifthCard.getCardNo()).geteRank()) {
			isHandPair = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()));
		}

		if (isHandPair) {
			hs.setHandStrength(eHandStrength.Pair);
			hs.setLoHand(null);
			hs.setKickers(kickers);
		}

		return isHandPair;

	}

	public static boolean isHandHighCard(Hand h, HandScore hs) {

		hs.setHandStrength(eHandStrength.HighCard);
		hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
		hs.getKickers().add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));
		hs.getKickers().add(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()));
		hs.getKickers().add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
		hs.getKickers().add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		hs.setLoHand(null);
		return true;
	}

	public static boolean isAcesAndEights(Hand h, HandScore hs) {

		boolean isAcesAndEights = false;
		if (Hand.isHandTwoPair(h, hs) == true) {
			if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == eRank.ACE)
					&& (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == eRank.EIGHT)) {
				hs.setHandStrength(eHandStrength.AcesAndEights);
				isAcesAndEights = true;
			}
		}

		return isAcesAndEights;
	}

	public static boolean isHandFullHouse(Hand h, HandScore hs) {

		boolean isFullHouse = false;

		ArrayList<Card> kickers = new ArrayList<Card>();
		if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isFullHouse = true;

			hs.setHandStrength(eHandStrength.FullHouse);
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank());

		} else if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isFullHouse = true;
			hs.setHandStrength(eHandStrength.FullHouse);
			hs.setHiHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank());
		}

		return isFullHouse;

	}

	public static Comparator<Hand> HandRank = new Comparator<Hand>() {

		public int compare(Hand h1, Hand h2) {

			int result = 0;

			result = h2.getHandScore().getHandStrength().getHandStrength()
					- h1.getHandScore().getHandStrength().getHandStrength();

			if (result != 0) {
				return result;
			}

			result = h2.getHandScore().getHiHand().getiRankNbr() - h1.getHandScore().getHiHand().getiRankNbr();
			if (result != 0) {
				return result;
			}

			if ((h2.getHandScore().getLoHand() != null) && (h1.getHandScore().getLoHand() != null)) {
				result = h2.getHandScore().getLoHand().getiRankNbr() - h1.getHandScore().getLoHand().getiRankNbr();
			}

			if (result != 0) {
				return result;
			}

			if (h2.getHandScore().getKickers().size() > 0) {
				if (h1.getHandScore().getKickers().size() > 0) {
					result = h2.getHandScore().getKickers().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr()
							- h1.getHandScore().getKickers().get(eCardNo.FirstCard.getCardNo()).geteRank()
									.getiRankNbr();
				}
				if (result != 0) {
					return result;
				}
			}

			if (h2.getHandScore().getKickers().size() > 1) {
				if (h1.getHandScore().getKickers().size() > 1) {
					result = h2.getHandScore().getKickers().get(eCardNo.SecondCard.getCardNo()).geteRank().getiRankNbr()
							- h1.getHandScore().getKickers().get(eCardNo.SecondCard.getCardNo()).geteRank()
									.getiRankNbr();
				}
				if (result != 0) {
					return result;
				}
			}

			if (h2.getHandScore().getKickers().size() > 2) {
				if (h1.getHandScore().getKickers().size() > 2) {
					result = h2.getHandScore().getKickers().get(eCardNo.ThirdCard.getCardNo()).geteRank().getiRankNbr()
							- h1.getHandScore().getKickers().get(eCardNo.ThirdCard.getCardNo()).geteRank()
									.getiRankNbr();
				}
				if (result != 0) {
					return result;
				}
			}

			if (h2.getHandScore().getKickers().size() > 3) {
				if (h1.getHandScore().getKickers().size() > 3) {
					result = h2.getHandScore().getKickers().get(eCardNo.FourthCard.getCardNo()).geteRank().getiRankNbr()
							- h1.getHandScore().getKickers().get(eCardNo.FourthCard.getCardNo()).geteRank()
									.getiRankNbr();
				}
				if (result != 0) {
					return result;
				}
			}
			return 0;
		}
	};
}
