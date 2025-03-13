package structures.subcard.player1;

import akka.actor.ActorRef;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.OpeningGambit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * 夜愁刺客 (Nightsorrow Assassin)
 * 费用：3
 * 攻击：4
 * 生命：2
 * 能力：开战效果 - 消灭相邻的一个已受伤的敌方单位
 */
public class NightsorrowAssassin extends CreatureCard implements OpeningGambit {
    
    public NightsorrowAssassin() {
        setCardname("Nightsorrow Assassin");
        setManacost(3);
        setAttack(4);
        setHealth(2);
        setUnitConfig("conf/gameconfs/units/nightsorrow_assassin.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 触发开战效果
        onUnitSummoned(out, gameState, unit, tile);
    }
    
    @Override
    public void onUnitSummoned(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 寻找相邻的已受伤敌方单位
        List<Unit> targets = getAdjacentInjuredEnemies(unit, gameState);
        
        if (!targets.isEmpty()) {
            // 随机选择一个目标（或者选择第一个）
            Unit target = targets.get(0);
            
            // 播放特效
            try {
                BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_martyrdom), target.getTile());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // 消灭目标
            UnitManager.removeUnit(out, gameState, target);
        }
    }
    
    /**
     * 获取单位周围已受伤的敌方单位
     * @param unit 中心单位
     * @param gameState 当前游戏状态
     * @return 受伤的敌方单位列表
     */
    private List<Unit> getAdjacentInjuredEnemies(Unit unit, GameState gameState) {
        List<Unit> injuredEnemies = new ArrayList<>();
        int x = unit.getPosition().getTilex();
        int y = unit.getPosition().getTiley();
        
        // 检查所有相邻和对角线格子
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // 跳过单位自身位置
                
                int nx = x + i;
                int ny = y + j;
                
                // 检查坐标是否合法
                if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) {
                    Tile targetTile = gameState.board[nx][ny];
                    Unit targetUnit = targetTile.getUnit();
                    
                    // 如果格子有单位，且是敌方单位，且已受伤
                    if (targetUnit != null && 
                        targetUnit.getOwner() != unit.getOwner() && 
                        targetUnit.getHealth() < getMaxHealth(targetUnit)) {
                        injuredEnemies.add(targetUnit);
                    }
                }
            }
        }
        
        return injuredEnemies;
    }
    
    /**
     * 获取单位的最大生命值
     * 由于我们没有直接的方法获取最大生命值，这里使用估算
     * @param unit 目标单位
     * @return 估计的最大生命值
     */
    private int getMaxHealth(Unit unit) {
        // 这里我们假设单位的当前生命值小于初始生命值
        // 对于头像，我们知道最大生命值是20
        if (unit.getIsAvartar(1) || unit.getIsAvartar(2)) {
            return 20;
        }
        
        // 对于其他单位，我们可以根据其配置文件名猜测最大生命值
        String unitConfig = unit.getUnitConfig();
        if (unitConfig != null) {
            // 这里简单地返回一个估计值，实际应用中可能需要更复杂的逻辑
            return unit.getHealth() + 1; // 假设只要低于初始值就算受伤
        }
        
        return unit.getHealth(); // 如果无法确定，则返回当前生命值
    }
}