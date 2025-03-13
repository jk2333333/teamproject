package structures.subcard.player2;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Rush;

/**
 * 剑齿虎 (Saberspine Tiger)
 * 费用：3
 * 攻击：3
 * 生命：2
 * 能力：冲锋 - 可以在召唤的回合移动和攻击
 */
public class SaberspineTiger extends CreatureCard implements Rush {
    
    public SaberspineTiger() {
        setCardname("Saberspine Tiger");
        setManacost(3);
        setAttack(3);
        setHealth(2);
        setUnitConfig("conf/gameconfs/units/saberspine_tiger.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 启用冲锋能力
        enableRush(unit);
    }
    
    @Override
    public void enableRush(Unit unit) {
        // 设置单位可以在召唤的回合移动和攻击
        // 重置移动和攻击计数
        unit.resetTurnStatus();
        // 令单位不处于刚召唤的"睡眠"状态
        unit.setSleeping(false);
    }
}