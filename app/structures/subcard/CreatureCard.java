package structures.subcard;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * 生物卡牌的抽象类，继承自Card类，实现了生物卡共有的行为。
 * 所有的生物卡都应该继承此类，而不是直接继承Card类。
 */
public abstract class CreatureCard extends Card {
    
    public CreatureCard() {
        this.isCreature = true;
    }
    
    /**
     * 当生物被召唤到场上时执行的逻辑
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param unit 被召唤的单位
     * @param tile 单位被放置的格子
     */
    public abstract void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile);
    
    /**
     * 获取此生物卡的有效目标格子（通常是友方单位附近的空格）
     */
    @Override
    public List<Tile> getValidTargets(GameState gameState) {
        List<Tile> validTiles = new ArrayList<>();
        
        // 只有当前玩家的回合才能使用卡牌
        if (gameState.currentPlayer != 1) {
            return validTiles;
        }
        
        // 遍历所有友方单位，查找其周围的空格
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() == gameState.currentPlayer) {
                int x = unit.getPosition().getTilex();
                int y = unit.getPosition().getTiley();
                
                // 检查相邻和对角线格子
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue; // 跳过单位自身位置
                        
                        int newX = x + i;
                        int newY = y + j;
                        
                        // 检查坐标是否合法
                        if (newX >= 0 && newX < 9 && newY >= 0 && newY < 5) {
                            Tile targetTile = gameState.board[newX][newY];
                            // 只有空格才是有效的目标
                            if (targetTile.getUnit() == null && !validTiles.contains(targetTile)) {
                                validTiles.add(targetTile);
                            }
                        }
                    }
                }
            }
        }
        
        return validTiles;
    }
    
    /**
     * 生物卡的标准打出逻辑：召唤单位并触发召唤效果
     */
    @Override
    public void onCardPlayed(ActorRef out, GameState gameState, Tile tile) {
        // 召唤单位
        Unit unit = UnitManager.summonUnit(out, gameState, this, tile);
        
        // 触发召唤效果
        if (unit != null) {
            onSummon(out, gameState, unit, tile);
            
            // 播放单位的闲置动画
            BasicCommands.playUnitAnimation(out, unit, structures.basic.UnitAnimationType.idle);
        }
    }
}