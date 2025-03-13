package structures.subcard.player2;

import akka.actor.ActorRef;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.SpellCard;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * 真实打击 (True Strike)
 * 费用：1
 * 效果：对一个敌方单位造成2点伤害
 */
public class TrueStrike extends SpellCard {
    
    public TrueStrike() {
        setCardname("True Strike");
        setManacost(1);
    }
    
    @Override
    public void onCardPlayed(ActorRef out, GameState gameState, Tile tile) {
        // 获取目标单位
        Unit targetUnit = tile.getUnit();
        if (targetUnit == null) return;
        
        // 播放特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_inmolation), tile);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 造成2点伤害
        int newHealth = targetUnit.getHealth() - 2;
        if (newHealth < 0) newHealth = 0;
        targetUnit.setHealth(newHealth);
        
        // 更新UI
        BasicCommands.setUnitHealth(out, targetUnit, newHealth);
        
        // 如果是化身，更新玩家生命值
        if (targetUnit.getIsAvartar(1)) {
            gameState.player1.setHealth(newHealth);
            BasicCommands.setPlayer1Health(out, gameState.player1);
        } else if (targetUnit.getIsAvartar(2)) {
            gameState.player2.setHealth(newHealth);
            BasicCommands.setPlayer2Health(out, gameState.player2);
        }
        
        // 如果单位死亡，移除它
        if (newHealth <= 0) {
            UnitManager.removeUnit(out, gameState, targetUnit);
        }
    }
    
    @Override
    public List<Tile> getValidTargets(GameState gameState) {
        List<Tile> validTargets = new ArrayList<>();
        
        // 只有当前玩家的回合才能使用卡牌
        if (gameState.currentPlayer != 1) {
            return validTargets;
        }
        
        // 查找所有敌方单位
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() != gameState.currentPlayer) {
                validTargets.add(unit.getTile());
            }
        }
        
        return validTargets;
    }
}