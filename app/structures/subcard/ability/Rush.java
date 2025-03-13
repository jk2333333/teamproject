package structures.subcard.ability;

import structures.basic.Unit;

/**
 * 冲锋能力接口
 * 可以在召唤的回合移动和攻击
 */
public interface Rush {
    /**
     * 当单位被召唤时启用其移动和攻击能力
     * @param unit 拥有冲锋能力的单位
     */
    void enableRush(Unit unit);
}