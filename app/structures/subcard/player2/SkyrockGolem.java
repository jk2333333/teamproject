package structures.subcard.player2;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;

/**
 * 天岩魔像 (Skyrock Golem)
 * 费用：2
 * 攻击：4
 * 生命：2
 * 能力：无
 */
public class SkyrockGolem extends CreatureCard {
    
    public SkyrockGolem() {
        setCardname("Skyrock Golem");
        setManacost(2);
        setAttack(4);
        setHealth(2);
        setUnitConfig("conf/gameconfs/units/skyrock_golem.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 没有特殊能力，无需额外操作
    }
}