package structures.subcard.player2;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.SpellCard;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * 光束震击 (Beam Shock)
 * 费用：0
 * 效果：眩晕一个非化身敌方单位（下回合无法移动或攻击）
 */
public class BeamShock extends SpellCard {
    
    public BeamShock() {
        setCardname("Beam Shock");
        setManacost(0);
    }
    
    @Override
    public void onCardPlayed(ActorRef out, GameState gameState, Tile tile) {
        // 获取目标单位
        Unit targetUnit = tile.getUnit();
        if (targetUnit == null) return;
        
        // 播放特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_martyrdom), tile);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 眩晕目标单位（使其在下一回合无法移动或攻击）
        stunUnit(targetUnit);
        
        // 显示单位被眩晕的提示
        BasicCommands.addPlayer1Notification(out, targetUnit.getCardname() + " is stunned", 2);
    }
    
    @Override
    public List<Tile> getValidTargets(GameState gameState) {
        List<Tile> validTargets = new ArrayList<>();
        
        // 只有当前玩家的回合才能使用卡牌
        if (gameState.currentPlayer != 1) {
            return validTargets;
        }
        
        // 查找所有非化身敌方单位
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() != gameState.currentPlayer && 
                !unit.getIsAvartar(1) && !unit.getIsAvartar(2)) {
                validTargets.add(unit.getTile());
            }
        }
        
        return validTargets;
    }
    
    /**
     * 眩晕单位，使其在下一回合无法移动或攻击
     * @param unit 目标单位
     */
    private void stunUnit(Unit unit) {
        // 将单位的移动次数和攻击次数设置为最大值，使其无法在当前回合执行这些操作
        unit.setMoves(unit.getMaxMoves());
        unit.setAttacks(unit.getMaxAttacks());
        
        // 标记单位为眩晕状态，这需要在Unit类中添加相应的字段
        unit.setStunned(true);
    }
}