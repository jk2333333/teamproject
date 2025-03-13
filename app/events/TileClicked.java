package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import managers.BoardManager;
import managers.HandManager;
import managers.TurnManager;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.subcard.SpellCard;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.List;
import java.util.ArrayList;

/**
 * Handles logic when a tile is clicked.
 * - If a card is selected, checks if the tile is valid for summoning.
 * - If valid, the unit is summoned, mana is deducted, and the card is removed.
 * - If no card is selected, the event is ignored.
 */
public class TileClicked implements EventProcessor {

	/**
     * 处理生物卡牌召唤
     */
	public void summonUnit(ActorRef out, GameState gameState, Tile clickedTile) {
		// 1 Ensure the clicked tile is a valid summonable location
		if (!gameState.summonableTiles.contains(clickedTile)) {
			return;
		}

		Card cardToPlay = gameState.selectedCard;
		int handPos = gameState.selectedHandPosition;

		// 2 Deduct mana and remove the card from the player's hand
		if (!HandManager.deductManaAndRemoveCard(out, gameState, cardToPlay, handPos)) {
			return; // Mana insufficient or other restrictions
		}

		// 3 Summon the unit - 使用卡牌的onCardPlayed方法
		cardToPlay.onCardPlayed(out, gameState, clickedTile);

		// 4 Clear selection and reset tile highlights
		gameState.selectedCard = null;
		gameState.selectedHandPosition = -1;

		for (Tile tile : gameState.summonableTiles) {
			tile.setHighlightStatus(out, 0);
		}
		gameState.summonableTiles.clear();
		
		// 转移回合结束后的高亮
		TurnManager.highlightPlayer1ReadyUnits(out, gameState);
	}

	/**
     * 处理法术卡牌施放
     */
	public void playSpell(ActorRef out, GameState gameState, Tile clickedTile) {
	    // 确保选中了卡牌且是法术卡
	    if (gameState.selectedCard == null || gameState.selectedCard.isCreature()) {
	        return;
	    }
	    
	    // 确保点击的格子是有效目标
	    if (!gameState.summonableTiles.contains(clickedTile)) {
			return;
		}
		
		Card cardToPlay = gameState.selectedCard;
		int handPos = gameState.selectedHandPosition;
		
		// 扣除法力值并从手牌中移除卡牌
		if (!HandManager.deductManaAndRemoveCard(out, gameState, cardToPlay, handPos)) {
			return;
		}
		
		// 执行法术效果
		cardToPlay.onCardPlayed(out, gameState, clickedTile);
		
		// 清除选择状态和高亮
		gameState.selectedCard = null;
		gameState.selectedHandPosition = -1;
		
		for (Tile tile : gameState.summonableTiles) {
			tile.setHighlightStatus(out, 0);
		}
		gameState.summonableTiles.clear();
		
		// 转移回合结束后的高亮
		TurnManager.highlightPlayer1ReadyUnits(out, gameState);
	}

	/**
     * 处理单位移动
     */
	public void moveUnit(ActorRef out, GameState gameState, Tile clickedTile) {
		if (!gameState.movableTiles.contains(clickedTile)) {
			return;
		}
		System.out.println("event: " + gameState.selectedUnit.getId());
		UnitManager.moveUnit(out, gameState, gameState.selectedUnit, clickedTile);

		// Clear all highlights
		BoardManager.clearMovableTiles(out, gameState);
		BoardManager.clearAttackableTiles(out, gameState);
		TurnManager.highlightPlayer1ReadyUnits(out, gameState);
	}

	/**
     * 处理单位攻击
     */
	public void attackUnit(ActorRef out, GameState gameState, JsonNode message, Tile targetTile) {
		if (targetTile == null) {
			return;
		}

		UnitAttacking unitAttacking = new UnitAttacking();
		unitAttacking.processEvent(out, gameState, message);
	}

	/*
	 * 1. Check if the clicked tile is within bounds
	 * 
	 * 2. Declare the clicked tile
	 * 
	 * 3. If there is a card selected, summon unit or play spell
	 * 
	 * 4. If there is a unit selected, process the act
	 * 
	 * 5. If the tile is a movable or attackable tile, highlight the movable and
	 * attackable tiles
	 */
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();

		// 1. Check if the clicked tile is within bounds
		if (tilex < 0 || tilex >= 9 || tiley < 0 || tiley >= 5) {
			return;
		}

		// 2. Declare the clicked tile
		Tile clickedTile = gameState.board[tilex][tiley];

		// 3. If there is a card selected, summon unit or play spell
		if (gameState.selectedCard != null) {
			if (gameState.selectedCard.isCreature()) {
				summonUnit(out, gameState, clickedTile);
			} else {
				playSpell(out, gameState, clickedTile);
			}
			return;
		}

		// 4. If there is a unit selected, process the act
		if (gameState.selectedUnit != null) {

			// If the clicked tile is not highlighted, exit
			if (clickedTile.getHighlightStatus() == 0) {
				// Clear all highlights
				BoardManager.clearMovableTiles(out, gameState);
				BoardManager.clearAttackableTiles(out, gameState);
				TurnManager.highlightPlayer1ReadyUnits(out, gameState);
				gameState.selectedUnit = null;
				return;
			}

			// If the clicked tile is movable, move the unit
			if (clickedTile.getHighlightStatus() == 1) {
				moveUnit(out, gameState, clickedTile);
			}

			// If the clicked tile is attackable, attack
			if (clickedTile.getHighlightStatus() == 2) {
				attackUnit(out, gameState, message, clickedTile);
			}

			// Set selectedUnit as null again
			gameState.selectedUnit = null;

			return;
		}

		// 5. If the tile has an own unit, highlight the movable and attackable tiles
		if (clickedTile.getUnit() != null && clickedTile.getUnit().getOwner() == gameState.currentPlayer) {

			// Store selected unit
			gameState.selectedUnit = clickedTile.getUnit();

			// Dehighlight clicked tile
			clickedTile.setHighlightStatus(out, 0);

			// Highlight movable unit
			if (clickedTile.getUnit().canMove()) {
				UnitManager.highlightMovableTile(out, gameState, clickedTile);
			}

			// Highlight attackable unit
			if (clickedTile.getUnit().canAttack(gameState)) {
				System.out.println("Can Attack: " + clickedTile.getUnit().canAttack(gameState));
				UnitManager.highlightAttackableTile(out, gameState, clickedTile);
			}

			return;
		}
	}
}