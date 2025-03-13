package structures.subcard.player1;

import akka.actor.ActorRef;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;  // 添加这一行导入Unit类
import structures.subcard.SpellCard;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * 幽灵群 (Wraithling Swarm)
 * 费用：3
 * 效果：召唤3个幽灵
 */
public class WraithlingSwarm extends SpellCard {
    
    public WraithlingSwarm() {
        setCardname("Wraithling Swarm");
        setManacost(3);
    }
    
    @Override
    public void onCardPlayed(ActorRef out, GameState gameState, Tile tile) {
        // 在指定位置召唤第一个幽灵
        UnitManager.summonWraithling(out, gameState, tile);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 查找两个额外的空格来召唤幽灵
        List<Tile> emptyTiles = getAdditionalEmptyTiles(gameState, tile);
        
        // 召唤额外的幽灵
        for (int i = 0; i < Math.min(2, emptyTiles.size()); i++) {
            UnitManager.summonWraithling(out, gameState, emptyTiles.get(i));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public List<Tile> getValidTargets(GameState gameState) {
        List<Tile> validTargets = new ArrayList<>();
        
        // 只有当前玩家的回合才能使用卡牌
        if (gameState.currentPlayer != 1) {
            return validTargets;
        }
        
        // 查找所有友方单位周围的空格
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
                            if (targetTile.getUnit() == null && !validTargets.contains(targetTile)) {
                                validTargets.add(targetTile);
                            }
                        }
                    }
                }
            }
        }
        
        return validTargets;
    }
    
    /**
     * 获取额外的空格用于召唤幽灵
     * @param gameState 当前游戏状态
     * @param firstTile 第一个幽灵所在的格子
     * @return 额外的空格列表
     */
    private List<Tile> getAdditionalEmptyTiles(GameState gameState, Tile firstTile) {
        List<Tile> emptyTiles = new ArrayList<>();
        int x = firstTile.getTilex();
        int y = firstTile.getTiley();
        
        // 检查相邻和对角线格子
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // 跳过中心位置
                
                int newX = x + i;
                int newY = y + j;
                
                // 检查坐标是否合法
                if (newX >= 0 && newX < 9 && newY >= 0 && newY < 5) {
                    Tile targetTile = gameState.board[newX][newY];
                    // 只有空格才是有效的目标
                    if (targetTile.getUnit() == null) {
                        emptyTiles.add(targetTile);
                    }
                }
            }
        }
        
        return emptyTiles;
    }
}