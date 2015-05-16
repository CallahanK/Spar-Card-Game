package spar;

import java.util.List;

public class ComputerPlayer {

	//Makes a non-lead trick play
	//Must try to match suit if possible
	public int makePlay(List<Card> hand, int suit){
		int play = 0;
		int playRank = 0;
		//When only one card remains play
		//regardless of suit
		if(hand.size()==1){
			return hand.get(0).getId();
		}
		for (int i = 0; i < hand.size(); i++){
			int tempId = hand.get(i).getId();
			int tempRank = hand.get(i).getRank();
			int tempSuit = hand.get(i).getSuit();

			//Must first attempt to play a matching suit
			if(tempSuit==suit){
				if(play==0){
					play = tempId;
					playRank = tempRank;
				} else if(tempId<play){
					play = tempId;
					playRank = tempRank;
				}
			}
		}
		//If no play of a matching suit, play the lowest card
		//If tie for lowest, play the least frequent suit
		if(play==0){
			for (int i = 0; i < hand.size(); i++){
				int tempId = hand.get(i).getId();
				int tempRank = hand.get(i).getRank();
				int tempSuit = hand.get(i).getSuit();


				if(play==0){
					play = tempId;
					playRank = tempRank;
				} else if(tempRank<playRank){
					play = tempId;
					playRank = tempRank;
				}
				//TODO
				//In event of tie for lowest card,
				//Pick card of least frequent suit

			}
		}
		return play;
	}

	//Makes a lead trick play
	//Plays lowest card in hand,
	//of least frequent suit if tie
	public int makePlay(List<Card> hand){
		int play = 0;
		int playRank = 0;
		if(hand.size()==1){
			return hand.get(0).getId();
		}
		for (int i = 0; i < hand.size(); i++){
			int tempId = hand.get(i).getId();
			int tempRank = hand.get(i).getRank();
			int tempSuit = hand.get(i).getSuit();


			if(play==0){
				play = tempId;
				playRank = tempRank;
			} else if(tempRank<playRank){
				play = tempId;
				playRank = tempRank;
			}
			//TODO
			//In event of tie for lowest card,
			//Pick card of least frequent suit

		}

		return play;
	}


	
}
