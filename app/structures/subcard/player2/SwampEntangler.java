package structures.subcard.player2;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Provoke;

/**
 * 沼泽纠缠者 (Swamp Entangler)
 * 费用：1
 * 攻击：0
 * 生命：3
 * 能力：嘲讽
 */
public class SwampEntangler extends CreatureCard implements Provoke {
    
    public SwampEntangler() {
        setCardname("Swamp Entangler");
        setManacost(1);
        setAttack(0);
        setHealth(3);
        setUnitConfig("conf/gameconfs/units/swamp_entangler.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 注册为嘲讽单位
        registerProvoke(unit, gameState);
    }
    
    @Override
    public void registerProvoke(Unit unit, GameState gameState) {
        // 将单位注册为嘲讽单位
        gameState.registerProvokeUnit(unit);
    }
    
    @Override
    public void unregisterProvoke(Unit unit, GameState gameState) {
        // 取消注册嘲讽单位
        gameState.unregisterProvokeUnit(unit);
    }
}