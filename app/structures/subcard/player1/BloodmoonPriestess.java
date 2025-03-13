package structures.subcard.player1;

import akka.actor.ActorRef;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Deathwatch;

/**
 * 血月女祭司 (Bloodmoon Priestess)
 * 费用：4
 * 攻击：3
 * 生命：3
 * 能力：死亡守望 - 在随机相邻空格召唤一个幽灵
 */
public class BloodmoonPriestess extends CreatureCard implements Deathwatch {
    
    public BloodmoonPriestess() {
        setCardname("Bloodmoon Priestess");
        setManacost(4);
        setAttack(3);
        setHealth(3);
        setUnitConfig("conf/gameconfs/units/bloodmoon_priestess.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 注册死亡守望监听器
        gameState.addDeathwatchListener(unit, deadUnit -> onUnitDeath(out, gameState, unit));
    }
    
    @Override
    public void onUnitDeath(ActorRef out, GameState gameState, Unit unit) {
        // 获取随机相邻空格
        Tile targetTile = UnitManager.getRandomAdjacentEmptyTile(gameState, unit);
        
        if (targetTile != null) {
            // 召唤幽灵
            UnitManager.summonWraithling(out, gameState, targetTile);
        }
    }
}