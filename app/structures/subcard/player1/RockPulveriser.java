package structures.subcard.player1;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Provoke;

/**
 * 岩石粉碎者 (Rock Pulveriser)
 * 费用：2
 * 攻击：1
 * 生命：4
 * 能力：嘲讽
 */
public class RockPulveriser extends CreatureCard implements Provoke {
    
    public RockPulveriser() {
        setCardname("Rock Pulveriser");
        setManacost(2);
        setAttack(1);
        setHealth(4);
        setUnitConfig("conf/gameconfs/units/rock_pulveriser.json");
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