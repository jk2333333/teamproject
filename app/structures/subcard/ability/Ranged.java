package structures.subcard.ability;

import java.util.List;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * 远程攻击能力接口
 * 可以攻击棋盘上的任何敌方单位
 */
public interface Ranged {
    /**
     * 获取单位可以攻击的目标列表
     * @param unit 拥有远程攻击能力的单位
     * @param gameState 当前游戏状态
     * @return 可以攻击的目标列表
     */
    List<Unit> getAttackableTargets(Unit unit, GameState gameState);
    
    /**
     * 高亮可以攻击的目标
     * @param out WebSocket通信通道
     * @param unit 拥有远程攻击能力的单位
     * @param gameState 当前游戏状态
     */
    void highlightAttackableTargets(ActorRef out, Unit unit, GameState gameState);
}