/**
 * Created by Andrew Bertino
 * Started 2/23/19
 * CS490
 */
package com.example.yahtzee

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

data class Player(val name: String, val playerScoreSheet: List<ScoreBox>,
                  var upperTotalScore: Int, var totalScore: Int, var upperScoreBonus: Boolean = false,
                  var upperScoreBonusActivated: Boolean = false)
data class Dice(val button: Button, var isSelected: Boolean = false, var value: Int = 0)
data class ScoreBox(val button: Button, var value: Int = 0, var isSelected: Boolean = false, var isSaved: Boolean = false, var isCalculated: Boolean = false)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val player1ScoreTextView = findViewById<TextView>(R.id.player1ScoreTextView)
        val player2ScoreTextView = findViewById<TextView>(R.id.player2ScoreTextView)
        // This symbol: > That is next to each players turn on their turn
        val player1IndicatorTextView = findViewById<TextView>(R.id.player1IndicatorTextView)
        val player2IndicatorTextView = findViewById<TextView>(R.id.player2IndicatorTextView)

        val upperScoreBonusTextView = findViewById<TextView>(R.id.upperScoreBonusTextView)

        //initializing the roll button and play button
        val rollButton = findViewById<Button>(R.id.rollButton)
        val playButton = findViewById<Button>(R.id.playButton)
        val nextTurnButton = findViewById<Button>(R.id.nextTurnButton)
        val endGameButton  =findViewById<Button>(R.id.endGameButton)

        //initializing dice buttons
        val dice1Button = findViewById<Button>(R.id.dice1Button)
        val dice2Button = findViewById<Button>(R.id.dice2Button)
        val dice3Button = findViewById<Button>(R.id.dice3Button)
        val dice4Button = findViewById<Button>(R.id.dice4Button)
        val dice5Button = findViewById<Button>(R.id.dice5Button)

        //initializing the Dice items
        val dice1 = Dice(dice1Button)
        val dice2 = Dice(dice2Button)
        val dice3 = Dice(dice3Button)
        val dice4 = Dice(dice4Button)
        val dice5 = Dice(dice5Button)
        //slapping the dice items into this list of type Dice
        val diceList = listOf(dice1, dice2, dice3, dice4, dice5)

        val numPlayers = 2
        val playerList = mutableListOf<Player>()
        var thisPlayersTurn = 0
        val maxNumberOfRounds = 0 //for testing purposes

        for (n in 0 until numPlayers) {
            playerList.add(Player("Player ${n + 1}", createScoreSheet(), 0, 0, false))
        }

        var turnCount = 1
        var roundCount = 1

            //when you click the ROLL button
            rollButton.setOnClickListener {
                    //make the score boxes clickable again unless they are saved already
                    playerList[thisPlayersTurn].playerScoreSheet.forEach { n ->
                        if (!n.isSaved) {
                            n.button.isClickable = true
                        }
                    }

                setPlayerNameColor(thisPlayersTurn, player1ScoreTextView, player2ScoreTextView, player1IndicatorTextView, player2IndicatorTextView)

                    //player can only roll 3 times
                    when {
                        turnCount <= 3 -> {
                            rollButton.text = "Roll #${turnCount + 1}"
                            startNewRole(
                                diceList,
                                playerList,
                                turnCount,
                                numPlayers,
                                thisPlayersTurn,
                                this@MainActivity
                            )
                            if (turnCount == 3) {
                                rollButton.text = "No more rolls!"
                            }
                            turnCount++

                        }
                    }
            }




            playButton.setOnClickListener {
                var itemIsSelected = false
                playerList[thisPlayersTurn].playerScoreSheet.forEach { n ->
                    if (n.isSelected && !n.isSaved) {
                        itemIsSelected = true
                    }
                }
                //makes sure a roll has happened and there is actually something selected
                if (turnCount > 1 && itemIsSelected) {
                    playerList[thisPlayersTurn].playerScoreSheet.forEachIndexed { index, scoreBox ->
                        if (scoreBox.isSelected) {
                            scoreBox.isSaved = true
                        }
                        if (scoreBox.isSaved) {
                            if (!scoreBox.isCalculated) {
                                //add to total score
                                playerList[thisPlayersTurn].totalScore += scoreBox.value
                                //if its in the upper section
                                if (index <= 5) {
                                    playerList[thisPlayersTurn].upperTotalScore += scoreBox.value
                                }
                                scoreBox.isCalculated = true

                            }

                            scoreBox.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))

                        } else {
                            scoreBox.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
                        }
                    }

                    player1ScoreTextView.text = "Player 1's Score: ${playerList[0].totalScore}"
                    player2ScoreTextView.text = "Player 2's Score: ${playerList[1].totalScore}"
                    //test
                    //upperScoreTotalTextView.text = "Upper Score: ${playerList[thisPlayersTurn].upperTotalScore}"

                    activateUpperBonus(playerList, thisPlayersTurn)

                    playButton.visibility = (View.INVISIBLE)
                    playButton.isClickable = false

                    nextTurnButton.visibility = (View.VISIBLE)
                    nextTurnButton.isClickable = true

                }
            }



            nextTurnButton.setOnClickListener {

                if(roundCount == maxNumberOfRounds + 1) {
                    endGameButton.visibility = (View.VISIBLE)
                    endGameButton.isClickable = true

                }


                when {
                    //goes to next players turn
                    (thisPlayersTurn + 1) < numPlayers -> {
                        thisPlayersTurn++
                        turnCount = 1
                        nextTurn(playerList, thisPlayersTurn, diceList)
                        setPlayerNameColor(thisPlayersTurn, player1ScoreTextView, player2ScoreTextView, player1IndicatorTextView, player2IndicatorTextView)

                    }
                    //goes to the first players turn
                    (thisPlayersTurn + 1) == numPlayers -> {
                        thisPlayersTurn = 0
                        turnCount = 1
                        nextTurn(playerList, thisPlayersTurn, diceList)
                        roundCount++
                        setPlayerNameColor(thisPlayersTurn, player1ScoreTextView, player2ScoreTextView, player1IndicatorTextView, player2IndicatorTextView)
                        Toast.makeText(this, "Round Count: $roundCount", Toast.LENGTH_SHORT).show()
                    }
                }
                rollButton.text = "ROLL #1"

                player1ScoreTextView.text = "Player 1's Score: ${playerList[0].totalScore}"
                player2ScoreTextView.text = "Player 2's Score: ${playerList[1].totalScore}"

                if(playerList[thisPlayersTurn].upperScoreBonusActivated){
                    upperScoreBonusTextView.visibility = View.VISIBLE
                }
                else {
                    upperScoreBonusTextView.visibility = View.INVISIBLE
                }

                nextTurnButton.visibility = (View.INVISIBLE)
                nextTurnButton.isClickable = false

                playButton.isClickable = true
                playButton.visibility = (View.VISIBLE)
            }



        endGameButton.setOnClickListener{
            endGameMessage(playerList)

        }
    }

    private fun endGameMessage(playerList: MutableList<Player>){

        val intent = Intent(this,EndGameActivity::class.java)
        intent.putExtra("player1Score",playerList[0].totalScore)
        intent.putExtra("player2Score", playerList[1].totalScore)
        startActivity(intent)

    }

    private fun activateUpperBonus(playersScoreSheet: MutableList<Player>, thisPlayersTurn: Int)
    {
        if(playersScoreSheet[thisPlayersTurn].upperTotalScore >= 63){
            playersScoreSheet[thisPlayersTurn].upperScoreBonus = true
            if(playersScoreSheet[thisPlayersTurn].upperScoreBonus&&!playersScoreSheet[thisPlayersTurn].upperScoreBonusActivated){
                upperScoreBonusTextView.visibility = View.VISIBLE
                playersScoreSheet[thisPlayersTurn].totalScore += 35
            }
            if(playersScoreSheet[thisPlayersTurn].upperScoreBonus){

                playersScoreSheet[thisPlayersTurn].upperScoreBonusActivated = true
            }
        }
    }

    //activates dice buttons
    private fun startNewRole(diceList: List<Dice>, playersScoreSheet: MutableList<Player>, turnCount: Int, numPlayers: Int, thisPlayersTurn: Int, context: Context) {
        Toast.makeText(this,"Player ${thisPlayersTurn+1} Turn #$turnCount",Toast.LENGTH_SHORT).show()

        if(turnCount==1)
        {
            diceList.forEach { n->
                n.isSelected = false
                n.value = 0
                n.button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))}
        }
        for(n in 0..4) {
            //create a random number
            val rng = (1..6).random()
            //set the button text to the random number
            if(!diceList[n].isSelected) {
                diceList[n].button.text = rng.toString()
                diceList[n].value = rng
            }
        }
        //set click listeners for all of the dice buttons in an efficient loop
        diceList.forEach { n-> n.button.setOnClickListener{
            if(!n.isSelected)
            {
                n.isSelected=true
                n.button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSavedButton))
            }
            else
            {
                n.isSelected=false
                n.button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

            }
        }}

        //set click listeners for all of the upper score buttons in an efficient loop
        playersScoreSheet[thisPlayersTurn].playerScoreSheet.forEach { n -> n.button.setOnClickListener{
            if(!n.isSaved) {
                if (!n.isSelected) {
                    //makes sure you can only select one button at a time
                    playersScoreSheet[thisPlayersTurn].playerScoreSheet.forEach { n ->
                        /*if(!n.isSaved) {
                            n.isSelected = false
                            n.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
                        }*/
                            n.isSelected = false
                        if(!n.isSaved) {
                            n.button.setBackgroundColor(ContextCompat.getColor(context,R.color.colorWhite))
                        }
                    }
                    n.isSelected = true
                    //n.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSavedButton))
                } else {
                    n.isSelected = false
                    //n.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
                }
                if(n.isSelected)
                {
                    n.button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSavedButton))
                }
                else {
                    n.button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
            }


            }
        }}
        calcScore(diceList,playersScoreSheet[thisPlayersTurn].playerScoreSheet, playersScoreSheet[thisPlayersTurn].upperTotalScore,
            playersScoreSheet[thisPlayersTurn].upperScoreBonus, playersScoreSheet[thisPlayersTurn].totalScore,
            playersScoreSheet[thisPlayersTurn].upperScoreBonusActivated)

        //calculates and returns total score
        playersScoreSheet[thisPlayersTurn].totalScore = calcPlayerTotalScore(playersScoreSheet, thisPlayersTurn)
    }

    @TargetApi(24)
    private fun calcScore(diceList: List<Dice>, playerScoreSheet: List<ScoreBox>, playerUpperScore: Int, playerUpperScoreBonus: Boolean, playerTotalScore: Int, playerUpperScoreBonusActivated: Boolean){
       /* var playerUpperScoreBonus = playerUpperScoreBonus
        var playerTotalScore = playerTotalScore
        var playerUpperScoreBonusActivated = playerUpperScoreBonusActivated*/
        val frequenciesOfNumbers = diceList.groupingBy { it.value }.eachCount()
        var sumOfAllDice = 0



        //makes sure the values reset to 0 if they are not saved
        playerScoreSheet.forEach { n ->
            if(!n.isSaved) {
                n.value = 0
            }
        }
        //determines the points in the upper section
        for(n in 0 until diceList.size){
            when(diceList[n].value){
                1 -> {if(!playerScoreSheet[0].isSaved)
                {playerScoreSheet[0].value+=1}
                sumOfAllDice+=1}
                2 -> {if(!playerScoreSheet[1].isSaved)
                {playerScoreSheet[1].value+=2}
                    sumOfAllDice+=2}
                3 -> {if(!playerScoreSheet[2].isSaved)
                {playerScoreSheet[2].value+=3}
                    sumOfAllDice+=3}
                4 -> {if(!playerScoreSheet[3].isSaved)
                {playerScoreSheet[3].value+=4}
                    sumOfAllDice+=4}
                5 -> {if(!playerScoreSheet[4].isSaved)
                {playerScoreSheet[4].value+=5}
                    sumOfAllDice+=5}
                6 -> {if(!playerScoreSheet[5].isSaved)
                {playerScoreSheet[5].value+=6}
                    sumOfAllDice+=6}
            }
        }
        //chance
        if(!playerScoreSheet[12].isSaved){
        playerScoreSheet[12].value = sumOfAllDice
        }

        //checks and calculates 3x, 4x, full house,  and yahtzee
        frequenciesOfNumbers.forEach{ (number, frequency) ->
            //three Of A Kind check
            if(frequency >= 3){
                if(!playerScoreSheet[6].isSaved){
                playerScoreSheet[6].value = sumOfAllDice}

                //full house check

                if(frequenciesOfNumbers.size == 2 && frequency==3){
                    if(!playerScoreSheet[8].isSaved){
                    playerScoreSheet[8].value = 25}
                }
            }

            //four Of A Kind check
            if(frequency >= 4){
                if(!playerScoreSheet[7].isSaved){
                    playerScoreSheet[7].value = sumOfAllDice
                }
            }

            //yahtzee check
            if(frequency == 5){
                if(!playerScoreSheet[11].isSaved){
                    playerScoreSheet[11].value = 50
                }
            }

            //large straight check
            if(frequenciesOfNumbers.size == 5)
            {
                //dice values
                val listOfValues  = mutableListOf<Int>()

                for(n in 0 until diceList.size) {
                    listOfValues.add(diceList[n].value)
                }
                //sorted list of dice values
                val sortedListOfValues = listOfValues.sorted()

                if(sortedListOfValues == mutableListOf(1,2,3,4,5) || sortedListOfValues == mutableListOf(2,3,4,5,6)) {
                    if(!playerScoreSheet[9].isSaved){
                        playerScoreSheet[9].value = 30
                    }
                    if(!playerScoreSheet[10].isSaved){
                    playerScoreSheet[10].value = 40
                    }
                }
            }

            //small straight check
            if(frequenciesOfNumbers.size >= 4)
            {
                //dice values
                val listOfValues  = mutableListOf<Int>()

                for(n in 0 until diceList.size) {

                    listOfValues.add(diceList[n].value)
                }

                //sorted list of distinct dice values
                val sortedDistinctListOfValues = listOfValues.sorted().distinct()

                //all possible small straight combos
                if(sortedDistinctListOfValues == mutableListOf(1,2,3,4) || sortedDistinctListOfValues == mutableListOf(1,2,3,4,5) ||
                    sortedDistinctListOfValues == mutableListOf(1,2,3,4,6) || sortedDistinctListOfValues == mutableListOf(2,3,4,5) ||
                    sortedDistinctListOfValues == mutableListOf(1,3,4,5,6) || sortedDistinctListOfValues == mutableListOf(3,4,5,6)) {
                    if(!playerScoreSheet[9].isSaved){playerScoreSheet[9].value = 30}
                }
            }
        }



        //displays the value on the score buttons
        for(n in 0 until playerScoreSheet.size) {
            //stops the number from updating if you have selected to save it, might be a bad implementation bc
            //the numbers still change in the background
            playerScoreSheet[n].button.text = playerScoreSheet[n].value.toString()
        }

        //ended here
        upperScoreBonusTextView.setText("BONUS +35")
    }

    private fun calcPlayerTotalScore(playersScoreSheet: MutableList<Player>, thisPlayersTurn: Int): Int{

        var totalScore = playersScoreSheet[thisPlayersTurn].totalScore
        for(scoreBox in playersScoreSheet[thisPlayersTurn].playerScoreSheet) {
            if(scoreBox.isSaved)
            totalScore += scoreBox.value
        }

        return totalScore

    }



    private fun createScoreSheet(): List<ScoreBox>{
        val onesButton = findViewById<Button>(R.id.onesButton)
        val twosButton = findViewById<Button>(R.id.twosButton)
        val threesButton = findViewById<Button>(R.id.threesButton)
        val foursButton = findViewById<Button>(R.id.foursButton)
        val fivesButton = findViewById<Button>(R.id.fivesButton)
        val sixesButton = findViewById<Button>(R.id.sixesButton)
        val threeOfAKindButton = findViewById<Button>(R.id.threeOfAKindButton)
        val fourOfAKindButton = findViewById<Button>(R.id.fourOfAKindButton)
        val fullHouseButton = findViewById<Button>(R.id.fullHouseButton)
        val smallStraightButton = findViewById<Button>(R.id.smallStraightButton)
        val largeStraightButton = findViewById<Button>(R.id.largeStraightButton)
        val yahtzeeButton = findViewById<Button>(R.id.yahtzeeButton)
        val chanceButton = findViewById<Button>(R.id.chanceButton)

        //initializing score boxes
        val upperScoreBox1 = ScoreBox(onesButton)
        val upperScoreBox2 = ScoreBox(twosButton)
        val upperScoreBox3 = ScoreBox(threesButton)
        val upperScoreBox4 = ScoreBox(foursButton)
        val upperScoreBox5 = ScoreBox(fivesButton)
        val upperScoreBox6 = ScoreBox(sixesButton)
        //initializing lower score boxes
        val lowerScoreBox1 = ScoreBox(threeOfAKindButton)
        val lowerScoreBox2 = ScoreBox(fourOfAKindButton)
        val lowerScoreBox3 = ScoreBox(fullHouseButton)
        val lowerScoreBox4 = ScoreBox(smallStraightButton)
        val lowerScoreBox5 = ScoreBox(largeStraightButton)
        val lowerScoreBox6 = ScoreBox(yahtzeeButton)
        val lowerScoreBox7 = ScoreBox(chanceButton)

        //returns score-sheet to player
        return listOf(upperScoreBox1,upperScoreBox2,upperScoreBox3,
            upperScoreBox4,upperScoreBox5,upperScoreBox6, lowerScoreBox1,
            lowerScoreBox2, lowerScoreBox3, lowerScoreBox4, lowerScoreBox5,
            lowerScoreBox6, lowerScoreBox7)
    }

    private fun nextTurn(playerList: MutableList<Player>, thisPlayersTurn: Int,diceList: List<Dice>){
        //supposed to make all saved boxes green and non saved ones white
        playerList[thisPlayersTurn].playerScoreSheet.forEach { n ->
            if(n.isSaved)
            {

                n.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            }
            else
                n.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
        }

        diceList.forEach { n->
            n.button.isClickable = false
            n.button.text = "0"
            n.button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
        }

        playerList[thisPlayersTurn].playerScoreSheet.forEach { n->
            //shows appropriate saved score-boxes
            if(n.isSaved) { n.button.text = n.value.toString()}
            else{
                n.button.text = "0"
            }
            n.button.isClickable = false
        }
    }
    //Changes the color of the correlating text view based on whos turn it is
    private fun setPlayerNameColor(thisPlayersTurn: Int, player1ScoreTextView: TextView,
                                   player2ScoreTextView: TextView, player1IndicatorTextView: TextView, player2IndicatorTextView: TextView){
        if(thisPlayersTurn==0){

            player1ScoreTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            player2ScoreTextView.setTextColor(ContextCompat.getColor(this, R.color.colorGrey))
            player1IndicatorTextView.visibility = View.VISIBLE
            player2IndicatorTextView.visibility = View.INVISIBLE

        } else if(thisPlayersTurn==1)
        {
            player2ScoreTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            player1ScoreTextView.setTextColor(ContextCompat.getColor(this, R.color.colorGrey))
            player1IndicatorTextView.visibility = View.INVISIBLE
            player2IndicatorTextView.visibility = View.VISIBLE
        }

    }
//todo change random algorithm because its pseudo-random??
    /**
     * add end game screen
     * add home menu
     * add data persistence for highest scores
     * add option to play against a bot
     *
     */

    /**
     * LOGS
     * Version: 0.1 Development
     *
     *
     */
}