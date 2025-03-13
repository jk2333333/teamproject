package structures.subcard.player1;

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
 * 黑暗终结 (Dark Terminus)
 * 费用：4
 * 效果：消灭一个敌方单位，在其位置召唤一个幽灵
 */
public class DarkTerminus extends SpellCard {
    
    public DarkTerminus() {
        setCardname("Dark Terminus");
        setManacost(4);
    }
    
    @Override
    public void onCardPlayed(ActorRef out, GameState gameState, Tile tile) {
        // 获取目标单位
        Unit targetUnit = tile.getUnit();
        if (targetUnit == null) return;
        
        // 保存位置信息
        Tile targetTile = targetUnit.getTile();
        
        // 播放特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_martyrdom), targetTile);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 消灭目标单位
        UnitManager.removeUnit(out, gameState, targetUnit);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 在目标位置召唤幽灵
        UnitManager.summonWraithling(out, gameState, targetTile);
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
            if (unit.getOwner() != gameState.currentPlayer && !unit.getIsAvartar(1) && !unit.getIsAvartar(2)) {
                // 不能对化身使用
                validTargets.add(unit.getTile());
            }
        }
        
        return validTargets;
    }
}