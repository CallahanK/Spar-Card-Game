package spar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends View {

	private int screenW;
	private int screenH;
	private Context myContext;
	private List<Card> deck = new ArrayList<Card>();
	private int scaledCardW;
	private int scaledCardH;
	private Paint whitePaint;
	private Paint trickPaint;
	private Paint rightPaint;
	private List<Card> myHand = new ArrayList<Card>();
	private List<Card> oppHand = new ArrayList<Card>();
	private List<Card> myTrick = new ArrayList<Card>();
	private List<Card> oppTrick = new ArrayList<Card>();
	private int myScore = 0;
	private int oppScore = 0;
	private float scale;
	private Bitmap cardBack;
	private List<Card> discardPile = new ArrayList<Card>();
	private boolean myTurn;
	private ComputerPlayer computerPlayer = new ComputerPlayer();
	private int movingCardIdx = -1;
	private int movingX;
	private int movingY;
	private int validSuit = 0;
	private Bitmap nextCardButton;
	private int myScoreThisHand = 0;
	private int oppScoreThisHand = 0;
	private int trickNumber = 0;
	private Random gen = new Random();
	private Boolean leadTrick;
	static final int MAXSCORE = 30;
	
	public GameView(Context context) {
		super(context);
		myContext = context;
		// DIP: device independent pixel
		// See http://developer.android.com/reference/android/util/DisplayMetrics.html#density
		scale = myContext.getResources().getDisplayMetrics().density;  
		
		whitePaint = new Paint();
		whitePaint.setAntiAlias(true);
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStyle(Paint.Style.STROKE);
		whitePaint.setTextAlign(Paint.Align.LEFT);
		whitePaint.setTextSize(scale * 30);

		trickPaint = new Paint();
		trickPaint.setAntiAlias(true);
		trickPaint.setColor(Color.WHITE);
		trickPaint.setStyle(Paint.Style.STROKE);
		trickPaint.setTextAlign(Paint.Align.LEFT);
		trickPaint.setTextSize(scale * 45);

		rightPaint = trickPaint;
		rightPaint.setTextAlign(Paint.Align.RIGHT);
	}

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        screenW = w;
        screenH = h;
		Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.card_back);
        scaledCardW = (int) (screenW/8);
        scaledCardH = (int) (scaledCardW*1.28);
		cardBack = Bitmap.createScaledBitmap(tempBitmap, scaledCardW, scaledCardH, false); 
		nextCardButton = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_next); 
        initCards();
        dealCards();
        drawCard(discardPile);
        validSuit = discardPile.get(0).getSuit();
		myTurn = new Random().nextBoolean();
		leadTrick = true;
		System.out.println("Started Round");
		System.out.println("MyTurn: " + myTurn);

		if (!myTurn) {
			makeComputerPlay();			
		}
    }
	   
	@Override 
	protected void onDraw(Canvas canvas) {
		canvas.drawText("Computer Score: " + Integer.toString(oppScore), 10, whitePaint.getTextSize()+10, whitePaint);
		canvas.drawText("My Score: " + Integer.toString(myScore), 10, screenH-whitePaint.getTextSize()-10, whitePaint);

		canvas.drawLine(0,(screenH / 2) - (200 * scale),screenW,(screenH / 2) - (200 * scale),whitePaint);
		canvas.drawLine(0,(screenH / 2) + (200 * scale),screenW,(screenH / 2) + (200 * scale),whitePaint);

		canvas.drawText("1", 0*(scaledCardW+5)+(scaledCardW/2), screenH/2, trickPaint);
		canvas.drawText("2", 1*(scaledCardW+5)+(scaledCardW/2), screenH/2, trickPaint);
		canvas.drawText("3", 2*(scaledCardW+5)+(scaledCardW/2), screenH/2, trickPaint);
		canvas.drawText("4", 3*(scaledCardW+5)+(scaledCardW/2), screenH/2, trickPaint);
		canvas.drawText("5", 4*(scaledCardW+5)+(scaledCardW/2), screenH/2, trickPaint);
		canvas.drawText("Tricks", screenW-(30*scale), screenH/2, rightPaint);
		for (int i = 0; i < oppHand.size(); i++) {
			canvas.drawBitmap(cardBack,
			//canvas.drawBitmap(oppHand.get(i).getBitmap(),
					//i*(scale*5),
					i*(scaledCardW+5),
					whitePaint.getTextSize()+(40*scale),
					null);
		}

		if (myHand.size() > 7) {
			canvas.drawBitmap(nextCardButton, 
					screenW-nextCardButton.getWidth()-(30*scale), 
					screenH-nextCardButton.getHeight()-scaledCardH-(90*scale), 
					null);
		}
		//Displays computer's current trick
		for (int i = 0; i < oppTrick.size(); i++) {
			canvas.drawBitmap(oppTrick.get(i).getBitmap(),
					(oppTrick.size()-1-i)*(scaledCardW+5),
					(screenH/3)-(20*scale),
					null);
		}

		//Displays player's current trick
		for (int i = 0; i < myTrick.size(); i++) {
			canvas.drawBitmap(myTrick.get(i).getBitmap(),
					(myTrick.size()-1-i)*(scaledCardW+5),
					(screenH/2)+(30*scale),
					null);
		}
		for (int i = 0; i < myHand.size(); i++) {
			if (i == movingCardIdx) {
				canvas.drawBitmap(myHand.get(i).getBitmap(), 
						movingX, 
						movingY, 
						null);						
			} else {
				if (i < 7) {
					canvas.drawBitmap(myHand.get(i).getBitmap(), 
							i*(scaledCardW+5), 
							screenH-scaledCardH-whitePaint.getTextSize()-(50*scale), 
							null);							
				}
			}
		}			
		invalidate();
	}
	     
	public boolean onTouchEvent(MotionEvent event) {
	        int eventaction = event.getAction();   
	        int X = (int)event.getX();
	        int Y = (int)event.getY();

	        switch (eventaction ) {

	        case MotionEvent.ACTION_DOWN:
	        	if (myTurn) {
		        	for (int i = 0; i < 7; i++) {
		        		if (X > i*(scaledCardW+5) && X < i*(scaledCardW+5) + scaledCardW && 
		        			Y > screenH-scaledCardH-whitePaint.getTextSize()-(50*scale)) {
		        			movingCardIdx = i;
		    	        	movingX = X-(int)(30*scale);
		    	        	movingY = Y-(int)(70*scale);
		        		}
		        	}	        		
	        	}
	        	break;

	        case MotionEvent.ACTION_MOVE: 
	        	movingX = X-(int)(30*scale);
	        	movingY = Y-(int)(70*scale);
	        	break;
	        	
	        case MotionEvent.ACTION_UP:
			if(myTurn) {
				if (leadTrick) {
					if (movingCardIdx > -1 &&
							//X > (screenW / 2) - (100 * scale) &&
							//X < (screenW / 2) + (100 * scale) &&
							Y > (screenH / 2) - (200 * scale) &&
							Y < (screenH / 2) + (200 * scale)) {
						validSuit = myHand.get(movingCardIdx).getSuit();
						System.out.println("New Player Suit " + validSuit);
						myTrick.add(0, myHand.get(movingCardIdx));
						myHand.remove(movingCardIdx);

						leadTrick = false;
						myTurn = false;
						makeComputerPlay();
					}
				} else {
					if (movingCardIdx > -1 &&
							//X > (screenW / 2) - (100 * scale) &&
							//X < (screenW / 2) + (100 * scale) &&
							Y > (screenH / 2) - (200 * scale) &&
							Y < (screenH / 2) + (200 * scale) &&
							(myHand.get(movingCardIdx).getSuit() == validSuit) || checkForValidPlay()) {
						myTrick.add(0, myHand.get(movingCardIdx));
						myHand.remove(movingCardIdx);

						endTrick();

					} else {
						String suitText = "";
						if (validSuit == 100) {
							suitText = "Diamonds";
						} else if (validSuit == 200) {
							suitText = "Clubs";
						} else if (validSuit == 300) {
							suitText = "Hearts";
						} else if (validSuit == 400) {
							suitText = "Spades";
						}
						Toast.makeText(myContext, "The current suit is " + suitText, Toast.LENGTH_SHORT).show();
					}
				}
			}
	        	if (myHand.size() > 7 &&
	        		X > screenW-nextCardButton.getWidth()-(30*scale) &&
	        		Y > screenH-nextCardButton.getHeight()-scaledCardH-(90*scale) &&
	        		Y < screenH-nextCardButton.getHeight()-scaledCardH-(60*scale)) {
	        			Collections.rotate(myHand, 1);
	        	}
	        	movingCardIdx = -1;
	        	break;
	        } 
	        invalidate();
			return true;      
	}

	private void initCards() {
		//Generates a deck of cards 6-A all suits
		for (int i = 0; i < 4; i++) {
			for (int j = 106; j < 115; j++) {
				int tempId = j + (i*100);
				Card tempCard = new Card(tempId);
				
				// pkgName is the java class package name
				String pkgName = myContext.getPackageName();
				
				// 1) getResources() or myContext.getResources() doesn't matter		
				// 2) nowhere is ".png" mentioned
				int resourceId = myContext.getResources().getIdentifier("card" + tempId, "drawable", pkgName);
				
				// decodeResource apparently interprets resourceId
				Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
		        scaledCardW = (int) (screenW/8);
		        scaledCardH = (int) (scaledCardW*1.28);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardW, scaledCardH, false); 
				tempCard.setBitmap(scaledBitmap);
				deck.add(tempCard);
			}
		}
	}
	
	private void drawCard(List<Card> handToDraw) {
		handToDraw.add(0, deck.get(0));
		deck.remove(0);
		if (deck.isEmpty()) {
			for (int i = discardPile.size()-1; i > 0 ; i--) {
				deck.add(discardPile.get(i));
				discardPile.remove(i);
				
				// Looks like this could be outside the loop
				Collections.shuffle(deck,new Random());
			}
		}
	}
	
	private void dealCards() {
		Collections.shuffle(deck,new Random());
		for (int i = 0; i < 5; i++) {
			drawCard(myHand);
			drawCard(oppHand);
		}		
	}
	
	private boolean checkForValidPlay() {
		boolean canDraw = true;
		for (int i = 0; i < myHand.size(); i++) {
			int tempId = myHand.get(i).getId();
			int tempSuit = myHand.get(i).getSuit();
			if (validSuit == tempSuit ) {
				canDraw = false; 
			} 
		}
		return canDraw;
	}
	
	private void makeComputerPlay() {
		int tempPlay = 0;

		if(leadTrick){
			tempPlay = computerPlayer.makePlay(oppHand);
			validSuit = Math.round((tempPlay / 100) * 100);
			System.out.println("New Computer Suit " + validSuit);
		} else {
			tempPlay = computerPlayer.makePlay(oppHand, validSuit);
		}

		for (int i = 0; i < oppHand.size(); i++) {
			Card tempCard = oppHand.get(i);
			if (tempPlay == tempCard.getId()) {
				oppTrick.add(0,oppHand.get(i));
				oppHand.remove(i);				
			}
		}
		//Not lead trick means this is the
		//second move on a trick
		if(leadTrick){
			leadTrick = false;
			myTurn = true;
		} else {
			leadTrick = true;
			endTrick();
		}

	}

	private void endTrick(){
		System.out.println("End of Trick " + trickNumber);

		if(trickNumber==4){
			trickNumber = 0;
			endHand();
		} else {
			System.out.println("My suit: " + myTrick.get(0).getSuit() );
			//Checks if player has played a valid suit
			if (myTrick.get(0).getSuit() == validSuit) {
				//Checks if opp has valid suit and if player has higher ranked card
				if (myTrick.get(0).getRank() > oppTrick.get(0).getRank() || oppTrick.get(0).getSuit() != validSuit) {
					Toast.makeText(myContext, "You won the trick.", Toast.LENGTH_SHORT).show();
					System.out.println("Player Wins Trick");
					//Player wins trick
					myTurn = true;
				} else {
					Toast.makeText(myContext, "You lost the trick.", Toast.LENGTH_SHORT).show();
					System.out.println("Computer Wins Trick");
					//Opp wins trick
					myTurn = false;
				}
			} else {
				Toast.makeText(myContext, "You lost the trick.", Toast.LENGTH_SHORT).show();
				System.out.println("Computer Wins Trick");
				//Opp wins trick
				myTurn = false;
			}
			leadTrick = true;
			trickNumber++;
			/*if(trickNumber==5){
				trickNumber = 0;
				endHand();
			}*/

			leadTrick = true;
			if (!myTurn) {
				makeComputerPlay();
			}
		}
	}

	private void endHand() {
		final Dialog endHandDialog = new Dialog(myContext);
		endHandDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		endHandDialog.setContentView(R.layout.end_hand_dialog);
		updateScores();
		TextView endHandText = (TextView) endHandDialog.findViewById(R.id.endHandText);
		if (myScoreThisHand>0) {
			if (myScore >= MAXSCORE) {
				endHandText.setText("You reached " + myScore + " points. You won! Would you like to play again?");
			} else {
				endHandText.setText("You won the final trick and got " + myScoreThisHand + " points!");
			}
		} else if (oppScoreThisHand>0) {
			if (oppScore >= MAXSCORE) {
				endHandText.setText("The computer reached " + oppScore + " points. Sorry, you lost. Would you like to play again?");
			} else {
				endHandText.setText("The computer won the final trick and got " + oppScoreThisHand + " points.");
			}
		}
        Button nextHandButton = (Button) endHandDialog.findViewById(R.id.nextHandButton);
        if (oppScore >= MAXSCORE || myScore >= MAXSCORE) {
        	nextHandButton.setText("New Game");
        }
        nextHandButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if (oppScore >= MAXSCORE || myScore >= MAXSCORE) {
                	myScore = 0;
                	oppScore = 0;
                } 
                initNewHand();                	
            	endHandDialog.dismiss();
            }
        }); 
        endHandDialog.show();
	}

	//Scores for spar scoring now
	private void updateScores() {
		Card oppCard = oppTrick.get(0);
		Card myCard = myTrick.get(0);

		if(myCard.getSuit()==validSuit){
			if(oppCard.getSuit()==validSuit){
				if(myCard.getRank()>oppCard.getRank()){
					if(myCard.getRank()==7){
						myScore += 2;
						myScoreThisHand = 2;
					} else {
						myScore += 1;
						myScoreThisHand = 1;
					}
				} else {
					if(oppCard.getRank()==7){
						oppScore += 2;
						oppScoreThisHand = 2;
					} else {
						oppScore += 1;
						oppScoreThisHand = 1;
					}
				}
			} else if(myCard.getRank()==6){
				myScore += 3;
				myScoreThisHand = 3;
			} else if(myCard.getRank()==7){
				myScore += 2;
				myScoreThisHand = 2;
			} else {
				myScore += 1;
				myScoreThisHand = 1;
			}
		} else if(oppCard.getRank()==6){
			oppScore += 3;
			oppScoreThisHand = 3;
		} else if(oppCard.getRank()==7){
			oppScore += 2;
			oppScoreThisHand = 2;
		} else {
			oppScore += 1;
			oppScoreThisHand = 1;
		}
	}
	
	private void initNewHand() {
		if(myScoreThisHand>0){
			myTurn = false;
		} else {
			myTurn = true;
		}

    	myScoreThisHand = 0;
		oppScoreThisHand = 0;

		//First hand of game, so random
		//a starting player
		if(myScore+oppScore==0){
			myTurn = gen.nextBoolean();
		}

		deck.addAll(myTrick);
		deck.addAll(oppTrick);
		deck.addAll(myHand);
		deck.addAll(oppHand);

		myTrick.clear();
		oppTrick.clear();
		myHand.clear();
		oppHand.clear();
        dealCards();

		leadTrick = true;
		if (!myTurn) {
			makeComputerPlay();			
		}
	}
}
