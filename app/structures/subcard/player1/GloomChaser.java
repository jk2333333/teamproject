package structures.subcard.player1;

import akka.actor.ActorRef;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.OpeningGambit;

/**
 * 阴暗追随者 (Gloom Chaser)
 * 费用：2
 * 攻击：3
 * 生命：1
 * 能力：开战效果 - 在该单位身后召唤一个幽灵
 */
public class GloomChaser extends CreatureCard implements OpeningGambit {
    
    public GloomChaser() {
        setCardname("Gloom Chaser");
        setManacost(2);
        setAttack(3);
        setHealth(1);
        setUnitConfig("conf/gameconfs/units/gloom_chaser.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 触发开战效果
        onUnitSummoned(out, gameState, unit, tile);
    }
    
    @Override
    public void onUnitSummoned(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 获取单位身后的位置（对于玩家1来说是左边，即x-1）
        int behindX = unit.getPosition().getTilex() - 1;
        int sameY = unit.getPosition().getTiley();
        
        // 检查坐标是否合法且格子为空
        if (behindX >= 0 && behindX < 9) {
            Tile targetTile = gameState.board[behindX][sameY];
            if (targetTile.getUnit() == null) {
                // 召唤幽灵
                UnitManager.summonWraithling(out, gameState, targetTile);
            }
        }
    }
}